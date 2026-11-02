import type { ReactNode } from 'react';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../types';

const ROLE_LABELS: Record<Role, string> = { ADMIN: 'Admin', CLIENT: 'Client', FREELANCER: 'Freelancer' };

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  return (
    <>
      <header className="topbar">
        <span className="brand">Lanceify</span>
        {user && (
          <div className="topbar-user">
            <span>{user.firstName} {user.lastName} · {ROLE_LABELS[user.role]}</span>
            <button className="button secondary" onClick={logout}>Log out</button>
          </div>
        )}
      </header>
      <main className="page">{children}</main>
    </>
  );
}
