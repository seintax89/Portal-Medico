# 🏥 Portal Médico EPS

> **Sistema de gestión de citas médicas y expediente clínico para una EPS (Entidad Promotora de Salud)**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue)](https://react.dev/)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/projects/jdk/17/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-336791)](https://supabase.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT-yellow)](https://jwt.io/)

---

## 📋 Descripción del Proyecto

El **Portal Médico EPS** es una aplicación web full-stack diseñada para digitalizar y automatizar la gestión de consultas médicas dentro de una EPS. Permite a pacientes agendar citas, a médicos registrar evoluciones clínicas y gestionar el historial de sus pacientes, todo con un sistema de autenticación seguro basado en JWT.

### Funcionalidades Principales

| Módulo | Descripción |
|--------|-------------|
| 🔐 **Autenticación** | Registro de pacientes/médicos, login con JWT |
| 📅 **Citas** | Agendamiento con validación de 5 reglas de negocio |
| 👨‍⚕️ **Directorio Médico** | Listado y filtrado de médicos por especialidad |
| 📁 **Historial Clínico** | Registro de evoluciones médicas (diagnóstico + observaciones) |
| 👤 **Perfil de Paciente** | Consulta de datos personales y tipo de paciente |

### Reglas de Negocio Implementadas

| Código | Regla | Descripción |
|--------|-------|-------------|
| RN-01 | **Horario EPS** | Solo se atiende Lunes–Viernes, 8:00 AM–6:00 PM |
| RN-02 | **Fecha futura** | No se puede agendar una cita en el pasado |
| RN-03 | **Sin solapamiento** | El médico no puede tener dos citas al mismo tiempo |
| RN-04 | **Remisión especializada** | Un paciente no puede agendar directamente con especialistas; requiere que lo haga un médico |
| RN-05 | **Pediatría exclusiva** | Pacientes clasificados como NINO solo pueden ver pediatras |

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND (React + Vite)                  │
│  - React 19 + React Router 7                                 │
│  - Axios (cliente HTTP)                                      │
│  - JWT Decode (manejo de tokens)                             │
│  - Puerto: http://localhost:5173                             │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/REST + JWT
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                     │
│                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌────────────────────┐  │
│  │ Controllers │→ │  Services   │→ │   Repositories     │  │
│  │ (REST API)  │  │ (Lógica RN) │  │ (Spring Data JPA)  │  │
│  └─────────────┘  └─────────────┘  └────────────────────┘  │
│                                                              │
│  Spring Security + JWT Filter                                │
│  Puerto: http://localhost:8080                               │
└───────────────────────────┬─────────────────────────────────┘
                            │ JDBC
                            ▼
┌─────────────────────────────────────────────────────────────┐
│               BASE DE DATOS (PostgreSQL - Supabase)          │
│  Tablas: usuarios, roles, pacientes, medicos, especialidades │
│          citas, historial_clinico, formulas_medicas          │
└─────────────────────────────────────────────────────────────┘
```

---

## ⚙️ Tecnologías Utilizadas

### Backend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 3.2.4 | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Acceso a base de datos |
| JJWT | 0.11.5 | Generación y validación de tokens JWT |
| Lombok | Latest | Reducción de código boilerplate |
| Maven | 3.x | Gestión de dependencias y build |
| PostgreSQL | 15+ | Base de datos en producción |
| H2 (tests) | Latest | Base de datos en memoria para pruebas |

### Frontend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| React | 19 | Librería de interfaces de usuario |
| Vite | 8.x | Herramienta de build y dev server |
| React Router | 7.x | Navegación y rutas protegidas |
| Axios | 1.x | Cliente HTTP para llamadas a la API |
| JWT Decode | 4.x | Decodificación de tokens en el cliente |

---

## 📦 Requisitos Técnicos

### Para ejecutar el Backend
- **Java Development Kit (JDK) 17** o superior
- **Maven 3.6+** (o usar el wrapper incluido `mvnw`)
- Acceso a internet (para conectar con Supabase en producción)

### Para ejecutar el Frontend
- **Node.js 18+**
- **npm 9+**

### Para ejecutar las Pruebas
- Los mismos requisitos del backend
- **No se requiere** base de datos externa (se usa H2 en memoria automáticamente)

---

## 🚀 Guía de Instalación y Uso

### 1. Clonar el repositorio

```bash
git clone https://github.com/seintax89/Portal-Medico.git
cd Portal-Medico
```

### 2. Ejecutar el Backend

```bash
cd portal-medico/portal-medico

# Opción A: Con el wrapper de Maven (recomendado)
.\mvnw.cmd spring-boot:run          # Windows
./mvnw spring-boot:run              # Linux/macOS

# Opción B: Si tienes Maven instalado globalmente
mvn spring-boot:run
```

El backend estará disponible en: **http://localhost:8080**

### 3. Ejecutar el Frontend

```bash
cd frontend-medico
npm install
npm run dev
```

El frontend estará disponible en: **http://localhost:5173**

### 4. Credenciales de Administrador (preconfiguradas)

| Campo | Valor |
|-------|-------|
| Email | `admin@eps.com` |
| Contraseña | `Admin123*` |
| Rol | `ROLE_ADMIN` |

---

## 🔌 API REST — Endpoints

### Autenticación (`/api/auth`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/auth/login` | Público | Iniciar sesión |
| `POST` | `/api/auth/registro/paciente` | Público | Registrar nuevo paciente |
| `POST` | `/api/auth/registro/medico` | Público | Registrar nuevo médico |

**Ejemplo de Login:**
```json
// Request
POST /api/auth/login
{
  "email": "admin@eps.com",
  "password": "Admin123*"
}

// Response 200 OK
{
  "token": "eyJhbGci...",
  "type": "Bearer",
  "id": 1,
  "email": "admin@eps.com",
  "rol": "ROLE_ADMIN"
}
```

### Citas (`/api/citas`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/citas` | PACIENTE, MEDICO | Agendar una cita médica |

### Directorio Médico (`/api/medicos`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/medicos` | Autenticado | Listar todos los médicos |
| `GET` | `/api/medicos?especialidadId=2` | Autenticado | Filtrar médicos por especialidad |

### Historial Clínico (`/api/historial`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/api/historial` | MEDICO | Registrar evolución clínica |
| `GET` | `/api/historial/paciente/{id}` | MEDICO, PACIENTE | Ver historial de un paciente |

### Especialidades (`/api/especialidades`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/especialidades` | Público | Listar todas las especialidades |

### Perfil de Paciente (`/api/pacientes`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `GET` | `/api/pacientes/mi-perfil` | PACIENTE | Ver perfil propio |

---

## 🧪 Pruebas

El proyecto cuenta con una suite completa de pruebas automatizadas:

### Tipos de Pruebas

| Tipo | Clases | Pruebas | Descripción |
|------|--------|---------|-------------|
| **Unitarias (Función)** | `AuthServiceTest`, `CitaServiceTest`, `MedicoServiceTest`, `PacienteServiceTest` | 20 | Lógica de negocio con Mockito, sin BD |
| **De Ruta** | `AuthControllerTest`, `CitaControllerTest`, `MedicoControllerTest` | 15 | HTTP status codes, seguridad de rutas con `@WebMvcTest` |
| **De Estado** | `CitaServiceTest` (sección Estado) | 3 | Transiciones PROGRAMADA → COMPLETADA |
| **De Integración** | `IntegracionAuthTest`, `IntegracionCitasTest` | 14 | Flujos end-to-end con H2 en memoria |

### Ejecutar las Pruebas

```bash
cd portal-medico/portal-medico

# Ejecutar TODAS las pruebas + generar reporte de coverage
.\mvnw.cmd test                     # Windows
./mvnw test                         # Linux/macOS
```

### Ver el Reporte de Coverage (HTML)

Después de ejecutar las pruebas, abrir en el navegador:

```
portal-medico/portal-medico/target/site/jacoco/index.html
```

El reporte de **JaCoCo** muestra el porcentaje de código cubierto por las pruebas, desglosado por:
- **Paquete** (controller, service, repository, etc.)
- **Clase**
- **Método**
- **Línea de código**

### Resumen de Pruebas por Módulo

| Módulo | Prueba Ejemplo | Resultado Esperado |
|--------|---------------|-------------------|
| Auth → Registro | Email duplicado | `RuntimeException` |
| Auth → Clasificación | Paciente de 8 años | Tipo `NINO` |
| Citas → RN-01 | Cita en sábado | `RuntimeException` |
| Citas → RN-04 | Paciente agenda cardiología | `RuntimeException` |
| Citas → Estado | Cita recién creada | `estado = "PROGRAMADA"` |
| Integración → Estado | Post-evolución clínica | `estado = "COMPLETADA"` |
| Ruta → Seguridad | GET /api/medicos sin token | `403 FORBIDDEN` |

---

## 🗂️ Estructura del Proyecto

```
Portal-Medico/
├── 📁 portal-medico/          ← Backend (Spring Boot)
│   └── portal-medico/
│       ├── src/main/java/com/eps/portal/
│       │   ├── config/        ← SecurityConfig, DataInitializer
│       │   ├── controller/    ← AuthController, CitaController...
│       │   ├── dto/           ← Request/Response DTOs
│       │   ├── entity/        ← Entidades JPA
│       │   ├── repository/    ← Spring Data JPA Repos
│       │   ├── security/      ← JWT Filter, JwtUtils, UserDetails
│       │   └── service/       ← Lógica de negocio
│       ├── src/test/java/     ← Suite de pruebas
│       │   ├── service/       ← Pruebas unitarias
│       │   ├── controller/    ← Pruebas de ruta
│       │   └── integration/   ← Pruebas de integración
│       └── pom.xml
│
├── 📁 frontend-medico/        ← Frontend (React + Vite)
│   └── src/
│       ├── components/        ← PrivateRoute
│       ├── context/           ← AuthContext (estado global de sesión)
│       ├── pages/             ← Login, RegisterPaciente
│       └── services/          ← api.js (Axios configurado)
│
└── README.md                  ← Este archivo
```

---

## 🔒 Seguridad

- **Autenticación**: JWT (JSON Web Tokens) con expiración de 24 horas
- **Contraseñas**: Cifradas con BCrypt (factor de costo estándar)
- **Autorización**: Control de acceso basado en roles (`ROLE_PACIENTE`, `ROLE_MEDICO`, `ROLE_ADMIN`)
- **CORS**: Configurado para orígenes permitidos (`localhost:5173`, `localhost:3000`, producción)
- **CSRF**: Desactivado (se usa JWT stateless en su lugar)
- **Sesiones**: Sin estado (Stateless) — cada petición incluye su propio token

---

## 🚢 Despliegue

| Componente | Plataforma | URL |
|------------|------------|-----|
| Backend | Render | `https://portal-medico-uicj.onrender.com` |
| Frontend | Vercel | `https://portal-medico.vercel.app` |
| Base de datos | Supabase | PostgreSQL gestionado en la nube |

---

## 👥 Desarrollado por

**Equipo de Ingeniería de Software — EPS Portal Médico**  
Universidad — Proyecto de Ingeniería de Software  
2025–2026
