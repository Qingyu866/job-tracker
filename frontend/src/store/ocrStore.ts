import { create } from 'zustand';
import { ocrApi } from '@/services/ocrApi';
import type { OcrResult, OcrRecord } from '@/types/ocr';

interface OcrState {
  currentImage: {
    dataUrl: string;
    name: string;
    type: 'resume' | 'jd' | 'general';
  } | null;
  
  ocrResult: OcrResult | null;
  
  records: OcrRecord[];
  
  isRecognizing: boolean;
}

interface OcrStore extends OcrState {
  setCurrentImage: (image: OcrState['currentImage']) => void;
  clearCurrentImage: () => void;
  recognizeImage: (imageData: string) => Promise<void>;
  clearResult: () => void;
  fetchRecords: () => Promise<void>;
}

export const useOcrStore = create<OcrStore>((set) => ({
  currentImage: null,
  ocrResult: null,
  records: [],
  isRecognizing: false,

  setCurrentImage: (image) => set({ currentImage: image }),

  clearCurrentImage: () => set({ currentImage: null, ocrResult: null }),

  recognizeImage: async (imageData: string) => {
    set({ ocrResult: { status: 'pending' }, isRecognizing: true });

    try {
      const result = await ocrApi.recognizeImage(imageData);
      set({ ocrResult: result, isRecognizing: false });
    } catch (error) {
      set({
        ocrResult: { status: 'failed', error: error instanceof Error ? error.message : '识别失败' },
        isRecognizing: false
      });
    }
  },

  clearResult: () => set({ ocrResult: null }),

  fetchRecords: async () => {
    try {
      const records = await ocrApi.getRecords();
      set({ records });
    } catch (error) {
      console.error('获取 OCR 记录失败:', error);
    }
  },
}));
