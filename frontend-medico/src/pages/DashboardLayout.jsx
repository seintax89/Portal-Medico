import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext.jsx';
import '../styles/dashboard.css';

const DashboardLayout = ({ titulo, subtitulo, children }) => {
    const { user, logout } = useContext(AuthContext);

    return (
        <div className="dashboard-shell">
            <header className="dashboard-header">
                <div className="dashboard-title">
                    <h1>{titulo}</h1>
                    <p>{subtitulo}</p>
                    {user && <p>Sesión: <strong>{user.email}</strong> · {user.rol}</p>}
                </div>
                <div className="dashboard-actions">
                    <button className="btn-danger" onClick={logout}>Cerrar sesión</button>
                </div>
            </header>
            <main className="dashboard-main">
                {children}
            </main>
        </div>
    );
};

export default DashboardLayout;
