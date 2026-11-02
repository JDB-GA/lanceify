import type { Page, Project, ProjectRequest } from '../types';
import { query, request } from './client';

type RawProject = Partial<Project> & { budget?: number; budgetMax?: number; skills?: string[] };

const normalize = (raw: RawProject): Project => ({
  id: raw.id ?? '',
  clientId: raw.clientId ?? '',
  title: raw.title ?? 'Untitled project',
  description: raw.description ?? '',
  budgetMin: raw.budgetMin ?? null,
  budgetMax: raw.budgetMax ?? raw.budget ?? null,
  skillIds: raw.skillIds ?? raw.skills ?? [],
  deadline: raw.deadline ?? null,
  status: raw.status ?? 'OPEN',
  createdAt: raw.createdAt ?? null,
});

const normalizeList = (data: RawProject[] | Page<RawProject> | null): Project[] => {
  if (!data) return [];
  const items = Array.isArray(data) ? data : data.content ?? [];
  return items.map(normalize);
};

export interface ProjectFilters {
  search?: string;
  skillId?: string;
}

export const projectsApi = {
  browse: async (filters: ProjectFilters, signal?: AbortSignal) =>
    normalizeList(await request<RawProject[] | Page<RawProject>>(`/api/projects${query({ ...filters, status: 'OPEN' })}`, { signal })),
  mine: async (signal?: AbortSignal) =>
    normalizeList(await request<RawProject[] | Page<RawProject>>('/api/projects/mine', { signal })),
  get: async (id: string, signal?: AbortSignal) => normalize(await request<RawProject>(`/api/projects/${id}`, { signal })),
  create: async (body: ProjectRequest) => normalize(await request<RawProject>('/api/projects', { method: 'POST', body })),
};
