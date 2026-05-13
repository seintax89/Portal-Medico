import { useEffect, useState } from 'react';
import api from '../services/api.js';
import DashboardLayout from './DashboardLayout.jsx';

const DashboardPaciente = () => {
    const [perfil, setPerfil] = useState(null);
    const [modoEdicion, setModoEdicion] = useState(false);
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });

    const [especialidades, setEspecialidades] = useState([]);
    const [especialidadId, setEspecialidadId] = useState('');
    const [fechaHora, setFechaHora] = useState('');
    
    const [citas, setCitas] = useState([]);
    const [medicamentos, setMedicamentos] = useState([]);

    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const cargarDatos = async () => {
        setError('');
        try {
            const perfilRes = await api.get('/pacientes/mi-perfil');
            setPerfil(perfilRes.data);
            setFormData({
                email: perfilRes.data.email,
                password: ''
            });
        } catch (err) {
            setError(err.response?.data?.mensaje || 'No se pudo cargar la información del paciente.');
        }

        try {
            const especialidadesRes = await api.get('/especialidades/disponibles');
            setEspecialidades(especialidadesRes.data || []);
            if (especialidadesRes.data && especialidadesRes.data.length > 0) {
                setEspecialidadId(especialidadesRes.data[0].id);
            }
        } catch (err) {
            console.error('No se pudieron cargar las especialidades:', err);
        }

        cargarCitas();
        cargarMedicamentos();
    };

    const cargarCitas = async () => {
        try {
            const citasRes = await api.get('/citas/mis-citas');
            setCitas(citasRes.data || []);
        } catch (err) {
            console.error('No se pudieron cargar las citas.', err);
        }
    };

    const cargarMedicamentos = async () => {
        try {
            const medRes = await api.get('/pacientes/mis-medicamentos');
            setMedicamentos(medRes.data || []);
        } catch (err) {
            console.error('No se pudieron cargar los medicamentos.', err);
        }
    };

    useEffect(() => {
        cargarDatos();
    }, []);

    const handleActualizarPerfil = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');
        setCargando(true);
        try {
            const res = await api.put('/pacientes/mi-perfil', formData);
            setPerfil(res.data);
            setModoEdicion(false);
            setMensaje('Perfil actualizado exitosamente. Si cambiaste tus credenciales, deberás iniciar sesión nuevamente la próxima vez.');
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'Error al actualizar el perfil.');
        } finally {
            setCargando(false);
        }
    };

    const agendarCita = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');

        if (!perfil?.id) {
            setError('No se encontró el ID del paciente autenticado.');
            return;
        }
        if (!especialidadId) {
            setError('Debe seleccionar una especialidad.');
            return;
        }

        setCargando(true);
        try {
            const payload = {
                pacienteId: Number(perfil.id),
                especialidadId: Number(especialidadId),
                fechaHora
            };
            const res = await api.post('/citas', payload);
            setMensaje(`Cita programada exitosamente con ${res.data.nombreMedico} para ${new Date(res.data.fechaHora).toLocaleString()}.`);
            setFechaHora('');
            cargarCitas();
            // Actualizar especialidades por si se consumió una orden
            const especialidadesRes = await api.get('/especialidades/disponibles');
            setEspecialidades(especialidadesRes.data || []);
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'No se pudo agendar la cita.');
        } finally {
            setCargando(false);
        }
    };

    const cancelarCita = async (idCita) => {
        if (!window.confirm("¿Está seguro que desea cancelar esta cita?")) return;
        setMensaje('');
        setError('');
        try {
            await api.put(`/citas/${idCita}/cancelar`);
            setMensaje('Cita cancelada exitosamente.');
            cargarCitas();
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'No se pudo cancelar la cita.');
        }
    }

    return (
        <DashboardLayout titulo="Panel del Paciente" subtitulo="Gestiona tu salud de forma integral.">
            {mensaje && <div className="alert ok">{mensaje}</div>}
            {error && <div className="alert error">{error}</div>}

            <div className="grid-2">
                <section className="card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h2>Mi perfil</h2>
                        {!modoEdicion && (
                            <button className="btn-secondary" onClick={() => setModoEdicion(true)}>Editar</button>
                        )}
                    </div>

                    {!perfil ? (
                        <div className="empty-state">Cargando perfil del paciente...</div>
                    ) : modoEdicion ? (
                        <form onSubmit={handleActualizarPerfil} style={{ marginTop: '1rem' }}>
                            <div className="form-group">
                                <label>Nombres</label>
                                <input disabled value={perfil.nombres} />
                            </div>
                            <div className="form-group">
                                <label>Apellidos</label>
                                <input disabled value={perfil.apellidos} />
                            </div>
                            <div className="form-group">
                                <label>Documento</label>
                                <input disabled value={perfil.numeroDocumento} />
                            </div>
                            <div className="form-group">
                                <label>Fecha de Nacimiento</label>
                                <input type="date" disabled value={perfil.fechaNacimiento} />
                            </div>
                            <div className="form-group">
                                <label>Correo Electrónico (Editable)</label>
                                <input type="email" required value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} />
                            </div>
                            <div className="form-group">
                                <label>Nueva Contraseña (Editable)</label>
                                <input type="password" placeholder="Dejar en blanco para no cambiar" value={formData.password} onChange={e => setFormData({...formData, password: e.target.value})} />
                            </div>
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <button type="submit" className="btn-primary" disabled={cargando}>Guardar</button>
                                <button type="button" className="btn-secondary" onClick={() => setModoEdicion(false)}>Cancelar</button>
                            </div>
                        </form>
                    ) : (
                        <div className="list">
                            <p><strong>Nombre:</strong> {perfil.nombres} {perfil.apellidos}</p>
                            <p><strong>Correo:</strong> {perfil.email}</p>
                            <p><strong>Documento:</strong> {perfil.numeroDocumento}</p>
                            <p><strong>Fecha de nacimiento:</strong> {perfil.fechaNacimiento}</p>
                            <p><strong>Tipo de paciente:</strong> <span className="badge">{perfil.tipoPaciente}</span></p>
                        </div>
                    )}
                </section>

                <section className="card">
                    <h2>Agendar cita</h2>
                    <p style={{marginBottom: '1rem', color: '#666'}}>El sistema buscará automáticamente un médico disponible.</p>
                    <form onSubmit={agendarCita}>
                        <div className="form-group">
                            <label>Especialidad</label>
                            <select value={especialidadId} onChange={e => setEspecialidadId(e.target.value)} required>
                                {especialidades.length === 0 && <option value="">Sin especialidades disponibles</option>}
                                {especialidades.map((esp) => (
                                    <option key={esp.id} value={esp.id}>{esp.nombre}</option>
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

            <div className="grid-2" style={{ marginTop: 18 }}>
                <section className="card">
                    <h2>Mis citas</h2>
                    {citas.length > 0 ? (
                        <div className="list">
                            {citas.map((cita) => (
                                <div className="list-item" key={cita.idCita}>
                                    <h3>{cita.nombreMedico}</h3>
                                    <p><strong>Especialidad:</strong> {cita.especialidad}</p>
                                    <p><strong>Fecha y hora:</strong> {new Date(cita.fechaHora).toLocaleString()}</p>
                                    <p>
                                        <strong>Estado: </strong>
                                        <span className="badge" style={{ backgroundColor: cita.estado === 'CANCELADA' ? '#fee2e2' : '#dcfce7', color: cita.estado === 'CANCELADA' ? '#991b1b' : '#166534'}}>
                                            {cita.estado}
                                        </span>
                                    </p>
                                    {cita.estado === 'PROGRAMADA' && (
                                        <button 
                                            className="btn-secondary" 
                                            style={{marginTop: '10px', borderColor: '#ef4444', color: '#ef4444'}}
                                            onClick={() => cancelarCita(cita.idCita)}
                                        >
                                            Cancelar Cita
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">No tienes citas programadas.</div>
                    )}
                </section>

                <section className="card">
                    <h2>Mis medicamentos recetados</h2>
                    {medicamentos.length > 0 ? (
                        <div className="list">
                            {medicamentos.map((med, index) => (
                                <div className="list-item" key={index}>
                                    <h3>{med.medicamentoNombre}</h3>
                                    <p><strong>Dosis:</strong> {med.dosis}</p>
                                    <p><strong>Frecuencia:</strong> {med.frecuencia}</p>
                                    <p><strong>Duración:</strong> {med.duracionDias} días</p>
                                    <p><strong>Recetado por:</strong> {med.medicoNombre}</p>
                                    <p><strong>Fecha:</strong> {new Date(med.fechaReceta).toLocaleDateString()}</p>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="empty-state">No tienes medicamentos recetados.</div>
                    )}
                </section>
            </div>
        </DashboardLayout>
    );
};

export default DashboardPaciente;
