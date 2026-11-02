import { useEffect, useMemo, useState } from 'react';
import { ApiError } from '../api/client';
import { contactsApi } from '../api/contacts';
import type { Contact, Role } from '../types';

export function useContacts(role?: Role) {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [error, setError] = useState<ApiError | null>(null);

  useEffect(() => {
    if (!role) return;
    contactsApi.listFor(role).then(setContacts).catch((err: ApiError) => setError(err));
  }, [role]);

  const byId = useMemo(() => new Map(contacts.map((contact) => [contact.id, contact])), [contacts]);

  return { contacts, byId, error };
}
