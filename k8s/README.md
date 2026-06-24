# Kubernetes

The `k8s/base` directory contains production-shaped manifests for running EMS on Kubernetes.

Create environment-specific secrets before applying the base:

```bash
cp k8s/base/secret.example.yaml /tmp/ems-secret.yaml
# edit /tmp/ems-secret.yaml with real values
kubectl apply -f /tmp/ems-secret.yaml
```

The same rule applies to local Docker Compose runs: set `KEYCLOAK_ADMIN_PASSWORD` and other required secrets through your shell or an ignored `.env` file.

Apply manifests:

```bash
kubectl apply -k k8s/base
```

The base exposes the gateway through an NGINX ingress host:

```text
http://ems.local
```

The checked-in `secret.example.yaml` is documentation only and is not included in `kustomization.yaml`.
