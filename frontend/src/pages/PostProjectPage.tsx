import { useState, type ChangeEvent, type FormEvent } from 'react';
import { catalogApi } from '../api/catalog';
import { useNavigate } from 'react-router-dom';
import { projectsApi } from '../api/projects';
import ErrorMessage from '../components/ErrorMessage';
import Icon from '../components/Icon';
import SkillPicker from '../components/SkillPicker';
import { useToast } from '../context/ToastContext';
import { budget } from '../format';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useLoad } from '../hooks/useLoad';
import { useSkills } from '../hooks/useSkills';

const DESCRIPTION_MAX = 5000;

export default function PostProjectPage() {
  const navigate = useNavigate();
  const { notify } = useToast();
  const catalog = useSkills();
  const { busy, error, run } = useAsyncAction();
  const categories = useLoad((signal) => catalogApi.listCategories(signal), []);
  const [form, setForm] = useState({ title: '', description: '', budget: '', deadline: '', categoryId: '', skillIds: [] as string[] });

  const update = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm({ ...form, [event.target.name]: event.target.value });

  const amount = form.budget === '' ? null : Number(form.budget);
  const tomorrow = new Date(Date.now() + 86_400_000).toISOString().slice(0, 10);
  const category = (categories.data ?? []).find((item) => item.id === form.categoryId);
  const skillOptions = category && category.skillIds.length > 0
    ? catalog.skills.filter((skill) => category.skillIds.includes(skill.id))
    : catalog.skills;

  const chooseCategory = (event: ChangeEvent<HTMLSelectElement>) => {
    const next = (categories.data ?? []).find((item) => item.id === event.target.value);
    setForm({
      ...form,
      categoryId: event.target.value,
      skillIds: next ? form.skillIds.filter((id) => next.skillIds.length === 0 || next.skillIds.includes(id)) : form.skillIds,
    });
  };

  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (busy) return;
    run(async () => {
      const created = await projectsApi.create({
        title: form.title.trim(),
        description: form.description.trim(),
        budget: Number(form.budget),
        deadline: form.deadline,
        categoryId: form.categoryId,
        requiredSkillIds: form.skillIds,
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
            <input name="title" required minLength={5} maxLength={100} placeholder="e.g. Build a landing page for my bakery" value={form.title} onChange={update} />
          </label>
          <label>Description
            <textarea name="description" rows={7} required minLength={30} maxLength={DESCRIPTION_MAX} placeholder="What should be delivered? Any examples, constraints or tools?" value={form.description} onChange={update} />
            <span className="field-meta"><span>At least 30 characters</span><span>{form.description.length}/{DESCRIPTION_MAX}</span></span>
          </label>
          <h2 className="section-label"><span className="step-dot">2</span>Budget & timing</h2>
          <div className="row">
            <label>Budget
              <span className="input-group"><span className="prefix">$</span><input name="budget" type="number" min="1" step="1" required value={form.budget} onChange={update} /></span>
            </label>
            <label>Deadline
              <input name="deadline" type="date" min={tomorrow} required value={form.deadline} onChange={update} />
            </label>
          </div>
          <h2 className="section-label"><span className="step-dot">3</span>Category & skills</h2>
          <label>Category
            <select name="categoryId" required value={form.categoryId} onChange={chooseCategory}>
              <option value="" disabled>{categories.loading ? 'Loading categories…' : 'Choose a category'}</option>
              {(categories.data ?? []).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
            </select>
          </label>
          <SkillPicker
            skills={skillOptions}
            loading={catalog.loading}
            unavailable={catalog.error !== null}
            selected={form.skillIds}
            disabled={busy}
            onChange={(skillIds) => setForm({ ...form, skillIds })}
          />
        </fieldset>
        <div className="actions end">
          <button type="button" className="button secondary" onClick={() => navigate('/client')}>Cancel</button>
          <button className="button" disabled={busy || !form.categoryId || form.skillIds.length === 0}>
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
          <div className="row-between"><small className="muted">{category?.name ?? 'Budget'}</small><b>{budget(amount)}</b></div>
        </div>
        <div className="panel tips">
          <h2>Tips for great proposals</h2>
          <ul>
            <li><Icon name="check" size={16} />Describe the finished result, not just the task</li>
            <li><Icon name="check" size={16} />Share a realistic budget</li>
            <li><Icon name="check" size={16} />Pick 2–5 skills that really matter</li>
          </ul>
        </div>
      </aside>
    </div>
  );
}
