import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '../api/client';
import { chatApi, streamMessages } from '../api/chat';
import type { ChatMessage } from '../types';
import { usePolling } from './usePolling';

const REFRESH_MS = 5000;

function merge(current: ChatMessage[], incoming: ChatMessage[]): ChatMessage[] {
  const byId = new Map(current.map((message) => [message.id, message]));
  incoming.forEach((message) => byId.set(message.id, message));
  return [...byId.values()].sort((a, b) => a.createdAt.localeCompare(b.createdAt));
}

export function useMessages(conversationId: string) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  const add = useCallback((message: ChatMessage) => {
    setMessages((current) => merge(current, [message]));
  }, []);

  const refresh = useCallback(() => {
    chatApi.messages(conversationId)
      .then((items) => {
        setMessages((current) => merge(current, items));
        setError(null);
      })
      .catch((err: ApiError) => setError(err))
      .finally(() => setLoading(false));
  }, [conversationId]);

  useEffect(() => {
    setMessages([]);
    setLoading(true);
    setError(null);
    const controller = new AbortController();
    streamMessages(conversationId, controller.signal, add).catch(() => undefined);
    return () => controller.abort();
  }, [conversationId, add]);

  usePolling(refresh, REFRESH_MS);

  return { messages, loading, error, add };
}
