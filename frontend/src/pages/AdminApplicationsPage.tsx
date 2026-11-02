import { useCallback, useEffect, useMemo, useState } from 'react';
import { adminApi } from '../api/admin';
import ApplicationCard from '../components/ApplicationCard';
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
  const { skills } = useSkills();
  const skillNames = useMemo(() => new Map(skills.map((skill) => [skill.id, skill.name])), [skills]);

  const load = useCallback(async () => {
    setData(await adminApi.list(status, page, PAGE_SIZE));
  }, [status, page]);

  useEffect(() => {
    run(load);
  }, [run, load]);

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
      <h1>Freelancer applications</h1>
      <div className="tabs" role="group" aria-label="Filter by status">
        {FILTERS.map((item) => (
          <button key={item} className={item === status ? 'tab active' : 'tab'} aria-pressed={item === status} onClick={() => chooseFilter(item)}>
            {capitalize(item)}
          </button>
        ))}
      </div>
      <ErrorMessage error={error} />
      {!data && !error && <p className="muted">Loading…</p>}
      {data?.content.length === 0 && <div className="card muted">No {status.toLowerCase()} applications.</div>}
      {data?.content.map((application) => (
        <ApplicationCard key={application.id} application={application} skillNames={skillNames} busy={busy} onApprove={approve} onReject={reject} />
      ))}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </section>
  );
}
