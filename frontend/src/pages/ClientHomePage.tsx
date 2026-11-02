import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { projectsApi } from '../api/projects';
import { proposalsApi } from '../api/proposals';
import EmptyState from '../components/EmptyState';
import Icon from '../components/Icon';
import ProjectCard from '../components/ProjectCard';
import ProposalReviewCard from '../components/ProposalReviewCard';
import { useAuth } from '../context/AuthContext';
import { useLoad } from '../hooks/useLoad';
import { useProposalDecisions } from '../hooks/useProposalDecisions';
import { useSkills } from '../hooks/useSkills';
import { useUsers } from '../hooks/useUsers';
import type { ProposalStatus } from '../types';

const FILTERS: (ProposalStatus | 'ALL')[] = ['PENDING', 'ACCEPTED', 'ALL'];
const LABELS: Record<string, string> = { PENDING: 'To review', ACCEPTED: 'Hired', ALL: 'All' };

export default function ClientHomePage() {
  const { user } = useAuth();
  const { names } = useSkills();
  const [filter, setFilter] = useState<ProposalStatus | 'ALL'>('PENDING');
  const projects = useLoad((signal) => projectsApi.mine(signal), []);
  const proposals = useLoad((signal) => proposalsApi.received(signal), []);
  const all = proposals.data ?? [];
  const users = useUsers(all.map((item) => item.freelancerId));
  const decisions = useProposalDecisions(proposals.reload);

  const hiredProjects = useMemo(
    () => new Set(all.filter((item) => item.status === 'ACCEPTED').map((item) => item.projectId)),
    [all],
  );
  const visible = all.filter((item) => filter === 'ALL' || item.status === filter);
  const pending = all.filter((item) => item.status === 'PENDING').length;

  return (
    <section className="stack-lg">
      <div className="welcome">
        <div>
          <span className="kicker">Client dashboard</span>
          <h1>Hello {user?.firstName}, let’s get it <em>done</em>.</h1>
        </div>
        <Link to="/client/projects/new" className="button"><Icon name="plus" size={16} /> Post a project</Link>
      </div>

      <div className="stat-grid">
        <div className="stat"><span>Projects</span><strong>{projects.data?.length ?? '—'}</strong><small>posted by you</small></div>
        <div className="stat lime"><span>To review</span><strong>{pending}</strong><small>pending proposals</small></div>
        <div className="stat"><span>Hired</span><strong>{hiredProjects.size}</strong><small>freelancers working</small></div>
        <div className="stat"><span>Received</span><strong>{all.length}</strong><small>proposals in total</small></div>
      </div>

      <div className="panel">
        <div className="panel-head">
          <h2>Your projects</h2>
          <Link to="/client/projects/new" className="link">New project <Icon name="arrow" size={14} /></Link>
        </div>
        {projects.loading && !projects.data && <div className="card-grid">{[0, 1, 2].map((key) => <div key={key} className="skeleton project-sk" />)}</div>}
        {projects.error && (
          <EmptyState title="Project posting is almost here" icon="briefcase">
            {projects.error.missing
              ? 'The project service is not running yet. Proposals you receive still show up below.'
              : projects.error.message}
          </EmptyState>
        )}
        {projects.data && projects.data.length === 0 && (
          <EmptyState title="No projects yet" icon="briefcase" action={<Link to="/client/projects/new" className="button sm">Post your first project</Link>}>
            Describe what you need and vetted freelancers will send proposals.
          </EmptyState>
        )}
        <div className="card-grid">
          {(projects.data ?? []).map((project) => <ProjectCard key={project.id} project={project} skillNames={names} />)}
        </div>
      </div>

      <div className="panel">
        <div className="panel-head">
          <h2>Proposals</h2>
          <div className="tabs compact" role="group" aria-label="Filter proposals">
            {FILTERS.map((item) => (
              <button key={item} className={item === filter ? 'tab active' : 'tab'} aria-pressed={item === filter} onClick={() => setFilter(item)}>
                {LABELS[item]}
              </button>
            ))}
          </div>
        </div>
        {proposals.error && <div className="alert error">{proposals.error.message}</div>}
        {proposals.loading && !proposals.data && <div className="skeleton card-sk" />}
        {proposals.data && visible.length === 0 && (
          <EmptyState title={filter === 'PENDING' ? 'All caught up' : 'Nothing here'} icon="inbox">
            {filter === 'PENDING' ? 'No proposals are waiting for you right now.' : 'No proposals match this filter.'}
          </EmptyState>
        )}
        <div className="stack">
          {visible.map((proposal) => (
            <ProposalReviewCard
              key={proposal.id}
              proposal={proposal}
              freelancer={users.get(proposal.freelancerId)}
              busy={decisions.busyId !== null}
              locked={hiredProjects.has(proposal.projectId)}
              showProject
              onAccept={decisions.accept}
              onReject={decisions.reject}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
