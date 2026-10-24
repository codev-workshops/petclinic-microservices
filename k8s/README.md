# Kubernetes manifests (Kustomize)

Deploys the core PetClinic microservices: config-server, discovery-server, api-gateway,
customers-service, visits-service and vets-service. genai-service and admin-server are not included.

```
k8s/
├── base/                     # per service: deployment.yaml, service.yaml, hpa.yaml, application.yml
│   ├── kustomization.yaml    # ConfigMaps are generated here (hash suffix => rollout on change)
│   └── ingress.yaml          # the only external entry point -> api-gateway
└── overlays/
    ├── dev/                  # petclinic-dev: small requests, 1 replica min, HSQLDB, host petclinic.localhost
    └── prod/                 # petclinic-prod: HA replicas, PDBs, zone/host spread, MySQL, TLS host
```

## What each service gets

| Service | Port | Probes | HPA (base / dev / prod) | Role |
|---|---|---|---|---|
| config-server | 8888 | actuator liveness/readiness | 1-2 / 1-1 / 2-3 @80% CPU | infrastructure, busy only while clients start |
| discovery-server | 8761 | actuator liveness/readiness | 1-1 everywhere | Eureka is standalone (no peer replication), so it is pinned to one replica |
| api-gateway | 8080 (actuator on 8090) | actuator liveness/readiness on 8090 | 2-6 / 1-3 / 3-12 @60% CPU | edge, scales first and fastest (no scale-up stabilization) |
| customers-service | 8081 | actuator liveness/readiness | 2-5 / 1-2 / 2-8 @70% CPU | read/write business service |
| visits-service | 8082 | actuator liveness/readiness | 2-5 / 1-2 / 2-8 @70% CPU | read/write business service |
| vets-service | 8083 | actuator liveness/readiness | 1-3 / 1-2 / 2-4 @75% CPU | read-mostly, cached |

- **Probes**: `startupProbe` and `livenessProbe` hit `/actuator/health/liveness`, `readinessProbe` hits
  `/actuator/health/readiness`. The startup probe allows up to 3 minutes for JVM start + Config Server fetch.
- **ConfigMaps**: `<service>-config` holds the Kubernetes-specific `application.yml` (ports, Config Server
  and Eureka URLs, probe groups, actuator exposure), mounted at `/etc/petclinic/config` and loaded through
  `SPRING_CONFIG_ADDITIONAL_LOCATION`. These values override the Config Server ones because the config
  repo sets `spring.cloud.config.override-none: true`. `petclinic-env` holds env vars shared by all services.
- **Startup order**: every service except config-server has a `wait-for-config-server` init container, since
  the `docker` profile makes the Config Server import mandatory.
- **Ingress**: routes `/` to `api-gateway`, which serves the UI and forwards `/api/**` via Eureka. The
  gateway's actuator runs on a separate port so it is not reachable through the Ingress.
- Deployments omit `replicas` so `kubectl apply` never fights the HPA.

## Prerequisites

- An ingress-nginx controller (`ingressClassName: nginx`) and metrics-server (for the HPAs).
- Images: `./mvnw clean install -P buildDocker` builds `springcommunity/spring-petclinic-<service>:latest`
  from this checkout. The upstream Docker Hub images don't include the actuator in config-server, which the
  probes need.

## Dev

```bash
./mvnw clean install -P buildDocker -DskipTests
# kind example: kind load docker-image springcommunity/spring-petclinic-{config-server,discovery-server,api-gateway,customers-service,visits-service,vets-service}:latest
kubectl apply -k k8s/overlays/dev
kubectl -n petclinic-dev get pods,hpa
curl http://petclinic.localhost/api/gateway/owners/1
```

Dev keeps the in-memory HSQLDB, so each customers/visits replica has its own data. Use prod (MySQL) for
consistent writes across replicas.

## Prod

1. Push images tagged `4.0.1` to your registry and point `images:` in `overlays/prod/kustomization.yaml`
   at it (`kustomize edit set image ...`).
2. Provide MySQL reachable as `petclinic-mysql:3306` (or change `SPRING_DATASOURCE_URL`) and create the
   credentials Secret (not committed):
   ```bash
   kubectl create namespace petclinic-prod
   kubectl -n petclinic-prod create secret generic petclinic-db-credentials \
     --from-literal=username=petclinic --from-literal=password='<password>'
   ```
   The backend services run with the `docker,mysql` profiles and create their tables on start
   (`spring.sql.init.mode=ALWAYS`). visits-service restarts until customers-service has created `pets`.
3. Create the TLS Secret `petclinic-tls` for `petclinic.example.com` (or let cert-manager issue it) and
   change the host in the overlay.
4. `kubectl apply -k k8s/overlays/prod`

## Validate

```bash
kubectl kustomize k8s/overlays/dev  >/dev/null
kubectl kustomize k8s/overlays/prod >/dev/null
```
