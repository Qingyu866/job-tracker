import { Bell, Menu, Search, Settings, X, BookOpen, Download, FileSpreadsheet, FileJson, XCircle, Loader2, Building2 } from 'lucide-react';
import { useState, useCallback, useRef, useEffect } from 'react';
import { dataApi } from '@/services/dataApi';
import { toast } from '@/store/toastStore';
import { useApplicationStore } from '@/store/applicationStore';
import { useNavigate } from 'react-router-dom';

export function Header() {
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [showExportMenu, setShowExportMenu] = useState(false);
  const [isExporting, setIsExporting] = useState<'excel' | 'json' | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isSearchLoading, setIsSearchLoading] = useState(false);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const { setKeyword } = useApplicationStore();

  // 防抖搜索
  const handleSearchChange = useCallback((value: string) => {
    setSearchKeyword(value);
    setIsSearchLoading(true);

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(async () => {
      await setKeyword(value);
      setIsSearchLoading(false);
    }, 300);
  }, [setKeyword]);

  // 清空搜索
  const handleClearSearch = useCallback(async () => {
    setSearchKeyword('');
    setIsSearchLoading(true);
    await setKeyword('');
    setIsSearchLoading(false);
    searchInputRef.current?.focus();
  }, [setKeyword]);

  // 回车搜索
  const handleSearchKeyDown = useCallback(async (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
      setIsSearchLoading(true);
      await setKeyword(searchKeyword);
      setIsSearchLoading(false);
    }
  }, [setKeyword, searchKeyword]);

  const handleExportExcel = async () => {
    setIsExporting('excel');
    try {
      await dataApi.downloadExcelBlob();
      toast.success('Excel 文件导出成功！');
    } catch (error) {
      console.error('导出 Excel 失败:', error);
      toast.error('导出 Excel 失败，请重试');
    } finally {
      setIsExporting(null);
      setShowExportMenu(false);
    }
  };

  const handleExportJson = async () => {
    setIsExporting('json');
    try {
      await dataApi.downloadJsonFile();
      toast.success('JSON 文件导出成功！');
    } catch (error) {
      console.error('导出 JSON 失败:', error);
      toast.error('导出 JSON 失败，请重试');
    } finally {
      setIsExporting(null);
      setShowExportMenu(false);
    }
  };

  // 点击外部关闭导出菜单
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
    const target = e.target as HTMLElement;
    if (!target.closest('[data-export-menu]')) {
      setShowExportMenu(false);
    }
  };
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

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
            ref={searchInputRef}
            type="text"
            value={searchKeyword}
            onChange={(e) => handleSearchChange(e.target.value)}
            onKeyDown={handleSearchKeyDown}
            placeholder="搜索公司、职位..."
            className="w-full pl-10 pr-10 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent transition-all"
            style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}
          />
          {/* 搜索加载指示器或清除按钮 */}
          {(isSearchLoading || searchKeyword) && (
            <button
              onClick={handleClearSearch}
              className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-paper-200 rounded text-paper-400 hover:text-paper-600 transition-colors"
            >
              {isSearchLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
            </button>
          )}
        </div>
      </div>

      {/* 右侧操作 */}
      <div className="flex items-center space-x-2 md:space-x-4">
        {/* 公司管理按钮 */}
        <button
          onClick={() => navigate('/companies')}
          className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors"
          title="公司管理"
        >
          <Building2 className="w-5 h-5" />
        </button>

        {/* 导出按钮 */}
        <div className="relative" data-export-menu>
          <button
            onClick={() => setShowExportMenu(!showExportMenu)}
            disabled={isExporting !== null}
            className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors disabled:opacity-50"
            title="导出数据"
          >
            <Download className="w-5 h-5" />
          </button>

          {/* 导出菜单 */}
          {showExportMenu && (
            <div className="absolute right-0 top-full mt-2 bg-white border-2 border-paper-300 rounded-lg shadow-xl py-2 min-w-[160px] z-[70]" data-export-menu>
              <div className="px-3 py-1.5 border-b border-paper-200 text-xs text-paper-500 font-medium" style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}>
                导出格式
              </div>
              <button
                onClick={handleExportExcel}
                disabled={isExporting !== null}
                className="w-full flex items-center gap-2 px-4 py-2 hover:bg-paper-100 text-paper-700 transition-colors disabled:opacity-50"
                style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}
              >
                <FileSpreadsheet className="w-4 h-4" />
                <span className="text-sm">{isExporting === 'excel' ? '导出中...' : '导出 Excel'}</span>
              </button>
              <button
                onClick={handleExportJson}
                disabled={isExporting !== null}
                className="w-full flex items-center gap-2 px-4 py-2 hover:bg-paper-100 text-paper-700 transition-colors disabled:opacity-50"
                style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}
              >
                <FileJson className="w-4 h-4" />
                <span className="text-sm">{isExporting === 'json' ? '导出中...' : '导出 JSON'}</span>
              </button>
            </div>
          )}
        </div>

        {/* 通知和设置 - 桌面端 */}
        <div className="hidden md:flex items-center space-x-4">
          <button 
            onClick={() => toast.info('通知功能开发中...')}
            className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors"
            title="通知"
          >
            <Bell className="w-5 h-5" />
          </button>
          <button 
            onClick={() => toast.info('设置功能开发中...')}
            className="p-2 hover:bg-paper-200 rounded-lg text-paper-600 hover:text-paper-700 transition-colors"
            title="设置"
          >
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
        <div className="absolute top-16 left-0 right-0 bg-white border-b border-paper-200 shadow-lg p-4 md:hidden z-50">
          <div className="space-y-4">
            {/* 搜索 */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-paper-400" size={18} />
              <input
                type="text"
                value={searchKeyword}
                onChange={(e) => handleSearchChange(e.target.value)}
                onKeyDown={handleSearchKeyDown}
                placeholder="搜索公司、职位..."
                className="w-full pl-10 pr-10 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber"
                style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}
              />
              {(isSearchLoading || searchKeyword) && (
                <button
                  onClick={handleClearSearch}
                  className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-paper-200 rounded text-paper-400 hover:text-paper-600"
                >
                  {isSearchLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <XCircle className="w-4 h-4" />}
                </button>
              )}
            </div>

            {/* 导出按钮 - 移动端 */}
            <div className="flex gap-2">
              <button
                onClick={handleExportExcel}
                disabled={isExporting !== null}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-accent-amber text-paper-900 rounded-lg hover:bg-accent-amber/90 transition-colors disabled:opacity-50"
                style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}
              >
                <FileSpreadsheet className="w-4 h-4" />
                <span className="text-sm">{isExporting === 'excel' ? '导出中...' : 'Excel'}</span>
              </button>
              <button
                onClick={handleExportJson}
                disabled={isExporting !== null}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-paper-200 text-paper-700 rounded-lg hover:bg-paper-300 transition-colors disabled:opacity-50"
                style={{ fontFamily: 'Georgia, "Times New Roman", Times, serif' }}
              >
                <FileJson className="w-4 h-4" />
                <span className="text-sm">{isExporting === 'json' ? '导出中...' : 'JSON'}</span>
              </button>
            </div>

            {/* 通知和设置 */}
            <div className="flex items-center space-x-4 pt-2">
              <button 
                onClick={() => toast.info('通知功能开发中...')}
                className="p-2 hover:bg-paper-200 rounded-lg text-paper-600"
              >
                <Bell className="w-5 h-5" />
              </button>
              <button 
                onClick={() => toast.info('设置功能开发中...')}
                className="p-2 hover:bg-paper-200 rounded-lg text-paper-600"
              >
                <Settings className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
