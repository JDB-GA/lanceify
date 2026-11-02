import { Link } from 'react-router-dom';
import { money, timeAgo } from '../format';
import { displayName } from '../hooks/useUsers';
import type { Proposal, UserSummary } from '../types';
import Avatar from './Avatar';
import Icon from './Icon';
import MessageButton from './MessageButton';
import StatusBadge from './StatusBadge';

interface Props {
  proposal: Proposal;
  freelancer?: UserSummary;
  busy: boolean;
  locked: boolean;
  showProject?: boolean;
  onAccept: (proposal: Proposal) => void;
  onReject: (proposal: Proposal) => void;
}

export default function ProposalReviewCard({ proposal, freelancer, busy, locked, showProject, onAccept, onReject }: Props) {
  const name = displayName(freelancer, 'Freelancer');
  return (
    <article className={`proposal-card ${proposal.status === 'ACCEPTED' ? 'hired' : ''}`}>
      <div className="proposal-main">
        <div className="row-between">
          <div className="person">
            <Avatar name={name} seed={proposal.freelancerId} />
            <div>
              <strong>{name}</strong>
              <small className="muted">{freelancer ? `@${freelancer.username} · ` : ''}{timeAgo(proposal.createdAt)}</small>
            </div>
          </div>
          <StatusBadge status={proposal.status} />
        </div>
        {showProject && (
          <Link to={`/projects/${proposal.projectId}`} className="project-ref"><Icon name="briefcase" size={14} />{proposal.projectTitle ?? 'Project'}</Link>
        )}
        <p>{proposal.coverLetter}</p>
        <div className="meta-row">
          <span className="bid"><Icon name="dollar" size={14} />{money(proposal.bidAmount)}</span>
          <span><Icon name="clock" size={14} />{proposal.deliveryDays} days</span>
        </div>
      </div>
      <div className="proposal-actions">
        <MessageButton recipientId={proposal.freelancerId} projectId={proposal.projectId} />
        {proposal.status === 'PENDING' && !locked && (
          <>
            <button className="button danger-ghost sm" disabled={busy} onClick={() => onReject(proposal)}>
              <Icon name="x" size={16} />Decline
            </button>
            <button className="button sm" disabled={busy} onClick={() => onAccept(proposal)}>
              <Icon name="check" size={16} />Hire
            </button>
          </>
        )}
      </div>
    </article>
  );
}
