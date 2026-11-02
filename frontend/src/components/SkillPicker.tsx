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
  const toggle = (id: string) =>
    onChange(selected.includes(id) ? selected.filter((item) => item !== id) : [...selected, id]);

  return (
    <fieldset className="skill-picker">
      <legend>Skills</legend>
      {loading && <p className="muted">Loading skills…</p>}
      {unavailable && <p className="alert error">Skills could not be loaded. Check that the catalog service is running.</p>}
      {!loading && !unavailable && skills.length === 0 && <p className="muted">No skills are available yet. An admin needs to add them.</p>}
      <div className="skill-options">
        {skills.map((skill) => {
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
      </div>
      <small className="muted">Choose 1 to {MAX_SKILLS} skills</small>
    </fieldset>
  );
}
