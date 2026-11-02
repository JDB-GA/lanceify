import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { ApiError } from '../api/client';
import { freelancerApi } from '../api/freelancer';
import ErrorMessage from '../components/ErrorMessage';
import SkillPicker from '../components/SkillPicker';
import StatusBadge from '../components/StatusBadge';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useSkills } from '../hooks/useSkills';
import type { ApprovalStatus, FreelancerProfile, FreelancerProfileRequest } from '../types';

interface ProfileForm {
  bio: string;
  minPrice: string;
  maxPrice: string;
  skillIds: string[];
}

const EMPTY_FORM: ProfileForm = { bio: '', minPrice: '', maxPrice: '', skillIds: [] };

const STATUS_HELP: Partial<Record<ApprovalStatus, string>> = {
  PENDING: 'An admin is reviewing your application. You can edit it again only if it is rejected.',
  APPROVED: 'You are approved and can work on the marketplace.',
};

const toForm = (profile: FreelancerProfile): ProfileForm => ({
  bio: profile.bio ?? '',
  minPrice: profile.minPrice?.toString() ?? '',
  maxPrice: profile.maxPrice?.toString() ?? '',
  skillIds: profile.skillIds,
});

const toRequest = (form: ProfileForm): FreelancerProfileRequest => ({
  bio: form.bio.trim() || null,
  minPrice: form.minPrice === '' ? null : Number(form.minPrice),
  maxPrice: form.maxPrice === '' ? null : Number(form.maxPrice),
  skillIds: form.skillIds,
});

export default function FreelancerApplicationPage() {
  const [profile, setProfile] = useState<FreelancerProfile | null>(null);
  const [form, setForm] = useState<ProfileForm>(EMPTY_FORM);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState<string | null>(null);
  const { busy, error, setError, run } = useAsyncAction();
  const catalog = useSkills();

  const show = (next: FreelancerProfile | null) => {
    setProfile(next);
    setForm(next ? toForm(next) : EMPTY_FORM);
  };

  useEffect(() => {
    freelancerApi.get()
      .then(show)
      .catch((err: ApiError) => {
        if (err.status !== 404) setError(err);
      })
      .finally(() => setLoading(false));
  }, [setError]);

  const perform = async (action: () => Promise<void>, message: string) => {
    setNotice(null);
    if (await run(action)) setNotice(message);
  };

  const persist = async () => {
    const body = toRequest(form);
    show(await (profile ? freelancerApi.update(body) : freelancerApi.create(body)));
  };

  const save = (event: FormEvent) => {
    event.preventDefault();
    perform(persist, 'Draft saved.');
  };

  const submit = () =>
    perform(async () => {
      await persist();
      show(await freelancerApi.submit());
    }, 'Application submitted to the admin.');

  const withdraw = () => {
    if (!window.confirm('Withdraw and delete your application?')) return;
    perform(async () => {
      await freelancerApi.withdraw();
      show(null);
    }, 'Application withdrawn.');
  };

  const update = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm({ ...form, [event.target.name]: event.target.value });

  if (loading) return <p className="muted">Loading…</p>;

  const status = profile?.approvalStatus;
  const editable = !status || status === 'DRAFT' || status === 'REJECTED';
  const help = status && STATUS_HELP[status];

  return (
    <section className="card narrow">
      <div className="card-heading">
        <h1>Freelancer application</h1>
        {status && <StatusBadge status={status} />}
      </div>
      {!profile && <p className="muted">Tell us about your services. An admin reviews every application before you can join the marketplace.</p>}
      {help && <p className="muted">{help}</p>}
      {status === 'REJECTED' && profile?.rejectionReason && (
        <div className="alert warning"><strong>Rejection reason:</strong> {profile.rejectionReason}<br />Edit your application and submit it again.</div>
      )}
      <ErrorMessage error={error} />
      {notice && <div className="alert success" role="status">{notice}</div>}
      <form onSubmit={save}>
        <fieldset disabled={!editable || busy} className="plain">
          <label>Bio
            <textarea name="bio" rows={4} maxLength={2000} value={form.bio} onChange={update} />
          </label>
          <div className="row">
            <label>Minimum price
              <input name="minPrice" type="number" min="0" step="0.01" value={form.minPrice} onChange={update} />
            </label>
            <label>Maximum price
              <input name="maxPrice" type="number" min="0" step="0.01" value={form.maxPrice} onChange={update} />
            </label>
          </div>
          <SkillPicker
            skills={catalog.skills}
            loading={catalog.loading}
            unavailable={catalog.error !== null}
            selected={form.skillIds}
            disabled={!editable || busy}
            onChange={(skillIds) => setForm({ ...form, skillIds })}
          />
        </fieldset>
        <div className="actions">
          {editable && <button className="button secondary" disabled={busy}>Save draft</button>}
          {editable && <button type="button" className="button" disabled={busy} onClick={submit}>Submit for review</button>}
          {profile && status !== 'APPROVED' && (
            <button type="button" className="button danger" disabled={busy} onClick={withdraw}>Withdraw</button>
          )}
        </div>
      </form>
    </section>
  );
}
