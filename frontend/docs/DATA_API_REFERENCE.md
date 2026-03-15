# 数据管理 API 接口文档

**文档版本**: 1.0
**创建日期**: 2026-03-13
**维护者**: Backend Team

---

## 一、接口概览

### 1.1 P1阶段 - CRUD操作接口

| 方法 | 路径 | 说明 |
|------|------|------|
| PUT | `/api/data/applications/{id}` | 更新求职申请 |
| DELETE | `/api/data/applications/{id}` | 删除求职申请（保护性） |
| PUT | `/api/data/interviews/{id}` | 更新面试记录 |
| DELETE | `/api/data/interviews/{id}` | 删除面试记录 |
| PUT | `/api/data/companies/{id}` | 更新公司信息 |
| DELETE | `/api/data/companies/{id}` | 删除公司（保护性） |

### 1.2 P2阶段 - 聚合与导出接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/data/applications/{id}/detail` | 获取申请详情聚合 |
| GET | `/api/data/export/excel` | 导出Excel文件 |
| GET | `/api/data/export/json` | 导出JSON数据 |

---

## 二、通用说明

### 2.1 基础URL

```
http://localhost:8080/api/data
```

### 2.2 响应格式

所有接口统一返回格式：

```json
{
  "code": 200,
  "message": "操作描述",
  "data": { ... }
}
```

### 2.3 错误码

| 错误码 | 说明 | 处理建议 |
|--------|------|---------|
| 200 | 操作成功 | - |
| 400 | 请求参数错误或保护性阻止 | 检查参数或先删除关联数据 |
| 404 | 资源不存在 | 确认ID是否正确 |
| 500 | 服务器内部错误 | 联系管理员或查看日志 |

---

## 三、P1阶段接口详情

### 3.1 更新求职申请

**请求**

```http
PUT /api/data/applications/{id}
Content-Type: application/json
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | number | 是 | 申请ID |

**请求体**

```typescript
interface UpdateApplicationRequest {
  companyId?: number;        // 公司ID
  jobTitle?: string;         // 职位名称
  jobDescription?: string;   // 职位描述
  jobType?: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP';
  workLocation?: string;     // 工作地点
  salaryMin?: number;        // 最低薪资
  salaryMax?: number;        // 最高薪资
  salaryCurrency?: string;   // 薪资货币（默认CNY）
  jobUrl?: string;           // 职位链接
  status?: 'WISHLIST' | 'APPLIED' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN';
  applicationDate?: string;  // 申请日期 (YYYY-MM-DD)
  priority?: number;         // 优先级 (1-10)
  notes?: string;            // 备注
}
```

**响应**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": true
}
```

**前端调用示例**

```typescript
// 更新申请状态
async function updateApplicationStatus(id: number, status: string) {
  const response = await apiClient.put(`/applications/${id}`, { status });
  return response.data;
}

// 更新多个字段
async function updateApplication(id: number, data: Partial<UpdateApplicationRequest>) {
  const response = await apiClient.put(`/applications/${id}`, data);
  return response.data;
}
```

---

### 3.2 删除求职申请（保护性删除）

**请求**

```http
DELETE /api/data/applications/{id}
```

**响应（成功）**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

**响应（保护性阻止）**

```json
{
  "code": 400,
  "message": "该申请下有 2 条面试记录，请先删除面试记录",
  "data": null
}
```

**前端调用示例**

```typescript
async function deleteApplication(id: number): Promise<{ success: boolean; message: string }> {
  try {
    const response = await apiClient.delete(`/applications/${id}`);
    return { success: true, message: response.data.message };
  } catch (error: any) {
    // 捕获保护性删除错误
    const message = error.response?.data?.message || '删除失败';
    return { success: false, message };
  }
}

// 使用示例
const result = await deleteApplication(1);
if (!result.success) {
  // 显示错误提示：如 "该申请下有 2 条面试记录，请先删除面试记录"
  toast.error(result.message);
}
```

---

### 3.3 更新面试记录

**请求**

```http
PUT /api/data/interviews/{id}
Content-Type: application/json
```

**请求体**

```typescript
interface UpdateInterviewRequest {
  applicationId?: number;
  interviewType?: 'PHONE' | 'VIDEO' | 'ONSITE' | 'TECHNICAL' | 'HR';
  interviewDate?: string;      // ISO 8601 格式
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status?: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  rating?: number;             // 1-5
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired?: boolean;
}
```

**前端调用示例**

