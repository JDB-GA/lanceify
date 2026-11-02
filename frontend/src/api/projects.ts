import type { Project, ProjectRequest } from '../types';
import { query, request } from './client';

interface RawProject {
  id: string;
  clientId: string;
  assignedFreelancerId?: string | null;
  title: string;
  description?: string | null;
  budget?: number | null;
  deadline?: string | null;
  categoryId?: string | null;
  requiredSkillIds?: string[] | null;
  status?: string | null;
  createdAt?: string | null;
}

const normalize = (raw: RawProject): Project => ({
  id: raw.id,
  clientId: raw.clientId,
  assignedFreelancerId: raw.assignedFreelancerId ?? null,
  title: raw.title,
  description: raw.description ?? '',
  budget: raw.budget ?? null,
  deadline: raw.deadline ?? null,
  categoryId: raw.categoryId ?? null,
  skillIds: raw.requiredSkillIds ?? [],
  status: raw.status ?? 'OPEN',
  createdAt: raw.createdAt ?? null,
});

export interface ProjectFilters {
  search?: string;
  skillId?: string;
  categoryId?: string;
}

const matches = (project: Project, search: string) => {
  const term = search.trim().toLowerCase();
  return !term || project.title.toLowerCase().includes(term) || project.description.toLowerCase().includes(term);
};

export const projectsApi = {
  browse: async ({ search = '', skillId, categoryId }: ProjectFilters, signal?: AbortSignal) => {
    const list = await request<RawProject[]>(`/api/projects${query({ skillIds: skillId, categoryId })}`, { signal });
    return list.map(normalize).filter((project) => matches(project, search));
  },
  mine: async (signal?: AbortSignal) => (await request<RawProject[]>('/api/projects/my', { signal })).map(normalize),
  get: async (id: string, signal?: AbortSignal) => normalize(await request<RawProject>(`/api/projects/${id}`, { signal })),
  create: async (body: ProjectRequest) => normalize(await request<RawProject>('/api/projects', { method: 'POST', body })),
};
