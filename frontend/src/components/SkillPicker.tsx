import { useState } from 'react';
import type { Skill } from '../types';

const MAX_SKILLS = 15;

interface Props {
  skills: Skill[];
  loading: boolean;
  unavailable: boolean;
  selected: string[];
  disabled: boolean;
  onChange: (selected: string[]) => void;
}

export default function SkillPicker({ skills, loading, unavailable, selected, disabled, onChange }: Props) {
  const [query, setQuery] = useState('');
  const toggle = (id: string) =>
    onChange(selected.includes(id) ? selected.filter((item) => item !== id) : [...selected, id]);

  const visible = skills.filter((skill) => skill.name.toLowerCase().includes(query.trim().toLowerCase()));

  return (
    <fieldset className="skill-picker">
      <legend>Skills <span>{selected.length}/{MAX_SKILLS}</span></legend>
      {loading && <p className="muted">Loading skills…</p>}
      {unavailable && <p className="alert error">Skills could not be loaded. Check that the catalog service is running.</p>}
      {!loading && !unavailable && skills.length === 0 && <p className="muted">No skills are available yet. An admin needs to add them.</p>}
      {skills.length > 8 && (
        <input
          type="search"
          className="skill-search"
          placeholder="Search skills…"
          aria-label="Search skills"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      )}
      <div className="skill-options">
        {visible.map((skill) => {
          const checked = selected.includes(skill.id);
          return (
            <label key={skill.id} className={checked ? 'skill selected' : 'skill'}>
              <input
                type="checkbox"
                checked={checked}
                disabled={disabled || (!checked && selected.length >= MAX_SKILLS)}
                onChange={() => toggle(skill.id)}
              />
              {skill.name}
            </label>
          );
        })}
        {query && visible.length === 0 && <span className="muted">No skills match “{query}”.</span>}
      </div>
      <small className="muted">Choose 1 to {MAX_SKILLS} skills.</small>
    </fieldset>
  );
}
