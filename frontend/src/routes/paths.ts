import type { Role } from '../types';

const HOME_BY_ROLE: Record<Role, string> = {
  ADMIN: '/admin/applications',
  FREELANCER: '/freelancer',
  CLIENT: '/client',
};

export const homeFor = (role?: Role): string => (role ? HOME_BY_ROLE[role] : '/login');
