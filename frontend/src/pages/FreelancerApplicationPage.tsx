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

const BIO_MAX = 2000;

const STEPS: { label: string; reached: (status?: ApprovalStatus) => boolean }[] = [
  { label: 'Draft', reached: () => true },
  { label: 'Submitted', reached: (status) => status === 'PENDING' || status === 'APPROVED' || status === 'REJECTED' },
  { label: 'Approved', reached: (status) => status === 'APPROVED' },
];

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

  if (loading) return <div className="skeleton card-sk narrow" style={{ height: 420 }} aria-label="Loading" />;

  const status = profile?.approvalStatus;
  const editable = !status || status === 'DRAFT' || status === 'REJECTED';
  const help = status && STATUS_HELP[status];
  const priceInvalid = form.minPrice !== '' && form.maxPrice !== '' && Number(form.minPrice) > Number(form.maxPrice);

  return (
    <section className="card narrow">
      <div className="card-heading">
        <div>
          <span className="eyebrow">Freelancer file</span>
          <h1>Your <em>application</em></h1>
          <p className="muted" style={{ margin: 0 }}>
            {profile ? 'Keep your profile up to date.' : 'Tell us about your services. An admin reviews every application before you join the marketplace.'}
          </p>
        </div>
        {status && <StatusBadge status={status} />}
      </div>
      <ol className="steps" aria-label="Application progress">
        {STEPS.map((step, index) => {
          const reached = step.reached(status);
          const failed = status === 'REJECTED' && index === STEPS.length - 1;
          return (
            <li key={step.label} className={failed ? 'failed' : reached ? 'done' : ''}>
              <b>0{index + 1}</b>{failed ? 'Rejected' : step.label}
            </li>
          );
        })}
      </ol>
      {help && <div className={status === 'APPROVED' ? 'alert success' : 'alert info'}>{help}</div>}
      {status === 'REJECTED' && profile?.rejectionReason && (
        <div className="alert warning">
          <div><strong>Rejection reason:</strong> {profile.rejectionReason}<br />Update your application and submit it again.</div>
        </div>
      )}
      <ErrorMessage error={error} />
      {notice && <div className="alert success" role="status">{notice}</div>}
      <form onSubmit={save}>
        <fieldset disabled={!editable || busy} className="plain">
          <h2 className="section-title"><span>01</span> About you</h2>
          <label>Bio
            <textarea
              name="bio"
              rows={5}
              maxLength={BIO_MAX}
              placeholder="Describe your experience, the services you offer and what makes you stand out…"
              value={form.bio}
              onChange={update}
            />
            <span className="field-meta"><span>A clear bio helps your application get approved faster.</span><span>{form.bio.length}/{BIO_MAX}</span></span>
          </label>
          <h2 className="section-title"><span>02</span> Pricing</h2>
          <div className="row">
            <label>Minimum price
              <span className="input-group">
                <span className="prefix">$</span>
                <input name="minPrice" type="number" min="0" step="0.01" placeholder="0.00" value={form.minPrice} onChange={update} />
              </span>
            </label>
            <label>Maximum price
              <span className="input-group">
                <span className="prefix">$</span>
                <input name="maxPrice" type="number" min="0" step="0.01" placeholder="0.00" value={form.maxPrice} onChange={update} />
              </span>
            </label>
          </div>
          {priceInvalid && <p className="field-error" style={{ marginTop: -8 }}>Minimum price can’t be higher than maximum price.</p>}
          <h2 className="section-title"><span>03</span> Skills</h2>
          <SkillPicker
            skills={catalog.skills}
            loading={catalog.loading}
            unavailable={catalog.error !== null}
            selected={form.skillIds}
            disabled={!editable || busy}
            onChange={(skillIds) => setForm({ ...form, skillIds })}
          />
        </fieldset>
        <div className="actions end">
          {profile && status !== 'APPROVED' && (
            <button type="button" className="button danger" disabled={busy} onClick={withdraw}>Withdraw</button>
          )}
          {editable && <button className="button secondary" disabled={busy || priceInvalid}>Save draft</button>}
          {editable && (
            <button type="button" className="button" disabled={busy || priceInvalid || form.skillIds.length === 0} onClick={submit}>
              Submit for review
              {busy ? <span className="spinner" aria-hidden="true" /> : <span className="arrow" aria-hidden="true">→</span>}
            </button>
          )}
        </div>
      </form>
    </section>
  );
}
