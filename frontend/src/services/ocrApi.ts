import { apiClient } from '@/lib/apiClient';
import type { OcrResult, OcrImageType, OcrRecord, ResumeInfo, JdInfo } from '@/types/ocr';

export const ocrApi = {
  async recognizeImage(imageData: string): Promise<OcrResult> {
    const response = await apiClient.post<OcrResult>('/ocr/recognize', { imageData });
    return response.data;
  },

  async recognizeResume(file: File): Promise<ResumeInfo> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<ResumeInfo>('/ocr/resume', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
  },

  async recognizeJd(file: File, applicationId?: number): Promise<JdInfo> {
    const formData = new FormData();
    formData.append('file', file);
    if (applicationId) {
      formData.append('applicationId', applicationId.toString());
    }
    const response = await apiClient.post<JdInfo>('/ocr/jd', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
  },

  async getRecords(page = 0, size = 20): Promise<OcrRecord[]> {
    const response = await apiClient.get<OcrRecord[]>('/ocr/records', {
      params: { page, size }
    });
    return response.data;
  },

  async uploadImage(file: File): Promise<{ id: number; publicUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post<{ id: number; publicUrl: string }>('/ocr/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
  },
};

export function detectImageType(filename: string): OcrImageType {
  const lowerName = filename.toLowerCase();
  if (lowerName.includes('resume') || lowerName.includes('简历') || lowerName.includes('cv')) {
    return 'resume';
  }
  if (lowerName.includes('jd') || lowerName.includes('job') || lowerName.includes('职位') || lowerName.includes('招聘')) {
    return 'jd';
  }
  return 'general';
}

export function fileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

export function validateImageFile(file: File): { valid: boolean; error?: string } {
  const MAX_SIZE = 10 * 1024 * 1024;
  if (file.size > MAX_SIZE) {
    return { valid: false, error: '图片大小不能超过 10MB' };
  }

  const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/jpg', 'application/pdf'];
  if (!ACCEPTED_TYPES.includes(file.type)) {
    return { valid: false, error: '不支持的文件类型，请上传 JPG、PNG 或 PDF' };
  }

  return { valid: true };
}

export function validateImageDimensions(file: File): Promise<{ valid: boolean; error?: string }> {
  return new Promise((resolve) => {
    if (file.type === 'application/pdf') {
      resolve({ valid: true });
      return;
    }

    const img = new Image();
    const url = URL.createObjectURL(file);
    
    img.onload = () => {
      URL.revokeObjectURL(url);
      if (img.width > 4096 || img.height > 4096) {
        resolve({ valid: false, error: '图片尺寸过大，建议使用 4096x4096 以下的图片' });
      } else {
        resolve({ valid: true });
      }
    };
    
    img.onerror = () => {
      URL.revokeObjectURL(url);
      resolve({ valid: false, error: '图片文件损坏' });
    };
    
    img.src = url;
  });
}
