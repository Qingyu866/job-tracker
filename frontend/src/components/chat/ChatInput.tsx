import { useState, useRef } from 'react';
import { Send, Image, X, Loader2 } from 'lucide-react';
import type { ImageAttachment, PendingImage } from '@/types/chat';
import { chatApi } from '@/services/chatApi';

interface ChatInputProps {
  onSend: (content: string, images: ImageAttachment[]) => void;
  disabled?: boolean;
  sessionKey: string;
}

const MAX_IMAGES = 5;
const MAX_FILE_SIZE = 10 * 1024 * 1024;
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

export function ChatInput({ onSend, disabled, sessionKey }: ChatInputProps) {
  const [input, setInput] = useState('');
  const [pendingImages, setPendingImages] = useState<PendingImage[]>([]);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const hasContent = input.trim().length > 0;
    const hasImages = pendingImages.length > 0;
    
    if ((!hasContent && !hasImages) || disabled) {
      return;
    }

    const uploadedImages: ImageAttachment[] = [];
    
    for (const pending of pendingImages) {
      if (pending.uploadedAttachment) {
        uploadedImages.push(pending.uploadedAttachment);
      }
    }

    onSend(input.trim(), uploadedImages);
    setInput('');
    
    pendingImages.forEach(img => {
      if (img.preview) {
        URL.revokeObjectURL(img.preview);
      }
    });
    setPendingImages([]);
    
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const handleInput = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    setInput(value);
    
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      const newHeight = textareaRef.current.scrollHeight;
      textareaRef.current.style.height = `${newHeight}px`;
    }
  };

  const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    
    if (pendingImages.length + files.length > MAX_IMAGES) {
      alert(`最多只能上传 ${MAX_IMAGES} 张图片`);
      return;
    }

    for (const file of files) {
      if (!ALLOWED_TYPES.includes(file.type)) {
        alert(`不支持的文件类型: ${file.type}`);
        continue;
      }

      if (file.size > MAX_FILE_SIZE) {
        alert(`文件过大: ${file.name}，最大支持 10MB`);
        continue;
      }

      const pendingId = `pending-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      const preview = URL.createObjectURL(file);

      const pendingImage: PendingImage = {
        id: pendingId,
        file,
        preview,
        uploading: true,
      };

      setPendingImages(prev => [...prev, pendingImage]);

      try {
        const attachment = await chatApi.uploadImage(file, sessionKey);
        
        setPendingImages(prev => 
          prev.map(img => 
            img.id === pendingId 
              ? { ...img, uploading: false, uploadedAttachment: attachment }
              : img
          )
        );
      } catch (error) {
        console.error('上传图片失败:', error);
        setPendingImages(prev => 
          prev.map(img => 
            img.id === pendingId 
              ? { ...img, uploading: false, error: '上传失败' }
              : img
          )
        );
      }
    }

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = (id: string) => {
    setPendingImages(prev => {
      const img = prev.find(i => i.id === id);
      if (img?.preview) {
        URL.revokeObjectURL(img.preview);
      }
      return prev.filter(i => i.id !== id);
    });
  };

  const canSend = (input.trim().length > 0 || pendingImages.length > 0) && 
                  !disabled && 
                  !pendingImages.some(img => img.uploading);

  return (
    <div className="space-y-2">
      {pendingImages.length > 0 && (
        <div className="flex flex-wrap gap-2 p-2 bg-paper-100 rounded-lg">
          {pendingImages.map(img => (
            <div key={img.id} className="relative group">
              <div className="w-16 h-16 rounded-lg overflow-hidden border border-paper-200 bg-paper-50">
                {img.error ? (
                  <div className="w-full h-full flex items-center justify-center text-xs text-red-500 text-center p-1">
                    {img.error}
                  </div>
                ) : (
                  <img
                    src={img.preview}
                    alt="预览"
                    className="w-full h-full object-cover"
                  />
                )}
                {img.uploading && (
                  <div className="absolute inset-0 bg-paper-900/50 flex items-center justify-center">
                    <Loader2 className="w-5 h-5 text-white animate-spin" />
                  </div>
                )}
              </div>
              <button
                type="button"
                onClick={() => handleRemoveImage(img.id)}
                className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600"
              >
                <X className="w-3 h-3" />
              </button>
            </div>
          ))}
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex gap-2">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          multiple
          onChange={handleImageSelect}
          className="hidden"
          disabled={disabled || pendingImages.length >= MAX_IMAGES}
        />
        
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          disabled={disabled || pendingImages.length >= MAX_IMAGES}
          className="px-3 py-3 rounded-lg bg-paper-100 text-paper-600 hover:bg-paper-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          title={pendingImages.length >= MAX_IMAGES ? `最多${MAX_IMAGES}张图片` : '添加图片'}
        >
          <Image className="w-5 h-5" />
        </button>

        <textarea
          ref={textareaRef}
          value={input}
          onChange={handleInput}
          onKeyDown={handleKeyDown}
          placeholder="输入消息...（Shift+Enter 换行）"
          disabled={disabled}
          rows={1}
          className="flex-1 px-3 md:px-4 py-3 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber disabled:opacity-50 text-base resize-none overflow-hidden"
          style={{ minHeight: '48px' }}
        />
        
        <button
          type="submit"
          disabled={!canSend}
          className="px-3 md:px-4 py-3 bg-accent-amber text-paper-800 rounded-lg hover:bg-accent-amber/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          aria-label="发送"
        >
          <Send className="w-5 h-5" />
        </button>
      </form>
      
      {pendingImages.length > 0 && (
        <div className="text-xs text-paper-500 text-right">
          {pendingImages.length}/{MAX_IMAGES} 张图片
        </div>
      )}
    </div>
  );
}
