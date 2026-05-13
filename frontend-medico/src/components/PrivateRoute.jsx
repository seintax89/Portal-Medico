// src/components/PrivateRoute.jsx  (ARCHIVO NUEVO)
import { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext.jsx';

/**
 * Protege rutas que requieren autenticación y, opcionalmente, un rol específico.
 *
 * Uso:
 *   <Route path="/dashboard/paciente" element={
 *     <PrivateRoute rolesPermitidos={['ROLE_PACIENTE']}>
 *       <DashboardPaciente />
 *     </PrivateRoute>
 *   } />
 */
const PrivateRoute = ({ children, rolesPermitidos = [] }) => {
    const { user, loading } = useContext(AuthContext);

    // Mientras se verifica el token guardado, no renderizamos nada
    if (loading) return null;

    // Si no hay sesión, redirigir al login
    if (!user) return <Navigate to="/login" replace />;

    // Si se especificaron roles y el usuario no tiene el rol requerido
    if (rolesPermitidos.length > 0 && !rolesPermitidos.includes(user.rol)) {
        return <Navigate to="/no-autorizado" replace />;
    }

    return children;
};

export default PrivateRoute;
