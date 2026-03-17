import { create } from 'zustand';
import { authUtil, type UserInfo, type RegisterRequest } from '@/utils/auth';
import { toast } from '@/store/toastStore';

interface UserState {
  userInfo: UserInfo | null;
  isLoggedIn: boolean;
  loading: boolean;

  login: (username: string, password: string) => Promise<boolean>;
  register: (data: RegisterRequest) => Promise<boolean>;
  logout: () => Promise<void>;
  fetchUserInfo: () => Promise<void>;
  changePassword: (oldPassword: string, newPassword: string) => Promise<boolean>;
  initAuth: () => Promise<void>;
}

export const useUserStore = create<UserState>((set, get) => ({
  userInfo: authUtil.getUserInfo(),
  isLoggedIn: authUtil.isLoggedIn(),
  loading: false,

  login: async (username: string, password: string) => {
    set({ loading: true });
    try {
      const response = await authUtil.login(username, password);

      set({
        userInfo: response.userInfo,
        isLoggedIn: true,
        loading: false,
      });

      const redirectPath = sessionStorage.getItem('redirect_after_login');
      sessionStorage.removeItem('redirect_after_login');

      if (redirectPath && redirectPath !== '/login' && redirectPath !== '/register') {
        window.location.href = redirectPath;
      } else {
        window.location.href = '/';
      }

      return true;
    } catch (error: any) {
      set({ loading: false });
      toast.error(error.message || '用户名或密码错误');
      return false;
    }
  },

  register: async (data: RegisterRequest) => {
    set({ loading: true });
    try {
      await authUtil.register(data);

      toast.success('注册成功，正在登录...');

      const loginSuccess = await get().login(data.username, data.password!);
      set({ loading: false });
      return loginSuccess;
    } catch (error: any) {
      set({ loading: false });
      toast.error(error.message || '注册失败，请重试');
      return false;
    }
  },

  logout: async () => {
    try {
      await authUtil.logout();
    } catch {
      authUtil.clearAll();
    }

    set({
      userInfo: null,
      isLoggedIn: false,
    });

    window.location.href = '/login';
  },

  fetchUserInfo: async () => {
    try {
      const userInfo = await authUtil.fetchUserInfo();
      set({
        userInfo,
        isLoggedIn: true,
      });
    } catch {
      authUtil.clearAll();
      set({
        userInfo: null,
        isLoggedIn: false,
      });
      throw new Error('获取用户信息失败');
    }
  },

  changePassword: async (oldPassword: string, newPassword: string) => {
    set({ loading: true });
    try {
      await authUtil.changePassword(oldPassword, newPassword);
      set({ loading: false });
      toast.success('密码修改成功');
      return true;
    } catch (error: any) {
      set({ loading: false });
      toast.error(error.message || '请检查原密码是否正确');
      return false;
    }
  },

  initAuth: async () => {
    if (!authUtil.isLoggedIn()) {
      return;
    }

    const storedUserInfo = authUtil.getUserInfo();
    if (storedUserInfo) {
      set({
        userInfo: storedUserInfo,
        isLoggedIn: true,
      });
    }

    try {
      await get().fetchUserInfo();
    } catch {
      authUtil.clearAll();
      set({
        userInfo: null,
        isLoggedIn: false,
      });
    }
  },
}));
