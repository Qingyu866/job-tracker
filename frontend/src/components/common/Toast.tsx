import { CheckCircle2, XCircle, AlertCircle } from 'lucide-react';
import { useEffect, useState } from 'react';

interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'info';
  duration?: number;
  onClose?: () => void;
}

export function Toast({ message, type, duration = 3000, onClose }: ToastProps) {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(false);
      setTimeout(() => onClose?.(), 300); // 等待动画完成
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose]);

  const icons = {
    success: <CheckCircle2 className="w-5 h-5 text-green-600" />,
    error: <XCircle className="w-5 h-5 text-red-600" />,
    info: <AlertCircle className="w-5 h-5 text-blue-600" />,
  };

  const bgColors = {
    success: 'bg-green-50 border-green-200',
    error: 'bg-red-50 border-red-200',
    info: 'bg-blue-50 border-blue-200',
  };

  return (
    <div
      className={`fixed top-4 right-4 z-[100] flex items-center gap-3 px-4 py-3 rounded-lg border-2 shadow-lg transition-all duration-300 ${
        bgColors[type]
      } ${isVisible ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-full'}`}
    >
      {icons[type]}
      <span className="text-sm font-medium text-gray-800">{message}</span>
    </div>
  );
}

// 简单的 Toast 管理器
let toastId = 0;

export const toast = {
  success: (message: string, duration?: number) => {
    const id = toastId++;
    // 这里需要通过事件或其他方式来触发显示
    // 暂时先返回 alert，后续可以实现完整的 Toast 系统
    console.log(`[Toast Success] ${message}`);
  },
  error: (message: string, duration?: number) => {
    const id = toastId++;
    console.log(`[Toast Error] ${message}`);
  },
  info: (message: string, duration?: number) => {
    const id = toastId++;
    console.log(`[Toast Info] ${message}`);
  },
};
