# Frontend Module Design

Date: 2026-05-04  
Branch: feature/frontend

## Overview

React + TypeScript SPA module inside the existing `video-platform` Gradle multi-project. Provides signup, login, video post upload, and video watching. Integrated with Gradle and Docker Compose for local dev.

---

## Architecture

### Module location

```
video-platform/
└── frontend/          ← new Gradle subproject
    ├── build.gradle.kts
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    ├── index.html
    ├── src/
    │   ├── main.tsx
    │   ├── App.tsx
    │   ├── api/
    │   │   ├── client.ts         # axios instance + auth interceptor
    │   │   ├── auth.ts           # signup / login / logout
    │   │   └── posts.ts          # post CRUD + video upload
    │   ├── pages/
    │   │   ├── LoginPage.tsx
    │   │   ├── SignUpPage.tsx
    │   │   ├── PostListPage.tsx  # video list + search
    │   │   ├── PostUploadPage.tsx
    │   │   └── VideoWatchPage.tsx
    │   ├── components/
    │   │   └── ProtectedRoute.tsx
    │   └── types/
    │       └── index.ts
    └── e2e/
        ├── playwright.config.ts
        ├── auth.spec.ts
        ├── video-upload.spec.ts
        └── video-watch.spec.ts
```

### Tech stack

| Concern | Choice | Reason |
|---------|--------|--------|
| Framework | React 18 + TypeScript | as requested |
| Build | Vite 5 | fast dev server, first-class TS |
| Routing | React Router v6 | standard |
| HTTP | Axios | interceptors for auth header |
| E2E tests | Playwright | browser-based, as requested |
| Styling | plain CSS | UI polish out of scope |

---

## API Endpoints (via Gateway at `localhost:10100`)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| POST | /users/signup | no | Register + auto-login |
| POST | /users/login | no | Login |
| DELETE | /users/logout | no | Logout |
| GET | /streams/posts | yes | List posts (paginated, searchable) |
| GET | /streams/posts/{postId} | yes | Get post detail |
| POST | /streams/posts | yes | Create post + video (multipart) |
| GET | /streams/posts/{postId}/videos/{videoId} | yes | Stream video bytes |

---

## Auth Flow

1. Login / Signup → server returns `Authorization: <raw_jwt>` in response header
2. Frontend stores token in `localStorage` as-is
3. Axios request interceptor adds `Authorization: Bearer <stored_token>` to every request
4. Axios response interceptor reads `Authorization` header from login/signup responses and saves it

---

## Pages

### SignUpPage (`/signup`)
- Fields: email, password, confirmPassword, name
- On success → redirect to `/`

### LoginPage (`/login`)
- Fields: email, password
- On success → redirect to `/`
- Link to signup

### PostListPage (`/`)
- Protected route (redirects to `/login` if no token)
- Renders a list of posts with title, category, author
- Search by keyword / category
- Each row links to `VideoWatchPage`
- "Upload" button → `PostUploadPage`

### PostUploadPage (`/posts/new`)
- Protected route
- Fields: title, content, category (select: COMEDY / VIDEO_GAME / MUSIC / AUTOS_VEHICLES / EDUCATION)
- File input for video (mp4)
- Sends multipart FormData: video file + text fields
- On success → redirect to `/`

### VideoWatchPage (`/posts/:postId/videos/:videoId`)
- Protected route
- Fetches video bytes with `Authorization` header
- Renders `<video>` using Blob URL

---

## Gradle Integration

`settings.gradle.kts` — add `include("frontend")`.

`build.gradle.kts` (root) — skip Kotlin plugin for frontend:
```kotlin
subprojects {
    if (project.name == "frontend") return@subprojects
    apply(plugin = "kotlin")
    ...
}
```

`frontend/build.gradle.kts`:
```kotlin
plugins { base }

tasks.register<Exec>("npmInstall") { commandLine("npm", "install") }
tasks.register<Exec>("npmBuild") { commandLine("npm", "run", "build"); dependsOn("npmInstall") }
tasks.register<Exec>("playwrightTest") { commandLine("npx", "playwright", "test"); dependsOn("npmInstall") }

tasks.named("assemble") { dependsOn("npmBuild") }
tasks.named("check") { dependsOn("playwrightTest") }
```

---

## Docker Compose

Add service to `compose.yaml`:
```yaml
frontend:
  container_name: frontend
  image: node:20-alpine
  working_dir: /app
  volumes:
    - ./frontend:/app
  ports:
    - "3000:3000"
  command: sh -c "npm install && npm run dev -- --host 0.0.0.0"
  networks:
    - cherhy-network
  depends_on:
    - gateway
```

The React app runs in the user's browser; API calls go to `localhost:10100` (host gateway exposed by Docker). No internal proxy needed.

---

## Playwright E2E Tests

Tests run against the Vite dev server at `http://localhost:3000`.

### `auth.spec.ts`
- Signup with new email → expect redirect to `/`
- Login with valid credentials → expect redirect to `/`
- Login with invalid credentials → expect error message

### `video-upload.spec.ts`
- Login → navigate to `/posts/new`
- Fill title, content, category, attach a test mp4
- Submit → expect redirect to `/`
- Verify new post appears in list

### `video-watch.spec.ts`
- Login → click post in list
- Expect `<video>` element present and loaded

---

## Production Dockerfile

Multi-stage: build with Node → serve with nginx on port 3000.

---

## Self-Review

- No TBD sections.
- Auth flow consistent with backend header conventions.
- All STREAM_DOMAIN endpoints require Bearer token — handled by axios interceptor.
- PostCategory enum values hardcoded in select options (COMEDY, VIDEO_GAME, MUSIC, AUTOS_VEHICLES, EDUCATION).
- Gradle `subprojects` exclusion prevents Kotlin plugin being applied to frontend.
- Docker Compose API calls go through host-exposed gateway (browser-side), avoiding container-to-host routing issues.
