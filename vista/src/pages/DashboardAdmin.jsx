import { useEffect, useState } from 'react';
import api from '../services/api.js';
import DashboardLayout from './DashboardLayout.jsx';

const DashboardAdmin = () => {
    const [especialidades, setEspecialidades] = useState([]);
    const [medicos, setMedicos] = useState([]);
    const [form, setForm] = useState({
        email: '',
        password: '',
        nombres: '',
        apellidos: '',
        registroMedico: '',
        especialidadId: ''
    });
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const cargarDatos = async () => {
        try {
            const [espRes, medRes] = await Promise.all([
                api.get('/especialidades'),
                api.get('/medicos')
            ]);
            setEspecialidades(espRes.data || []);
            setMedicos(medRes.data || []);
        } catch {
            setError('No se pudieron cargar los datos administrativos.');
        }
    };

    useEffect(() => {
        cargarDatos();
    }, []);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const registrarMedico = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');
        setCargando(true);

        try {
            await api.post('/auth/registro/medico', {
                ...form,
                especialidadId: Number(form.especialidadId)
            });
            setMensaje('Médico registrado exitosamente.');
            setForm({ email: '', password: '', nombres: '', apellidos: '', registroMedico: '', especialidadId: '' });
            cargarDatos();
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'No se pudo registrar el médico.');
        } finally {
            setCargando(false);
        }
    };

    return (
        <DashboardLayout titulo="Panel de Administración" subtitulo="Gestión básica de médicos y catálogos del sistema.">
            {mensaje && <div className="alert ok">{mensaje}</div>}
            {error && <div className="alert error">{error}</div>}

            <div className="grid-3">
                <section className="card stat-card">
                    <h3>Médicos registrados</h3>
                    <span className="stat-number">{medicos.length}</span>
                    <p>Total consultado desde el directorio médico.</p>
                </section>
                <section className="card stat-card">
                    <h3>Especialidades</h3>
                    <span className="stat-number">{especialidades.length}</span>
                    <p>Catálogo disponible para asignar médicos.</p>
                </section>
                <section className="card stat-card">
                    <h3>Estado</h3>
                    <span className="stat-number">OK</span>
                    <p>Backend conectado y endpoints principales activos.</p>
                </section>
            </div>

            <div className="grid-2" style={{ marginTop: 18 }}>
                <section className="card">
                    <h2>Registrar médico</h2>
                    <form onSubmit={registrarMedico} className="form-grid">
                        <div className="form-group">
                            <label>Nombres</label>
                            <input name="nombres" required value={form.nombres} onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Apellidos</label>
                            <input name="apellidos" required value={form.apellidos} onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Correo electrónico</label>
                            <input type="email" name="email" required value={form.email} onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Contraseña</label>
                            <input type="password" name="password" required minLength="6" value={form.password} onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Registro médico</label>
                            <input name="registroMedico" required value={form.registroMedico} onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label>Especialidad</label>
                            <select name="especialidadId" required value={form.especialidadId} onChange={handleChange}>
                                <option value="">Seleccione...</option>
                                {especialidades.map((esp) => (
                                    <option key={esp.id} value={esp.id}>{esp.nombre}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-group full">
                            <button className="btn-primary" type="submit" disabled={cargando}>
                                {cargando ? 'Registrando...' : 'Registrar médico'}
                            </button>
                        </div>
                    </form>
                </section>

                <section className="card">
                    <h2>Médicos registrados</h2>
                    {medicos.length > 0 ? (
                        <div className="list">
                            {medicos.map((medico) => (
                                <div className="list-item" key={medico.id}>
                                    <h3>{medico.nombreCompleto}</h3>
                                    <p><strong>Especialidad:</strong> {medico.especialidad}</p>
                                    <p><strong>Registro médico:</strong> {medico.registroMedico}</p>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">No hay médicos registrados.</div>
                    )}
                </section>
            </div>
        </DashboardLayout>
    );
};

export default DashboardAdmin;
