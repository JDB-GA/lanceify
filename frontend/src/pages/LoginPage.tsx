import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useLocation } from 'react-router-dom';
import ErrorMessage from '../components/ErrorMessage';
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
    run(() => login(form));
  };

  return (
    <main className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Lanceify</h1>
        <p className="muted">Log in to your account</p>
        {message && !error && <div className="alert success" role="status">{message}</div>}
        <ErrorMessage error={error} />
        <label>Email
          <input name="email" type="email" autoComplete="email" required value={form.email} onChange={update} />
        </label>
        <label>Password
          <input name="password" type="password" autoComplete="current-password" required value={form.password} onChange={update} />
        </label>
        <button className="button" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
        <p className="muted center">New here? <Link to="/register">Create an account</Link></p>
      </form>
    </main>
  );
}
