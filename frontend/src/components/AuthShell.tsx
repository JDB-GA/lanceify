import type { ReactNode } from 'react';
import Brand from './Brand';

const POINTS = ['Every freelancer is reviewed by a person', 'Clear price ranges, no surprises', 'Matched on real skills'];

export default function AuthShell({ children }: { children: ReactNode }) {
  return (
    <div className="auth-page">
      <aside className="auth-hero">
        <Brand />
        <span className="auth-stamp" aria-hidden="true">Hand<br />reviewed<br />talent</span>
        <div>
          <h2>Good work,<br /><em>found.</em></h2>
          <p>A small marketplace for people who take their craft seriously, and the clients who notice.</p>
        </div>
        <ul className="auth-features">
          {POINTS.map((point, index) => (
            <li key={point}><span>0{index + 1}</span>{point}</li>
          ))}
        </ul>
        <div className="auth-foot">
          <span>Est. {new Date().getFullYear()}</span>
          <span>Freelance, done properly</span>
        </div>
      </aside>
      <main className="auth-main">{children}</main>
    </div>
  );
}
