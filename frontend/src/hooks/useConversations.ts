import { useCallback, useState } from 'react';
import { ApiError } from '../api/client';
import { chatApi } from '../api/chat';
import type { Conversation } from '../types';
import { usePolling } from './usePolling';

const REFRESH_MS = 5000;

export function useConversations() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  const refresh = useCallback(() => {
    chatApi.list()
      .then((items) => {
        setConversations(items);
        setError(null);
      })
      .catch((err: ApiError) => setError(err))
      .finally(() => setLoading(false));
  }, []);

  usePolling(refresh, REFRESH_MS);

  return { conversations, loading, error, refresh };
}
