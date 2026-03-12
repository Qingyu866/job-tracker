import { useApplicationStore } from '@/store/applicationStore';
import { STATUS_CONFIG } from '@/utils/constants';

// 状态颜色映射（牛皮纸风格）
const STATUS_CLASS_MAP = {
  WISHLIST: 'bg-paper-100 text-paper-600 border border-paper-200',
  APPLIED: 'bg-accent-blue/20 text-accent-blue border border-accent-blue/30',
  INTERVIEW: 'bg-accent-purple/20 text-accent-purple border border-accent-purple/30',
  OFFER: 'bg-accent-green/20 text-accent-green border border-accent-green/30',
  REJECTED: 'bg-accent-red/20 text-accent-red border border-accent-red/30',
  WITHDRAWN: 'bg-accent-amber/20 text-accent-amber border border-accent-amber/30',
} as const;

export function TableView() {
  const { applications, loading, error } = useApplicationStore();

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-paper-500 flex items-center space-x-2">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-paper-400"></div>
          <span>加载中...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-accent-red bg-accent-red/10 px-4 py-2 rounded-lg border border-accent-red/20">
          {error}
        </div>
      </div>
    );
  }

  if (applications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center h-64 text-paper-400">
        <div className="text-4xl mb-2">📭</div>
        <div>暂无数据</div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="paper-card rounded-lg overflow-hidden">
        <table className="w-full border-collapse">
          <thead>
            <tr className="bg-paper-100 border-b border-paper-200">
              <th className="px-6 py-3 text-left text-xs font-medium text-paper-600 uppercase tracking-wider">
                公司
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-paper-600 uppercase tracking-wider">
                职位
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-paper-600 uppercase tracking-wider">
                状态
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-paper-600 uppercase tracking-wider">
                申请日期
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-paper-600 uppercase tracking-wider">
                优先级
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-paper-200">
            {applications.map((app) => (
              <tr
                key={app.id}
                className="hover:bg-paper-100 transition-colors cursor-pointer"
              >
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-paper-700 font-reading">
                    {app.company?.name || '-'}
                  </div>
                  {app.company?.location && (
                    <div className="text-sm text-paper-500">
                      {app.company.location}
                    </div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm text-paper-700">{app.jobTitle}</div>
                  {app.jobType && (
                    <div className="text-sm text-paper-500">{app.jobType}</div>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span
                    className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${STATUS_CLASS_MAP[app.status]}`}
                  >
                    {STATUS_CONFIG[app.status].label}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-paper-500">
                  {app.applicationDate || '-'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-paper-500">
                  {app.priority ? (
                    <div className="flex items-center">
                      {[1, 2, 3].map((level) => (
                        <span
                          key={level}
                          className={`mx-0.5 text-base ${
                            level <= (app.priority || 0)
                              ? 'text-accent-amber'
                              : 'text-paper-200'
                          }`}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                  ) : (
                    '-'
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-4 text-sm text-paper-500 text-center">
        📚 共 {applications.length} 条记录
      </div>
    </div>
  );
}
