import { AlertTriangle } from 'lucide-react';

interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  confirmVariant?: 'danger' | 'primary';
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  isOpen,
  title,
  message,
  confirmText = '确定',
  cancelText = '取消',
  confirmVariant = 'danger',
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onCancel}
      />

      <div className="relative bg-[#f5f0e6] rounded-xl shadow-2xl max-w-sm w-full border-2 border-paper-400 animate-in fade-in zoom-in-95 duration-200">
        <div className="p-5">
          <div className="flex items-center gap-3 mb-4">
            <div className={`p-2 rounded-full ${confirmVariant === 'danger' ? 'bg-red-100' : 'bg-blue-100'}`}>
              <AlertTriangle className={`w-5 h-5 ${confirmVariant === 'danger' ? 'text-red-600' : 'text-blue-600'}`} />
            </div>
            <h3 className="font-serif text-paper-800 font-bold text-lg">{title}</h3>
          </div>

          <p className="text-paper-600 text-sm mb-6">{message}</p>

          <div className="flex gap-3">
            <button
              onClick={onCancel}
              className="flex-1 px-4 py-2.5 bg-paper-100 text-paper-700 rounded-lg hover:bg-paper-200 transition-colors border-2 border-paper-300 font-medium"
            >
              {cancelText}
            </button>
            <button
              onClick={onConfirm}
              className={`flex-1 px-4 py-2.5 rounded-lg transition-colors font-medium ${
                confirmVariant === 'danger'
                  ? 'bg-red-600 text-white hover:bg-red-700 border-2 border-red-500'
                  : 'bg-accent-amber text-paper-900 hover:bg-accent-amber/90 border-2 border-amber-500'
              }`}
            >
              {confirmText}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
