import { describe, it, expect, vi, beforeEach } from 'vitest';
import { apiClient } from '@/lib/apiClient';
import { authApi } from '@/services/authApi';
import { dataApi } from '@/services/dataApi';
import { interviewApi } from '@/services/interviewApi';
import { resumeApi } from '@/services/resumeApi';
import { skillApi } from '@/services/skillApi';
import { statusApi } from '@/services/statusApi';
import { ocrApi } from '@/services/ocrApi';
import { chatApi } from '@/services/chatApi';

vi.mock('@/lib/apiClient', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

describe('API接口测试', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('认证模块', () => {
    it('登录接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: { token: 'test-token', user: { id: 1, username: 'test' } },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

      const result = await authApi.login({ username: 'test', password: 'test' });

      expect(apiClient.post).toHaveBeenCalledWith('/auth/login', {
        username: 'test',
        password: 'test',
      });
      expect(result).toEqual(mockResponse);
    });

    it('获取用户信息接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, username: 'test', email: 'test@example.com' },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await authApi.getCurrentUserInfo();

      expect(apiClient.get).toHaveBeenCalledWith('/auth/info');
      expect(result).toEqual(mockResponse);
    });
  });

  describe('求职申请模块', () => {
    it('获取所有申请接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, jobTitle: 'Java开发工程师' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.getApplications();

      expect(apiClient.get).toHaveBeenCalledWith('/applications', { params: {} });
      expect(result).toEqual(mockResponse.data);
    });

    it('根据状态获取申请接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, status: 'APPLIED' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.getApplicationsByStatus('APPLIED');

      expect(apiClient.get).toHaveBeenCalledWith('/applications/status/APPLIED');
      expect(result).toEqual(mockResponse.data);
    });

    it('分页查询申请接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: {
          records: [{ id: 1 }],
          total: 100,
          size: 10,
          current: 1,
          pages: 10,
        },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.getApplicationsPage({ pageNum: 1, pageSize: 10 });

      expect(apiClient.get).toHaveBeenCalledWith('/applications/page', {
        params: { pageNum: 1, pageSize: 10 },
      });
      expect(result).toEqual(mockResponse.data);
    });

    it('更新申请状态接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: '状态更新成功',
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.put).mockResolvedValue(mockResponse);

      const result = await dataApi.updateApplicationStatus(1, 'INTERVIEW');

      expect(apiClient.put).toHaveBeenCalledWith('/applications/1/status', null, {
        params: { status: 'INTERVIEW' },
      });
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('面试记录模块', () => {
    it('获取即将进行的面试接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, status: 'SCHEDULED' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.getUpcomingInterviews();

      expect(apiClient.get).toHaveBeenCalledWith('/interviews/upcoming');
      expect(result).toEqual(mockResponse.data);
    });

    it('获取面试进度接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: {
          totalRounds: 3,
          completedRounds: 1,
          passedRounds: 1,
          currentRound: 2,
          progressText: '第2轮面试进行中',
          allPassed: false,
          hasFailed: false,
        },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.getInterviewProgress(1);

      expect(apiClient.get).toHaveBeenCalledWith('/interviews/applications/1/progress');
      expect(result).toEqual(mockResponse.data);
    });

    it('开始面试接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: true,
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.put).mockResolvedValue(mockResponse);

      const result = await dataApi.startInterview(1);

      expect(apiClient.put).toHaveBeenCalledWith('/interviews/1/start');
      expect(result).toBe(true);
    });
  });

  describe('模拟面试模块', () => {
    it('暂停面试接口应该正确调用', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({});

      await interviewApi.pauseInterview('session-123');

      expect(apiClient.post).toHaveBeenCalledWith('/mock-interview/sessions/session-123/pause');
    });

    it('恢复面试接口应该正确调用', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({});

      await interviewApi.resumeInterview('session-123');

      expect(apiClient.post).toHaveBeenCalledWith('/mock-interview/sessions/session-123/resume');
    });

    it('获取面试进度接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: {
          sessionId: 'session-123',
          state: 'TECHNICAL_QA',
          currentRound: 5,
          totalPlans: 25,
          completedPlans: 4,
          pendingPlans: 21,
          progressPercentage: 16.0,
          pausedAt: null,
          resumedAt: null,
        },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await interviewApi.getProgress('session-123');

      expect(apiClient.get).toHaveBeenCalledWith('/mock-interview/sessions/session-123/progress');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('简历模块', () => {
    it('获取当前用户简历列表接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ resumeId: 1, resumeName: '简历1' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await resumeApi.getMyResumes();

      expect(apiClient.get).toHaveBeenCalledWith('/resumes/my');
      expect(result).toEqual(mockResponse.data);
    });

    it('获取当前用户默认简历接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: { resumeId: 1, resumeName: '默认简历', isDefault: true },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await resumeApi.getMyDefaultResume();

      expect(apiClient.get).toHaveBeenCalledWith('/resumes/my/default');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('公司信息模块', () => {
    it('根据名称获取公司接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: { id: 1, name: '字节跳动' },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.getCompanyByName('字节跳动');

      expect(apiClient.get).toHaveBeenCalledWith('/companies/name', {
        params: { name: '字节跳动' },
      });
      expect(result).toEqual(mockResponse.data);
    });

    it('搜索公司接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, name: '字节跳动' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await dataApi.searchCompanies('字节');

      expect(apiClient.get).toHaveBeenCalledWith('/companies/search', {
        params: { keyword: '字节' },
      });
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('技能标签模块', () => {
    it('获取所有技能接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ skillId: 1, skillName: 'Java' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await skillApi.getAllSkills();

      expect(apiClient.get).toHaveBeenCalledWith('/skills');
      expect(result).toEqual(mockResponse.data);
    });

    it('搜索技能接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ skillId: 1, skillName: 'Java' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await skillApi.searchSkills('Java');

      expect(apiClient.get).toHaveBeenCalledWith('/skills/search', {
        params: { keyword: 'Java' },
      });
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('状态转换模块', () => {
    it('获取申请状态转换规则接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: { APPLIED: ['SCREENING', 'INTERVIEW'] },
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await statusApi.getApplicationTransitions();

      expect(apiClient.get).toHaveBeenCalledWith('/status/transitions');
      expect(result).toEqual(mockResponse.data);
    });

    it('验证申请状态转换接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: true,
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await statusApi.validateApplicationTransition('APPLIED', 'INTERVIEW');

      expect(apiClient.get).toHaveBeenCalledWith('/status/applications/validate', {
        params: { from: 'APPLIED', to: 'INTERVIEW' },
      });
      expect(result).toBe(true);
    });
  });

  describe('OCR识别模块', () => {
    it('获取当前用户OCR记录接口应该正确调用', async () => {
      const mockResponse = {
        code: 200,
        data: [{ id: 1, imageType: 'RESUME' }],
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await ocrApi.getMyRecords();

      expect(apiClient.get).toHaveBeenCalledWith('/ocr/records/my');
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('聊天模块', () => {
    it('获取图片接口应该正确调用', async () => {
      const mockBlob = new Blob(['image data'], { type: 'image/png' });
      const mockResponse = {
        code: 200,
        data: mockBlob,
        message: '成功',
        timestamp: Date.now(),
        success: true,
      };
      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await chatApi.getImage(1, 'session-123');

      expect(apiClient.get).toHaveBeenCalledWith(
        'http://localhost:8080/api/chat/images/1',
        {
          params: { sessionId: 'session-123' },
          responseType: 'blob',
        }
      );
      expect(result).toEqual(mockResponse.data);
    });
  });
});
