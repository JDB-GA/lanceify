import { Link } from 'react-router-dom';

export default function Brand({ to = '/' }: { to?: string }) {
  return (
    <Link to={to} className="brand" aria-label="Lanceify home">
      <span className="brand-mark" aria-hidden="true">l</span>
      lanceify<span className="brand-dot">.</span>
    </Link>
  );
}
