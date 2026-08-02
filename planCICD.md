# CI/CD Plan — Java Microservices Monorepo

> Status: **CI IMPLEMENTED** (build + push to GHCR) · deploy still deferred
> Platform: **GitHub Actions** · Registry: **GHCR (ghcr.io), public** · Scope: **CI + build & push images now, deploy later**
> Decisions locked: tests **skipped in CI** for now · `auth_service` `COPY *.pem` **removed** · images **public**

---

## 1. Context (what this repo is)

A **monorepo** containing 5 independent Spring Boot services, each with its own
`pom.xml`, `mvnw`, and `Dockerfile`. There is **no parent/aggregator POM** — services build
independently. A root `docker-compose.yml` wires up shared infra (Kafka + Postgres per service)
for local dev; `docker-compose.prod.yml` is the intended prod topology.

| Service | Java (pom) | Docker base | Tests | Notes |
|---|---|---|---|---|
| `auth_service` | 17 | temurin-21 | 1 | **Dockerfile copies `*.pem` keys** (see §5.1) |
| `order_service` | 17 | temurin-21 | 1 | |
| `payment_service` | 17 | temurin-21 | 1 | |
| `user_service` | 17 | temurin-21 | 2 | |
| `notif_service` | 17 | temurin-21 | 1 | |

Spring Boot parent: `4.1.0`. All Dockerfiles are multi-stage (`maven:3.9-eclipse-temurin-21`
build stage → `eclipse-temurin:21-jre` runtime) and build with `-DskipTests`.

---

## 2. Where the CI/CD config lives

CI runners read **one fixed location per repo**, so the pipeline definition goes at the **root**:

```
microservices/
├── .github/
│   └── workflows/
│       ├── auth_service.yml
│       ├── order_service.yml
│       ├── payment_service.yml
│       ├── user_service.yml
│       └── notif_service.yml
├── auth_service/        ← Dockerfile, pom.xml, mvnw stay per-service
├── order_service/
└── ...
```

**Structure decision: one workflow file per service** (not a single matrix file), because:
- Services are uniform but deploy independently — a `notif_service` test flake shouldn't block
  an `auth_service` release.
- Each file uses a **path filter** so a commit only triggers the affected service's pipeline.

---

## 3. Pipeline stages (per service)

```
 push / PR ──► [ BUILD + TEST ] ──► [ BUILD IMAGE ] ──► [ PUSH → GHCR ] ──► ( DEPLOY: TODO )
                 mvnw verify          docker build        ghcr.io/<owner>/    added once a
               (matching JDK)        (multi-stage)        <svc>:<tag>         prod target exists
```

