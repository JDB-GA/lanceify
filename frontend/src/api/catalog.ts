import type { Category, Skill } from '../types';
import { request } from './client';

export const catalogApi = {
  listSkills: (signal?: AbortSignal) => request<Skill[]>('/api/skills', { signal }),
  listCategories: (signal?: AbortSignal) => request<Category[]>('/api/categories', { signal }),
  createSkill: (name: string) => request<Skill>('/api/skills', { method: 'POST', body: { name } }),
  renameSkill: (skill: Skill, name: string) =>
    request<Skill>(`/api/skills/${skill.id}`, { method: 'PUT', body: { name, version: skill.version } }),
  deactivateSkill: (id: string) => request<void>(`/api/skills/${id}`, { method: 'DELETE' }),
  createCategory: (name: string, description: string, skillIds: string[]) =>
    request<Category>('/api/categories', { method: 'POST', body: { name, description, skillIds } }),
  updateCategory: (category: Category, name: string, description: string) =>
    request<Category>(`/api/categories/${category.id}`, {
      method: 'PUT',
      body: { name, description, version: category.version },
    }),
  replaceCategorySkills: (category: Category, skillIds: string[]) =>
    request<Category>(`/api/categories/${category.id}/skills`, {
      method: 'PUT',
      body: { skillIds, version: category.version },
    }),
  deactivateCategory: (id: string) => request<void>(`/api/categories/${id}`, { method: 'DELETE' }),
};
