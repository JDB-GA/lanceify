import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthShell from '../components/AuthShell';
import ErrorMessage from '../components/ErrorMessage';
import Icon, { type IconName } from '../components/Icon';
import PasswordInput from '../components/PasswordInput';
import { useAuth } from '../context/AuthContext';
import { useAsyncAction } from '../hooks/useAsyncAction';
import type { RegisterRequest, RegisterRole } from '../types';

const ROLES: { value: RegisterRole; icon: IconName; title: string; hint: string }[] = [
  { value: 'CLIENT', icon: 'briefcase', title: 'I want to hire', hint: 'Post projects and pick the best proposal' },
  { value: 'FREELANCER', icon: 'spark', title: 'I want to work', hint: 'Offer your skills and send proposals' },
];

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
    role: 'CLIENT',
  });

  const update = (event: ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [event.target.name]: event.target.value });

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (busy) return;
    run(async () => {
      const message = await register(form);
      navigate('/login', { state: { message } });
    });
  };

  const passwordShort = form.password.length > 0 && form.password.length < 8;

  return (
    <AuthShell>
      <form className="auth-card" onSubmit={submit}>
        <h1>Join Lanceify</h1>
        <p className="subtitle">First, how will you use it?</p>
        <ErrorMessage error={error} />
        <fieldset className="role-picker">
          <legend className="sr-only">Account type</legend>
          {ROLES.map((role) => (
            <label key={role.value} className={form.role === role.value ? 'role-option selected' : 'role-option'}>
              <input type="radio" name="role" value={role.value} checked={form.role === role.value} onChange={update} />
              <span className="role-icon"><Icon name={role.icon} size={20} /></span>
              <strong>{role.title}</strong>
              <small>{role.hint}</small>
            </label>
          ))}
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
          <input name="email" type="email" autoComplete="email" placeholder="you@example.com" required value={form.email} onChange={update} />
        </label>
        <label>Password
          <PasswordInput name="password" autoComplete="new-password" required minLength={8} maxLength={72} value={form.password} onChange={update} />
          <small className={passwordShort ? 'field-error' : 'muted'}>
            {passwordShort ? `${8 - form.password.length} more characters needed` : '8 to 72 characters'}
          </small>
        </label>
        <button className="button block" disabled={busy}>
          {busy ? 'Creating account…' : 'Create account'}
          {busy ? <span className="spinner" aria-hidden="true" /> : <Icon name="arrow" size={16} />}
        </button>
        <p className="muted center">Already have an account? <Link to="/login">Sign in</Link></p>
      </form>
    </AuthShell>
  );
}
