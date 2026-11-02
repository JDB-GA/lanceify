import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../api/client';
import { proposalsApi } from '../api/proposals';
import EmptyState from '../components/EmptyState';
import Icon from '../components/Icon';
import MessageButton from '../components/MessageButton';
import StatusBadge from '../components/StatusBadge';
import { useToast } from '../context/ToastContext';
import { money, timeAgo } from '../format';
import { useLoad } from '../hooks/useLoad';
import type { Proposal, ProposalStatus } from '../types';

const FILTERS: (ProposalStatus | 'ALL')[] = ['ALL', 'PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'];
const LABELS: Record<string, string> = { ALL: 'All', PENDING: 'Pending', ACCEPTED: 'Hired', REJECTED: 'Declined', WITHDRAWN: 'Withdrawn' };

export default function MyProposalsPage() {
  const { notify } = useToast();
  const [filter, setFilter] = useState<ProposalStatus | 'ALL'>('ALL');
  const [busyId, setBusyId] = useState<string | null>(null);
  const { data, setData, error, loading, reload } = useLoad((signal) => proposalsApi.mine(signal), []);

  const withdraw = async (proposal: Proposal) => {
    if (busyId || !window.confirm('Withdraw this proposal? The client will no longer see it.')) return;
    setBusyId(proposal.id);
    try {
      const updated = await proposalsApi.withdraw(proposal.id);
      setData((current) => current?.map((item) => (item.id === updated.id ? updated : item)) ?? null);
      notify('Proposal withdrawn');
    } catch (err) {
      const apiError = err instanceof ApiError ? err : null;
      notify(apiError?.message ?? 'Could not withdraw the proposal', 'error');
      if (apiError?.conflict) reload();
    } finally {
      setBusyId(null);
    }
  };

  const list = (data ?? []).filter((item) => filter === 'ALL' || item.status === filter);

  return (
    <section className="stack-lg">
      <div className="welcome">
        <div>
          <span className="kicker">Your pipeline</span>
          <h1>My <em>proposals</em></h1>
        </div>
        <Link to="/projects" className="button secondary"><Icon name="search" size={16} /> Find more work</Link>
      </div>

      <div className="tabs" role="group" aria-label="Filter by status">
        {FILTERS.map((item) => (
          <button key={item} className={item === filter ? 'tab active' : 'tab'} aria-pressed={item === filter} onClick={() => setFilter(item)}>
            {LABELS[item]}
            <span className="tab-count">{item === 'ALL' ? data?.length ?? 0 : (data ?? []).filter((p) => p.status === item).length}</span>
          </button>
        ))}
      </div>

      {error && <div className="alert error">{error.message}</div>}
      {loading && !data && [0, 1, 2].map((key) => <div key={key} className="skeleton card-sk" />)}
      {data && list.length === 0 && (
        <EmptyState title="Nothing here yet" icon="file" action={<Link to="/projects" className="button sm">Browse projects</Link>}>
          Proposals you send will be tracked here.
        </EmptyState>
      )}

      <div className="stack">
        {list.map((proposal) => (
          <article key={proposal.id} className="proposal-card">
            <div className="proposal-main">
              <div className="row-between">
                <Link to={`/projects/${proposal.projectId}`} className="strong-link lg">{proposal.projectTitle ?? 'Project'}</Link>
                <StatusBadge status={proposal.status} />
              </div>
              <p className="clamp">{proposal.coverLetter}</p>
              <div className="meta-row">
                <span><Icon name="dollar" size={14} />{money(proposal.bidAmount)}</span>
                <span><Icon name="clock" size={14} />{proposal.deliveryDays} days</span>
                <span className="muted">Sent {timeAgo(proposal.createdAt)}</span>
              </div>
            </div>
            <div className="proposal-actions">
              <MessageButton recipientId={proposal.clientId} projectId={proposal.projectId} label="Message client" />
              {proposal.status === 'PENDING' && (
                <button className="button danger-ghost sm" disabled={busyId === proposal.id} onClick={() => withdraw(proposal)}>
                  {busyId === proposal.id ? 'Withdrawing…' : 'Withdraw'}
                </button>
              )}
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
