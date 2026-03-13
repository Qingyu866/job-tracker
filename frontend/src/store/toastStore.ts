import { create } from 'zustand';

export interface ToastItem {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

interface ToastStore {
  toasts: ToastItem[];
  addToast: (message: string, type: 'success' | 'error' | 'info') => void;
  removeToast: (id: number) => void;
}

let toastIdCounter = 0;

export const useToastStore = create<ToastStore>((set) => ({
  toasts: [],
  addToast: (message, type) => {
    const id = ++toastIdCounter;
    set((state) => ({
      toasts: [...state.toasts, { id, message, type }],
    }));
  },
  removeToast: (id) => {
    set((state) => ({
      toasts: state.toasts.filter((toast) => toast.id !== id),
    }));
  },
}));

// 便捷方法
export const toast = {
  success: (message: string) => {
    useToastStore.getState().addToast(message, 'success');
  },
  error: (message: string) => {
    useToastStore.getState().addToast(message, 'error');
  },
  info: (message: string) => {
    useToastStore.getState().addToast(message, 'info');
  },
};
