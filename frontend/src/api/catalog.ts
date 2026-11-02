import type { Skill } from '../types';
import { request } from './client';

export const catalogApi = {
  listSkills: () => request<Skill[]>('/api/skills'),
};
