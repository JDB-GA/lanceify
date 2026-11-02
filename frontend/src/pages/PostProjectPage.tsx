import { useState, type ChangeEvent, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { projectsApi } from '../api/projects';
import ErrorMessage from '../components/ErrorMessage';
import Icon from '../components/Icon';
import SkillPicker from '../components/SkillPicker';
import { useToast } from '../context/ToastContext';
import { budget } from '../format';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useSkills } from '../hooks/useSkills';

const DESCRIPTION_MAX = 5000;

export default function PostProjectPage() {
  const navigate = useNavigate();
  const { notify } = useToast();
  const catalog = useSkills();
  const { busy, error, run } = useAsyncAction();
  const [form, setForm] = useState({ title: '', description: '', budgetMin: '', budgetMax: '', deadline: '', skillIds: [] as string[] });

  const update = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm({ ...form, [event.target.name]: event.target.value });

  const min = form.budgetMin === '' ? null : Number(form.budgetMin);
  const max = form.budgetMax === '' ? null : Number(form.budgetMax);
  const budgetInvalid = min != null && max != null && min > max;
  const today = new Date().toISOString().slice(0, 10);

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (busy) return;
    run(async () => {
      const created = await projectsApi.create({
        title: form.title.trim(),
        description: form.description.trim(),
        budgetMin: min,
        budgetMax: max,
        skillIds: form.skillIds,
        deadline: form.deadline || null,
      });
      notify('Project posted. Freelancers can now send proposals.');
      navigate(created.id ? `/projects/${created.id}` : '/client');
    });
  };

  return (
    <div className="detail-layout">
      <form className="panel detail-main" onSubmit={submit}>
        <span className="kicker">New project</span>
        <h1>Tell us what you <em>need</em></h1>
        <p className="muted">Clear projects get better proposals. Be specific about the result you want.</p>
        <ErrorMessage error={error} />
        <fieldset className="plain" disabled={busy}>
          <h2 className="section-label"><span className="step-dot">1</span>The basics</h2>
          <label>Project title
            <input name="title" required minLength={5} maxLength={120} placeholder="e.g. Build a landing page for my bakery" value={form.title} onChange={update} />
          </label>
          <label>Description
            <textarea name="description" rows={7} required minLength={30} maxLength={DESCRIPTION_MAX} placeholder="What should be delivered? Any examples, constraints or tools?" value={form.description} onChange={update} />
            <span className="field-meta"><span>At least 30 characters</span><span>{form.description.length}/{DESCRIPTION_MAX}</span></span>
          </label>
          <h2 className="section-label"><span className="step-dot">2</span>Budget & timing</h2>
          <div className="row three">
            <label>Min budget
              <span className="input-group"><span className="prefix">$</span><input name="budgetMin" type="number" min="1" step="1" value={form.budgetMin} onChange={update} /></span>
            </label>
            <label>Max budget
              <span className="input-group"><span className="prefix">$</span><input name="budgetMax" type="number" min="1" step="1" required value={form.budgetMax} onChange={update} /></span>
            </label>
            <label>Deadline
              <input name="deadline" type="date" min={today} value={form.deadline} onChange={update} />
            </label>
          </div>
          {budgetInvalid && <p className="field-error">Minimum budget can’t be higher than maximum.</p>}
          <h2 className="section-label"><span className="step-dot">3</span>Skills</h2>
          <SkillPicker
            skills={catalog.skills}
            loading={catalog.loading}
            unavailable={catalog.error !== null}
            selected={form.skillIds}
            disabled={busy}
            onChange={(skillIds) => setForm({ ...form, skillIds })}
          />
        </fieldset>
        <div className="actions end">
          <button type="button" className="button secondary" onClick={() => navigate('/client')}>Cancel</button>
          <button className="button" disabled={busy || budgetInvalid || form.skillIds.length === 0}>
            {busy ? 'Posting…' : 'Post project'}
            {busy ? <span className="spinner" aria-hidden="true" /> : <Icon name="arrow" size={16} />}
          </button>
        </div>
      </form>
      <aside className="detail-side">
        <div className="panel preview-card">
          <span className="kicker">Live preview</span>
          <span className="badge open">Open</span>
          <h3>{form.title || 'Your project title'}</h3>
          <p className="muted clamp">{form.description || 'Your description will appear here.'}</p>
          <div className="tag-list">
            {form.skillIds.slice(0, 5).map((id) => <span key={id} className="tag">{catalog.names.get(id)}</span>)}
          </div>
          <div className="row-between"><small className="muted">Budget</small><b>{budget(min, max)}</b></div>
        </div>
        <div className="panel tips">
          <h2>Tips for great proposals</h2>
          <ul>
            <li><Icon name="check" size={16} />Describe the finished result, not just the task</li>
            <li><Icon name="check" size={16} />Share a realistic budget range</li>
            <li><Icon name="check" size={16} />Pick 2–5 skills that really matter</li>
          </ul>
        </div>
      </aside>
    </div>
  );
}
