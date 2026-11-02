import { useCallback, useEffect, useRef, useState, type DependencyList } from 'react';
import { ApiError, isAbort } from '../api/client';

export function useLoad<T>(loader: (signal: AbortSignal) => Promise<T>, deps: DependencyList) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(true);
  const controller = useRef<AbortController | null>(null);

  const reload = useCallback(async () => {
    controller.current?.abort();
    const current = new AbortController();
    controller.current = current;
    setLoading(true);
    try {
      const result = await loader(current.signal);
      if (!current.signal.aborted) {
        setData(result);
        setError(null);
      }
    } catch (err) {
      if (isAbort(err) || current.signal.aborted) return;
      setError(err instanceof ApiError ? err : new ApiError(0, 'Something went wrong. Please try again.'));
    } finally {
      if (!current.signal.aborted) setLoading(false);
    }
  }, deps);

  useEffect(() => {
    reload();
    return () => controller.current?.abort();
  }, [reload]);

  return { data, setData, error, loading, reload };
}
