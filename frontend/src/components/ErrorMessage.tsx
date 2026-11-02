import type { ApiError } from '../api/client';

export default function ErrorMessage({ error }: { error: ApiError | null }) {
  if (!error) return null;
  return (
    <div className="alert error" role="alert">
      <p>{error.message}</p>
      {error.errors.length > 0 && (
        <ul>{error.errors.map((item) => <li key={item}>{item}</li>)}</ul>
      )}
    </div>
  );
}
