import { Bell, Menu, Search, Settings, X, BookOpen, Download, FileSpreadsheet, FileJson, XCircle, Loader2, Building2, MessageSquare } from 'lucide-react';
import { useState, useCallback, useRef, useEffect } from 'react';
import { dataApi } from '@/services/dataApi';
import { toast } from '@/store/toastStore';
import { useApplicationStore } from '@/store/applicationStore';
import { useInterviewStore } from '@/store/interviewStore';
import { useNavigate, useLocation } from 'react-router-dom';

export function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [showExportMenu, setShowExportMenu] = useState(false);
  const [isExporting, setIsExporting] = useState<'excel' | 'json' | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [isSearchLoading, setIsSearchLoading] = useState(false);
  const searchInputRef = useRef<HTMLInputElement>(null);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const { setKeyword } = useApplicationStore();
  const { setSearchKeyword: setInterviewSearchKeyword } = useInterviewStore();

  const handleSearchChange = useCallback((value: string) => {
    setSearchKeyword(value);
    setIsSearchLoading(true);

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(async () => {
      if (location.pathname.startsWith('/interview')) {
        setInterviewSearchKeyword(value);
      } else {
        await setKeyword(value);
      }
      setIsSearchLoading(false);
    }, 300);
  }, [setKeyword, setInterviewSearchKeyword, location.pathname]);

  const handleClearSearch = useCallback(async () => {
    setSearchKeyword('');
    setIsSearchLoading(true);
    if (location.pathname.startsWith('/interview')) {
      setInterviewSearchKeyword('');
    } else {
      await setKeyword('');
    }
    setIsSearchLoading(false);
    searchInputRef.current?.focus();
  }, [setKeyword, setInterviewSearchKeyword, location.pathname]);

  const handleSearchKeyDown = useCallback(async (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
      setIsSearchLoading(true);
      if (location.pathname.startsWith('/interview')) {
        setInterviewSearchKeyword(searchKeyword);
      } else {
        await setKeyword(searchKeyword);
      }
      setIsSearchLoading(false);
    }
  }, [setKeyword, setInterviewSearchKeyword, searchKeyword, location.pathname]);

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

  useEffect(() => {
    setSearchKeyword('');
    setIsSearchLoading(false);
  }, [location.pathname]);

  const isActive = (path: string) => location.pathname === path;

  return (
    <header className="h-16 border-b border-paper-200 bg-paper-50 flex items-center justify-between px-4 md:px-6 shadow-paper">
      <div className="flex items-center space-x-2">
        <BookOpen className="w-5 h-5 text-paper-600" />
        <h1 className="text-base md:text-xl font-semibold text-paper-700 font-reading">
          Job Tracker
        </h1>
      </div>

      <div className="hidden md:block flex-1 max-w-md mx-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-paper-400" size={18} />
          <input
            ref={searchInputRef}
            type="text"
            value={searchKeyword}
            onChange={(e) => handleSearchChange(e.target.value)}
            onKeyDown={handleSearchKeyDown}
            placeholder={location.pathname.startsWith('/interview') ? '搜索面试会话...' : '搜索公司、职位...'}
            className="w-full pl-10 pr-10 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber focus:border-transparent"
          />
          {(isSearchLoading || searchKeyword) && (
            <button
              onClick={handleClearSearch}
              className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-paper-200 rounded"
            >
              {isSearchLoading ? (
                <Loader2 className="w-4 h-4 text-paper-400 animate-spin" />
              ) : (
                <XCircle className="w-4 h-4 text-paper-400" />
              )}
            </button>
          )}
        </div>
      </div>

      <div className="flex items-center space-x-1 md:space-x-2">
        <button
          onClick={() => navigate('/')}
          className={`p-2 rounded-lg transition-colors ${
            isActive('/') ? 'bg-accent-amber text-paper-800' : 'text-paper-600 hover:bg-paper-100'
          }`}
          title="工作台"
        >
          <BookOpen className="w-5 h-5" />
        </button>

        <button
          onClick={() => navigate('/companies')}
          className={`p-2 rounded-lg transition-colors ${
            isActive('/companies') ? 'bg-accent-amber text-paper-800' : 'text-paper-600 hover:bg-paper-100'
          }`}
          title="公司管理"
        >
          <Building2 className="w-5 h-5" />
        </button>

        <button
          onClick={() => navigate('/interviews')}
          className={`p-2 rounded-lg transition-colors ${
            location.pathname.startsWith('/interview') ? 'bg-accent-amber text-paper-800' : 'text-paper-600 hover:bg-paper-100'
          }`}
          title="模拟面试"
        >
          <MessageSquare className="w-5 h-5" />
        </button>

        <div className="relative" data-export-menu>
          <button
            onClick={() => setShowExportMenu(!showExportMenu)}
            className="p-2 rounded-lg text-paper-600 hover:bg-paper-100 transition-colors"
            title="导出数据"
          >
            <Download className="w-5 h-5" />
          </button>

          {showExportMenu && (
            <div className="absolute right-0 top-full mt-2 w-48 bg-white border border-paper-200 rounded-lg shadow-paper-lg z-50">
              <div className="p-2 border-b border-paper-100">
                <span className="text-xs text-paper-500">导出格式</span>
              </div>
              <button
                onClick={handleExportExcel}
                disabled={isExporting !== null}
                className="w-full flex items-center gap-2 px-3 py-2 text-sm text-paper-700 hover:bg-paper-50 disabled:opacity-50"
              >
                <FileSpreadsheet className="w-4 h-4" />
                {isExporting === 'excel' ? '导出中...' : '导出 Excel'}
              </button>
              <button
                onClick={handleExportJson}
                disabled={isExporting !== null}
                className="w-full flex items-center gap-2 px-3 py-2 text-sm text-paper-700 hover:bg-paper-50 disabled:opacity-50"
              >
                <FileJson className="w-4 h-4" />
                {isExporting === 'json' ? '导出中...' : '导出 JSON'}
              </button>
            </div>
          )}
        </div>

        <button
          onClick={() => toast.info('通知功能开发中...')}
          className="p-2 rounded-lg text-paper-600 hover:bg-paper-100 transition-colors"
          title="通知"
        >
          <Bell className="w-5 h-5" />
        </button>

        <button
          onClick={() => toast.info('设置功能开发中...')}
          className="p-2 rounded-lg text-paper-600 hover:bg-paper-100 transition-colors hidden md:block"
          title="设置"
        >
          <Settings className="w-5 h-5" />
        </button>

        <button
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          className="md:hidden p-2 rounded-lg text-paper-600 hover:bg-paper-100 transition-colors"
        >
          {isMobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
        </button>
      </div>

      {isMobileMenuOpen && (
        <div className="absolute top-16 left-0 right-0 bg-white border-b border-paper-200 shadow-lg md:hidden z-50">
          <div className="p-4 space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-paper-400" size={18} />
              <input
                ref={searchInputRef}
                type="text"
                value={searchKeyword}
                onChange={(e) => handleSearchChange(e.target.value)}
                onKeyDown={handleSearchKeyDown}
                placeholder={location.pathname.startsWith('/interview') ? '搜索面试会话...' : '搜索公司、职位...'}
                className="w-full pl-10 pr-10 py-2 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber"
              />
              {(isSearchLoading || searchKeyword) && (
                <button
                  onClick={handleClearSearch}
                  className="absolute right-3 top-1/2 -translate-y-1/2 p-1 hover:bg-paper-200 rounded"
                >
                  {isSearchLoading ? (
                    <Loader2 className="w-4 h-4 text-paper-400 animate-spin" />
                  ) : (
                    <XCircle className="w-4 h-4 text-paper-400" />
                  )}
                </button>
              )}
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => {
                  navigate('/');
                  setIsMobileMenuOpen(false);
                }}
                className={`flex-1 flex items-center justify-center gap-2 p-2 rounded-lg transition-colors ${
                  isActive('/') ? 'bg-accent-amber text-paper-800' : 'bg-paper-100 text-paper-600'
                }`}
              >
                <BookOpen className="w-4 h-4" />
                工作台
              </button>
              <button
                onClick={() => {
                  navigate('/companies');
                  setIsMobileMenuOpen(false);
                }}
                className={`flex-1 flex items-center justify-center gap-2 p-2 rounded-lg transition-colors ${
                  isActive('/companies') ? 'bg-accent-amber text-paper-800' : 'bg-paper-100 text-paper-600'
                }`}
              >
                <Building2 className="w-4 h-4" />
                公司
              </button>
              <button
                onClick={() => {
                  navigate('/interviews');
                  setIsMobileMenuOpen(false);
                }}
                className={`flex-1 flex items-center justify-center gap-2 p-2 rounded-lg transition-colors ${
                  location.pathname.startsWith('/interview') ? 'bg-accent-amber text-paper-800' : 'bg-paper-100 text-paper-600'
                }`}
              >
                <MessageSquare className="w-4 h-4" />
                面试
              </button>
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleExportExcel}
                disabled={isExporting !== null}
                className="flex-1 flex items-center justify-center gap-2 p-2 bg-paper-100 rounded-lg text-paper-700 disabled:opacity-50"
              >
                <FileSpreadsheet className="w-4 h-4" />
                Excel
              </button>
              <button
                onClick={handleExportJson}
                disabled={isExporting !== null}
                className="flex-1 flex items-center justify-center gap-2 p-2 bg-paper-100 rounded-lg text-paper-700 disabled:opacity-50"
              >
                <FileJson className="w-4 h-4" />
                JSON
              </button>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}
