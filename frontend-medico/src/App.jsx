import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import PrivateRoute from './components/PrivateRoute.jsx';
import Login from './pages/Login.jsx';
import RegisterPaciente from './pages/RegisterPaciente.jsx';
import DashboardPaciente from './pages/DashboardPaciente.jsx';
import DashboardMedico from './pages/DashboardMedico.jsx';
import DashboardAdmin from './pages/DashboardAdmin.jsx';
import NoAutorizado from './pages/NoAutorizado.jsx';

function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/" element={<Navigate to="/login" replace />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/registro/paciente" element={<RegisterPaciente />} />
                    <Route path="/no-autorizado" element={<NoAutorizado />} />

                    <Route path="/dashboard/paciente" element={
                        <PrivateRoute rolesPermitidos={['ROLE_PACIENTE']}>
                            <DashboardPaciente />
                        </PrivateRoute>
                    } />

                    <Route path="/dashboard/medico" element={
                        <PrivateRoute rolesPermitidos={['ROLE_MEDICO']}>
                            <DashboardMedico />
                        </PrivateRoute>
                    } />

                    <Route path="/dashboard/admin" element={
                        <PrivateRoute rolesPermitidos={['ROLE_ADMIN']}>
                            <DashboardAdmin />
                        </PrivateRoute>
                    } />

                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
