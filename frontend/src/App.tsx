import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import AdminApplicationsPage from './pages/AdminApplicationsPage';
import ClientHomePage from './pages/ClientHomePage';
import FreelancerApplicationPage from './pages/FreelancerApplicationPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import { Protected, PublicOnly } from './routes/guards';
import { homeFor } from './routes/paths';

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={<PublicOnly><LoginPage /></PublicOnly>} />
      <Route path="/register" element={<PublicOnly><RegisterPage /></PublicOnly>} />
      <Route path="/freelancer" element={<Protected role="FREELANCER"><FreelancerApplicationPage /></Protected>} />
      <Route path="/client" element={<Protected role="CLIENT"><ClientHomePage /></Protected>} />
      <Route path="/admin/applications" element={<Protected role="ADMIN"><AdminApplicationsPage /></Protected>} />
      <Route path="*" element={<Navigate to={homeFor(user?.role)} replace />} />
    </Routes>
  );
}
