import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import Layout from '../components/Layout';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../types';
import { homeFor } from './paths';

export function Protected({ role, children }: { role: Role | Role[]; children: ReactNode }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!(Array.isArray(role) ? role : [role]).includes(user.role)) return <Navigate to={homeFor(user.role)} replace />;
  return <Layout>{children}</Layout>;
}

export function PublicOnly({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  return user ? <Navigate to={homeFor(user.role)} replace /> : <>{children}</>;
}
