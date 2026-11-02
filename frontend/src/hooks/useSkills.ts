import { useMemo } from 'react';
import { catalogApi } from '../api/catalog';
import type { Skill } from '../types';
import { useLoad } from './useLoad';

let cached: Skill[] | null = null;

export function useSkills() {
  const { data, loading, error, reload } = useLoad(async (signal) => {
    if (cached) return cached;
    cached = await catalogApi.listSkills(signal);
    return cached;
  }, []);
  const skills = data ?? [];
  const names = useMemo(() => new Map(skills.map((skill) => [skill.id, skill.name])), [skills]);
  const refresh = () => {
    cached = null;
    return reload();
  };
  return { skills, names, loading, error, refresh };
}
