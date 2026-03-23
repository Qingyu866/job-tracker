// 示例：使用 createComplete API 创建完整简历
import { resumeApi } from '@/services/resumeApi';
import type { CreateCompleteResumeRequest } from '@/types/resume';

// 示例数据
const completeResumeData: CreateCompleteResumeRequest = {
  resume: {
    resumeName: 'Java后端开发工程师-5年经验',
    isDefault: true,
    workYears: 5,
    currentPosition: '高级Java开发工程师',
    targetLevel: 'SENIOR',
    summary: '5年Java后端开发经验，精通Spring Boot、微服务架构，具有多个大型项目的设计和开发经验。'
  },
  skills: [
    {
      skillId: 1, // 假设从 /api/skills 获取的技能ID
      proficiencyLevel: 'EXPERT',
      experienceYears: 5,
      isCoreSkill: true
    },
    {
      skillId: 2,
      proficiencyLevel: 'ADVANCED',
      experienceYears: 4
    },
    {
      skillId: 3,
      proficiencyLevel: 'INTERMEDIATE',
      experienceYears: 3
    }
  ],
  projects: [
    {
      projectName: '电商订单系统重构',
      role: '技术负责人',
      startDate: '2023-01-01',
      endDate: '2023-12-31',
      description: '负责电商平台订单系统的架构设计和开发，实现了订单处理流程的优化。',
      responsibilities: '负责系统架构设计、核心模块开发、性能优化',
      achievements: '系统响应时间减少40%，订单处理能力提升3倍',
      techStack: 'Java, Spring Boot, Redis, MySQL, RabbitMQ',
    },
    {
      projectName: '用户管理系统',
      role: '核心开发者',
      startDate: '2022-01-01',
      endDate: '2022-12-31',
      description: '开发企业级用户管理系统，支持多租户、权限管理等功能。',
      techStack: 'Java, Spring Cloud, PostgreSQL',
    }
  ],
  workExperiences: [
    {
      companyName: '字节跳动',
      position: '高级Java开发工程师',
      startDate: '2021-06-01',
      endDate: '2023-12-31',
      description: '参与抖音后端服务的开发和维护，负责视频推荐系统的优化。',
      achievements: '优化推荐算法，提升用户点击率15%'
    },
    {
      companyName: '阿里巴巴',
      position: 'Java开发工程师',
      startDate: '2019-01-01',
      endDate: '2021-05-31',
      description: '负责淘宝商品详情页的后端开发，参与大促活动的技术支持。'
    }
  ]
};

// 调用API创建完整简历
async function createCompleteResume() {
  try {
    console.log('开始创建完整简历...');
    
    const result = await resumeApi.createComplete(completeResumeData);
    
    console.log('简历创建成功！');
    console.log('简历ID:', result.resume.resumeId);
    console.log('技能数量:', result.skills.length);
    console.log('项目数量:', result.projects.length);
    console.log('工作经历数量:', result.workExperiences.length);
    
    return result;
  } catch (error) {
    console.error('创建简历失败:', error);
    throw error;
  }
}

// 使用示例
createCompleteResume()
  .then(result => {
    console.log('创建成功，结果:', result);
  })
  .catch(error => {
    console.error('创建失败:', error);
  });

/**
 * 注意事项：
 * 1. 技能ID (skillId) 需要先调用 /api/skills 接口获取
 * 2. 日期格式为 YYYY-MM-DD
 * 3. 熟练度级别：BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
 * 4. targetLevel 取值：JUNIOR, MIDDLE, SENIOR, LEAD
 * 5. 所有必填字段必须提供
 */
