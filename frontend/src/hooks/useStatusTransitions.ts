import { useState, useEffect, useCallback } from 'react';
import { apiClient } from '@/lib/apiClient';
import { STATUS_TRANSITIONS } from '@/utils/constants';

export type TransitionRules = Record<string, string[]>;

export function useStatusTransitions() {
  const [rules, setRules] = useState<TransitionRules>(STATUS_TRANSITIONS);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const fetchRules = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await apiClient.get<TransitionRules>('/status/transitions');
      if (response.data) {
        setRules(response.data);
      }
    } catch (err) {
      console.warn('Failed to fetch status transitions from API, using local rules:', err);
      setRules(STATUS_TRANSITIONS);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchRules();
  }, [fetchRules]);

  return {
    rules,
    isLoading,
    error,
    refetch: fetchRules,
  };
}

export function canTransition(
  rules: TransitionRules | undefined,
  fromStatus: string,
  toStatus: string
): boolean {
  if (!rules) return true;
  const allowedTargets = rules[fromStatus] || [];
  return allowedTargets.includes(toStatus);
}
