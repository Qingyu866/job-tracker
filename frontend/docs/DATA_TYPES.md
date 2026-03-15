# 数据类型定义

**文档版本**: 1.0
**创建日期**: 2026-03-13

---

## 一、枚举类型

### 1.1 申请状态 (ApplicationStatus)

```typescript
export type ApplicationStatus =
  | 'WISHLIST'    // 待投递
  | 'APPLIED'     // 已投递
  | 'INTERVIEW'   // 面试中
  | 'OFFER'       // 已录用
  | 'REJECTED'    // 已拒绝
  | 'WITHDRAWN';  // 已撤回

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
  WISHLIST: '待投递',
  APPLIED: '已投递',
  INTERVIEW: '面试中',
  OFFER: '已录用',
  REJECTED: '已拒绝',
  WITHDRAWN: '已撤回',
};

export const APPLICATION_STATUS_COLORS: Record<ApplicationStatus, string> = {
  WISHLIST: 'gray',
  APPLIED: 'blue',
  INTERVIEW: 'yellow',
  OFFER: 'green',
  REJECTED: 'red',
  WITHDRAWN: 'slate',
};
```

### 1.2 面试类型 (InterviewType)

```typescript
export type InterviewType =
  | 'PHONE'      // 电话面试
  | 'VIDEO'      // 视频面试
  | 'ONSITE'     // 现场面试
  | 'TECHNICAL'  // 技术面试
  | 'HR';        // HR面试

export const INTERVIEW_TYPE_LABELS: Record<InterviewType, string> = {
  PHONE: '电话面试',
  VIDEO: '视频面试',
  ONSITE: '现场面试',
  TECHNICAL: '技术面试',
  HR: 'HR面试',
};
```

### 1.3 面试状态 (InterviewStatus)

```typescript
export type InterviewStatus =
  | 'SCHEDULED'   // 已安排
  | 'COMPLETED'   // 已完成
  | 'CANCELLED'   // 已取消
  | 'NO_SHOW';    // 未出席

export const INTERVIEW_STATUS_LABELS: Record<InterviewStatus, string> = {
  SCHEDULED: '已安排',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  NO_SHOW: '未出席',
};
```

### 1.4 工作类型 (JobType)

```typescript
export type JobType =
  | 'FULL_TIME'    // 全职
  | 'PART_TIME'    // 兼职
  | 'CONTRACT'     // 合同
  | 'INTERNSHIP';  // 实习

export const JOB_TYPE_LABELS: Record<JobType, string> = {
  FULL_TIME: '全职',
  PART_TIME: '兼职',
  CONTRACT: '合同',
  INTERNSHIP: '实习',
};
```

---

## 二、实体类型

### 2.1 求职申请 (JobApplication)

```typescript
export interface JobApplication {
  id: number;
  companyId: number;
  jobTitle: string;
  jobDescription?: string;
  jobType?: JobType;
  workLocation?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  jobUrl?: string;
  status: ApplicationStatus;
  applicationDate?: string;  // YYYY-MM-DD
  priority?: number;         // 1-10
  notes?: string;
  createdAt: string;         // ISO 8601
  updatedAt: string;         // ISO 8601
  deleted: number;
}

// 更新请求（所有字段可选）
export type UpdateApplicationRequest = Partial<Omit<JobApplication, 'id' | 'createdAt' | 'updatedAt' | 'deleted'>>;
```

### 2.2 公司 (Company)

```typescript
export interface Company {
  id: number;
  name: string;
  industry?: string;
  size?: string;
  location?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

// 更新请求（所有字段可选）
export type UpdateCompanyRequest = Partial<Omit<Company, 'id' | 'createdAt' | 'updatedAt' | 'deleted'>>;
```

### 2.3 面试记录 (InterviewRecord)

```typescript
export interface InterviewRecord {
  id: number;
  applicationId: number;
  interviewType: InterviewType;
  interviewDate: string;         // ISO 8601
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status: InterviewStatus;
  rating?: number;               // 1-5
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired: boolean;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

// 更新请求（所有字段可选）
export type UpdateInterviewRequest = Partial<Omit<InterviewRecord, 'id' | 'createdAt' | 'updatedAt' | 'deleted'>>;
```

### 2.4 申请日志 (ApplicationLog)

```typescript
export type LogType =
  | 'STATUS_CHANGE'
  | 'INTERVIEW_SCHEDULED'
  | 'FEEDBACK_RECEIVED'
  | 'NOTE_ADDED'
  | 'DOCUMENT_UPLOADED';

export interface ApplicationLog {
  id: number;
  applicationId: number;
  logType: LogType;
  logTitle: string;
  logContent?: string;
  loggedBy: string;
  createdAt: string;
  deleted: number;
}
```

---

## 三、聚合DTO类型

### 3.1 申请详情聚合 (ApplicationDetail)

```typescript
export interface ApplicationDetail {
  application: JobApplication;
  company: Company | null;
  interviews: InterviewRecord[];
  logs: ApplicationLog[];
  statistics: InterviewStatistics;
}

export interface InterviewStatistics {
  total: number;          // 面试总数
  completed: number;      // 已完成
  scheduled: number;      // 已安排
  averageRating: number;  // 平均评分
}
```

---

## 四、API响应类型

### 4.1 通用响应

