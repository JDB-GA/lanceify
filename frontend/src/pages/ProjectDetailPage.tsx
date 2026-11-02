import { useMemo, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { projectsApi } from '../api/projects';
import { proposalsApi } from '../api/proposals';
import Avatar from '../components/Avatar';
import EmptyState from '../components/EmptyState';
import ErrorMessage from '../components/ErrorMessage';
import Icon from '../components/Icon';
import MessageButton from '../components/MessageButton';
import ProposalReviewCard from '../components/ProposalReviewCard';
import SkillTags from '../components/SkillTags';
import StatusBadge from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { budget, money, shortDate, timeAgo } from '../format';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useLoad } from '../hooks/useLoad';
import { useProposalDecisions } from '../hooks/useProposalDecisions';
import { useSkills } from '../hooks/useSkills';
import { displayName, useUsers } from '../hooks/useUsers';
import type { Project } from '../types';

const COVER_MIN = 20;
const COVER_MAX = 2000;

export default function ProjectDetailPage() {
  const { id = '' } = useParams();
  const { user } = useAuth();
  const { names } = useSkills();
  const project = useLoad((signal) => projectsApi.get(id, signal), [id]);

  if (project.loading && !project.data) return <div className="skeleton card-sk tall" />;
  if (project.error || !project.data) {
    return (
      <EmptyState title="Project unavailable" icon="briefcase" action={<Link to={user?.role === 'CLIENT' ? '/client' : '/projects'} className="button sm">Go back</Link>}>
        {project.error?.missing ? 'This project could not be found, or the project service is not running yet.' : project.error?.message}
      </EmptyState>
    );
  }

  const data = project.data;
  const isOwner = user?.userId === data.clientId;

  return (
    <div className="detail-layout">
      <article className="panel detail-main">
        <Link to={isOwner ? '/client' : '/projects'} className="back-link"><Icon name="back" size={16} />Back</Link>
        <div className="row-between">
          <StatusBadge status={data.status} />
          <span className="muted small">Posted {timeAgo(data.createdAt)}</span>
        </div>
        <h1 className="detail-title">{data.title}</h1>
        <div className="fact-row">
          <div><small>Budget</small><strong>{budget(data.budgetMin, data.budgetMax)}</strong></div>
          <div><small>Deadline</small><strong>{shortDate(data.deadline)}</strong></div>
          <div><small>Skills</small><strong>{data.skillIds.length}</strong></div>
        </div>
        <h2 className="section-label">About the project</h2>
        <p className="prose">{data.description}</p>
        <h2 className="section-label">Skills needed</h2>
        <SkillTags ids={data.skillIds} names={names} limit={20} />
        {isOwner && <ClientProposals projectId={data.id} />}
      </article>
      <aside className="detail-side">
        {user?.role === 'FREELANCER' && <ApplyPanel project={data} />}
        {isOwner && (
          <div className="panel">
            <h2>Your project</h2>
            <p className="muted">Review proposals on the left. Hiring one automatically declines the rest, even if someone else is reviewing at the same time.</p>
          </div>
        )}
        {!isOwner && <ClientCard clientId={data.clientId} projectId={data.id} />}
      </aside>
    </div>
  );
}

function ClientCard({ clientId, projectId }: { clientId: string; projectId: string }) {
  const users = useUsers([clientId]);
  const client = users.get(clientId);
  const name = displayName(client, 'Client');
  return (
    <div className="panel">
      <span className="kicker">About the client</span>
      <div className="person">
        <Avatar name={name} seed={clientId} size="lg" />
        <div>
          <strong>{name}</strong>
          {client && <small className="muted">@{client.username}</small>}
        </div>
      </div>
      <MessageButton recipientId={clientId} projectId={projectId} label="Ask a question" className="button secondary block" />
    </div>
  );
}

