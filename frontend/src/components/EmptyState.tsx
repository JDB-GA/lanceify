import type { ReactNode } from 'react';
import Icon, { type IconName } from './Icon';

interface Props {
  title: string;
  icon?: IconName;
  action?: ReactNode;
  children?: ReactNode;
}

export default function EmptyState({ title, icon = 'spark', action, children }: Props) {
  return (
    <div className="empty-state">
      <span className="empty-icon"><Icon name={icon} size={26} /></span>
      <h2>{title}</h2>
      {children && <p>{children}</p>}
      {action}
    </div>
  );
}