```typescript
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

// 成功响应（带布尔值）
export interface BooleanResponse extends ApiResponse<boolean> {}

// 列表响应
export interface ListResponse<T> extends ApiResponse<T[]> {}

// 单条响应
export interface ItemResponse<T> extends ApiResponse<T> {}
```

### 4.2 错误响应

```typescript
export interface ErrorResponse {
  code: 400 | 404 | 500;
  message: string;
  data: null;
}

// 保护性删除错误
export interface ProtectiveDeleteError extends ErrorResponse {
  code: 400;
  // message 示例：
  // "该申请下有 2 条面试记录，请先删除面试记录"
  // "该公司下有 3 条申请记录，请先删除申请记录"
}
```

---

## 五、前端类型文件

### 5.1 建议的文件结构

```
src/types/
├── index.ts          # 统一导出
├── enums.ts          # 枚举类型和标签
├── application.ts    # 申请相关类型
├── company.ts        # 公司相关类型
├── interview.ts      # 面试相关类型
├── api.ts            # API响应类型
└── chat.ts           # 聊天相关类型
```

### 5.2 完整类型文件示例

**src/types/enums.ts**

```typescript
export type ApplicationStatus =
  | 'WISHLIST'
  | 'APPLIED'
  | 'INTERVIEW'
  | 'OFFER'
  | 'REJECTED'
  | 'WITHDRAWN';

export type InterviewType =
  | 'PHONE'
  | 'VIDEO'
  | 'ONSITE'
  | 'TECHNICAL'
  | 'HR';

export type InterviewStatus =
  | 'SCHEDULED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export type JobType =
  | 'FULL_TIME'
  | 'PART_TIME'
  | 'CONTRACT'
  | 'INTERNSHIP';

export type LogType =
  | 'STATUS_CHANGE'
  | 'INTERVIEW_SCHEDULED'
  | 'FEEDBACK_RECEIVED'
  | 'NOTE_ADDED'
  | 'DOCUMENT_UPLOADED';

// 标签映射
export const LABELS = {
  applicationStatus: {
    WISHLIST: '待投递',
    APPLIED: '已投递',
    INTERVIEW: '面试中',
    OFFER: '已录用',
    REJECTED: '已拒绝',
    WITHDRAWN: '已撤回',
  } as const,

  interviewType: {
    PHONE: '电话面试',
    VIDEO: '视频面试',
    ONSITE: '现场面试',
    TECHNICAL: '技术面试',
    HR: 'HR面试',
  } as const,

  interviewStatus: {
    SCHEDULED: '已安排',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    NO_SHOW: '未出席',
  } as const,

  jobType: {
    FULL_TIME: '全职',
    PART_TIME: '兼职',
    CONTRACT: '合同',
    INTERNSHIP: '实习',
  } as const,
} as const;
```

**src/types/application.ts**

```typescript
import type { ApplicationStatus, JobType } from './enums';

export interface JobApplication {
  id: number;
  companyId: number;
  jobTitle: string;
  jobDescription?: string;
  jobType?: JobType;
  workLocation?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  jobUrl?: string;
  status: ApplicationStatus;
  applicationDate?: string;
  priority?: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

export type UpdateApplicationRequest = Partial<
  Omit<JobApplication, 'id' | 'createdAt' | 'updatedAt' | 'deleted'>
>;

export interface ApplicationDetail {
  application: JobApplication;
  company: import('./company').Company | null;
  interviews: import('./interview').InterviewRecord[];
  logs: import('./interview').ApplicationLog[];
  statistics: import('./interview').InterviewStatistics;
}
```

**src/types/company.ts**

```typescript
export interface Company {
  id: number;
  name: string;
  industry?: string;
  size?: string;
  location?: string;
  website?: string;
  description?: string;
  logoUrl?: string;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

export type UpdateCompanyRequest = Partial<
  Omit<Company, 'id' | 'createdAt' | 'updatedAt' | 'deleted'>
>;
```

**src/types/interview.ts**

```typescript
import type { InterviewType, InterviewStatus, LogType } from './enums';

export interface InterviewRecord {
  id: number;
  applicationId: number;
  interviewType: InterviewType;
  interviewDate: string;
  interviewerName?: string;
  interviewerTitle?: string;
  durationMinutes?: number;
  status: InterviewStatus;
  rating?: number;
  feedback?: string;
  technicalQuestions?: string;
  notes?: string;
  followUpRequired: boolean;
  createdAt: string;
  updatedAt: string;
  deleted: number;
}

export type UpdateInterviewRequest = Partial<
  Omit<InterviewRecord, 'id' | 'createdAt' | 'updatedAt' | 'deleted'>
>;

export interface ApplicationLog {
  id: number;
  applicationId: number;
  logType: LogType;
  logTitle: string;
  logContent?: string;
  loggedBy: string;
  createdAt: string;
  deleted: number;
}

export interface InterviewStatistics {
  total: number;
  completed: number;
  scheduled: number;
  averageRating: number;
}
```

**src/types/api.ts**

```typescript
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export type BooleanResponse = ApiResponse<boolean>;
export type ListResponse<T> = ApiResponse<T[]>;
export type ItemResponse<T> = ApiResponse<T>;
```

**src/types/index.ts**

```typescript
// 枚举
export * from './enums';

// 实体
export * from './application';
export * from './company';
export * from './interview';

// API
export * from './api';

// 聊天
export * from './chat';
```

---

**文档版本**: 1.0
**最后更新**: 2026-03-13