- **Trigger:** `push` and `pull_request` filtered on `<service>/**` (+ the service's own workflow file).
- **BUILD + TEST:** `setup-java` (Temurin) + `./mvnw -B verify`, with Maven dependency caching.
- **BUILD IMAGE:** `docker/build-push-action` using the service's existing `Dockerfile`.
- **PUSH:** only on `push` to `main` (not on PRs). Login via the built-in `GITHUB_TOKEN`.

### Image naming & tags
```
ghcr.io/<github-owner>/auth_service:latest
ghcr.io/<github-owner>/auth_service:<git-sha>
```
`<github-owner>` = repo owner (lowercased — GHCR requires lowercase). Tag with both `latest`
(moving) and the immutable `sha` (for rollback / traceability).

---

## 4. GHCR authentication

No manual secret needed for pushing. GitHub Actions injects a per-run `GITHUB_TOKEN`; grant the
workflow `packages: write` permission and log in:

```yaml
permissions:
  contents: read
  packages: write
# ...
- uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}
```

Published packages default to **private**; make them public (or grant pull access) from the repo's
Packages settings if the deploy host needs anonymous pulls.

---

## 5. Blockers / decisions to resolve before or during implementation

### 5.1 🔴 `auth_service` keys must move to a runtime volume (baking into image is broken)
The RSA keys are **JWT signing secrets** and are **rotated at runtime**:
- `RsaKeyConfig.init()` (`@PostConstruct`) **reads** keys from `app.keys.{private,public}-key-path`.
- On rotation, `RsaKeyConfig.persistKeyPair()` **writes new `.pem` files back to those same paths**
  (`Files.writeString`, line 83), then publishes the new public key to Kafka
  (`public-key-rotation` topic) for other services to pick up.

The current `auth_service/Dockerfile` `COPY`s the keys into `/app/keys/` as an **image layer**.
This is wrong for two reasons:
1. **Rotation is lost** — writes to `/app/keys/*.pem` land in the container's ephemeral writable
   layer; on restart the baked-in originals return, and replicas don't share rotated keys.
2. **CI build fails** — `.gitignore` excludes `*.pem`, so a fresh checkout has no keys to `COPY`.

**Decision (agreed): inject keys at runtime via a mounted volume.**
- **Dockerfile:** remove the two `COPY *.pem` lines. Keep `APP_KEYS_*_PATH=/app/keys/...`.
  Image ships key-free → CI build works, and no signing key ever lands in a published GHCR layer.
- **Runtime:** mount a persistent named volume (or host path) at `/app/keys` in
  `docker-compose*.yml`. Rotation writes now persist across restarts.
- **⚠️ Bootstrap:** `init()` only *reads* keys — it does **not** generate them if missing, so an
  empty volume crashes startup. The initial keypair must be **seeded once** into the volume before
  first boot. Options for seeding:
  - **(A) Recommended:** store initial `private_key.pem` + `public_key.pem` as GitHub Actions
    secrets; a deploy step writes them into the volume on first launch only.
  - **(B)** A one-time init container / `openssl` job that generates the keypair into the volume.
> Open: pick seeding option (A) or (B). Note: the local `docker-compose.yml` currently defines
> only infra (Kafka + Postgres), no `auth_service` app container — wherever the auth app runs, its
> `/app/keys` must be a mounted volume, not an image path.

### 5.2 🟡 Tests may need Postgres/Kafka
Services use `spring-boot-starter-test` and have DB/Kafka deps. If any test is a full
`@SpringBootTest` that boots the context, `mvnw verify` fails in CI without infra. **Options:**
- Add **service containers** (Postgres/Kafka) to the test job, or
- Split unit vs. integration tests and run only unit tests in CI (fastest), or
- Use Testcontainers.
> Will verify per service during implementation; may start with unit-only + `-DskipITs`.

### 5.3 🟡 JDK version — build with 21 to match Docker
poms target Java 17 but Docker uses 21. CI test job will use **Temurin 21** (compiles 17 bytecode
fine) to match the runtime image and avoid surprises.

### 5.4 🟢 Deploy stage deferred
Prod target undecided (VPS / k8s / local). Deploy is a **single appended job** later; nothing in
stages 1–3 changes. `docker-compose.prod.yml` image refs will need to point at
`ghcr.io/<owner>/<svc>` when deploy is wired.

---

## 6. Implementation checklist

- [x] Decide key-handling for `auth_service` (§5.1) — runtime volume; `COPY *.pem` removed from Dockerfile
- [x] Confirm test strategy (§5.2) — **skip tests in CI**; Dockerfile builds jar with `-DskipTests`
- [x] Create `.github/workflows/<service>.yml` × 5 (build image → push GHCR, path-filtered)
- [x] Set `permissions: packages: write`; login to GHCR via `GITHUB_TOKEN`
- [x] Tag images `latest` + `<sha>` (via `docker/metadata-action`), push only on `main` (not PRs)
- [ ] (Optional) Add a shared reusable workflow to keep the 5 files DRY
- [ ] Verify first run green after pushing to GitHub; confirm packages are **public** in repo → Packages
- [ ] LATER: seed initial keypair into the auth `/app/keys` volume + add `/app/keys` mount to compose (§5.1)
- [ ] LATER: add deploy job + point `docker-compose.prod.yml` at GHCR images

---

## 7. Open questions for the user

1. **`auth_service` key seeding** — decided to move keys to a runtime volume (§5.1). Seed the
   *initial* keypair via (A) GitHub secrets written to the volume, or (B) an init/`openssl` job?
2. **Dockerfile change** — OK to remove the `COPY *.pem` lines from `auth_service/Dockerfile` now
   as part of this work?
3. **Tests in CI** — start unit-only and add integration infra later, or wire up service
   containers now?
4. **Package visibility** — public or private GHCR images?
