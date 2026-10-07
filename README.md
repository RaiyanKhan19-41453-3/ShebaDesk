# 🏥 ShebaDesk

**ShebaDesk** ("sheba" = care/service) is a production-style **Spring Boot 3.5 / Java 21** clinic and hospital management system — patients, doctors, and appointment scheduling — with paginated search, booking conflict protection, RFC7807 errors, Flyway migrations, and a one-command Docker setup.

No local Java, Maven, or MySQL required. Clone it, run `make up`, and hit the API in under 3 minutes.

---

## ✨ Highlights

| Area | What you get |
|---|---|
| 🧑‍⚕️ Domain | Patient CRUD, Doctor CRUD, appointment booking with double-booking + past-date rejection |
| 🔎 API quality | Pagination, sorting, case-insensitive search, `201 + Location`, correct `404/409/400` semantics |
| 🧯 Errors | RFC7807 `ProblemDetail` everywhere — machine-readable `status/title/detail`, field-level `errors{}` for validation |
| 🗄️ Data | Flyway `V1` schema + `V2` seed, Hibernate `validate` (never auto-migrates), FK + `UNIQUE(doctor, date)` guards against race conditions |
| 🔐 Auth | JWT Bearer login, `ADMIN` / `RECEPTIONIST` roles, BCrypt hashing, JSON `401/403` |
| 🕰️ Data safety | `createdAt`/`updatedAt` auditing on every record; soft delete (deletes hide, never destroy; emails stay reserved) |
| 🐳 Ops | Multi-stage Dockerfile (non-root `appuser`, healthcheck), Compose with healthy MySQL gating, Actuator health |
| ✅ Tests | 10 tests — MockMvc API suite on H2 + Mockito unit tests, all runnable without Docker |
| 📖 Docs | Live Swagger UI with `@Tag/@Operation` annotations |
| 🤖 CI | GitHub Actions: tests → `compose config` → image build |

---

## 🚀 Quick start

```bash
git clone <repo> && cd shebadesk
make up      # builds the image and starts MySQL + API
```

Then open:

- **Swagger UI** → http://localhost:4000/swagger-ui.html
- **Health** → http://localhost:4000/actuator/health → `{"status":"UP"}`
- **Seeded doctors** → http://localhost:4000/doctors?size=2 (login first — see Auth below)

Default admin (seeded by Flyway `V4`): username `admin`, password `Admin123!` — change it after first login.

```bash
# login and call the API
TOKEN=$(curl -s -X POST localhost:4000/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin123!"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")
curl -H "Authorization: Bearer $TOKEN" localhost:4000/patients | head -c 300
```

```bash
make logs    # follow API logs
make test    # run the 10 tests (H2 — no Docker needed)
make down    # stop everything
```

> The database is mapped to host port **3307** (`root/root`, db `shebadesk_db`) so it never clashes with a local MySQL on 3306. Inside Docker the app talks to `mysql:3306` — see `docker-compose.yml`.

<details>
<summary>Run without Docker (needs a local MySQL)</summary>

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/shebadesk_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
SPRING_DATASOURCE_USERNAME=root SPRING_DATASOURCE_PASSWORD=root ./mvnw spring-boot:run
```

</details>

---

## 🧭 Try it in 60 seconds

```bash
# 0. Login (all /patients, /doctors, /appointments calls need the token)
TOKEN=$(curl -s -X POST localhost:4000/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"Admin123!"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")
AUTH=(-H "Authorization: Bearer $TOKEN")

# 1. Create a patient (201 + Location header)
curl -i -X POST localhost:4000/patients "${AUTH[@]}" -H 'Content-Type: application/json' -d '{
  "name": "Ada Lovelace", "email": "ada@example.com", "location": "Dhaka",
  "dateOfBirth": "1990-01-01", "registeredDate": "2026-10-07"
}'

# 2. Search patients (paginated)
curl "${AUTH[@]}" 'localhost:4000/patients?name=ada&size=5' | python3 -m json.tool | head -20

# 3. Book an appointment with seeded Dr House (id 1)
curl -X POST localhost:4000/appointments "${AUTH[@]}" -H 'Content-Type: application/json' -d '{
  "doctorId": 1, "patientId": "<id-from-step-1>",
  "appointmentDate": "2026-11-01", "notes": "Annual checkup"
}'

