import { useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

export interface UseLeaveConfirmationOptions {
  enabled?: boolean;
  message?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

export function useLeaveConfirmation({
  enabled = true,
  message = '面试进行中，确定要离开吗？',
  onConfirm,
  onCancel,
}: UseLeaveConfirmationOptions = {}) {
  const navigate = useNavigate();
  const isConfirmingRef = useRef(false);

  useEffect(() => {
    if (!enabled) return;

    const handleBeforeUnload = (e: BeforeUnloadEvent) => {
      e.preventDefault();
      e.returnValue = message;
      return message;
    };

    window.addEventListener('beforeunload', handleBeforeUnload);

    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [enabled, message]);

  const confirmLeave = useCallback((callback: () => void) => {
    if (!enabled || isConfirmingRef.current) {
      callback();
      return;
    }

    const confirmed = window.confirm(message);
    
    if (confirmed) {
      isConfirmingRef.current = true;
      onConfirm?.();
      callback();
    } else {
      onCancel?.();
    }
  }, [enabled, message, onConfirm, onCancel]);

  const navigateWithConfirmation = useCallback((to: string) => {
    confirmLeave(() => navigate(to));
  }, [confirmLeave, navigate]);

  return {
    confirmLeave,
    navigateWithConfirmation,
  };
}
