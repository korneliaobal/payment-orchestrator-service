# ObalFlow Payment Orchestrator

Coordinates Kafka validation events, stores payment and transaction details in PostgreSQL, and exposes status and history endpoints. Authorization decisions are persisted in separate tables through Flyway migrations.

## Railway deployment

Deploy this repository as an individual Railway service. The root `railway.toml` selects `Dockerfile.railway`, which builds the pinned public `payment-contracts` source revision before building the orchestrator. No Maven settings file, GitHub token, secret mount, or sibling checkout is required for this build. Update the archive revision together with the contracts revision pinned in CI when the shared contract API changes.

The regular `Dockerfile` remains available for local Compose builds and standalone GitHub Packages builds. Railway uses the separate Dockerfile because its builder rejects the secret mounts in the regular file.

In the service's **Variables** tab, configure:

| Variable | Value |
| --- | --- |
| `SPRING_DATASOURCE_URL` | A JDBC PostgreSQL URL, for example `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | The reachable Kafka bootstrap address, including its port |
| `SPRING_KAFKA_PROPERTIES_SCHEMA_REGISTRY_URL` | The reachable Schema Registry HTTP URL |
| `SPRING_JPA_SHOW_SQL` | `false` |

Replace `Postgres` in reference variables with the actual Railway database service name. Do not use a `postgresql://` URL directly as `SPRING_DATASOURCE_URL`; the PostgreSQL JDBC driver expects `jdbc:postgresql://`.

The application listens on Railway's `PORT` variable and uses port 8082 locally when `PORT` is absent. `railway.toml` checks `/api/payment-history` for a successful HTTP response. This checks the HTTP application and database, but does not prove Kafka consumers are connected. Inspect the logs for Kafka connection and partition assignment messages as well. Kafka's advertised listener addresses must be reachable from the orchestrator; a reachable bootstrap endpoint alone is insufficient.

Railway reference variables and networking settings must be applied in the dashboard before deploying. PostgreSQL, Kafka, and Schema Registry must be available. `localhost`, `kafka`, and `schema-registry` addresses from local Docker Compose do not identify Railway services. Use their actual service addresses. Supply Kafka authentication and TLS settings as additional `SPRING_KAFKA_*` variables if required by the provider.

Flyway runs at startup. New databases receive all migrations; an existing project schema is baselined at version 1, and subsequent migrations move statuses and transaction reasons into authorization tables. Back up an existing database before its first deployment with these migrations. Do not deploy an older image after migrating away from the original status columns.

Build the Railway image locally without credentials:

```bash
docker build -f Dockerfile.railway -t obalflow-orchestrator:railway .
```

For a Railway service connected to this repository, leave the root directory at the repository root and do not override the Dockerfile path with `Dockerfile`. The `railway.toml` file supplies the intended path. If a different config file is selected in the dashboard, select `/railway.toml`.

References: [Railway Dockerfiles](https://docs.railway.com/builds/dockerfiles), [configuration as code](https://docs.railway.com/config-as-code/reference), and [healthchecks](https://docs.railway.com/deployments/healthchecks).

## Frontend access

The status and history controllers allow cross-origin requests from `http://localhost:4200`. The Angular frontend uses the orchestrator public domain for both endpoints. When deploying the frontend, add its actual origin to the CORS allowlist.

## Local verification

Install the sibling `payment-contracts` project first, then run:

```bash
./mvnw spotless:check test
```

Start the local backend through `payment-api-service/docker-compose.yml`.
