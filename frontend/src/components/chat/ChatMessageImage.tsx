import { useState } from 'react';
import { ImageOff, ZoomIn } from 'lucide-react';
import type { ImageAttachment } from '@/types/chat';

interface ChatMessageImageProps {
  image: ImageAttachment;
  alt?: string;
  onClick?: () => void;
}

export function ChatMessageImage({ image, alt, onClick }: ChatMessageImageProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const handleError = () => {
    setLoading(false);
    setError(true);
  };

  const handleLoad = () => {
    setLoading(false);
  };

  if (error) {
    return (
      <div className="inline-flex items-center gap-2 p-3 bg-paper-100 rounded-lg border border-paper-200">
        <ImageOff className="w-5 h-5 text-paper-400" />
        <span className="text-sm text-paper-500">图片无法加载</span>
      </div>
    );
  }

  return (
    <div className="relative inline-block max-w-full">
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center bg-paper-100 rounded-lg min-w-[100px] min-h-[100px]">
          <div className="w-6 h-6 border-2 border-paper-300 border-t-accent-amber rounded-full animate-spin" />
        </div>
      )}
      <img
        src={image.publicUrl}
        alt={alt || image.fileName || '图片'}
        className={`max-w-full h-auto rounded-lg cursor-pointer transition-opacity duration-200 ${
          loading ? 'opacity-0' : 'opacity-100'
        } hover:opacity-90`}
        style={{ maxHeight: '300px' }}
        onLoad={handleLoad}
        onError={handleError}
        onClick={onClick}
        loading="lazy"
      />
      {!loading && !error && onClick && (
        <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <div className="p-1.5 bg-paper-100/80 rounded-lg backdrop-blur-sm">
            <ZoomIn className="w-4 h-4 text-paper-600" />
          </div>
        </div>
      )}
    </div>
  );
}

interface ChatMessageImageGridProps {
  images: ImageAttachment[];
  onImageClick?: (image: ImageAttachment) => void;
}

export function ChatMessageImageGrid({ images, onImageClick }: ChatMessageImageGridProps) {
  if (!images || images.length === 0) {
    return null;
  }

  if (images.length === 1) {
    return (
      <div className="mt-2 group">
        <ChatMessageImage
          image={images[0]}
          onClick={() => onImageClick?.(images[0])}
        />
      </div>
    );
  }

  const gridClass = images.length === 2
    ? 'grid grid-cols-2 gap-2'
    : images.length <= 4
      ? 'grid grid-cols-2 gap-2'
      : 'grid grid-cols-3 gap-2';

  return (
    <div className={`mt-2 ${gridClass}`}>
      {images.map((image, index) => (
        <div key={image.id || index} className="group relative">
          <ChatMessageImage
            image={image}
            onClick={() => onImageClick?.(image)}
          />
        </div>
      ))}
    </div>
  );
}
