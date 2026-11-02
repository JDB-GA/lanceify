import { useEffect, useRef } from 'react';

export function usePolling(action: () => void, intervalMs: number): void {
  const latest = useRef(action);
  latest.current = action;

  useEffect(() => {
    latest.current();
    const timer = window.setInterval(() => {
      if (!document.hidden) latest.current();
    }, intervalMs);
    return () => window.clearInterval(timer);
  }, [intervalMs]);
}
