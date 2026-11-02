import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '../api/client';
import { catalogApi } from '../api/catalog';
import type { Skill } from '../types';

export function useSkills() {
  const [skills, setSkills] = useState<Skill[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  const reload = useCallback(() => {
    catalogApi.listSkills()
      .then(setSkills)
      .catch((err: ApiError) => setError(err))
      .finally(() => setLoading(false));
  }, []);

  useEffect(reload, [reload]);

  return { skills, loading, error, reload };
}
