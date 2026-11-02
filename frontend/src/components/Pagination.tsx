interface Props {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export default function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null;
  return (
    <div className="pager">
      <button className="button secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>Previous</button>
      <span className="muted">Page {page + 1} of {totalPages}</span>
      <button className="button secondary" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>Next</button>
    </div>
  );
}
