import { useCallback, useEffect, useMemo, useState } from 'react';
import { adminApi } from '../api/admin';
import ApplicationCard from '../components/ApplicationCard';
import EmptyState from '../components/EmptyState';
import ErrorMessage from '../components/ErrorMessage';
import Pagination from '../components/Pagination';
import { useAsyncAction } from '../hooks/useAsyncAction';
import { useSkills } from '../hooks/useSkills';
import type { ApprovalStatus, FreelancerProfile, Page } from '../types';

const FILTERS: ApprovalStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'DRAFT'];
const PAGE_SIZE = 10;

const capitalize = (value: string) => value.charAt(0) + value.slice(1).toLowerCase();

export default function AdminApplicationsPage() {
  const [status, setStatus] = useState<ApprovalStatus>('PENDING');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<Page<FreelancerProfile> | null>(null);
  const { busy, error, run } = useAsyncAction();
  const { skills, reload: reloadSkills } = useSkills();
  const skillNames = useMemo(() => new Map(skills.map((skill) => [skill.id, skill.name])), [skills]);

  const load = useCallback(async () => {
    setData(await adminApi.list(status, page, PAGE_SIZE));
  }, [status, page]);

  useEffect(() => {
    run(load);
    reloadSkills();
  }, [run, load, reloadSkills]);

  const chooseFilter = (next: ApprovalStatus) => {
    setStatus(next);
    setPage(0);
  };

  const approve = (id: string) =>
    run(async () => {
      await adminApi.approve(id);
      await load();
    });

  const reject = (id: string, reason: string) =>
    run(async () => {
      await adminApi.reject(id, reason);
      await load();
    });

  return (
    <section>
      <div className="page-header">
        <div>
          <span className="eyebrow">Admin · Review queue</span>
          <h1>The <em>applications</em></h1>
          <p className="muted">Read carefully. Every approval is a promise to our clients.</p>
        </div>
        {data && <div className="count">{String(data.totalElements).padStart(2, '0')}<small>{status.toLowerCase()}</small></div>}
      </div>
      <div className="tabs" role="group" aria-label="Filter by status">
        {FILTERS.map((item) => (
          <button key={item} className={item === status ? 'tab active' : 'tab'} aria-pressed={item === status} onClick={() => chooseFilter(item)}>
            {capitalize(item)}
          </button>
        ))}
      </div>
      <ErrorMessage error={error} />
      {!data && !error && [0, 1, 2].map((key) => <div key={key} className="skeleton card-sk" aria-hidden="true" />)}
      {data?.content.length === 0 && (
        <EmptyState title={status === 'PENDING' ? 'Inbox zero.' : 'Nothing here.'}>
          {status === 'PENDING' ? 'No one is waiting on you. Go make a coffee.' : `No ${status.toLowerCase()} applications yet.`}
        </EmptyState>
      )}
      {data?.content.map((application) => (
        <ApplicationCard key={application.id} application={application} skillNames={skillNames} busy={busy} onApprove={approve} onReject={reject} />
      ))}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </section>
  );
}
