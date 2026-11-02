import type { FreelancerProfile, FreelancerProfileRequest } from '../types';
import { request } from './client';

const BASE = '/api/freelancers/me';

export const freelancerApi = {
  get: () => request<FreelancerProfile>(BASE),
  create: (body: FreelancerProfileRequest) => request<FreelancerProfile>(BASE, { method: 'POST', body }),
  update: (body: FreelancerProfileRequest) => request<FreelancerProfile>(BASE, { method: 'PUT', body }),
  submit: () => request<FreelancerProfile>(`${BASE}/submit`, { method: 'POST' }),
  withdraw: () => request<void>(BASE, { method: 'DELETE' }),
};
