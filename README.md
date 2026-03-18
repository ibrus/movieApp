# MovieApp (Step 1 scaffold)

Monorepo layout:

- `backend/`: Spring Boot (Gradle) API on `:8080`
- `frontend/`: Next.js app on `:3000`
- `docker-compose.yml`: MySQL on `:3306`

## Prerequisites

- Docker + Docker Compose
- Java (for backend) and Node.js (for frontend) — versions will be pinned once the apps are generated

## Run (end-to-end, once Step 1 is complete)

From the repo root:

1. Start MySQL (dev):

```bash
docker compose up -d
```

2. Start backend:

```bash
cd backend
./gradlew bootRun
```

3. Start frontend:

```bash
cd frontend
npm install
npm run dev
```

## Quick verification checklist

- Backend endpoint:

```bash
curl http://localhost:8080/api/hello
```

- Frontend proxy (Next.js rewrite) to backend:

```bash
curl http://localhost:3000/api/hello
```

- Browser UI: open `http://localhost:3000` and confirm you see “Hello Movie App” plus the backend response.