```typescript
// 更新面试状态为已完成
async function completeInterview(id: number, rating: number, feedback: string) {
  return apiClient.put(`/interviews/${id}`, {
    status: 'COMPLETED',
    rating,
    feedback
  });
}

// 重新安排面试时间
async function rescheduleInterview(id: number, newDate: string) {
  return apiClient.put(`/interviews/${id}`, {
    interviewDate: newDate,
    status: 'SCHEDULED'
  });
}
```

---

### 3.4 删除面试记录

**请求**

```http
DELETE /api/data/interviews/{id}
```

**响应**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": true
}
```

**前端调用示例**

```typescript
async function deleteInterview(id: number) {
  return apiClient.delete(`/interviews/${id}`);
}
```

---

### 3.5 更新公司信息

**请求**

```http
PUT /api/data/companies/{id}
Content-Type: application/json
```

**请求体**

```typescript
interface UpdateCompanyRequest {
  name?: string;
  industry?: string;
  size?: string;
  location?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
}
```

---

### 3.6 删除公司（保护性删除）

**请求**

```http
DELETE /api/data/companies/{id}
```

**响应（保护性阻止）**

```json
{
  "code": 400,
  "message": "该公司下有 3 条申请记录，请先删除申请记录",
  "data": null
}
```

---

## 四、P2阶段接口详情

### 4.1 获取申请详情（聚合信息）

**请求**

```http
GET /api/data/applications/{id}/detail
```

**响应**

```typescript
interface ApplicationDetailResponse {
  code: number;
  message: string;
  data: {
    application: JobApplication;
    company: Company | null;
    interviews: InterviewRecord[];
    logs: ApplicationLog[];
    statistics: {
      total: number;        // 面试总数
      completed: number;    // 已完成
      scheduled: number;    // 已安排
      averageRating: number; // 平均评分
    };
  };
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "application": {
      "id": 1,
      "companyId": 1,
      "jobTitle": "Java高级工程师",
      "status": "INTERVIEW",
      "priority": 8,
      "workLocation": "北京",
      "salaryMin": 25000,
      "salaryMax": 40000,
      "applicationDate": "2026-03-10",
      "notes": "重点跟进"
    },
    "company": {
      "id": 1,
      "name": "字节跳动",
      "industry": "互联网",
      "location": "北京",
      "website": "https://bytedance.com"
    },
    "interviews": [
      {
        "id": 1,
        "applicationId": 1,
        "interviewType": "TECHNICAL",
        "interviewDate": "2026-03-15T14:00:00",
        "status": "COMPLETED",
        "rating": 4,
        "feedback": "技术基础扎实"
      }
    ],
    "logs": [
      {
        "id": 1,
        "applicationId": 1,
        "logType": "STATUS_CHANGE",
        "logTitle": "状态变更",
        "logContent": "申请状态从 APPLIED 变更为 INTERVIEW",
        "createdAt": "2026-03-12T10:00:00"
      }
    ],
    "statistics": {
      "total": 3,
      "completed": 2,
      "scheduled": 1,
      "averageRating": 4.0
    }
  }
}
```

**前端调用示例**

```typescript
// 获取申请详情用于详情页展示
async function fetchApplicationDetail(id: number) {
  const response = await apiClient.get(`/applications/${id}/detail`);
  return response.data.data;
}

// 在详情页组件中使用
function ApplicationDetailPage({ id }: { id: number }) {
  const [detail, setDetail] = useState<ApplicationDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchApplicationDetail(id)
      .then(setDetail)
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spinner />;
  if (!detail) return <NotFound />;

  return (
    <div>
      <ApplicationCard application={detail.application} company={detail.company} />
      <StatisticsPanel stats={detail.statistics} />
      <InterviewList interviews={detail.interviews} />
      <ActivityLog logs={detail.logs} />
    </div>
  );
}
```

---

### 4.2 导出Excel文件

**请求**

```http
GET /api/data/export/excel
```

**响应**

- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- 文件名: `求职记录_yyyy-MM-dd.xlsx`

**Excel列定义**

| 列名 | 说明 |
|------|------|
| ID | 申请ID |
| 职位名称 | 职位标题 |
| 公司名称 | 公司名称 |
| 状态 | 已翻译为中文 |
| 工作类型 | 全职/兼职/实习/合同 |
| 工作地点 | 工作地点 |
| 薪资范围 | 格式：20K-35K |
| 申请日期 | yyyy-MM-dd |
| 优先级 | 1-10 |
| 面试次数 | 关联的面试数量 |
| 备注 | 备注信息 |
| 创建时间 | yyyy-MM-dd HH:mm:ss |

**前端调用示例**

```typescript
// 方式1：直接打开链接下载
function downloadExcel() {
  window.open('http://localhost:8080/api/data/export/excel');
}

