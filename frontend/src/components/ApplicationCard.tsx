import { useState, type FormEvent } from 'react';
import { money } from '../format';
import { displayName, useUsers } from '../hooks/useUsers';
import type { FreelancerProfile } from '../types';
import Avatar from './Avatar';
import Icon from './Icon';
import SkillTags from './SkillTags';
import StatusBadge from './StatusBadge';

interface Props {
  application: FreelancerProfile;
  skillNames: Map<string, string>;
  busy: boolean;
  onApprove: (id: string) => void;
  onReject: (id: string, reason: string) => void;
}

export default function ApplicationCard({ application, skillNames, busy, onApprove, onReject }: Props) {
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState('');
  const users = useUsers([application.userId]);
  const applicant = users.get(application.userId);
  const name = displayName(applicant, 'Applicant');

  const submitRejection = (event: FormEvent) => {
    event.preventDefault();
    onReject(application.id, reason);
  };

  const meta = [
    applicant ? `@${applicant.username}` : null,
    application.submittedAt ? `Submitted ${new Date(application.submittedAt).toLocaleString()}` : null,
  ].filter(Boolean).join(' · ');

  return (
    <article className="panel application">
      <div className="row-between">
        <div className="person">
          <Avatar name={name} seed={application.userId} size="lg" />
          <div>
            <strong>{name}</strong>
            {meta && <small className="muted">{meta}</small>}
          </div>
        </div>
        <StatusBadge status={application.approvalStatus} />
      </div>
      <p className="prose">{application.bio || <span className="muted">No bio provided.</span>}</p>
      <dl className="fact-row">
        <div><dt>Price range</dt><dd>{money(application.minPrice)} – {money(application.maxPrice)}</dd></div>
        <div className="wide">
          <dt>Skills</dt>
          <dd>{application.skillIds.length === 0 ? '—' : <SkillTags ids={application.skillIds} names={skillNames} limit={15} />}</dd>
        </div>
        {application.rejectionReason && <div className="wide"><dt>Rejection reason</dt><dd>{application.rejectionReason}</dd></div>}
      </dl>
      {application.approvalStatus === 'PENDING' && !rejecting && (
        <div className="actions end">
          <button className="button danger-ghost sm" disabled={busy} onClick={() => setRejecting(true)}><Icon name="x" size={16} />Reject</button>
          <button className="button sm" disabled={busy} onClick={() => onApprove(application.id)}><Icon name="check" size={16} />Approve</button>
        </div>
      )}
      {rejecting && (
        <form onSubmit={submitRejection}>
          <label>Reason for rejection
            <textarea rows={2} required autoFocus maxLength={500} placeholder="Explain what the freelancer should improve…" value={reason} onChange={(event) => setReason(event.target.value)} />
            <span className="field-meta"><span>The freelancer will see this message.</span><span>{reason.length}/500</span></span>
          </label>
          <div className="actions end">
            <button type="button" className="button secondary sm" onClick={() => setRejecting(false)}>Cancel</button>
            <button className="button danger sm" disabled={busy}>Confirm rejection</button>
          </div>
        </form>
      )}
    </article>
  );
}
