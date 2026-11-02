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
        <h2>Applicant {application.userId}</h2>
        <StatusBadge status={application.approvalStatus} />
      </div>
      <p className="bio">{application.bio || 'No bio provided.'}</p>
      <dl>
        <div><dt>Price range</dt><dd>{application.minPrice ?? '—'} to {application.maxPrice ?? '—'}</dd></div>
        <div><dt>Skills</dt><dd>{application.skillIds.map((id) => skillNames.get(id) ?? id).join(', ') || '—'}</dd></div>
        {application.submittedAt && <div><dt>Submitted</dt><dd>{new Date(application.submittedAt).toLocaleString()}</dd></div>}
        {application.rejectionReason && <div><dt>Rejection reason</dt><dd>{application.rejectionReason}</dd></div>}
      </dl>
      {application.approvalStatus === 'PENDING' && !rejecting && (
        <div className="actions">
          <button className="button" disabled={busy} onClick={() => onApprove(application.id)}>Approve</button>
          <button className="button danger" disabled={busy} onClick={() => setRejecting(true)}>Reject</button>
        </div>
      )}
      {rejecting && (
        <form onSubmit={submitRejection}>
          <label>Reason for rejection
            <textarea rows={2} required maxLength={500} value={reason} onChange={(event) => setReason(event.target.value)} />
          </label>
          <div className="actions">
            <button className="button danger" disabled={busy}>Confirm rejection</button>
            <button type="button" className="button secondary" onClick={() => setRejecting(false)}>Cancel</button>
          </div>
        </form>
      )}
    </article>
  );
}
