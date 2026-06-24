# Kubernetes

The `k8s/base` directory contains production-shaped manifests for running EMS on Kubernetes.

Create environment-specific secrets before applying the base:

```bash
cp k8s/base/secret.example.yaml /tmp/ems-secret.yaml
# edit /tmp/ems-secret.yaml with real values
kubectl apply -f /tmp/ems-secret.yaml
```

Apply manifests:

```bash
kubectl apply -k k8s/base
```

The checked-in `secret.example.yaml` is documentation only and is not included in `kustomization.yaml`.
