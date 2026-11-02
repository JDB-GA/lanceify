interface Props {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export default function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pager" aria-label="Pagination">
      <button className="button secondary" disabled={page === 0} onClick={() => onChange(page - 1)}>← Previous</button>
      <span>{page + 1} / {totalPages}</span>
      <button className="button secondary" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>Next →</button>
    </nav>
  );
}
