import { useEffect, useState } from 'react';
import api from '../services/api.js';
import DashboardLayout from './DashboardLayout.jsx';

const DashboardPaciente = () => {
    const [perfil, setPerfil] = useState(null);
    const [medicos, setMedicos] = useState([]);
    const [especialidades, setEspecialidades] = useState([]);
    const [especialidadId, setEspecialidadId] = useState('');
    const [medicoId, setMedicoId] = useState('');
    const [fechaHora, setFechaHora] = useState('');
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const cargarDatos = async () => {
        setError('');
        try {
            const [perfilRes, especialidadesRes] = await Promise.all([
                api.get('/pacientes/mi-perfil'),
                api.get('/especialidades')
            ]);
            setPerfil(perfilRes.data);
            setEspecialidades(especialidadesRes.data || []);
        } catch (err) {
            setError(err.response?.data?.mensaje || 'No se pudo cargar la información del paciente.');
        }
    };

    const cargarMedicos = async (idEspecialidad = '') => {
        try {
            const url = idEspecialidad ? `/medicos?especialidadId=${idEspecialidad}` : '/medicos';
            const res = await api.get(url);
            setMedicos(res.data || []);
        } catch {
            setError('No se pudo cargar el directorio médico.');
        }
    };

    useEffect(() => {
        cargarDatos();
        cargarMedicos();
    }, []);

    const handleEspecialidad = (e) => {
        const value = e.target.value;
        setEspecialidadId(value);
        setMedicoId('');
        cargarMedicos(value);
    };

    const agendarCita = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');

        if (!perfil?.id) {
            setError('No se encontró el ID del paciente autenticado.');
            return;
        }

        setCargando(true);
        try {
            const payload = {
                pacienteId: Number(perfil.id),
                medicoId: Number(medicoId),
                fechaHora
            };
            const res = await api.post('/citas', payload);
            setMensaje(`Cita programada exitosamente con ${res.data.nombreMedico} para ${new Date(res.data.fechaHora).toLocaleString()}.`);
            setMedicoId('');
            setFechaHora('');
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'No se pudo agendar la cita.');
        } finally {
            setCargando(false);
        }
    };

    return (
        <DashboardLayout titulo="Panel del Paciente" subtitulo="Consulta tu perfil y agenda citas médicas.">
            {mensaje && <div className="alert ok">{mensaje}</div>}
            {error && <div className="alert error">{error}</div>}

            <div className="grid-2">
                <section className="card">
                    <h2>Mi perfil</h2>
                    {perfil ? (
                        <div className="list">
                            <p><strong>Nombre:</strong> {perfil.nombres} {perfil.apellidos}</p>
                            <p><strong>Correo:</strong> {perfil.email}</p>
                            <p><strong>Documento:</strong> {perfil.numeroDocumento}</p>
                            <p><strong>Fecha de nacimiento:</strong> {perfil.fechaNacimiento}</p>
                            <p><strong>Tipo de paciente:</strong> <span className="badge">{perfil.tipoPaciente}</span></p>
                        </div>
                    ) : (
                        <div className="empty-state">Cargando perfil del paciente...</div>
                    )}
                </section>

                <section className="card">
                    <h2>Agendar cita</h2>
                    <form onSubmit={agendarCita}>
                        <div className="form-group">
                            <label>Especialidad</label>
                            <select value={especialidadId} onChange={handleEspecialidad}>
                                <option value="">Todas las especialidades</option>
                                {especialidades.map((esp) => (
                                    <option key={esp.id} value={esp.id}>{esp.nombre}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Médico</label>
                            <select required value={medicoId} onChange={(e) => setMedicoId(e.target.value)}>
                                <option value="">Seleccione un médico</option>
                                {medicos.map((medico) => (
                                    <option key={medico.id} value={medico.id}>
                                        {medico.nombreCompleto} - {medico.especialidad}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Fecha y hora</label>
                            <input
                                type="datetime-local"
                                required
                                value={fechaHora}
                                onChange={(e) => setFechaHora(e.target.value)}
                            />
                        </div>

                        <button className="btn-primary" type="submit" disabled={cargando}>
                            {cargando ? 'Agendando...' : 'Agendar cita'}
                        </button>
                    </form>
                </section>
            </div>

            <section className="card" style={{ marginTop: 18 }}>
                <h2>Directorio médico</h2>
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
                    <div className="empty-state">No hay médicos disponibles para el filtro seleccionado.</div>
                )}
            </section>
        </DashboardLayout>
    );
};

export default DashboardPaciente;
