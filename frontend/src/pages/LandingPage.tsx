import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Brand from '../components/Brand';
import Icon, { type IconName } from '../components/Icon';

const CATEGORIES: { name: string; icon: IconName; hue: number }[] = [
  { name: 'Web development', icon: 'grid', hue: 262 },
  { name: 'Brand & design', icon: 'spark', hue: 18 },
  { name: 'Mobile apps', icon: 'layers', hue: 168 },
  { name: 'Writing', icon: 'edit', hue: 205 },
  { name: 'Data & AI', icon: 'bolt', hue: 45 },
  { name: 'Marketing', icon: 'chat', hue: 330 },
];

const POPULAR = ['React', 'Logo design', 'Spring Boot', 'Copywriting', 'Figma'];

const STEPS = [
  { title: 'Post the work', text: 'Describe the project, set a budget and pick the skills it needs.' },
  { title: 'Compare proposals', text: 'Vetted freelancers send bids with a price and a delivery date.' },
  { title: 'Hire & chat live', text: 'Accept one proposal and talk in real time until it ships.' },
];

const STATS = [
  { value: '100%', label: 'hand-reviewed talent' },
  { value: '1', label: 'accepted proposal per project, guaranteed' },
  { value: 'Live', label: 'messaging with read receipts' },
];

export default function LandingPage() {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');

  const submit = (event: FormEvent) => {
    event.preventDefault();
    navigate('/register', { state: { search } });
  };

  return (
    <div className="landing">
      <header className="landing-nav">
        <Brand />
        <nav>
          <a href="#how">How it works</a>
          <a href="#categories">Categories</a>
          <Link to="/login" className="button ghost sm">Sign in</Link>
          <Link to="/register" className="button sm">Join</Link>
        </nav>
      </header>

      <section className="hero">
        <div className="hero-copy">
          <span className="kicker light"><Icon name="shield" size={14} /> Every freelancer reviewed by a person</span>
          <h1>Find the <em>right hands</em> for the work that matters.</h1>
          <p>Lanceify connects clients with vetted freelancers. Post a project, compare honest bids, hire one, and chat live — all in one place.</p>
          <form className="hero-search" onSubmit={submit} role="search">
            <Icon name="search" size={20} />
            <input
              placeholder="What do you need done?"
              aria-label="What do you need done?"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
            <button className="button">Get started</button>
          </form>
          <div className="popular">
            <span>Popular:</span>
            {POPULAR.map((item) => <Link key={item} to="/register" className="chip ghost">{item}</Link>)}
          </div>
        </div>
        <div className="hero-art" aria-hidden="true">
          <div className="orb" />
          <div className="hero-card one">
            <div className="row-between"><span className="badge open">Open</span><small>2m ago</small></div>
            <strong>Landing page for a fintech startup</strong>
            <div className="tag-list"><span className="tag">React</span><span className="tag">Figma</span></div>
            <div className="row-between"><small>Budget</small><b>$800 – $1,500</b></div>
          </div>
          <div className="hero-card two">
            <div className="row-between"><span className="badge accepted">Hired</span><small>just now</small></div>
            <strong>Sara A. · $1,100 · 7 days</strong>
            <small>“I’ve shipped three fintech sites this year…”</small>
          </div>
          <div className="hero-card three">
            <span className="live-dot on">Live</span>
            <small>New message from your client</small>
          </div>
        </div>
      </section>

      <section className="trust-band">
        {STATS.map((stat) => (
          <div key={stat.label}><strong>{stat.value}</strong><span>{stat.label}</span></div>
        ))}
      </section>

      <section id="categories" className="landing-section">
        <div className="section-head">
          <span className="kicker">Explore</span>
          <h2>Talent for every kind of <em>project</em></h2>
        </div>
        <div className="category-grid">
          {CATEGORIES.map((category) => (
            <Link key={category.name} to="/register" className="category-tile" style={{ ['--hue' as string]: category.hue }}>
              <span className="category-icon"><Icon name={category.icon} size={22} /></span>
              <strong>{category.name}</strong>
              <span className="muted small">Browse freelancers <Icon name="arrow" size={14} /></span>
            </Link>
          ))}
        </div>
      </section>

      <section id="how" className="landing-section">
        <div className="section-head">
          <span className="kicker">How it works</span>
          <h2>From idea to <em>hired</em> in three steps</h2>
        </div>
        <ol className="how-grid">
          {STEPS.map((step, index) => (
            <li key={step.title} className="how-step">
              <span className="how-num">{index + 1}</span>
              <h3>{step.title}</h3>
              <p>{step.text}</p>
            </li>
          ))}
        </ol>
      </section>

      <section className="cta-band">
        <div>
          <h2>Ready to make something <em>good</em>?</h2>
          <p>Join as a client to hire, or as a freelancer to get hired.</p>
        </div>
        <div className="actions">
          <Link to="/register" className="button lime">Create a free account <Icon name="arrow" size={16} /></Link>
          <Link to="/login" className="button ghost light">I have an account</Link>
        </div>
      </section>

      <footer className="footer">
        <Brand />
        <span>Vetted talent · Escrow-protected work · Real-time messaging</span>
        <span>© {new Date().getFullYear()} Lanceify</span>
      </footer>
    </div>
  );
}
