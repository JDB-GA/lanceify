import type { ApprovalStatus, FreelancerProfile, Page } from '../types';
import { query, request } from './client';

const BASE = '/api/admin/freelancer-applications';

export const adminApi = {
  list: (status: ApprovalStatus, page: number, size: number, signal?: AbortSignal) =>
    request<Page<FreelancerProfile>>(`${BASE}${query({ status, page, size })}`, { signal }),
  approve: (id: string) => request<FreelancerProfile>(`${BASE}/${id}/approve`, { method: 'PUT' }),
  reject: (id: string, reason: string) =>
    request<FreelancerProfile>(`${BASE}/${id}/reject`, { method: 'PUT', body: { reason } }),
};
