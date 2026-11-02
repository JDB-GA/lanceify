import { useState } from 'react';
import { adminApi } from '../api/admin';
import { ApiError } from '../api/client';
import ApplicationCard from '../components/ApplicationCard';
import EmptyState from '../components/EmptyState';
import Pagination from '../components/Pagination';
import { useToast } from '../context/ToastContext';
import { useLoad } from '../hooks/useLoad';
import { useSkills } from '../hooks/useSkills';
import type { ApprovalStatus } from '../types';

const FILTERS: ApprovalStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'DRAFT'];
const PAGE_SIZE = 10;

const capitalize = (value: string) => value.charAt(0) + value.slice(1).toLowerCase();

export default function AdminApplicationsPage() {
  const { notify } = useToast();
  const [status, setStatus] = useState<ApprovalStatus>('PENDING');
  const [page, setPage] = useState(0);
  const [busy, setBusy] = useState(false);
  const { names } = useSkills();
  const { data, error, loading, reload } = useLoad((signal) => adminApi.list(status, page, PAGE_SIZE, signal), [status, page]);

  const chooseFilter = (next: ApprovalStatus) => {
    setStatus(next);
    setPage(0);
  };

  const review = async (action: () => Promise<unknown>, success: string) => {
    if (busy) return;
    setBusy(true);
    try {
      await action();
      notify(success);
    } catch (err) {
      notify(err instanceof ApiError ? err.message : 'Something went wrong', 'error');
    } finally {
      setBusy(false);
      await reload();
    }
  };

  return (
    <section className="stack-lg">
      <div className="welcome">
        <div>
          <span className="kicker">Admin · Review queue</span>
          <h1>Freelancer <em>applications</em></h1>
          <p className="muted">Every approval is a promise to our clients. If two admins review the same application, only the first decision counts.</p>
        </div>
        {data && <div className="big-count">{data.totalElements}<small>{status.toLowerCase()}</small></div>}
      </div>
      <div className="tabs" role="group" aria-label="Filter by status">
        {FILTERS.map((item) => (
          <button key={item} className={item === status ? 'tab active' : 'tab'} aria-pressed={item === status} onClick={() => chooseFilter(item)}>
            {capitalize(item)}
          </button>
        ))}
      </div>
      {error && <div className="alert error">{error.message}</div>}
      {loading && !data && [0, 1, 2].map((key) => <div key={key} className="skeleton card-sk" aria-hidden="true" />)}
      {data?.content.length === 0 && (
        <EmptyState title={status === 'PENDING' ? 'Inbox zero' : 'Nothing here'} icon="shield">
          {status === 'PENDING' ? 'No one is waiting on you. Nice work.' : `No ${status.toLowerCase()} applications yet.`}
        </EmptyState>
      )}
      <div className="stack">
        {data?.content.map((application) => (
          <ApplicationCard
            key={application.id}
            application={application}
            skillNames={names}
            busy={busy}
            onApprove={(id) => review(() => adminApi.approve(id), 'Application approved')}
            onReject={(id, reason) => review(() => adminApi.reject(id, reason), 'Application rejected')}
          />
        ))}
      </div>
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </section>
  );
}
