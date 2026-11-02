import type { ReactNode } from 'react';

interface Props {
  title: string;
  children?: ReactNode;
}

export default function EmptyState({ title, children }: Props) {
  return (
    <div className="card empty-state">
      <h2>{title}</h2>
      {children && <p>{children}</p>}
    </div>
  );
}
