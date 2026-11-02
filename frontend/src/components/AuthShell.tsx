import type { ReactNode } from 'react';
import Brand from './Brand';
import Icon, { type IconName } from './Icon';

const POINTS: { icon: IconName; text: string }[] = [
  { icon: 'shield', text: 'Every freelancer is reviewed by a real person' },
  { icon: 'dollar', text: 'Clear budgets and bids, no surprises' },
  { icon: 'bolt', text: 'Live chat with the people you hire' },
];

export default function AuthShell({ children }: { children: ReactNode }) {
  return (
    <div className="auth-page">
      <aside className="auth-hero">
        <Brand />
        <div className="auth-copy">
          <span className="kicker light">The vetted marketplace</span>
          <h2>Work with people who <em>care</em> about the craft.</h2>
          <ul className="auth-features">
            {POINTS.map((point) => (
              <li key={point.text}><span><Icon name={point.icon} /></span>{point.text}</li>
            ))}
          </ul>
        </div>
        <div className="auth-quote">
          <div className="floating-card">
            <span className="badge accepted">Hired</span>
            <strong>Brand identity for a coffee roastery</strong>
            <small>$1,200 · delivered in 9 days</small>
          </div>
          <div className="floating-card offset">
            <span className="badge pending">New proposal</span>
            <strong>React dashboard rebuild</strong>
            <small>3 freelancers bid in the last hour</small>
          </div>
        </div>
      </aside>
      <main className="auth-main">{children}</main>
    </div>
  );
}