# 4. Book the same slot again → 409 Conflict (ProblemDetail)
curl -X POST localhost:4000/appointments "${AUTH[@]}" -H 'Content-Type: application/json' -d '{
  "doctorId": 1, "patientId": "<id-from-step-1>", "appointmentDate": "2026-11-01"
}'
# {"type":"about:blank","title":"Conflict","status":409,
#  "detail":"Doctor already booked on 2026-11-01","instance":"/appointments"}
```

---

## 🔌 API reference

### Auth `/auth`

| Method | Endpoint | Access | Notes |
|---|---|---|---|
| `POST` | `/auth/login` | Public | `{username, password}` → `{tokenType, accessToken, expiresInSeconds}`; `401` on bad credentials |
| `POST` | `/auth/register` | `ADMIN` only | `{username, password≥8, role}` → `201`; `403` for receptionists |

Reads need a valid token; writes need `ADMIN` or `RECEPTIONIST`. Send `Authorization: Bearer <token>`. Configure via env: `JWT_SECRET` (required in prod), `JWT_EXPIRATION_MS` (default 24h).

### Patients `/patients` (authenticated)

| Method | Endpoint | Success | Errors |
|---|---|---|---|
| `GET` | `/?page=&size=&sort=&name=&email=` | `200` page (`content[]`, `totalElements`) | `400` bad sort property |
| `GET` | `/{id}` | `200` | `404` unknown id, `400` malformed UUID |
| `POST` | `/` | `201` + `Location` | `400` validation, `409` duplicate email |
| `PUT` | `/{id}` | `200` | `404` / `409` (`registeredDate` immutable) |
| `DELETE` | `/{id}` | `204` | `404`, `409` if appointments exist (FK) |

### Doctors `/doctors`

Same semantics as patients, plus search by `name`/`specialty`:

| Method | Endpoint | Notes |
|---|---|---|
| `GET` | `/?page=&size=&sort=&name=&specialty=` | Paged + search |
| `GET` | `/{id}/appointments` | All appointments for one doctor |
| `POST/PUT/DELETE` | `/doctors...` | `201`/`200`/`204`, `404`/`409` as above |

### Appointments `/appointments`

| Method | Endpoint | Notes |
|---|---|---|
| `GET` | `/?doctorId=&patientId=&from=&to=&page=&size=` | All filters combinable; `400` on half or inverted range |
| `GET` | `/{id}` | `404` if missing |
| `POST` | `/` | `201`; `409` past date / double-book / unknown doctor+patient validated first (`404`) |
| `DELETE` | `/{id}` | `204` cancel / `404` |

### Error format (RFC7807)

```json
// 404
{"type":"about:blank","title":"Not found","status":404,
 "detail":"Patient not found with id: ...","instance":"/patients/..."}
// 400 validation
{"type":"about:blank","title":"Validation failed","status":400,
 "detail":"One or more fields are invalid","instance":"/patients",
 "errors":{"email":"Provide valid email","name":"Name is required"}}
```

---

## 🗄️ Data model & migrations

```
doctors (id, name, specialty, email UNIQUE, phone)
   1 ──┐
       ├── appointments (id, doctor_id, patient_id, appointment_date, notes)
patients (id UUID, name, location, email UNIQUE, date_of_birth, registered_date)
   1 ──┘   UNIQUE(doctor_id, appointment_date)  ← double-book proof at the DB level
```

- `src/main/resources/db/migration/V1__init.sql` — tables, unique keys, foreign keys
- `src/main/resources/db/migration/V2__seed.sql` — 2 doctors + 2 patients to explore with
- `src/main/resources/db/migration/V3__users.sql` + `V4__admin.sql` — login users + default `admin`
- `src/main/resources/db/migration/V5__audit.sql` — `created_at` / `updated_at` / `deleted` on all tables
- Hibernate runs `ddl-auto=validate`: the app refuses to start on schema drift instead of silently altering your database

---

## 🧪 Testing

```bash
./mvnw test
# Tests run: 12, Failures: 0, Errors: 0 — BUILD SUCCESS
```

- `HospitalApiTest` (MockMvc + H2): create→`201`+`Location`, duplicate→`409`, validation→`400` with `errors{}`, missing→`404`, book→`201`, double-book→`409`
- `PatientServiceTest` / `AppointmentServiceTest` (Mockito): duplicate email, missing entity, null-id guards that never touch the DB
- Tests run on **H2** (`src/test/resources/application.properties`, Flyway off) — no Docker needed; CI runs the same suite

---

## 🗂️ Project structure

```
src/main/java/com/shebadesk/
├── controller/      # PatientController, DoctorController, AppointmentController (REST + OpenAPI)
├── service/        # @Transactional boundaries, booking rules, search logic
├── repository/     # Spring Data paged queries (derived, no JPQL needed)
├── model/          # Patient (UUID), Doctor, Appointment (UNIQUE doctor+date)
├── dto/            # Request (validation) / Response (LocalDate, no Strings-as-dates)
├── mapper/         # Explicit DTO mappers (null-safe)
└── exception/      # Domain exceptions + ProblemDetail GlobalExceptionHandler
Dockerfile                    # multi-stage, appuser, curl healthcheck on /actuator/health
docker-compose.yml            # mysql:8.0 (healthy-gated) + app
.github/workflows/ci.yml      # test → compose config → image build
Makefile                      # up / down / logs / test / build / ps / seed-check
```

## 🛣️ Roadmap

- [x] JWT auth with `ADMIN` / `RECEPTIONIST` roles
- [x] Audit fields (`createdAt`, `updatedAt`) + soft delete
- [ ] Prometheus metrics + Grafana dashboard
- [ ] React admin frontend

---

Built with Spring Boot 3.5 · Java 21 · MySQL 8 · Flyway · springdoc-openapi · Docker.
