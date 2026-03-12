import { Bell, Menu, Search, Settings, X, BookOpen } from 'lucide-react';
import { useState } from 'react';

export function Header() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  return (
    <header className="h-16 border-b border-paper-200 bg-paper-50 flex items-center justify-between px-4 md:px-6 shadow-paper">
      {/* Logo */}
      <div className="flex items-center space-x-2">
        <BookOpen className="w-5 h-5 text-paper-600" />
        <h1 className="text-base md:text-xl font-semibold text-paper-700 font-reading">
          Job Tracker
        </h1>
      </div>

      {/* 搜索 - 移动端隐藏 */}
      <div className="hidden md:block flex-1 max-w-md mx-4 md:mx-8">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-paper-400" size={18} />
          <input
            type="text"
            placeholder="搜索公司、职位..."
            className="w-full pl-10 pr-4 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all"
          />
        </div>
      </div>

      {/* 右侧操作 */}
      <div className="flex items-center space-x-2 md:space-x-4">
        {/* 通知和设置 - 桌面端显示，移动端隐藏或放入菜单 */}
        <div className="hidden md:flex items-center space-x-4">
          <button className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors">
            <Bell className="w-5 h-5" />
          </button>
          <button className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors">
            <Settings className="w-5 h-5" />
          </button>
        </div>

        {/* 移动端菜单按钮 */}
        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="md:hidden p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors"
        >
          {isMobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
        </button>
      </div>

      {/* 移动端下拉菜单 */}
      {isMobileMenuOpen && (
        <div className="absolute top-16 left-0 right-0 bg-paper-50 border-b border-paper-200 shadow-paper p-4 md:hidden z-50">
          <div className="space-y-4">
            {/* 搜索 */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-paper-400" size={18} />
              <input
                type="text"
                placeholder="搜索公司、职位..."
                className="w-full pl-10 pr-4 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber"
              />
            </div>

            {/* 通知和设置 */}
            <div className="flex items-center space-x-4 pt-2">
              <button className="p-2 hover:bg-paper-200 rounded-lg text-paper-600">
                <Bell className="w-5 h-5" />
              </button>
              <button className="p-2 hover:bg-paper-200 rounded-lg text-paper-600">
                <Settings className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
