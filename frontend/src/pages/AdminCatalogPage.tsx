import { useState, type FormEvent } from 'react';
import { ApiError } from '../api/client';
import { catalogApi } from '../api/catalog';
import EmptyState from '../components/EmptyState';
import Icon from '../components/Icon';
import { useToast } from '../context/ToastContext';
import { useLoad } from '../hooks/useLoad';
import type { Category, Skill } from '../types';

export default function AdminCatalogPage() {
  const { notify } = useToast();
  const skills = useLoad((signal) => catalogApi.listSkills(signal), []);
  const categories = useLoad((signal) => catalogApi.listCategories(signal), []);
  const [busy, setBusy] = useState(false);

  const act = async (action: () => Promise<unknown>, success: string) => {
    if (busy) return false;
    setBusy(true);
    try {
      await action();
      notify(success);
      return true;
    } catch (err) {
      const apiError = err instanceof ApiError ? err : null;
      notify(apiError?.message ?? 'Something went wrong', 'error');
      return false;
    } finally {
      setBusy(false);
      await Promise.all([skills.reload(), categories.reload()]);
    }
  };

  const skillList = skills.data ?? [];
  const skillNames = new Map(skillList.map((skill) => [skill.id, skill.name]));

  return (
    <section className="stack-lg">
      <div className="welcome">
        <div>
          <span className="kicker">Admin · Catalog</span>
          <h1>Skills & <em>categories</em></h1>
          <p className="muted">Edits are version-checked: if another admin changes an item first, you will be asked to retry instead of overwriting their work.</p>
        </div>
      </div>
      <div className="catalog-grid">
        <SkillsPanel skills={skillList} loading={skills.loading && !skills.data} busy={busy} act={act} />
        <CategoriesPanel
          categories={categories.data ?? []}
          skills={skillList}
          skillNames={skillNames}
          loading={categories.loading && !categories.data}
          busy={busy}
          act={act}
        />
      </div>
    </section>
  );
}

type Act = (action: () => Promise<unknown>, success: string) => Promise<boolean>;

