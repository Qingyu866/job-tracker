import { useEffect, useState } from 'react';
import { Building2, MapPin, Globe, Users, Plus, Trash2, Edit2 } from 'lucide-react';
import { useApplicationStore } from '@/store/applicationStore';
import { toast } from '@/store/toastStore';
import { CreateCompanyForm } from '@/components/common/CreateCompanyForm';
import { Modal } from '@/components/common/Modal';
import { Header } from '@/components/layout/Header';
import type { Company } from '@/types';

export function CompanyManagementPage() {
  const { companies, fetchCompanies, deleteCompany, loading } = useApplicationStore();
  const [filteredCompanies, setFilteredCompanies] = useState<Company[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [companyToDelete, setCompanyToDelete] = useState<Company | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [companyToEdit, setCompanyToEdit] = useState<Company | null>(null);

  useEffect(() => {
    fetchCompanies();
  }, [fetchCompanies]);

  useEffect(() => {
    setFilteredCompanies(companies);
  }, [companies]);

  const handleDelete = async () => {
    if (!companyToDelete) return;

    try {
      await deleteCompany(companyToDelete.id);
      toast.success(`已删除公司：${companyToDelete.name}`);
      setShowDeleteConfirm(false);
      setCompanyToDelete(null);
    } catch (error) {
      toast.error('删除失败，请重试');
    }
  };

  const handleEdit = (company: Company) => {
    setCompanyToEdit(company);
    setShowEditModal(true);
  };

  const handleCompanyUpdated = async () => {
    setShowEditModal(false);
    setCompanyToEdit(null);
    await fetchCompanies();
    toast.success('公司信息已更新');
  };

  const handleCompanyCreated = async () => {
    setShowCreateModal(false);
    await fetchCompanies();
    toast.success('公司创建成功');
  };

  const getApplicationCount = (companyId: number) => {
    const { applications } = useApplicationStore.getState();
    return applications.filter(app => app.companyId === companyId).length;
  };

  return (
    <div className="h-screen flex flex-col">
      <Header />
      
      <div className="h-12 md:h-14 border-b flex items-center px-3 md:px-6 space-x-3 md:space-x-4 bg-paper-50">
        <div className="flex items-center space-x-1 md:space-x-2 bg-paper-100 p-1 rounded-lg border border-paper-200 overflow-x-auto">
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-2 md:px-3 py-1.5 rounded-md text-xs md:text-sm font-medium transition-all flex-shrink-0 flex items-center gap-1 md:gap-2 bg-paper-50 text-paper-700 shadow-paper border border-paper-200 hover:bg-paper-200"
          >
            <Plus className="w-4 h-4" />
            <span className="hidden sm:inline">添加公司</span>
          </button>
        </div>
      </div>

      <main className="flex-1 overflow-auto">
        <div className="max-w-7xl mx-auto px-4 py-6">

          {loading ? (
            <div className="flex items-center justify-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent-amber"></div>
            </div>
          ) : filteredCompanies.length === 0 ? (
            <div className="text-center py-16">
              <Building2 className="w-16 h-16 text-paper-300 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-paper-700 mb-2">
                {companies.length > 0 ? '未找到匹配的公司' : '还没有添加公司'}
              </h3>
              <p className="text-paper-500 mb-6">
                {companies.length > 0 ? '尝试使用其他关键词搜索' : '点击上方按钮添加您的第一个公司'}
              </p>
              {companies.length === 0 && (
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="inline-flex items-center gap-2 px-6 py-3 bg-accent-amber text-paper-900 rounded-lg hover:bg-accent-amber/90 transition-colors font-medium"
                >
                  <Plus className="w-5 h-5" />
                  添加公司
                </button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredCompanies.map((company) => (
                <div
                  key={company.id}
                  className="bg-white border-2 border-paper-200 rounded-lg p-6 hover:shadow-lg hover:border-accent-amber/50 transition-all"
                >
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-accent-amber/10 rounded-lg flex items-center justify-center">
                        <Building2 className="w-6 h-6 text-accent-amber" />
                      </div>
                      <div>
                        <h3 className="text-lg font-semibold text-paper-800">{company.name}</h3>
                        {company.industry && (
                          <p className="text-sm text-paper-500">{company.industry}</p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => handleEdit(company)}
                        className="p-2 hover:bg-paper-100 rounded-lg text-paper-400 hover:text-paper-600 transition-colors"
                        title="编辑"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => {
                          setCompanyToDelete(company);
                          setShowDeleteConfirm(true);
                        }}
                        className="p-2 hover:bg-red-50 rounded-lg text-paper-400 hover:text-red-600 transition-colors"
                        title="删除"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  <div className="space-y-3">
                    {company.location && (
                      <div className="flex items-center gap-2 text-sm text-paper-600">
                        <MapPin className="w-4 h-4 text-paper-400" />
                        <span>{company.location}</span>
                      </div>
                    )}

                    {company.website && (
                      <div className="flex items-center gap-2 text-sm text-paper-600">
                        <Globe className="w-4 h-4 text-paper-400" />
                        <a
                          href={company.website}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-accent-amber hover:underline truncate"
                        >
                          {company.website}
                        </a>
                      </div>
                    )}

                    {company.size && (
                      <div className="flex items-center gap-2 text-sm text-paper-600">
                        <Users className="w-4 h-4 text-paper-400" />
                        <span>{company.size}</span>
                      </div>
                    )}

                    <div className="pt-3 border-t border-paper-200">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-paper-500">关联申请</span>
                        <span className="font-semibold text-paper-800">
                          {getApplicationCount(company.id)} 个
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      <Modal isOpen={showCreateModal} onClose={() => setShowCreateModal(false)} title="创建新公司">
        <div className="p-6">
          <CreateCompanyForm
            companyName=""
            onSuccess={handleCompanyCreated}
            onCancel={() => setShowCreateModal(false)}
          />
        </div>
      </Modal>

      <Modal isOpen={showEditModal} onClose={() => setShowEditModal(false)} title="编辑公司">
        <div className="p-6">
          <CreateCompanyForm
            company={companyToEdit ?? undefined}
            onSuccess={handleCompanyUpdated}
            onCancel={() => setShowEditModal(false)}
          />
        </div>
      </Modal>

      <Modal isOpen={showDeleteConfirm} onClose={() => setShowDeleteConfirm(false)} title="确认删除">
        <div className="bg-white rounded-lg p-6 max-w-md">
          <div className="text-center">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <Trash2 className="w-8 h-8 text-red-600" />
            </div>
            <h3 className="text-xl font-bold text-paper-800 mb-2">确认删除</h3>
            <p className="text-paper-600 mb-6">
              确定要删除公司 <span className="font-semibold text-paper-800">{companyToDelete?.name}</span> 吗？
              {getApplicationCount(companyToDelete?.id || 0) > 0 && (
                <span className="block mt-2 text-red-600">
                  该公司下有 {getApplicationCount(companyToDelete?.id || 0)} 个申请记录，删除公司不会删除这些申请。
                </span>
              )}
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="flex-1 px-6 py-3 border-2 border-paper-300 rounded-lg text-paper-700 hover:bg-paper-50 transition-colors font-medium"
              >
                取消
              </button>
              <button
                onClick={handleDelete}
                className="flex-1 px-6 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors font-medium"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
}
