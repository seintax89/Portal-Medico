// src/pages/Login.jsx  (REEMPLAZA el archivo existente)
import { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext.jsx';
import { useNavigate, Link } from 'react-router-dom';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const { login } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setCargando(true);

        try {
            // login() ahora devuelve el rol para poder redirigir correctamente
            const rol = await login(email, password);

            // ── Redirección según rol ──────────────────────────────────────────
            // Ajusta las rutas cuando crees cada dashboard
            switch (rol) {
                case 'ROLE_PACIENTE':
                    navigate('/dashboard/paciente');
                    break;
                case 'ROLE_MEDICO':
                    navigate('/dashboard/medico');
                    break;
                case 'ROLE_ADMIN':
                    navigate('/dashboard/admin');
                    break;
                default:
                    navigate('/dashboard');
            }
        } catch (err) {
            const mensaje = err.response?.data?.mensaje || 'Credenciales inválidas. Verifique su correo y contraseña.';
            setError(mensaje);
        } finally {
            setCargando(false);
        }
    };

    return (
        <div style={{
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: '#f0f4f8',
        }}>
            <div style={{
                backgroundColor: '#fff',
                padding: '40px',
                borderRadius: '12px',
                boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
                width: '100%',
                maxWidth: '380px',
            }}>
                <h2 style={{ textAlign: 'center', color: '#0056b3', marginBottom: '6px' }}>
                    Portal Médico EPS
                </h2>
                <p style={{ textAlign: 'center', color: '#666', marginBottom: '28px', fontSize: '14px' }}>
                    Ingresa tus credenciales para continuar
                </p>

                {error && (
                    <p style={{ color: '#c0392b', backgroundColor: '#fdecea', padding: '10px', borderRadius: '6px', fontSize: '13px', marginBottom: '14px' }}>
                        ⚠️ {error}
                    </p>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    <div>
                        <label style={{ display: 'block', fontWeight: '600', fontSize: '13px', color: '#444', marginBottom: '5px' }}>
                            Correo Electrónico
                        </label>
                        <input
                            type="email"
                            required
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="correo@ejemplo.com"
                            style={{ width: '100%', padding: '10px 12px', border: '1px solid #ccc', borderRadius: '6px', fontSize: '14px', boxSizing: 'border-box' }}
                        />
                    </div>
                    <div>
                        <label style={{ display: 'block', fontWeight: '600', fontSize: '13px', color: '#444', marginBottom: '5px' }}>
                            Contraseña
                        </label>
                        <input
                            type="password"
                            required
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="Tu contraseña"
                            style={{ width: '100%', padding: '10px 12px', border: '1px solid #ccc', borderRadius: '6px', fontSize: '14px', boxSizing: 'border-box' }}
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={cargando}
                        style={{
                            padding: '12px',
                            backgroundColor: cargando ? '#7aadde' : '#0056b3',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            fontSize: '15px',
                            fontWeight: '600',
                            cursor: cargando ? 'not-allowed' : 'pointer',
                        }}>
                        {cargando ? 'Iniciando sesión...' : 'Iniciar Sesión'}
                    </button>
                </form>

                <hr style={{ border: 'none', borderTop: '1px solid #eee', margin: '24px 0 16px' }} />

                <p style={{ textAlign: 'center', fontSize: '13px', color: '#666', margin: 0 }}>
                    ¿Eres paciente nuevo?{' '}
                    <Link to="/registro/paciente" style={{ color: '#0056b3', fontWeight: '600', textDecoration: 'none' }}>
                        Regístrate aquí
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default Login;
