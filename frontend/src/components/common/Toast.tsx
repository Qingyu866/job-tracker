import { CheckCircle2, XCircle, AlertCircle, X } from 'lucide-react';
import { useEffect, useState } from 'react';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastProps {
  id: number;
  message: string;
  type: ToastType;
  duration?: number;
  onClose?: (id: number) => void;
}

export function Toast({ id, message, type, duration = 3000, onClose }: ToastProps) {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(false);
      setTimeout(() => onClose?.(id), 300);
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose, id]);

  const icons = {
    success: <CheckCircle2 className="w-5 h-5" style={{ color: '#228B22' }} />,
    error: <XCircle className="w-5 h-5" style={{ color: '#8B4513' }} />,
    info: <AlertCircle className="w-5 h-5" style={{ color: '#654321' }} />,
  };

  const bgColors = {
    success: 'bg-paper-50 border-paper-400',
    error: 'bg-paper-50 border-paper-400',
    info: 'bg-paper-50 border-paper-400',
  };

  return (
    <div
      className={`fixed top-4 right-4 z-[100] flex items-center gap-3 px-4 py-3 rounded-lg border-2 shadow-lg transition-all duration-300 ${
        bgColors[type]
      } ${isVisible ? 'opacity-100 translate-x-0' : 'opacity-0 translate-x-full'}`}
      style={{
        fontFamily: 'Georgia, "Times New Roman", Times, serif',
        backgroundColor: '#faf8f3',
        borderColor: type === 'success' ? '#ddd4c0' : type === 'error' ? '#c9bd9f' : '#ddd4c0',
      }}
    >
      {icons[type]}
      <span className="text-sm font-medium flex-1" style={{ color: '#4a3828' }}>
        {message}
      </span>
      <button
        onClick={() => {
          setIsVisible(false);
          setTimeout(() => onClose?.(id), 300);
        }}
        className="p-1 hover:bg-paper-200 rounded transition-colors"
        style={{ color: '#6b5344' }}
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
}

export interface ToastContainerProps {
  toasts: Array<{ id: number; message: string; type: ToastType }>;
  onClose: (id: number) => void;
}

export function ToastContainer({ toasts, onClose }: ToastContainerProps) {
  return (
    <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
      {toasts.map((toast) => (
        <div key={toast.id} className="pointer-events-auto">
          <Toast
            id={toast.id}
            message={toast.message}
            type={toast.type}
            onClose={onClose}
          />
        </div>
      ))}
    </div>
  );
}
