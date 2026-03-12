import { useState } from 'react';
import { Send } from 'lucide-react';

interface ChatInputProps {
  onSend: (content: string) => void;
  disabled?: boolean;
}

export function ChatInput({ onSend, disabled }: ChatInputProps) {
  const [input, setInput] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim() && !disabled) {
      onSend(input.trim());
      setInput('');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="输入消息..."
        disabled={disabled}
        className="flex-1 px-3 md:px-4 py-3 border border-paper-200 rounded-lg bg-paper-100 text-paper-700 placeholder-paper-400 focus:outline-none focus:ring-2 focus:ring-accent-amber disabled:opacity-50 text-base"
      />
      <button
        type="submit"
        disabled={disabled || !input.trim()}
        className="px-3 md:px-4 py-3 bg-accent-amber text-white rounded-lg hover:bg-accent-amber/90 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        aria-label="发送"
      >
        <Send className="w-5 h-5" />
      </button>
    </form>
  );
}