function ApplyPanel({ project }: { project: Project }) {
  const { notify } = useToast();
  const mine = useLoad((signal) => proposalsApi.mine(signal), []);
  const existing = useMemo(() => mine.data?.find((item) => item.projectId === project.id), [mine.data, project.id]);
  const { busy, error, run } = useAsyncAction();
  const [form, setForm] = useState({ coverLetter: '', bidAmount: '', deliveryDays: '' });

  if (mine.loading && !mine.data) return <div className="skeleton card-sk" />;

  if (existing) {
    return (
      <div className="panel">
        <div className="row-between"><h2>Your proposal</h2><StatusBadge status={existing.status} /></div>
        <div className="fact-row compact">
          <div><small>Bid</small><strong>{money(existing.bidAmount)}</strong></div>
          <div><small>Delivery</small><strong>{existing.deliveryDays} days</strong></div>
        </div>
        <p className="muted clamp">{existing.coverLetter}</p>
        <Link to="/freelancer/proposals" className="button secondary block">Manage proposals</Link>
      </div>
    );
  }

  if (project.status !== 'OPEN') {
    return <div className="panel"><h2>Closed</h2><p className="muted">This project is no longer accepting proposals.</p></div>;
  }

  const coverLength = form.coverLetter.trim().length;
  const submit = (event: FormEvent) => {
    event.preventDefault();
    if (busy) return;
    run(async () => {
      try {
        await proposalsApi.apply(project.id, {
          coverLetter: form.coverLetter.trim(),
          bidAmount: Number(form.bidAmount),
          deliveryDays: Number(form.deliveryDays),
        });
        notify('Proposal sent. Good luck!');
      } catch (err) {
        if (err instanceof ApiError && err.conflict) await mine.reload();
        throw err;
      }
      await mine.reload();
    });
  };

  return (
    <form className="panel apply-panel" onSubmit={submit}>
      <h2>Send a proposal</h2>
      <p className="muted small">Client budget: <b>{budget(project.budgetMin, project.budgetMax)}</b></p>
      <ErrorMessage error={error} />
      <div className="row">
        <label>Your bid
          <span className="input-group">
            <span className="prefix">$</span>
            <input type="number" min="1" step="0.01" required value={form.bidAmount} onChange={(event) => setForm({ ...form, bidAmount: event.target.value })} />
          </span>
        </label>
        <label>Delivery (days)
          <input type="number" min="1" max="365" required value={form.deliveryDays} onChange={(event) => setForm({ ...form, deliveryDays: event.target.value })} />
        </label>
      </div>
      <label>Cover letter
        <textarea
          rows={7}
          required
          minLength={COVER_MIN}
          maxLength={COVER_MAX}
          placeholder="Why are you the right person? Mention similar work you have done."
          value={form.coverLetter}
          onChange={(event) => setForm({ ...form, coverLetter: event.target.value })}
        />
        <span className="field-meta">
          <span className={coverLength > 0 && coverLength < COVER_MIN ? 'field-error' : ''}>At least {COVER_MIN} characters</span>
          <span>{form.coverLetter.length}/{COVER_MAX}</span>
        </span>
      </label>
      <button className="button block" disabled={busy || coverLength < COVER_MIN}>
        {busy ? 'Sending…' : 'Send proposal'}
        {busy ? <span className="spinner" aria-hidden="true" /> : <Icon name="send" size={16} />}
      </button>
    </form>
  );
}

function ClientProposals({ projectId }: { projectId: string }) {
  const proposals = useLoad((signal) => proposalsApi.forProject(projectId, signal), [projectId]);
  const list = proposals.data ?? [];
  const users = useUsers(list.map((item) => item.freelancerId));
  const decisions = useProposalDecisions(proposals.reload);
  const hired = list.some((item) => item.status === 'ACCEPTED');

  return (
    <section className="stack">
      <h2 className="section-label">Proposals <span className="pill-count">{list.length}</span></h2>
      {proposals.error && <div className="alert error">{proposals.error.message}</div>}
      {proposals.data && list.length === 0 && (
        <EmptyState title="No proposals yet" icon="inbox">Freelancers will start bidding soon. You will see them here.</EmptyState>
      )}
      {list.map((proposal) => (
        <ProposalReviewCard
          key={proposal.id}
          proposal={proposal}
          freelancer={users.get(proposal.freelancerId)}
          busy={decisions.busyId !== null}
          locked={hired}
          onAccept={decisions.accept}
          onReject={decisions.reject}
        />
      ))}
    </section>
  );
}
