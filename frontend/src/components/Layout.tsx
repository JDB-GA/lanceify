import { useEffect, useRef, useState, type FormEvent, type ReactNode } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useRealtime } from '../context/RealtimeContext';
import type { Role } from '../types';
import Avatar from './Avatar';
import Brand from './Brand';
import Icon, { type IconName } from './Icon';

const ROLE_LABELS: Record<Role, string> = { ADMIN: 'Admin', CLIENT: 'Client', FREELANCER: 'Freelancer' };

const NAV: Record<Role, { to: string; label: string; icon: IconName; end?: boolean }[]> = {
  FREELANCER: [
    { to: '/freelancer', label: 'Dashboard', icon: 'grid', end: true },
    { to: '/projects', label: 'Find work', icon: 'search' },
    { to: '/freelancer/proposals', label: 'Proposals', icon: 'file' },
    { to: '/freelancer/profile', label: 'My profile', icon: 'user' },
  ],
  CLIENT: [
    { to: '/client', label: 'Dashboard', icon: 'grid', end: true },
    { to: '/client/projects/new', label: 'Post a project', icon: 'plus' },
  ],
  ADMIN: [
    { to: '/admin/applications', label: 'Applications', icon: 'shield' },
    { to: '/admin/catalog', label: 'Catalog', icon: 'layers' },
  ],
};

export default function Layout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const { unread, live } = useRealtime();
  const navigate = useNavigate();
  const location = useLocation();
  const [search, setSearch] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => setMenuOpen(false), [location.pathname]);

  useEffect(() => {
    if (!menuOpen) return;
    const close = (event: MouseEvent) => {
      if (!menuRef.current?.contains(event.target as Node)) setMenuOpen(false);
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [menuOpen]);

  if (!user) return <>{children}</>;

  const fullName = `${user.firstName} ${user.lastName}`;
  const chat = user.role !== 'ADMIN';
  const submitSearch = (event: FormEvent) => {
    event.preventDefault();
    navigate(`/projects?q=${encodeURIComponent(search.trim())}`);
  };

  return (
    <div className="shell">
      <header className="topbar">
        <div className="topbar-inner">
          <Brand to="/" />
          {user.role === 'FREELANCER' && (
            <form className="topbar-search" onSubmit={submitSearch} role="search">
              <Icon name="search" />
              <input
                type="search"
                placeholder="Search projects, skills, keywords…"
                aria-label="Search projects"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <button className="search-go" aria-label="Search"><Icon name="arrow" size={16} /></button>
            </form>
          )}
          <div className="topbar-actions">
            {chat && (
              <NavLink to="/messages" className="icon-button" aria-label={`Messages${unread ? `, ${unread} unread` : ''}`}>
                <Icon name="chat" size={20} />
                {unread > 0 && <span className="count-bubble">{unread > 99 ? '99+' : unread}</span>}
              </NavLink>
            )}
            <div className="menu" ref={menuRef}>
              <button className="user-chip" aria-expanded={menuOpen} onClick={() => setMenuOpen(!menuOpen)}>
                <Avatar name={fullName} seed={user.userId} size="sm" online={live} />
                <span className="user-meta">
                  <strong>{user.firstName}</strong>
                  <small>{ROLE_LABELS[user.role]}</small>
                </span>
              </button>
              {menuOpen && (
                <div className="menu-panel" role="menu">
                  <div className="menu-head">
                    <strong>{fullName}</strong>
                    <small>{user.email}</small>
                  </div>
                  {NAV[user.role].map((item) => (
                    <Link key={item.to} to={item.to} role="menuitem"><Icon name={item.icon} />{item.label}</Link>
                  ))}
                  {chat && <Link to="/messages" role="menuitem"><Icon name="chat" />Messages</Link>}
                  <button role="menuitem" onClick={logout}><Icon name="logout" />Log out</button>
                </div>
              )}
            </div>
          </div>
        </div>
        <nav className="subnav" aria-label="Main">
          <div className="subnav-inner">
            {NAV[user.role].map((item) => (
              <NavLink key={item.to} to={item.to} end={item.end} className="subnav-link">
                <Icon name={item.icon} size={16} />{item.label}
              </NavLink>
            ))}
            {chat && (
              <NavLink to="/messages" className="subnav-link">
                <Icon name="chat" size={16} />Messages
                {unread > 0 && <span className="pill-count">{unread}</span>}
              </NavLink>
            )}
            <span className={live ? 'live-dot on' : 'live-dot'} title={live ? 'Live updates on' : 'Reconnecting…'}>
              {live ? 'Live' : 'Offline'}
            </span>
          </div>
        </nav>
      </header>
      <main className="page">{children}</main>
      <footer className="footer">
        <Brand to="/" />
        <span>Vetted talent · Escrow-protected work · Real-time messaging</span>
        <span>© {new Date().getFullYear()} Lanceify</span>
      </footer>
    </div>
  );
}
