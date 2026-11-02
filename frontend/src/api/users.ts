import type { UserSummary } from '../types';
import { query, request } from './client';

const cache = new Map<string, UserSummary>();
const pending = new Map<string, Promise<void>>();

async function fetchMissing(ids: string[]): Promise<void> {
  const missing = ids.filter((id) => !cache.has(id) && !pending.has(id));
  if (missing.length > 0) {
    const job = request<UserSummary[]>(`/api/users${query({ ids: missing.join(',') })}`)
      .then((users) => users.forEach((user) => cache.set(user.id, user)))
      .catch(() => undefined)
      .finally(() => missing.forEach((id) => pending.delete(id)));
    missing.forEach((id) => pending.set(id, job));
  }
  await Promise.all(ids.map((id) => pending.get(id)).filter(Boolean));
}

export const usersApi = {
  cached: (id: string) => cache.get(id),
  resolve: async (ids: string[]) => {
    const unique = [...new Set(ids.filter(Boolean))];
    await fetchMissing(unique);
    return new Map(unique.flatMap((id) => (cache.has(id) ? [[id, cache.get(id) as UserSummary]] : [])));
  },
};
