import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import AdminApplicationsPage from './pages/AdminApplicationsPage';
import AdminCatalogPage from './pages/AdminCatalogPage';
import ClientHomePage from './pages/ClientHomePage';
import ExploreProjectsPage from './pages/ExploreProjectsPage';
import FreelancerApplicationPage from './pages/FreelancerApplicationPage';
import FreelancerHomePage from './pages/FreelancerHomePage';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import MessagesPage from './pages/MessagesPage';
import MyProposalsPage from './pages/MyProposalsPage';
import PostProjectPage from './pages/PostProjectPage';
import ProjectDetailPage from './pages/ProjectDetailPage';
import RegisterPage from './pages/RegisterPage';
import { Protected, PublicOnly } from './routes/guards';
import { homeFor } from './routes/paths';

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/" element={<PublicOnly><LandingPage /></PublicOnly>} />
      <Route path="/login" element={<PublicOnly><LoginPage /></PublicOnly>} />
      <Route path="/register" element={<PublicOnly><RegisterPage /></PublicOnly>} />
      <Route path="/freelancer" element={<Protected role="FREELANCER"><FreelancerHomePage /></Protected>} />
      <Route path="/freelancer/profile" element={<Protected role="FREELANCER"><FreelancerApplicationPage /></Protected>} />
      <Route path="/freelancer/proposals" element={<Protected role="FREELANCER"><MyProposalsPage /></Protected>} />
      <Route path="/projects" element={<Protected role="FREELANCER"><ExploreProjectsPage /></Protected>} />
      <Route path="/projects/:id" element={<Protected role={['FREELANCER', 'CLIENT']}><ProjectDetailPage /></Protected>} />
      <Route path="/client" element={<Protected role="CLIENT"><ClientHomePage /></Protected>} />
      <Route path="/client/projects/new" element={<Protected role="CLIENT"><PostProjectPage /></Protected>} />
      <Route path="/admin/applications" element={<Protected role="ADMIN"><AdminApplicationsPage /></Protected>} />
      <Route path="/admin/catalog" element={<Protected role="ADMIN"><AdminCatalogPage /></Protected>} />
      <Route path="/messages" element={<Protected><MessagesPage /></Protected>} />
      <Route path="/messages/:id" element={<Protected><MessagesPage /></Protected>} />
      <Route path="*" element={<Navigate to={user ? homeFor(user.role) : '/'} replace />} />
    </Routes>
  );
}
