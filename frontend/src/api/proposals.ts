import type { ApplyRequest, Proposal } from '../types';
import { query, request } from './client';

export const proposalsApi = {
  apply: (projectId: string, body: ApplyRequest) =>
    request<Proposal>(`/api/projects/${projectId}/proposals`, { method: 'POST', body }),
  mine: (signal?: AbortSignal) => request<Proposal[]>('/api/proposals/me', { signal }),
  withdraw: (id: string) => request<Proposal>(`/api/proposals/${id}/withdraw`, { method: 'PUT' }),
  received: (signal?: AbortSignal) => request<Proposal[]>('/api/proposals/received', { signal }),
  forProject: (projectId: string, signal?: AbortSignal) =>
    request<Proposal[]>(`/api/projects/${projectId}/proposals`, { signal }),
  accept: (proposal: Proposal) =>
    request<Proposal>(`/api/proposals/${proposal.id}/accept${query({ version: proposal.version })}`, { method: 'PUT' }),
  reject: (proposal: Proposal) =>
    request<Proposal>(`/api/proposals/${proposal.id}/reject${query({ version: proposal.version })}`, { method: 'PUT' }),
};
