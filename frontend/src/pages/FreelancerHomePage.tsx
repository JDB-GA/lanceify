import { Link } from 'react-router-dom';
import { ApiError } from '../api/client';
import { freelancerApi } from '../api/freelancer';
import { proposalsApi } from '../api/proposals';
import EmptyState from '../components/EmptyState';
import Icon from '../components/Icon';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { money, timeAgo } from '../format';
import { useLoad } from '../hooks/useLoad';
import type { ApprovalStatus } from '../types';

const PROFILE_COPY: Record<ApprovalStatus | 'NONE', { title: string; text: string; cta: string }> = {
  NONE: { title: 'Create your freelancer profile', text: 'Tell clients what you do. An admin reviews every profile before you can bid.', cta: 'Start my profile' },
  DRAFT: { title: 'Your profile is a draft', text: 'Finish it and submit it for review to start sending proposals.', cta: 'Finish profile' },
  PENDING: { title: 'Your profile is under review', text: 'An admin is reading it now. You will be able to bid once approved.', cta: 'View profile' },
  APPROVED: { title: 'You are approved', text: 'Your profile is live. Browse open projects and send your best proposals.', cta: 'Edit profile' },
  REJECTED: { title: 'Your profile needs changes', text: 'Read the feedback, update your profile and submit it again.', cta: 'See feedback' },
};

export default function FreelancerHomePage() {
  const { user } = useAuth();
  const profile = useLoad(async () => {
    try {
      return await freelancerApi.get();
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) return null;
      throw err;
    }
  }, []);
  const proposals = useLoad((signal) => proposalsApi.mine(signal), []);

  const list = proposals.data ?? [];
  const count = (status: string) => list.filter((item) => item.status === status).length;
  const status = profile.data?.approvalStatus ?? 'NONE';
  const copy = PROFILE_COPY[status];

  return (
    <section className="stack-lg">
      <div className="welcome">
        <div>
          <span className="kicker">Freelancer dashboard</span>
          <h1>Hi {user?.firstName}, ready to <em>win</em> some work?</h1>
        </div>
        <Link to="/projects" className="button"><Icon name="search" size={16} /> Find projects</Link>
      </div>

      <div className="stat-grid">
        <div className="stat"><span>Pending</span><strong>{count('PENDING')}</strong><small>proposals waiting</small></div>
        <div className="stat lime"><span>Hired</span><strong>{count('ACCEPTED')}</strong><small>accepted proposals</small></div>
        <div className="stat"><span>Total</span><strong>{list.length}</strong><small>proposals sent</small></div>
        <div className="stat"><span>Profile</span><strong className="stat-text">{status === 'NONE' ? 'Not started' : <StatusBadge status={status} />}</strong><small>approval status</small></div>
      </div>

      {!profile.loading && (
        <div className={`banner ${status.toLowerCase()}`}>
          <span className="banner-icon"><Icon name={status === 'APPROVED' ? 'check' : 'shield'} size={22} /></span>
          <div>
            <strong>{copy.title}</strong>
            <p>{copy.text}</p>
          </div>
          <Link to="/freelancer/profile" className="button secondary sm">{copy.cta}</Link>
        </div>
      )}

      <div className="panel">
        <div className="panel-head">
          <h2>Recent proposals</h2>
          <Link to="/freelancer/proposals" className="link">View all <Icon name="arrow" size={14} /></Link>
        </div>
        {proposals.loading && !proposals.data && <div className="skeleton row-sk" />}
        {proposals.error && <div className="alert error">{proposals.error.message}</div>}
        {proposals.data && list.length === 0 && (
          <EmptyState title="No proposals yet" icon="file" action={<Link to="/projects" className="button sm">Browse projects</Link>}>
            When you apply to a project it will show up here with its status.
          </EmptyState>
        )}
        <ul className="list">
          {list.slice(0, 5).map((proposal) => (
            <li key={proposal.id} className="list-row">
              <div>
                <Link to={`/projects/${proposal.projectId}`} className="strong-link">{proposal.projectTitle ?? 'Project'}</Link>
                <small className="muted">{money(proposal.bidAmount)} · {proposal.deliveryDays} days · sent {timeAgo(proposal.createdAt)}</small>
              </div>
              <StatusBadge status={proposal.status} />
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
}
