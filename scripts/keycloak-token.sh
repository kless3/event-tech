#!/usr/bin/env bash
set -euo pipefail

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8088}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-ems}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-ems-api-gateway}"
USERNAME="${1:-${KEYCLOAK_USERNAME:-}}"
PASSWORD="${2:-${KEYCLOAK_PASSWORD:-}}"

if [[ -z "${USERNAME}" || -z "${PASSWORD}" ]]; then
  printf 'Usage: %s <username> <password>\n' "$0" >&2
  printf 'Or set KEYCLOAK_USERNAME and KEYCLOAK_PASSWORD.\n' >&2
  exit 1
fi

response="$(
  curl -fsS \
    -d "client_id=${KEYCLOAK_CLIENT_ID}" \
    -d "username=${USERNAME}" \
    -d "password=${PASSWORD}" \
    -d "grant_type=password" \
    "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token"
)"

if command -v jq >/dev/null 2>&1; then
  printf '%s\n' "${response}" | jq -r '.access_token'
else
  printf '%s\n' "${response}"
fi
