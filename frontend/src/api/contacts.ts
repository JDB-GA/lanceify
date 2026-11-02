import type { Contact, Role } from '../types';
import { request } from './client';

export const contactsApi = {
  listFor: (role: Role) =>
    request<Contact[]>(role === 'CLIENT' ? '/api/users/contacts/freelancers' : '/api/users/contacts/clients'),
};
