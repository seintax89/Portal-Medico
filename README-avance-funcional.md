# Avance funcional agregado

Se agregó una primera capa funcional posterior al login y registro de pacientes.

## Frontend agregado

- `DashboardPaciente.jsx`
  - Consulta perfil del paciente autenticado.
  - Lista especialidades.
  - Lista médicos.
  - Permite agendar cita usando `POST /api/citas`.

- `DashboardMedico.jsx`
  - Permite registrar evolución clínica usando `POST /api/historial`.
  - Muestra directorio médico.

- `DashboardAdmin.jsx`
  - Muestra conteos básicos.
  - Permite registrar médicos usando `POST /api/auth/registro/medico`.
  - Lista médicos registrados.

- `DashboardLayout.jsx`
  - Layout común para paneles.

- `NoAutorizado.jsx`
  - Vista para acceso sin permisos.

- `dashboard.css`
  - Estilos compartidos para los paneles.

## Archivos modificados

- `frontend-medico/src/App.jsx`
- `frontend-medico/src/styles/dashboard.css`
- `frontend-medico/src/pages/DashboardPaciente.jsx`
- `frontend-medico/src/pages/DashboardMedico.jsx`
- `frontend-medico/src/pages/DashboardAdmin.jsx`
- `frontend-medico/src/pages/DashboardLayout.jsx`
- `frontend-medico/src/pages/NoAutorizado.jsx`

## Requisitos para probar

1. Tener backend desplegado y funcionando.
2. Tener especialidades cargadas en la tabla `especialidades`.
3. Tener al menos un médico registrado para poder agendar citas.
4. Iniciar sesión con un paciente para probar agenda de citas.
5. Iniciar sesión con un médico para probar evolución clínica.
6. Iniciar sesión con un administrador para registrar médicos.