function SkillsPanel({ skills, loading, busy, act }: { skills: Skill[]; loading: boolean; busy: boolean; act: Act }) {
  const [name, setName] = useState('');
  const [editing, setEditing] = useState<string | null>(null);
  const [editName, setEditName] = useState('');

  const create = async (event: FormEvent) => {
    event.preventDefault();
    if (await act(() => catalogApi.createSkill(name.trim()), `Skill “${name.trim()}” added`)) setName('');
  };

  const rename = async (skill: Skill) => {
    if (await act(() => catalogApi.renameSkill(skill, editName.trim()), 'Skill renamed')) setEditing(null);
  };

  return (
    <div className="panel">
      <div className="panel-head"><h2>Skills <span className="pill-count">{skills.length}</span></h2></div>
      <form className="inline-form" onSubmit={create}>
        <input placeholder="New skill, e.g. Kotlin" required maxLength={60} value={name} onChange={(event) => setName(event.target.value)} aria-label="New skill name" />
        <button className="button sm" disabled={busy || !name.trim()}><Icon name="plus" size={16} />Add</button>
      </form>
      {loading && <div className="skeleton row-sk" />}
      {!loading && skills.length === 0 && <EmptyState title="No skills yet" icon="layers">Add the first skill above.</EmptyState>}
      <ul className="list">
        {skills.map((skill) => (
          <li key={skill.id} className="list-row">
            {editing === skill.id ? (
              <form className="inline-form grow" onSubmit={(event) => { event.preventDefault(); rename(skill); }}>
                <input autoFocus required maxLength={60} value={editName} onChange={(event) => setEditName(event.target.value)} aria-label="Skill name" />
                <button className="button sm" disabled={busy}>Save</button>
                <button type="button" className="button ghost sm" onClick={() => setEditing(null)}>Cancel</button>
              </form>
            ) : (
              <>
                <span className="tag">{skill.name}</span>
                <span className="row-actions">
                  <button className="icon-button" aria-label={`Rename ${skill.name}`} onClick={() => { setEditing(skill.id); setEditName(skill.name); }}><Icon name="edit" size={16} /></button>
                  <button
                    className="icon-button danger"
                    aria-label={`Remove ${skill.name}`}
                    disabled={busy}
                    onClick={() => window.confirm(`Remove “${skill.name}”?`) && act(() => catalogApi.deactivateSkill(skill.id), 'Skill removed')}
                  >
                    <Icon name="trash" size={16} />
                  </button>
                </span>
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

interface CategoriesProps {
  categories: Category[];
  skills: Skill[];
  skillNames: Map<string, string>;
  loading: boolean;
  busy: boolean;
  act: Act;
}

function CategoriesPanel({ categories, skills, skillNames, loading, busy, act }: CategoriesProps) {
  const [draft, setDraft] = useState({ name: '', description: '', skillIds: [] as string[] });
  const [editing, setEditing] = useState<Category | null>(null);
  const [edit, setEdit] = useState({ name: '', description: '', skillIds: [] as string[] });

  const toggle = (list: string[], id: string) => (list.includes(id) ? list.filter((item) => item !== id) : [...list, id]);

  const create = async (event: FormEvent) => {
    event.preventDefault();
    const ok = await act(
      () => catalogApi.createCategory(draft.name.trim(), draft.description.trim(), draft.skillIds),
      `Category “${draft.name.trim()}” created`,
    );
    if (ok) setDraft({ name: '', description: '', skillIds: [] });
  };

  const startEdit = (category: Category) => {
    setEditing(category);
    setEdit({ name: category.name, description: category.description ?? '', skillIds: category.skillIds });
  };

  const save = async (event: FormEvent) => {
    event.preventDefault();
    if (!editing) return;
    const ok = await act(async () => {
      const updated = await catalogApi.updateCategory(editing, edit.name.trim(), edit.description.trim());
      const changed = updated.skillIds.length !== edit.skillIds.length || updated.skillIds.some((id) => !edit.skillIds.includes(id));
      if (changed && edit.skillIds.length > 0) await catalogApi.replaceCategorySkills(updated, edit.skillIds);
    }, 'Category saved');
    if (ok) setEditing(null);
  };

  return (
    <div className="panel">
      <div className="panel-head"><h2>Categories <span className="pill-count">{categories.length}</span></h2></div>
      <form className="stack" onSubmit={create}>
        <div className="row">
          <input placeholder="Category name" required maxLength={60} value={draft.name} onChange={(event) => setDraft({ ...draft, name: event.target.value })} aria-label="Category name" />
          <input placeholder="Short description" maxLength={200} value={draft.description} onChange={(event) => setDraft({ ...draft, description: event.target.value })} aria-label="Category description" />
        </div>
        <div className="chip-row">
          {skills.map((skill) => (
            <button type="button" key={skill.id} className={draft.skillIds.includes(skill.id) ? 'chip active' : 'chip'} onClick={() => setDraft({ ...draft, skillIds: toggle(draft.skillIds, skill.id) })}>
              {skill.name}
            </button>
          ))}
        </div>
        <button className="button sm self-start" disabled={busy || !draft.name.trim()}><Icon name="plus" size={16} />Create category</button>
      </form>
      {loading && <div className="skeleton row-sk" />}
      {!loading && categories.length === 0 && <EmptyState title="No categories yet" icon="grid">Group skills into categories to help people browse.</EmptyState>}
      <div className="stack">
        {categories.map((category) =>
          editing?.id === category.id ? (
            <form key={category.id} className="category-card editing" onSubmit={save}>
              <div className="row">
                <input required maxLength={60} value={edit.name} onChange={(event) => setEdit({ ...edit, name: event.target.value })} aria-label="Category name" />
                <input maxLength={200} value={edit.description} onChange={(event) => setEdit({ ...edit, description: event.target.value })} aria-label="Category description" />
              </div>
              <div className="chip-row">
                {skills.map((skill) => (
                  <button type="button" key={skill.id} className={edit.skillIds.includes(skill.id) ? 'chip active' : 'chip'} onClick={() => setEdit({ ...edit, skillIds: toggle(edit.skillIds, skill.id) })}>
                    {skill.name}
                  </button>
                ))}
              </div>
              <div className="actions end">
                <button type="button" className="button ghost sm" onClick={() => setEditing(null)}>Cancel</button>
                <button className="button sm" disabled={busy}>Save changes</button>
              </div>
            </form>
          ) : (
            <article key={category.id} className="category-card">
              <div className="row-between">
                <div>
                  <strong>{category.name}</strong>
                  {category.description && <p className="muted small">{category.description}</p>}
                </div>
                <span className="row-actions">
                  <button className="icon-button" aria-label={`Edit ${category.name}`} onClick={() => startEdit(category)}><Icon name="edit" size={16} /></button>
                  <button
                    className="icon-button danger"
                    aria-label={`Remove ${category.name}`}
                    disabled={busy}
                    onClick={() => window.confirm(`Remove “${category.name}”?`) && act(() => catalogApi.deactivateCategory(category.id), 'Category removed')}
                  >
                    <Icon name="trash" size={16} />
                  </button>
                </span>
              </div>
              <div className="tag-list">
                {category.skillIds.map((id) => <span key={id} className="tag">{skillNames.get(id) ?? 'Inactive skill'}</span>)}
              </div>
            </article>
          ),
        )}
      </div>
    </div>
  );
}
