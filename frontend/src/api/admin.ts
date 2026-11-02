import type { ApprovalStatus, FreelancerProfile, Page } from '../types';
import { request } from './client';

const BASE = '/api/admin/freelancer-applications';

export const adminApi = {
  list: (status: ApprovalStatus, page: number, size: number) =>
    request<Page<FreelancerProfile>>(`${BASE}?status=${status}&page=${page}&size=${size}`),
  approve: (id: string) => request<FreelancerProfile>(`${BASE}/${id}/approve`, { method: 'PUT' }),
  reject: (id: string, reason: string) =>
    request<FreelancerProfile>(`${BASE}/${id}/reject`, { method: 'PUT', body: { reason } }),
};
