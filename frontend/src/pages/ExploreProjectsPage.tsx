import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { projectsApi } from '../api/projects';
import { proposalsApi } from '../api/proposals';
import EmptyState from '../components/EmptyState';
import Icon from '../components/Icon';
import ProjectCard from '../components/ProjectCard';
import { useLoad } from '../hooks/useLoad';
import { useSkills } from '../hooks/useSkills';

export default function ExploreProjectsPage() {
  const [params, setParams] = useSearchParams();
  const search = params.get('q') ?? '';
  const skillId = params.get('skill') ?? '';
  const [draft, setDraft] = useState(search);
  const { skills, names } = useSkills();

  useEffect(() => setDraft(search), [search]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      if (draft.trim() === search) return;
      const next = new URLSearchParams(params);
      if (draft.trim()) next.set('q', draft.trim());
      else next.delete('q');
      setParams(next, { replace: true });
    }, 350);
    return () => window.clearTimeout(timer);
  }, [draft]);

  const projects = useLoad((signal) => projectsApi.browse({ search, skillId }, signal), [search, skillId]);
  const mine = useLoad((signal) => proposalsApi.mine(signal), []);
  const applied = useMemo(() => new Set((mine.data ?? []).map((item) => item.projectId)), [mine.data]);

  const chooseSkill = (id: string) => {
    const next = new URLSearchParams(params);
    if (id && id !== skillId) next.set('skill', id);
    else next.delete('skill');
    setParams(next);
  };

  const list = projects.data ?? [];

  return (
    <section className="stack-lg">
      <div className="explore-head">
        <div>
          <span className="kicker">Find work</span>
          <h1>Open <em>projects</em></h1>
          <p className="muted">Fresh work from clients, matched to the skills you choose.</p>
        </div>
        <label className="search-field">
          <Icon name="search" />
          <input type="search" placeholder="Search by title or keyword" value={draft} onChange={(event) => setDraft(event.target.value)} aria-label="Search projects" />
        </label>
      </div>

      {skills.length > 0 && (
        <div className="chip-row" role="group" aria-label="Filter by skill">
          <button className={skillId ? 'chip' : 'chip active'} onClick={() => chooseSkill('')}>All</button>
          {skills.slice(0, 18).map((skill) => (
            <button key={skill.id} className={skill.id === skillId ? 'chip active' : 'chip'} aria-pressed={skill.id === skillId} onClick={() => chooseSkill(skill.id)}>
              {skill.name}
            </button>
          ))}
        </div>
      )}

      {projects.loading && !projects.data && (
        <div className="card-grid">{[0, 1, 2, 3, 4, 5].map((key) => <div key={key} className="skeleton project-sk" />)}</div>
      )}

      {projects.error && (
        projects.error.missing ? (
          <EmptyState title="Projects are on their way" icon="briefcase">
            The project service is not available yet. Once clients can post projects, they will appear here automatically.
          </EmptyState>
        ) : (
          <div className="alert error">{projects.error.message}</div>
        )
      )}

      {projects.data && list.length === 0 && (
        <EmptyState title="No projects match" icon="search">
          Try a different keyword or clear the skill filter.
        </EmptyState>
      )}

      <div className="card-grid">
        {list.map((project) => (
          <ProjectCard key={project.id} project={project} skillNames={names} applied={applied.has(project.id)} />
        ))}
      </div>
    </section>
  );
}
