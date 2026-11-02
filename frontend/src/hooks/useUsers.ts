import { useEffect, useState } from 'react';
import { usersApi } from '../api/users';
import type { UserSummary } from '../types';

export function useUsers(ids: string[]) {
  const key = [...new Set(ids.filter(Boolean))].sort().join(',');
  const [users, setUsers] = useState<Map<string, UserSummary>>(new Map());

  useEffect(() => {
    if (!key) return;
    let active = true;
    usersApi.resolve(key.split(',')).then((resolved) => {
      if (active) setUsers(resolved);
    });
    return () => {
      active = false;
    };
  }, [key]);

  return users;
}

export const displayName = (user: UserSummary | undefined, fallback = 'Member') =>
  user ? `${user.firstName} ${user.lastName}`.trim() || user.username : fallback;
