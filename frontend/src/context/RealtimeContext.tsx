import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { conversationsApi, openInboxStream, type StreamEvent } from '../api/conversations';
import { useAuth } from './AuthContext';

type Listener = (event: StreamEvent) => void;

interface RealtimeValue {
  unread: number;
  live: boolean;
  subscribe: (listener: Listener) => () => void;
  refreshUnread: () => void;
}

const RealtimeContext = createContext<RealtimeValue | null>(null);

const MAX_BACKOFF = 30_000;

export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [unread, setUnread] = useState(0);
  const [live, setLive] = useState(false);
  const listeners = useRef(new Set<Listener>());

  const refreshUnread = useCallback(() => {
    conversationsApi.unread().then((result) => setUnread(result.count)).catch(() => undefined);
  }, []);

  const subscribe = useCallback((listener: Listener) => {
    listeners.current.add(listener);
    return () => {
      listeners.current.delete(listener);
    };
  }, []);

  useEffect(() => {
    if (!user) {
      setUnread(0);
      setLive(false);
      return;
    }
    const controller = new AbortController();
    let attempt = 0;
    let timer: number | undefined;

    const handle = (event: StreamEvent) => {
      if (event.type === 'OPEN') {
        attempt = 0;
        setLive(true);
        refreshUnread();
      } else if (event.type === 'NEW_MESSAGE' && event.data.recipientId === user.userId) {
        setUnread((count) => count + 1);
      } else if (event.type === 'READ' && event.data.readerId === user.userId) {
        refreshUnread();
      }
      listeners.current.forEach((listener) => listener(event));
    };

    const connect = () => {
      openInboxStream(handle, controller.signal)
        .catch(() => undefined)
        .finally(() => {
          if (controller.signal.aborted) return;
          setLive(false);
          const delay = Math.min(MAX_BACKOFF, 1000 * 2 ** attempt) + Math.random() * 500;
          attempt += 1;
          timer = window.setTimeout(connect, delay);
        });
    };

    refreshUnread();
    connect();
    return () => {
      controller.abort();
      window.clearTimeout(timer);
    };
  }, [user, refreshUnread]);

  const value = useMemo(() => ({ unread, live, subscribe, refreshUnread }), [unread, live, subscribe, refreshUnread]);
  return <RealtimeContext.Provider value={value}>{children}</RealtimeContext.Provider>;
}

export function useRealtime(): RealtimeValue {
  const context = useContext(RealtimeContext);
  if (!context) throw new Error('useRealtime must be used within RealtimeProvider');
  return context;
}
