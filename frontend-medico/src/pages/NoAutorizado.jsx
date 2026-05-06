import { Link } from 'react-router-dom';
import '../styles/dashboard.css';

const NoAutorizado = () => (
    <div className="dashboard-shell" style={{ display: 'grid', placeItems: 'center', padding: 24 }}>
        <section className="card" style={{ maxWidth: 520, textAlign: 'center' }}>
            <h2>403 – No autorizado</h2>
            <p>No tienes permisos para acceder a este módulo del sistema.</p>
            <Link className="btn btn-secondary" to="/login" style={{ marginTop: 14 }}>Volver al inicio</Link>
        </section>
    </div>
);

export default NoAutorizado;
