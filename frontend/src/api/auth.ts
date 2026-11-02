import type { LoginRequest, MessageResponse, RegisterRequest, Session } from '../types';
import { request } from './client';

export const authApi = {
  login: (body: LoginRequest) => request<Session>('/api/auth/login', { method: 'POST', body }),
  register: (body: RegisterRequest) => request<MessageResponse>('/api/auth/register', { method: 'POST', body }),
};
