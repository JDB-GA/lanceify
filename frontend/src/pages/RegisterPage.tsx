import { useState, type ChangeEvent, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthShell from '../components/AuthShell';
import ErrorMessage from '../components/ErrorMessage';
import PasswordInput from '../components/PasswordInput';
import { useAuth } from '../context/AuthContext';
import { useAsyncAction } from '../hooks/useAsyncAction';
import type { RegisterRequest, RegisterRole } from '../types';

const ROLES: { value: RegisterRole; letter: string; title: string; hint: string }[] = [
  { value: 'FREELANCER', letter: 'A', title: 'I make things', hint: 'Offer your services and get hired' },
  { value: 'CLIENT', letter: 'B', title: 'I need things made', hint: 'Post projects and find talent' },
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

  const passwordShort = form.password.length > 0 && form.password.length < 8;

  return (
    <AuthShell>
      <form className="auth-card" onSubmit={submit}>
        <span className="eyebrow">New account</span>
        <h1>Join <em>lanceify.</em></h1>
        <p className="subtitle">Tell us which side of the table you sit on.</p>
        <ErrorMessage error={error} />
        <fieldset className="role-picker">
          <legend>Choose one</legend>
          {ROLES.map((role) => (
            <label key={role.value} className={form.role === role.value ? 'role-option selected' : 'role-option'}>
              <input type="radio" name="role" value={role.value} checked={form.role === role.value} onChange={update} />
              <span className="letter" aria-hidden="true">({role.letter})</span>
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
        <button className="button" disabled={busy}>
          {busy ? 'Creating account…' : 'Create account'}
          {busy ? <span className="spinner" aria-hidden="true" /> : <span className="arrow" aria-hidden="true">→</span>}
        </button>
        <p className="muted center">Already have an account? <Link to="/login">Log in</Link></p>
      </form>
    </AuthShell>
  );
}