// 方式2：使用axios下载并自定义文件名
async function downloadExcelWithAxios() {
  const response = await apiClient.get('/export/excel', {
    responseType: 'blob'
  });

  // 从响应头获取文件名，或使用默认名称
  const contentDisposition = response.headers['content-disposition'];
  const fileName = contentDisposition
    ? contentDisposition.split('filename=')[1]
    : `求职记录_${new Date().toISOString().split('T')[0]}.xlsx`;

  // 创建下载链接
  const url = window.URL.createObjectURL(new Blob([response.data]));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', fileName);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}
```

---

### 4.3 导出JSON数据

**请求**

```http
GET /api/data/export/json
```

**响应**

```typescript
interface ExportJsonResponse {
  code: number;
  message: string;
  data: ApplicationDetail[]; // 与 /applications/{id}/detail 相同结构
}
```

**前端调用示例**

```typescript
// 获取JSON数据用于本地备份或分析
async function exportJson() {
  const response = await apiClient.get('/export/json');
  return response.data.data;
}

// 下载为JSON文件
async function downloadJsonFile() {
  const data = await exportJson();

  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: 'application/json'
  });

  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `求职记录_${new Date().toISOString().split('T')[0]}.json`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
```

---

## 五、前端服务层封装

### 5.1 创建 dataApi.ts

```typescript
// src/services/dataApi.ts
import apiClient from './api';
import type {
  JobApplication,
  Company,
  InterviewRecord,
  ApplicationLog
} from '@/types';

// ==================== 类型定义 ====================

export interface ApplicationDetail {
  application: JobApplication;
  company: Company | null;
  interviews: InterviewRecord[];
  logs: ApplicationLog[];
  statistics: {
    total: number;
    completed: number;
    scheduled: number;
    averageRating: number;
  };
}

export interface UpdateApplicationRequest {
  companyId?: number;
  jobTitle?: string;
  jobDescription?: string;
  jobType?: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP';
  workLocation?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  jobUrl?: string;
  status?: 'WISHLIST' | 'APPLIED' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN';
  applicationDate?: string;
  priority?: number;
  notes?: string;
}

export interface UpdateInterviewRequest {
  applicationId?: number;
  interviewType?: 'PHONE' | 'VIDEO' | 'ONSITE' | 'TECHNICAL' | 'HR';
  interviewDate?: string;
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status?: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  rating?: number;
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired?: boolean;
}

export interface UpdateCompanyRequest {
  name?: string;
  industry?: string;
  size?: string;
  location?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
}

// ==================== API 服务 ====================

