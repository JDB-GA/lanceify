import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ErrorMessage from '../components/ErrorMessage';
import { useAuth } from '../context/AuthContext';
import { useAsyncAction } from '../hooks/useAsyncAction';
import type { RegisterRequest } from '../types';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const { busy, error, run } = useAsyncAction();
  const [form, setForm] = useState<RegisterRequest>({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
    role: 'FREELANCER',
  });

  const update = (event: ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [event.target.name]: event.target.value });

  const submit = (event: FormEvent) => {
    event.preventDefault();
    run(async () => {
      const message = await register(form);
      navigate('/login', { state: { message } });
    });
  };

  return (
    <main className="auth-page">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Create your account</h1>
        <ErrorMessage error={error} />
        <fieldset className="role-picker">
          <legend>I want to</legend>
          <label className={form.role === 'FREELANCER' ? 'selected' : ''}>
            <input type="radio" name="role" value="FREELANCER" checked={form.role === 'FREELANCER'} onChange={update} />
            Offer my services
          </label>
          <label className={form.role === 'CLIENT' ? 'selected' : ''}>
            <input type="radio" name="role" value="CLIENT" checked={form.role === 'CLIENT'} onChange={update} />
            Hire freelancers
          </label>
        </fieldset>
        <div className="row">
          <label>First name
            <input name="firstName" autoComplete="given-name" required maxLength={100} value={form.firstName} onChange={update} />
          </label>
          <label>Last name
            <input name="lastName" autoComplete="family-name" required maxLength={100} value={form.lastName} onChange={update} />
          </label>
        </div>
        <label>Username
          <input name="username" autoComplete="username" required minLength={3} maxLength={30} value={form.username} onChange={update} />
        </label>
        <label>Email
          <input name="email" type="email" autoComplete="email" required value={form.email} onChange={update} />
        </label>
        <label>Password
          <input name="password" type="password" autoComplete="new-password" required minLength={8} maxLength={72} value={form.password} onChange={update} />
          <small className="muted">8 to 72 characters</small>
        </label>
        <button className="button" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
        <p className="muted center">Already registered? <Link to="/login">Log in</Link></p>
      </form>
    </main>
  );
}
