import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '@/store/userStore';
import { BookOpen, Eye, EyeOff, Loader2 } from 'lucide-react';
import { Button } from '@/components/common';

export function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoggedIn, loading } = useUserStore();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    if (isLoggedIn) {
      navigate('/');
    }
  }, [isLoggedIn, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!username.trim() || !password.trim()) {
      return;
    }

    await login(username.trim(), password.trim());
  };

  return (
    <div className="min-h-screen bg-paper-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-xl shadow-paper-lg border border-paper-200 p-8">
          <div className="text-center mb-8">
            <div className="flex items-center justify-center gap-2 mb-4">
              <BookOpen className="w-8 h-8 text-accent-amber" />
              <h1 className="text-2xl font-bold text-paper-700">Job Tracker</h1>
            </div>
            <p className="text-paper-500">欢迎回来，请登录您的账号</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                用户名
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-2.5 border border-paper-200 rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all"
                placeholder="请输入用户名"
                autoComplete="username"
                required
                disabled={loading}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-paper-700 mb-1.5">
                密码
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-4 py-2.5 pr-10 border border-paper-200 rounded-lg bg-paper-50 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all"
                  placeholder="请输入密码"
                  autoComplete="current-password"
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
            </div>

            <Button
              type="submit"
              variant="primary"
              className="w-full"
              disabled={loading || !username.trim() || !password.trim()}
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  登录中...
                </>
              ) : (
                '登录'
              )}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-paper-500">
            还没有账号？
            <a
              href="/register"
              className="text-accent-amber hover:text-accent-amber/80 font-medium ml-1 transition-colors"
            >
              立即注册
            </a>
          </div>
        </div>

        <p className="mt-6 text-center text-xs text-paper-400">
          登录即表示您同意我们的服务条款和隐私政策
        </p>
      </div>
    </div>
  );
}
