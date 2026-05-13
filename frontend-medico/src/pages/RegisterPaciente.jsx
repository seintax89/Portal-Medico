// src/pages/RegisterPaciente.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api.js';

const RegisterPaciente = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmarPassword: '',
        nombres: '',
        apellidos: '',
        tipoDocumento: 'CC',
        numeroDocumento: '',
        fechaNacimiento: '',
    });

    const [error, setError] = useState('');
    const [exito, setExito] = useState('');
    const [cargando, setCargando] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setExito('');

        // Validación local de contraseñas
        if (formData.password !== formData.confirmarPassword) {
            setError('Las contraseñas no coinciden.');
            return;
        }

        if (formData.password.length < 6) {
            setError('La contraseña debe tener al menos 6 caracteres.');
            return;
        }

        setCargando(true);

        try {
            // El campo confirmarPassword NO se envía al backend
            const { confirmarPassword, ...datosParaEnviar } = formData;

            await api.post('/auth/registro/paciente', datosParaEnviar);

            setExito('¡Registro exitoso! Ahora puedes iniciar sesión.');
            setTimeout(() => navigate('/login'), 2500);
        } catch (err) {
            const mensaje = err.response?.data?.mensaje || 'Ocurrió un error en el registro. Intente de nuevo.';
            setError(mensaje);
        } finally {
            setCargando(false);
        }
    };

    const estiloContenedor = {
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f0f4f8',
        padding: '20px',
    };

    const estiloTarjeta = {
        backgroundColor: '#ffffff',
        padding: '40px',
        borderRadius: '12px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '480px',
    };

    const estiloInput = {
        width: '100%',
        padding: '10px 12px',
        marginTop: '5px',
        border: '1px solid #ccc',
        borderRadius: '6px',
        fontSize: '14px',
        boxSizing: 'border-box',
    };

    const estiloLabel = {
        display: 'block',
        fontWeight: '600',
        fontSize: '13px',
        color: '#444',
        marginBottom: '2px',
    };

    const estiloGrupo = {
        marginBottom: '14px',
    };

    const estiloBoton = {
        width: '100%',
        padding: '12px',
        backgroundColor: cargando ? '#7aadde' : '#0056b3',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        fontSize: '15px',
        fontWeight: '600',
        cursor: cargando ? 'not-allowed' : 'pointer',
        marginTop: '8px',
        transition: 'background-color 0.2s',
    };

    return (
        <div style={estiloContenedor}>
            <div style={estiloTarjeta}>
                <h2 style={{ textAlign: 'center', color: '#0056b3', marginBottom: '6px' }}>
                    Portal Médico EPS
                </h2>
                <p style={{ textAlign: 'center', color: '#666', marginBottom: '24px', fontSize: '14px' }}>
                    Crea tu cuenta como Paciente
                </p>

                {error && (
                    <p style={{ color: '#c0392b', backgroundColor: '#fdecea', padding: '10px', borderRadius: '6px', fontSize: '13px', marginBottom: '12px' }}>
                        ⚠️ {error}
                    </p>
                )}
                {exito && (
                    <p style={{ color: '#1e8449', backgroundColor: '#eafaf1', padding: '10px', borderRadius: '6px', fontSize: '13px', marginBottom: '12px' }}>
                        ✅ {exito}
                    </p>
                )}

                <form onSubmit={handleSubmit}>
                    {/* Datos personales */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                        <div style={estiloGrupo}>
                            <label style={estiloLabel}>Nombres *</label>
                            <input
                                type="text"
                                name="nombres"
                                required
                                value={formData.nombres}
                                onChange={handleChange}
                                placeholder="Ej: Juan Carlos"
                                style={estiloInput}
                            />
                        </div>
                        <div style={estiloGrupo}>
                            <label style={estiloLabel}>Apellidos *</label>
                            <input
                                type="text"
                                name="apellidos"
                                required
                                value={formData.apellidos}
                                onChange={handleChange}
                                placeholder="Ej: Pérez García"
                                style={estiloInput}
                            />
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '12px' }}>
                        <div style={estiloGrupo}>
                            <label style={estiloLabel}>Tipo de Documento *</label>
                            <select
                                name="tipoDocumento"
                                value={formData.tipoDocumento}
                                onChange={handleChange}
                                style={estiloInput}
                            >
                                <option value="CC">Cédula (CC)</option>
                                <option value="TI">Tarjeta de Identidad (TI)</option>
                                <option value="CE">Cédula Extranjería (CE)</option>
                                <option value="PA">Pasaporte (PA)</option>
                            </select>
                        </div>
                        <div style={estiloGrupo}>
                            <label style={estiloLabel}>Número de Documento *</label>
                            <input
                                type="text"
                                name="numeroDocumento"
                                required
                                value={formData.numeroDocumento}
                                onChange={handleChange}
                                placeholder="Ej: 1023456789"
                                style={estiloInput}
                            />
                        </div>
                    </div>

                    <div style={estiloGrupo}>
                        <label style={estiloLabel}>Fecha de Nacimiento *</label>
                        <input
                            type="date"
                            name="fechaNacimiento"
                            required
                            value={formData.fechaNacimiento}
                            onChange={handleChange}
                            max={new Date().toISOString().split('T')[0]}
                            style={estiloInput}
                        />
                    </div>

                    {/* Separador */}
                    <hr style={{ border: 'none', borderTop: '1px solid #eee', margin: '16px 0' }} />

                    {/* Credenciales de acceso */}
                    <div style={estiloGrupo}>
                        <label style={estiloLabel}>Correo Electrónico *</label>
                        <input
                            type="email"
                            name="email"
                            required
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="correo@ejemplo.com"
                            style={estiloInput}
                        />
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                        <div style={estiloGrupo}>
                            <label style={estiloLabel}>Contraseña *</label>
                            <input
                                type="password"
                                name="password"
                                required
                                value={formData.password}
                                onChange={handleChange}
                                placeholder="Mín. 6 caracteres"
                                style={estiloInput}
                            />
                        </div>
                        <div style={estiloGrupo}>
                            <label style={estiloLabel}>Confirmar Contraseña *</label>
                            <input
                                type="password"
                                name="confirmarPassword"
                                required
                                value={formData.confirmarPassword}
                                onChange={handleChange}
                                placeholder="Repite la contraseña"
                                style={estiloInput}
                            />
                        </div>
                    </div>

                    <button type="submit" style={estiloBoton} disabled={cargando}>
                        {cargando ? 'Registrando...' : 'Crear Cuenta'}
                    </button>
                </form>

                <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '13px', color: '#666' }}>
                    ¿Ya tienes cuenta?{' '}
                    <Link to="/login" style={{ color: '#0056b3', fontWeight: '600', textDecoration: 'none' }}>
                        Iniciar Sesión
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default RegisterPaciente;