export const dataApi = {
  // ---------- 申请相关 ----------
  /**
   * 更新求职申请
   */
  updateApplication: (id: number, data: UpdateApplicationRequest) =>
    apiClient.put(`/applications/${id}`, data).then(r => r.data),

  /**
   * 删除求职申请（保护性删除）
   */
  deleteApplication: (id: number) =>
    apiClient.delete(`/applications/${id}`).then(r => r.data),

  /**
   * 获取申请详情（聚合信息）
   */
  getApplicationDetail: (id: number): Promise<ApplicationDetail> =>
    apiClient.get(`/applications/${id}/detail`).then(r => r.data.data),

  // ---------- 面试相关 ----------
  /**
   * 更新面试记录
   */
  updateInterview: (id: number, data: UpdateInterviewRequest) =>
    apiClient.put(`/interviews/${id}`, data).then(r => r.data),

  /**
   * 删除面试记录
   */
  deleteInterview: (id: number) =>
    apiClient.delete(`/interviews/${id}`).then(r => r.data),

  // ---------- 公司相关 ----------
  /**
   * 更新公司信息
   */
  updateCompany: (id: number, data: UpdateCompanyRequest) =>
    apiClient.put(`/companies/${id}`, data).then(r => r.data),

  /**
   * 删除公司（保护性删除）
   */
  deleteCompany: (id: number) =>
    apiClient.delete(`/companies/${id}`).then(r => r.data),

  // ---------- 导出相关 ----------
  /**
   * 下载Excel文件
   */
  downloadExcel: () => {
    window.open(`${apiClient.defaults.baseURL}/export/excel`);
  },

  /**
   * 下载Excel文件（自定义处理）
   */
  downloadExcelBlob: async () => {
    const response = await apiClient.get('/export/excel', {
      responseType: 'blob'
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `求职记录_${new Date().toISOString().split('T')[0]}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  },

  /**
   * 获取JSON导出数据
   */
  getJsonExport: (): Promise<ApplicationDetail[]> =>
    apiClient.get('/export/json').then(r => r.data.data),

  /**
   * 下载JSON文件
   */
  downloadJsonFile: async () => {
    const data = await dataApi.getJsonExport();

    const blob = new Blob([JSON.stringify(data, null, 2)], {
      type: 'application/json'
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `求职记录_${new Date().toISOString().split('T')[0]}.json`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  },
};

export default dataApi;
```

---

## 六、前端使用示例

### 6.1 在组件中使用

```tsx
import { dataApi } from '@/services/dataApi';
import { useState } from 'react';

function ApplicationCard({ application }: { application: JobApplication }) {
  const [isDeleting, setIsDeleting] = useState(false);

  // 更新状态
  const handleStatusChange = async (newStatus: string) => {
    try {
      await dataApi.updateApplication(application.id, { status: newStatus as any });
      // 刷新数据或更新本地状态
    } catch (error) {
      console.error('更新失败', error);
    }
  };

  // 删除申请
  const handleDelete = async () => {
    if (!confirm('确定要删除这条申请吗？')) return;

    setIsDeleting(true);
    try {
      const result = await dataApi.deleteApplication(application.id);
      // 删除成功，跳转或刷新列表
    } catch (error: any) {
      // 显示保护性删除错误
      alert(error.response?.data?.message || '删除失败');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="application-card">
      {/* ... */}
      <button onClick={handleDelete} disabled={isDeleting}>
        {isDeleting ? '删除中...' : '删除'}
      </button>
    </div>
  );
}
```

### 6.2 详情页使用聚合接口

```tsx
function ApplicationDetailPage({ id }: { id: number }) {
  const [detail, setDetail] = useState<ApplicationDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dataApi.getApplicationDetail(id)
      .then(setDetail)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Spinner />;
  if (!detail) return <NotFound />;

  return (
    <div className="detail-page">
      {/* 基本信息 */}
      <ApplicationHeader application={detail.application} company={detail.company} />

      {/* 统计面板 */}
      <StatisticsPanel
        total={detail.statistics.total}
        completed={detail.statistics.completed}
        scheduled={detail.statistics.scheduled}
        avgRating={detail.statistics.averageRating}
      />

      {/* 面试列表 */}
      <InterviewTimeline interviews={detail.interviews} />

      {/* 活动日志 */}
      <ActivityLog logs={detail.logs} />
    </div>
  );
}
```

### 6.3 导出功能

```tsx
function ExportButton() {
  const [exporting, setExporting] = useState<'excel' | 'json' | null>(null);

  const handleExportExcel = async () => {
    setExporting('excel');
    try {
      await dataApi.downloadExcelBlob();
    } finally {
      setExporting(null);
    }
  };

  const handleExportJson = async () => {
    setExporting('json');
    try {
      await dataApi.downloadJsonFile();
    } finally {
      setExporting(null);
    }
  };

  return (
    <div className="flex gap-2">
      <button onClick={handleExportExcel} disabled={exporting !== null}>
        {exporting === 'excel' ? '导出中...' : '导出 Excel'}
      </button>
      <button onClick={handleExportJson} disabled={exporting !== null}>
        {exporting === 'json' ? '导出中...' : '导出 JSON'}
      </button>
    </div>
  );
}
```

---

## 七、注意事项

### 7.1 保护性删除

删除申请和公司时，如果存在关联数据会阻止删除：

```typescript
// 正确的错误处理方式
async function safeDeleteApplication(id: number) {
  try {
    await dataApi.deleteApplication(id);
    return { success: true };
  } catch (error: any) {
    const message = error.response?.data?.message;

    // 检查是否为保护性删除错误
    if (message?.includes('面试记录')) {
      return {
        success: false,
        reason: 'has_interviews',
        message
      };
    }

    return { success: false, reason: 'unknown', message };
  }
}
```

### 7.2 状态枚举

**申请状态**

| 值 | 中文 | 说明 |
|---|------|------|
| WISHLIST | 待投递 | 意愿清单 |
| APPLIED | 已投递 | 已提交申请 |
| INTERVIEW | 面试中 | 正在面试流程 |
| OFFER | 已录用 | 收到offer |
| REJECTED | 已拒绝 | 被拒绝或拒绝offer |
| WITHDRAWN | 已撤回 | 主动撤回申请 |

**面试状态**

| 值 | 中文 |
|---|------|
| SCHEDULED | 已安排 |
| COMPLETED | 已完成 |
| CANCELLED | 已取消 |
| NO_SHOW | 未出席 |

**工作类型**

| 值 | 中文 |
|---|------|
| FULL_TIME | 全职 |
| PART_TIME | 兼职 |
| CONTRACT | 合同 |
| INTERNSHIP | 实习 |

---

**文档版本**: 1.0
**最后更新**: 2026-03-13
