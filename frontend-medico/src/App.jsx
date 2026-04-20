// src/App.jsx  (REEMPLAZA el archivo existente)
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext.jsx';
import PrivateRoute from './components/PrivateRoute.jsx';
import Login from './pages/Login.jsx';
import RegisterPaciente from './pages/RegisterPaciente.jsx';

// ─── Placeholders: reemplaza con tus dashboards reales ──────────────────────
const DashboardPaciente = () => <h2 style={{ padding: 40 }}>Dashboard Paciente 🏥</h2>;
const DashboardMedico   = () => <h2 style={{ padding: 40 }}>Dashboard Médico 🩺</h2>;
const DashboardAdmin    = () => <h2 style={{ padding: 40 }}>Dashboard Administrador ⚙️</h2>;
const NoAutorizado      = () => <h2 style={{ padding: 40, color: 'red' }}>403 – No autorizado</h2>;
// ────────────────────────────────────────────────────────────────────────────

function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    {/* Rutas públicas */}
                    <Route path="/"                   element={<Navigate to="/login" replace />} />
                    <Route path="/login"              element={<Login />} />
                    <Route path="/registro/paciente"  element={<RegisterPaciente />} />
                    <Route path="/no-autorizado"      element={<NoAutorizado />} />

                    {/* Rutas protegidas por rol */}
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

                    {/* Fallback */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
