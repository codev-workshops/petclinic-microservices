# Kubernetes manifests (Kustomize)

Deploys the six core PetClinic services: `config-server`, `discovery-server`,
`api-gateway`, `customers-service`, `visits-service`, `vets-service`.

```
k8s/
├── base/                 # namespace-agnostic manifests
│   ├── configmap.yaml    # shared env: CONFIG_SERVER_URL, Eureka URL, active profile
│   ├── ingress.yaml      # "/" and "/api" -> api-gateway:8080
│   ├── <service>/        # deployment.yaml, service.yaml, (hpa.yaml)
│   └── kustomization.yaml
└── overlays/
    ├── dev/              # namespace petclinic-dev, 1 replica, small requests
    └── prod/             # namespace petclinic-prod, pinned image tags, bigger HPAs
```

## Deploy

```bash
kubectl kustomize k8s/overlays/dev          # render / review
kubectl apply -k k8s/overlays/dev           # or: k8s/overlays/prod
kubectl -n petclinic-dev get pods -w
```

Point the Ingress host (`dev.petclinic.local` / `petclinic.example.com`, patched in
each overlay) at your ingress controller and open `http://<host>/`.

## Startup order

Every Spring service imports its configuration from `config-server`, and all
services except `config-server` register with Eureka on `discovery-server`.
The logical order is therefore:

1. `config-server` (8888)
2. `discovery-server` (8761)
3. `api-gateway`, `customers-service`, `visits-service`, `vets-service`

Kubernetes has no `depends_on`; ordering is enforced *eventually* instead:

- **Readiness probes** keep a pod out of its Service until it is actually serving,
  so the gateway is never routed to a backend that has not finished bootstrapping.
- **Restart backoff**: a service that starts before `config-server` fails fast and
  is restarted by the kubelet with exponential backoff until config is reachable.
  Probes use generous `initialDelaySeconds` / `failureThreshold` because Spring Boot
  apps are slow to start and block on the config server.
- **Optional init container** (`wait-for-config-server`) polls `CONFIG_SERVER_URL`
  before the JVM starts. It is a convenience to reduce crash-loop noise, not the
  primary mechanism; remove it if you prefer pure probe-based convergence.

Services resolve each other by k8s Service DNS names, which match the docker-compose
host names (`config-server`, `discovery-server`), so the existing `docker` Spring
profile works unchanged. `SPRING_PROFILES_ACTIVE` is extended per overlay
(`docker,dev` / `docker,prod`) so environment-specific properties can be added to the
config repo without touching these manifests.

## Scaling

- `api-gateway` and the business services have an `autoscaling/v2` HPA on CPU
  (70% target). Base: 2–5 replicas; dev: 1–2; prod: 3–10.
- `config-server` and `discovery-server` have **no HPA** and a fixed single replica.
  They are infrastructure singletons: the config server is a stateless-but-cheap
  bootstrap dependency that gains nothing from CPU-based scaling, and a Eureka
  server needs explicit peer-awareness configuration to run as a cluster —
  scaling it blindly would produce independent registries with inconsistent
  views. Scale them deliberately (with peer config) rather than via HPA.

## Health-check caveat

`api-gateway`, `customers-service`, `visits-service` and `vets-service` use the
Actuator probe endpoints:

- liveness:  `GET /actuator/health/liveness`
- readiness: `GET /actuator/health/readiness`

These endpoints exist only when `management.endpoint.health.probes.enabled=true`
(auto-enabled on Kubernetes by Spring Boot) **and** the `health` endpoint is exposed
over HTTP. Those settings live in the external
[`spring-petclinic-microservices-config`](https://github.com/spring-petclinic/spring-petclinic-microservices-config)
repository, not in this one — verify them against a running instance
(`kubectl port-forward` then `curl :8081/actuator/health/readiness`) and fall back to
`/actuator/health` or a `tcpSocket` probe if they are not available.

`config-server` and `discovery-server` do not declare the Actuator dependency, so
they use `GET /` on 8888/8761, mirroring the docker-compose health checks.

## Not included

`genai-service`, `admin-server`, Zipkin, Prometheus and Grafana from
`docker-compose.yml` are intentionally out of scope for these manifests.
