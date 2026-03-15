import { useState, useEffect, useRef } from 'react';
import { apiClient } from '@/lib/apiClient';
import type { Company } from '@/types';
import { Search, Building2, Plus } from 'lucide-react';
import { CreateCompanyForm } from './CreateCompanyForm';
import { useApplicationStore } from '@/store/applicationStore';

interface CompanyAutocompleteProps {
  value: string;
  onChange: (value: string, companyId?: number) => void;
  placeholder?: string;
}

export function CompanyAutocomplete({
  value,
  onChange,
  placeholder = '例如：字节跳动'
}: CompanyAutocompleteProps) {
  const { fetchCompanies } = useApplicationStore();
  const [keyword, setKeyword] = useState(value);
  const [suggestions, setSuggestions] = useState<Company[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // 搜索公司
  const searchCompanies = async (query: string) => {
    if (!query.trim()) {
      setSuggestions([]);
      return;
    }

    setIsLoading(true);
    try {
      const response = await apiClient.get<Company[]>(`/companies/search?keyword=${encodeURIComponent(query)}`);
      setSuggestions(response.data || []);
    } catch (error) {
      console.error('搜索公司失败:', error);
      setSuggestions([]);
    } finally {
      setIsLoading(false);
    }
  };

  // 防抖搜索
  useEffect(() => {
    const timer = setTimeout(() => {
      if (keyword.trim()) {
        searchCompanies(keyword);
      } else {
        setSuggestions([]);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [keyword]);

  // 点击外部关闭下拉框
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        !inputRef.current?.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (company: Company) => {
    setKeyword(company.name);
    onChange(company.name, company.id);
    setIsOpen(false);
    setSuggestions([]);
  };

  const handleCreateNew = () => {
    setIsOpen(false);
    setShowCreateForm(true);
  };

  const handleCompanyCreated = async (company: Company) => {
    setKeyword(company.name);
    onChange(company.name, company.id);
    setShowCreateForm(false);
    setSuggestions([]);
    await fetchCompanies();
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setKeyword(newValue);
    onChange(newValue);
    setIsOpen(true);
  };

  return (
    <>
      <div className="relative">
        <div className="relative">
          <input
            ref={inputRef}
            type="text"
            required
            value={keyword}
            onChange={handleInputChange}
            onFocus={() => setIsOpen(true)}
            placeholder={placeholder}
            className="w-full px-4 py-3 pl-10 border-2 border-paper-400 rounded-lg bg-[#f5f0e6] text-paper-800 placeholder:text-paper-500 focus:outline-none focus:ring-2 focus:ring-paper-600 focus:border-paper-600 text-base"
          />
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-paper-400 w-4 h-4" />
          {isLoading && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2">
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-paper-400"></div>
            </div>
          )}
        </div>

        {/* 下拉建议列表 */}
        {isOpen && suggestions.length > 0 && (
          <div
            ref={dropdownRef}
            className="absolute z-10 w-full mt-1 bg-white border-2 border-paper-400 rounded-lg shadow-lg max-h-60 overflow-auto"
          >
            {suggestions.map((company) => (
              <div
                key={company.id}
                onClick={() => handleSelect(company)}
                className="px-4 py-3 hover:bg-paper-100 cursor-pointer border-b border-paper-100 last:border-b-0 transition-colors"
              >
                <div className="flex items-center gap-2">
                  <Building2 className="w-4 h-4 text-paper-500 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <div className="text-sm font-medium text-paper-800 truncate">
                      {company.name}
                    </div>
                    {company.industry && (
                      <div className="text-xs text-paper-500 truncate">
                        {company.industry}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 无结果提示 - 创建新公司选项 */}
        {isOpen && keyword.trim() && !isLoading && suggestions.length === 0 && (
          <div
            ref={dropdownRef}
            className="absolute z-10 w-full mt-1 bg-white border-2 border-paper-400 rounded-lg shadow-lg"
          >
            <div
              onClick={handleCreateNew}
              className="px-4 py-3 hover:bg-paper-100 cursor-pointer transition-colors"
            >
              <div className="flex items-center gap-2">
                <Plus className="w-4 h-4 text-accent-amber flex-shrink-0" />
                <div className="flex-1">
                  <div className="text-sm font-medium text-accent-amber">
                    创建 "{keyword}"
                  </div>
                  <div className="text-xs text-paper-500">
                    未找到匹配的公司，点击创建新公司
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* 创建公司侧边抽屉 */}
      {showCreateForm && (
        <div className="fixed inset-0 z-50 flex">
          {/* 左侧遮罩 */}
          <div
            className="flex-1 bg-black/40 backdrop-blur-sm transition-opacity"
            onClick={() => setShowCreateForm(false)}
          />

          {/* 右侧抽屉 */}
          <div className="w-full max-w-md bg-[#f5f0e6] shadow-2xl overflow-y-auto animate-slide-in-right">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="text-lg font-bold text-paper-800">创建新公司</h3>
                  <p className="text-sm text-paper-500">完善公司信息</p>
                </div>
                <button
                  onClick={() => setShowCreateForm(false)}
                  className="p-2 hover:bg-paper-200 rounded-lg text-paper-700 transition-colors border border-paper-400"
                  aria-label="关闭"
                >
                  ✕
                </button>
              </div>
              <CreateCompanyForm
                companyName={keyword}
                onSuccess={handleCompanyCreated}
                onCancel={() => setShowCreateForm(false)}
              />
            </div>
          </div>
        </div>
      )}
    </>
  );
}
