import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useLocation } from 'react-router-dom';
import AuthShell from '../components/AuthShell';
import ErrorMessage from '../components/ErrorMessage';
import Icon from '../components/Icon';
import PasswordInput from '../components/PasswordInput';
import { useAuth } from '../context/AuthContext';
import { useAsyncAction } from '../hooks/useAsyncAction';

export default function LoginPage() {
  const { login } = useAuth();
  const message = (useLocation().state as { message?: string } | null)?.message;
  const { busy, error, run } = useAsyncAction();
  const [form, setForm] = useState({ email: '', password: '' });

  const update = (event: ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [event.target.name]: event.target.value });

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (busy) return;
    run(() => login(form));
  };

  return (
    <AuthShell>
      <form className="auth-card" onSubmit={submit}>
        <h1>Welcome back</h1>
        <p className="subtitle">Sign in to pick up where you left off.</p>
        {message && !error && <div className="alert success" role="status">{message}</div>}
        <ErrorMessage error={error} />
        <label>Email
          <input name="email" type="email" autoComplete="email" placeholder="you@example.com" required autoFocus value={form.email} onChange={update} />
        </label>
        <label>Password
          <PasswordInput name="password" autoComplete="current-password" placeholder="••••••••" required value={form.password} onChange={update} />
        </label>
        <button className="button block" disabled={busy}>
          {busy ? 'Signing in…' : 'Sign in'}
          {busy ? <span className="spinner" aria-hidden="true" /> : <Icon name="arrow" size={16} />}
        </button>
        <p className="muted center">New to Lanceify? <Link to="/register">Create an account</Link></p>
      </form>
    </AuthShell>
  );
}
