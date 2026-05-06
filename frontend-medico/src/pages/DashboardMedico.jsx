import { useEffect, useState } from 'react';
import api from '../services/api.js';
import DashboardLayout from './DashboardLayout.jsx';

const DashboardMedico = () => {
    const [medicos, setMedicos] = useState([]);
    const [form, setForm] = useState({ citaId: '', diagnostico: '', observaciones: '' });
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    useEffect(() => {
        api.get('/medicos')
            .then((res) => setMedicos(res.data || []))
            .catch(() => setError('No se pudo cargar el directorio médico.'));
    }, []);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const registrarAtencion = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');
        setCargando(true);

        try {
            await api.post('/historial', {
                citaId: Number(form.citaId),
                diagnostico: form.diagnostico,
                observaciones: form.observaciones
            });
            setMensaje('Evolución clínica guardada exitosamente.');
            setForm({ citaId: '', diagnostico: '', observaciones: '' });
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'No se pudo registrar la evolución clínica.');
        } finally {
            setCargando(false);
        }
    };

    return (
        <DashboardLayout titulo="Panel del Médico" subtitulo="Registra atenciones y consulta información médica básica.">
            {mensaje && <div className="alert ok">{mensaje}</div>}
            {error && <div className="alert error">{error}</div>}

            <div className="grid-2">
                <section className="card">
                    <h2>Registrar atención clínica</h2>
                    <form onSubmit={registrarAtencion}>
                        <div className="form-group">
                            <label>ID de la cita</label>
                            <input
                                type="number"
                                name="citaId"
                                required
                                min="1"
                                value={form.citaId}
                                onChange={handleChange}
                                placeholder="Ej: 12"
                            />
                        </div>
                        <div className="form-group">
                            <label>Diagnóstico</label>
                            <textarea
                                name="diagnostico"
                                required
                                value={form.diagnostico}
                                onChange={handleChange}
                                placeholder="Diagnóstico clínico del paciente"
                            />
                        </div>
                        <div className="form-group">
                            <label>Observaciones</label>
                            <textarea
                                name="observaciones"
                                value={form.observaciones}
                                onChange={handleChange}
                                placeholder="Observaciones adicionales"
                            />
                        </div>
                        <button className="btn-primary" type="submit" disabled={cargando}>
                            {cargando ? 'Guardando...' : 'Guardar evolución'}
                        </button>
                    </form>
                </section>

                <section className="card">
                    <h2>Notas de uso</h2>
                    <p>Este módulo permite registrar la evolución clínica asociada a una cita existente.</p>
                    <p>El backend valida que el usuario autenticado tenga rol de médico.</p>
                    <p>Para una segunda fase se recomienda agregar listado de citas del médico y búsqueda de pacientes.</p>
                </section>
            </div>

            <section className="card" style={{ marginTop: 18 }}>
                <h2>Directorio médico registrado</h2>
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
                    <div className="empty-state">No hay médicos registrados para mostrar.</div>
                )}
            </section>
        </DashboardLayout>
    );
};

export default DashboardMedico;
