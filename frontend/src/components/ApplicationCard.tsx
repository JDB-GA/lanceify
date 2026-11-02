import { useState, type FormEvent } from 'react';
import type { FreelancerProfile } from '../types';
import StatusBadge from './StatusBadge';

interface Props {
  application: FreelancerProfile;
  skillNames: Map<string, string>;
  busy: boolean;
  onApprove: (id: string) => void;
  onReject: (id: string, reason: string) => void;
}

const money = (value: number | null) => (value == null ? '—' : `$${value.toLocaleString()}`);

export default function ApplicationCard({ application, skillNames, busy, onApprove, onReject }: Props) {
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState('');

  const submitRejection = (event: FormEvent) => {
    event.preventDefault();
    onReject(application.id, reason);
  };

  return (
    <article className="card application">
      <div className="card-heading">
        <div className="card-title">
          <span className="avatar lg" aria-hidden="true">{application.userId.slice(0, 2).toUpperCase()}</span>
          <div>
            <h2>Applicant {application.userId}</h2>
            {application.submittedAt && <small>Submitted {new Date(application.submittedAt).toLocaleString()}</small>}
          </div>
        </div>
        <StatusBadge status={application.approvalStatus} />
      </div>
      <p className="bio">{application.bio || <span className="muted">No bio provided.</span>}</p>
      <dl>
        <div><dt>Price range</dt><dd>{money(application.minPrice)} – {money(application.maxPrice)}</dd></div>
        <div>
          <dt>Skills</dt>
          <dd>
            {application.skillIds.length === 0 ? '—' : (
              <span className="tag-list">
                {application.skillIds.map((id) => <span key={id} className="tag">{skillNames.get(id) ?? 'Unavailable skill'}</span>)}
              </span>
            )}
          </dd>
        </div>
        {application.rejectionReason && <div><dt>Rejection reason</dt><dd>{application.rejectionReason}</dd></div>}
      </dl>
      {application.approvalStatus === 'PENDING' && !rejecting && (
        <div className="actions end">
          <button className="button danger" disabled={busy} onClick={() => setRejecting(true)}>Reject</button>
          <button className="button success" disabled={busy} onClick={() => onApprove(application.id)}>Approve <span className="arrow" aria-hidden="true">→</span></button>
        </div>
      )}
      {rejecting && (
        <form onSubmit={submitRejection}>
          <label>Reason for rejection
            <textarea rows={2} required autoFocus maxLength={500} placeholder="Explain what the freelancer should improve…" value={reason} onChange={(event) => setReason(event.target.value)} />
            <span className="field-meta"><span>The freelancer will see this message.</span><span>{reason.length}/500</span></span>
          </label>
          <div className="actions end">
            <button type="button" className="button secondary" onClick={() => setRejecting(false)}>Cancel</button>
            <button className="button danger" disabled={busy}>Confirm rejection</button>
          </div>
        </form>
      )}
    </article>
  );
}
