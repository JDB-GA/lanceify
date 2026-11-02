import { useAuth } from '../context/AuthContext';

const FEATURES = [
  { title: 'Post a project', text: 'Describe the job, set a budget, list the skills it needs.' },
  { title: 'Browse the roster', text: 'Every profile has been read and approved by a real person.' },
  { title: 'Hire & work together', text: 'Proposals, contracts and messages, all in one place.' },
];

export default function ClientHomePage() {
  const { user } = useAuth();
  return (
    <section>
      <div className="card hero-card">
        <span className="eyebrow">Client desk</span>
        <h1>Hello, <em>{user?.firstName}.</em></h1>
        <p>We’re still setting up shop. Soon you’ll post projects and hire hand-reviewed freelancers right from here.</p>
      </div>
      <h2 className="section-title"><span>§</span> On the way</h2>
      <div className="feature-grid">
        {FEATURES.map((feature, index) => (
          <article key={feature.title} className="feature">
            <span className="num" aria-hidden="true">0{index + 1}</span>
            <h2>{feature.title}</h2>
            <p>{feature.text}</p>
            <span className="soon">In the works</span>
          </article>
        ))}
      </div>
    </section>
  );
}
