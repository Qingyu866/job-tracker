import { create } from 'zustand';

interface UIStore {
  sidebarCollapsed: boolean;
  theme: 'light' | 'dark';
  loading: boolean;

  toggleSidebar: () => void;
  setTheme: (theme: UIStore['theme']) => void;
  setLoading: (loading: boolean) => void;
}

export const useUIStore = create<UIStore>((set) => ({
  // 初始状态
  sidebarCollapsed: false,
  theme: 'light',
  loading: false,

  // 切换侧边栏
  toggleSidebar: () => {
    set((state) => ({ sidebarCollapsed: !state.sidebarCollapsed }));
  },

  // 设置主题
  setTheme: (theme) => {
    set({ theme });
  },

  // 设置加载状态
  setLoading: (loading) => {
    set({ loading });
  },
}));
