import { useEffect, useState } from 'react';
import api from '../services/api.js';
import DashboardLayout from './DashboardLayout.jsx';

const DashboardPaciente = () => {
    const [perfil, setPerfil] = useState(null);
<<<<<<< HEAD
    const [modoEdicion, setModoEdicion] = useState(false);
    const [formData, setFormData] = useState({
        nombres: '',
        apellidos: '',
        numeroDocumento: '',
        fechaNacimiento: ''
    });

    const [especialidades, setEspecialidades] = useState([]);
    const [especialidadId, setEspecialidadId] = useState('');
    const [fechaHora, setFechaHora] = useState('');
    
    const [citas, setCitas] = useState([]);
    const [medicamentos, setMedicamentos] = useState([]);

=======
    const [medicos, setMedicos] = useState([]);
    const [especialidades, setEspecialidades] = useState([]);
    const [especialidadId, setEspecialidadId] = useState('');
    const [medicoId, setMedicoId] = useState('');
    const [fechaHora, setFechaHora] = useState('');
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
    const [mensaje, setMensaje] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

<<<<<<< HEAD
    const cargarDatos = async () => {
        setError('');
        try {
            const perfilRes = await api.get('/pacientes/mi-perfil');
            setPerfil(perfilRes.data);
            setFormData({
                nombres: perfilRes.data.nombres,
                apellidos: perfilRes.data.apellidos,
                numeroDocumento: perfilRes.data.numeroDocumento,
                fechaNacimiento: perfilRes.data.fechaNacimiento
            });
=======
    const [citas, setCitas] = useState([]);

    const cargarDatos = async () => {
        setError('');
        try {
            const [perfilRes, especialidadesRes] = await Promise.all([
                api.get('/pacientes/mi-perfil'),
                api.get('/especialidades')
            ]);
            setPerfil(perfilRes.data);
            setEspecialidades(especialidadesRes.data || []);
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
        } catch (err) {
            setError(err.response?.data?.mensaje || 'No se pudo cargar la información del paciente.');
        }

        try {
<<<<<<< HEAD
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
=======
            const citasRes = await api.get('/citas/mis-citas');
            setCitas(citasRes.data || []);
        } catch (err) {
            console.error('El endpoint de citas aún no está disponible:', err);
        }
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
    };

    const cargarCitas = async () => {
        try {
            const citasRes = await api.get('/citas/mis-citas');
            setCitas(citasRes.data || []);
        } catch (err) {
            console.error('No se pudieron cargar las citas.', err);
        }
    };

<<<<<<< HEAD
    const cargarMedicamentos = async () => {
        try {
            const medRes = await api.get('/pacientes/mis-medicamentos');
            setMedicamentos(medRes.data || []);
        } catch (err) {
            console.error('No se pudieron cargar los medicamentos.', err);
=======
    const cargarMedicos = async (idEspecialidad = '') => {
        try {
            const url = idEspecialidad ? `/medicos?especialidadId=${idEspecialidad}` : '/medicos';
            const res = await api.get(url);
            setMedicos(res.data || []);
        } catch {
            setError('No se pudo cargar el directorio médico.');
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
        }
    };

    useEffect(() => {
        cargarDatos();
<<<<<<< HEAD
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
            setMensaje('Perfil actualizado exitosamente.');
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'Error al actualizar el perfil.');
        } finally {
            setCargando(false);
        }
=======
        cargarMedicos();
    }, []);

    const handleEspecialidad = (e) => {
        const value = e.target.value;
        setEspecialidadId(value);
        setMedicoId('');
        cargarMedicos(value);
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
    };

    const agendarCita = async (e) => {
        e.preventDefault();
        setMensaje('');
        setError('');

        if (!perfil?.id) {
            setError('No se encontró el ID del paciente autenticado.');
            return;
        }
<<<<<<< HEAD
        if (!especialidadId) {
            setError('Debe seleccionar una especialidad.');
            return;
        }
=======
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99

        setCargando(true);
        try {
            const payload = {
                pacienteId: Number(perfil.id),
<<<<<<< HEAD
                especialidadId: Number(especialidadId),
=======
                medicoId: Number(medicoId),
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
                fechaHora
            };
            const res = await api.post('/citas', payload);
            setMensaje(`Cita programada exitosamente con ${res.data.nombreMedico} para ${new Date(res.data.fechaHora).toLocaleString()}.`);
<<<<<<< HEAD
            setFechaHora('');
            cargarCitas();
            // Actualizar especialidades por si se consumió una orden
            const especialidadesRes = await api.get('/especialidades/disponibles');
            setEspecialidades(especialidadesRes.data || []);
=======
            setMedicoId('');
            setFechaHora('');
            cargarCitas();
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
        } catch (err) {
            const msg = typeof err.response?.data === 'string' ? err.response.data : err.response?.data?.mensaje;
            setError(msg || 'No se pudo agendar la cita.');
        } finally {
            setCargando(false);
        }
    };

<<<<<<< HEAD
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
=======
    return (
        <DashboardLayout titulo="Panel del Paciente" subtitulo="Consulta tu perfil y agenda citas médicas.">
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
            {mensaje && <div className="alert ok">{mensaje}</div>}
            {error && <div className="alert error">{error}</div>}

            <div className="grid-2">
                <section className="card">
<<<<<<< HEAD
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
                                <input required value={formData.nombres} onChange={e => setFormData({...formData, nombres: e.target.value})} />
                            </div>
                            <div className="form-group">
                                <label>Apellidos</label>
                                <input required value={formData.apellidos} onChange={e => setFormData({...formData, apellidos: e.target.value})} />
                            </div>
                            <div className="form-group">
                                <label>Documento</label>
                                <input required value={formData.numeroDocumento} onChange={e => setFormData({...formData, numeroDocumento: e.target.value})} />
                            </div>
                            <div className="form-group">
                                <label>Fecha de Nacimiento</label>
                                <input type="date" required value={formData.fechaNacimiento} onChange={e => setFormData({...formData, fechaNacimiento: e.target.value})} />
                            </div>
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <button type="submit" className="btn-primary" disabled={cargando}>Guardar</button>
                                <button type="button" className="btn-secondary" onClick={() => setModoEdicion(false)}>Cancelar</button>
                            </div>
                        </form>
                    ) : (
=======
                    <h2>Mi perfil</h2>
                    {perfil ? (
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
                        <div className="list">
                            <p><strong>Nombre:</strong> {perfil.nombres} {perfil.apellidos}</p>
                            <p><strong>Correo:</strong> {perfil.email}</p>
                            <p><strong>Documento:</strong> {perfil.numeroDocumento}</p>
                            <p><strong>Fecha de nacimiento:</strong> {perfil.fechaNacimiento}</p>
                            <p><strong>Tipo de paciente:</strong> <span className="badge">{perfil.tipoPaciente}</span></p>
                        </div>
<<<<<<< HEAD
=======
                    ) : (
                        <div className="empty-state">Cargando perfil del paciente...</div>
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
                    )}
                </section>

                <section className="card">
                    <h2>Agendar cita</h2>
<<<<<<< HEAD
                    <p style={{marginBottom: '1rem', color: '#666'}}>El sistema buscará automáticamente un médico disponible.</p>
                    <form onSubmit={agendarCita}>
                        <div className="form-group">
                            <label>Especialidad</label>
                            <select value={especialidadId} onChange={e => setEspecialidadId(e.target.value)} required>
                                {especialidades.length === 0 && <option value="">Sin especialidades disponibles</option>}
=======
                    <form onSubmit={agendarCita}>
                        <div className="form-group">
                            <label>Especialidad</label>
                            <select value={especialidadId} onChange={handleEspecialidad}>
                                <option value="">Todas las especialidades</option>
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
                                {especialidades.map((esp) => (
                                    <option key={esp.id} value={esp.id}>{esp.nombre}</option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
<<<<<<< HEAD
=======
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
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
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

<<<<<<< HEAD
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
=======
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

            <section className="card" style={{ marginTop: 18 }}>
                <h2>Mis citas</h2>
                {citas.length > 0 ? (
                    <div className="list">
                        {citas.map((cita) => (
                            <div className="list-item" key={cita.idCita}>
                                <h3>{cita.nombreMedico}</h3>
                                <p><strong>Especialidad:</strong> {cita.especialidad}</p>
                                <p><strong>Fecha y hora:</strong> {new Date(cita.fechaHora).toLocaleString()}</p>
                                <p><strong>Estado:</strong> <span className="badge">{cita.estado}</span></p>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div className="empty-state">No tienes citas programadas.</div>
                )}
            </section>
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
        </DashboardLayout>
    );
};

export default DashboardPaciente;
