import type { ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../types';
import Brand from './Brand';

const ROLE_LABELS: Record<Role, string> = { ADMIN: 'Admin', CLIENT: 'Client', FREELANCER: 'Freelancer' };

export const initials = (first: string, last: string) => `${first.charAt(0)}${last.charAt(0)}`.toUpperCase();

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  return (
    <>
      <header className="topbar">
        <Brand />
        {user && (
          <div className="topbar-user">
            <div className="user-chip">
              <span className="avatar" aria-hidden="true">{initials(user.firstName, user.lastName)}</span>
              <span className="user-meta">
                <strong>{user.firstName} {user.lastName}</strong>
                <small>{ROLE_LABELS[user.role]}</small>
              </span>
            </div>
            <button className="button ghost" onClick={logout}>Log out</button>
          </div>
        )}
      </header>
      <main className="page">{children}</main>
    </>
  );
}
