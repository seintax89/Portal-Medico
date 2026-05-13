// src/context/AuthContext.jsx  (REEMPLAZA el archivo existente)
import { createContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../services/api.js';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Al cargar la app: restaura la sesión si el token sigue vigente
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                if (decoded.exp * 1000 < Date.now()) {
                    logout();
                } else {
                    setUser({ id: decoded.id, email: decoded.sub, rol: decoded.rol });
                }
            } catch {
                logout();
            }
        }
        setLoading(false);
    }, []);

    /**
     * Inicia sesión y retorna el ROL del usuario para que el componente
     * de Login pueda redirigir a la ruta correcta.
     */
    const login = async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        const { token, rol, id } = response.data;

        localStorage.setItem('token', token);

        const decoded = jwtDecode(token);
        setUser({ id, email: decoded.sub, rol });

        return rol; // ← importante: devolvemos el rol
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};
