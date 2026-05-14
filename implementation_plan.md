# Transform Backend to MVC Architecture

## Goal Description

Refactor the existing Java Spring Boot backend of the **Portal Médico** project into a clear Model‑View‑Controller (MVC) structure. The current codebase already separates data access (repositories) and business logic (services) but lacks a dedicated controller layer exposing REST endpoints. Adding controllers will:
- Provide a clean entry point (`View`) for HTTP requests.
- Keep services (`Model`) untouched.
- Use DTOs to decouple internal entities from the API contract.
- Improve testability and maintainability.

## User Review Required

> [!IMPORTANT]
> **Breaking change:** Existing front‑end code may currently call service methods directly (unlikely) or use undocumented endpoints. Adding controller routes will change the API URLs and request/response payloads. Ensure the front‑end is updated accordingly.
>
> **Design decision:** Do you prefer a *thin* controller (delegating straight to services) or a *rich* controller that performs request validation, error handling, and mapping to DTOs? The plan assumes a thin controller with validation via Spring's `@Valid` and DTO conversion.

## Open Questions

> [!CAUTION]
> 1. **Authentication flow:** Should the new controllers be secured using existing `AuthTokenFilter` (JWT) or should we expose some public endpoints (e.g., login, register)?
> 2. **Response format:** Do you want a uniform response wrapper `{ success: true, data: ..., error: null }` for all endpoints?
> 3. **API versioning:** Should we prefix routes with `/api/v1/`?
> 4. **Front‑end impact:** Confirm whether the front‑end will be updated to call the new endpoints.

## Proposed Changes

---
### 1. Create Controller Package

- **[NEW]** `src/main/java/com/eps/portal/controller/` directory.
- Add a base `AbstractController` (optional) for common exception handling.

---
### 2. Implement Controllers per Domain

#### AuthController
- Endpoints: `/api/auth/login`, `/api/auth/register`.
- Uses `AuthService`.

#### PacienteController
- Endpoints: CRUD for patients (`/api/pacientes`).
- Uses `PacienteService`.

#### MedicoController
- Endpoints: CRUD for doctors (`/api/medicos`).
- Uses `MedicoService`.

#### CitaController
- Endpoints: schedule, list, cancel appointments (`/api/citas`).
- Uses `CitaService`.

#### FormulaMedicaController
- Endpoints: manage prescriptions (`/api/formulas`).
- Uses `FormulaMedicaService`.

#### HistorialClinicoController
- Endpoints: patient medical history (`/api/historial`).
- Uses `HistorialClinicoService`.

---
### 3. DTOs (Data Transfer Objects)

- Create `src/main/java/com/eps/portal/dto/` package.
- For each entity, define `Request` and `Response` DTOs (e.g., `PacienteRequest`, `PacienteResponse`).
- Use Lombok (`@Data`) for brevity (add Lombok dependency if not present).

---
### 4. Mapper Utility

- Add a simple mapper class (`DtoMapper`) to convert between entities and DTOs using ModelMapper or manual mapping.

---
### 5. Update Spring Configuration

- Ensure component scanning includes `com.eps.portal.controller`.
- Verify `AuthTokenFilter` is applied to `/api/**` routes, excluding `/api/auth/**`.
- Add CORS configuration to allow requests from the React front‑end (origin `http://localhost:3000`).

---
### 6. Adjust Build Files

- Update `pom.xml` (or Gradle) to include any new dependencies (Lombok, ModelMapper).
- Run `mvn clean install` to verify compilation.

---
### 7. Unit & Integration Tests

- Add controller tests using `@WebMvcTest` and MockMvc.
- Write integration tests that hit the full stack (controller → service → repository).

---
### 8. Documentation

- Generate API documentation (Swagger/OpenAPI) via `springdoc-openapi-ui`.
- Add a README section describing the new endpoint URLs and payload examples.

## Verification Plan

### Automated Tests
- Run existing test suite (`mvn test`). All should pass after changes.
- Execute new controller unit tests.

### Manual Verification
- Start the backend (`mvn spring-boot:run`).
- Use `curl` or Postman to hit each new endpoint, confirming correct HTTP status and JSON payload.
- Verify JWT authentication works for protected routes.
- Ensure CORS headers are present.

---
**Next Steps**
1. Confirm the design choices above (controller style, response wrapper, API versioning).
2. Once approved, create a `task.md` with a detailed checklist and begin implementation.
