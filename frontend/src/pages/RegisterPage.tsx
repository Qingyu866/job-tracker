import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '@/store/userStore';
import { BookOpen, Eye, EyeOff, Loader2 } from 'lucide-react';
import { Button } from '@/components/common';
import type { RegisterRequest } from '@/utils/auth';

export function RegisterPage() {
  const navigate = useNavigate();
  const { register, isLoggedIn, loading } = useUserStore();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    email: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (isLoggedIn) {
      navigate('/');
    }
  }, [isLoggedIn, navigate]);

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.username.trim()) {
      newErrors.username = '请输入用户名';
    } else if (formData.username.length < 3) {
      newErrors.username = '用户名至少3个字符';
    }

    if (!formData.password) {
      newErrors.password = '请输入密码';
    } else if (formData.password.length < 6) {
      newErrors.password = '密码至少6个字符';
    }

    if (!formData.confirmPassword) {
      newErrors.confirmPassword = '请确认密码';
    } else if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = '两次输入的密码不一致';
    }

    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '请输入有效的邮箱地址';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const data: RegisterRequest = {
      username: formData.username.trim(),
      password: formData.password,
      nickname: formData.nickname.trim() || formData.username.trim(),
      email: formData.email.trim() || undefined,
    };

    await register(data);
  };

  const handleChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  return (
    <div className="min-h-screen bg-paper-50 flex items-center justify-center px-4 py-8">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-xl shadow-paper-lg border border-paper-200 p-8">
          <div className="text-center mb-8">
            <div className="flex items-center justify-center gap-2 mb-4">
              <BookOpen className="w-8 h-8 text-accent-amber" />
              <h1 className="text-2xl font-bold text-paper-700">Job Tracker</h1>
            </div>
            <p className="text-paper-500">创建账号，开始追踪您的求职进度</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                用户名 <span className="text-accent-red">*</span>
              </label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => handleChange('username', e.target.value)}
                className={`w-full px-4 py-2.5 border rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all ${
                  errors.username ? 'border-accent-red' : 'border-paper-200'
                }`}
                placeholder="请输入用户名"
                autoComplete="username"
                required
                disabled={loading}
              />
              {errors.username && (
                <p className="mt-1 text-xs text-accent-red">{errors.username}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                密码 <span className="text-accent-red">*</span>
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={formData.password}
                  onChange={(e) => handleChange('password', e.target.value)}
                  className={`w-full px-4 py-2.5 pr-10 border rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all ${
                    errors.password ? 'border-accent-red' : 'border-paper-200'
                  }`}
                  placeholder="请输入密码（至少6位）"
                  autoComplete="new-password"
                  required
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-paper-400 hover:text-paper-600 transition-colors"
                  tabIndex={-1}
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
              {errors.password && (
                <p className="mt-1 text-xs text-accent-red">{errors.password}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                确认密码 <span className="text-accent-red">*</span>
              </label>
              <div className="relative">
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  value={formData.confirmPassword}
                  onChange={(e) => handleChange('confirmPassword', e.target.value)}
                  className={`w-full px-4 py-2.5 pr-10 border rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all ${
                    errors.confirmPassword ? 'border-accent-red' : 'border-paper-200'
                  }`}
                  placeholder="请再次输入密码"
                  autoComplete="new-password"
                  required
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-paper-400 hover:text-paper-600 transition-colors"
                  tabIndex={-1}
                >
                  {showConfirmPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="mt-1 text-xs text-accent-red">{errors.confirmPassword}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                昵称
              </label>
              <input
                type="text"
                value={formData.nickname}
                onChange={(e) => handleChange('nickname', e.target.value)}
                className="w-full px-4 py-2.5 border border-paper-200 rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all"
                placeholder="请输入昵称（可选，默认为用户名）"
                disabled={loading}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                邮箱
              </label>
              <input
                type="email"
                value={formData.email}
                onChange={(e) => handleChange('email', e.target.value)}
                className={`w-full px-4 py-2.5 border rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all ${
                  errors.email ? 'border-accent-red' : 'border-paper-200'
                }`}
                placeholder="请输入邮箱（可选）"
                autoComplete="email"
                disabled={loading}
              />
              {errors.email && (
                <p className="mt-1 text-xs text-accent-red">{errors.email}</p>
              )}
            </div>

            <Button
              type="submit"
              variant="primary"
              className="w-full mt-6"
              disabled={loading}
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  注册中...
                </>
              ) : (
                '注册'
              )}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-paper-500">
            已有账号？
            <a
              href="/login"
              className="text-accent-amber hover:text-accent-amber/80 font-medium ml-1 transition-colors"
            >
              立即登录
            </a>
          </div>
        </div>

        <p className="mt-6 text-center text-xs text-paper-400">
          注册即表示您同意我们的服务条款和隐私政策
        </p>
      </div>
    </div>
  );
}
