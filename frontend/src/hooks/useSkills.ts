import { useEffect, useState } from 'react';
import { ApiError } from '../api/client';
import { catalogApi } from '../api/catalog';
import type { Skill } from '../types';

export function useSkills() {
  const [skills, setSkills] = useState<Skill[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);

  useEffect(() => {
    catalogApi.listSkills()
      .then(setSkills)
      .catch((err: ApiError) => setError(err))
      .finally(() => setLoading(false));
  }, []);

  return { skills, loading, error };
}
