import { Link } from 'react-router-dom';
import { budget, timeAgo } from '../format';
import type { Project } from '../types';
import Icon from './Icon';
import SkillTags from './SkillTags';
import StatusBadge from './StatusBadge';

interface Props {
  project: Project;
  skillNames: Map<string, string>;
  applied?: boolean;
}

export default function ProjectCard({ project, skillNames, applied }: Props) {
  return (
    <Link to={`/projects/${project.id}`} className="project-card">
      <div className="project-card-top">
        <StatusBadge status={project.status} />
        {applied && <span className="badge accepted">Applied</span>}
        <span className="muted small">{timeAgo(project.createdAt)}</span>
      </div>
      <h3>{project.title}</h3>
      <p className="clamp">{project.description}</p>
      <SkillTags ids={project.skillIds} names={skillNames} limit={4} />
      <div className="project-card-foot">
        <span className="price"><small>Budget</small>{budget(project.budget)}</span>
        {project.deadline && (
          <span className="muted small"><Icon name="clock" size={14} /> Due {new Date(project.deadline).toLocaleDateString()}</span>
        )}
      </div>
    </Link>
  );
}
