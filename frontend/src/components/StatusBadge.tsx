import type { ApprovalStatus } from '../types';

const LABELS: Record<ApprovalStatus, string> = {
  DRAFT: 'Draft',
  PENDING: 'Pending review',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
};

export default function StatusBadge({ status }: { status: ApprovalStatus }) {
  return <span className={`badge ${status.toLowerCase()}`}>{LABELS[status]}</span>;
}
