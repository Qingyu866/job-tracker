import { Bell, Settings, Search } from 'lucide-react';

export function Header() {
  return (
    <header className="h-16 border-b border-paper-200 bg-paper-50 flex items-center justify-between px-6 shadow-paper">
      {/* Logo */}
      <div className="flex items-center space-x-2">
        <h1 className="text-xl font-semibold text-paper-700 font-reading">
          📖 Job Tracker
        </h1>
      </div>

      {/* 搜索 */}
      <div className="flex-1 max-w-md mx-8">
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
      <div className="flex items-center space-x-4">
        <button className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors">
          <Bell className="w-5 h-5" />
        </button>
        <button className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors">
          <Settings className="w-5 h-5" />
        </button>
      </div>
    </header>
  );
}
