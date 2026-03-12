import { useEffect } from 'react';
import { X } from 'lucide-react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
}

export function Modal({ isOpen, onClose, title, children }: ModalProps) {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => {
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-2 md:p-4">
      {/* 遮罩 */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* 模态框 */}
      <div className="relative bg-[#f5f0e6] rounded-xl shadow-2xl max-w-lg w-full max-h-[90vh] overflow-y-auto border-2 border-paper-400 md:mx-4">
        {/* 头部 */}
        <div className="flex items-center justify-between p-5 border-b-2 border-paper-300 sticky top-0 bg-[#f5f0e6] z-10">
          <h3 className="font-serif text-paper-800 font-bold text-lg md:text-xl">{title}</h3>
          <button
            onClick={onClose}
            className="p-2 hover:bg-paper-200 rounded-lg text-paper-700 transition-colors border border-paper-400 hover:border-paper-500"
            aria-label="关闭"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 内容 */}
        <div className="p-5">{children}</div>
      </div>
    </div>
  );
}
