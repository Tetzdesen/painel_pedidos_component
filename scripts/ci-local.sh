#!/usr/bin/env bash
# Roda localmente o mesmo gate do componente executado pelo workflow de qualidade.
set -euo pipefail

raiz="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "==> Componente: mvn -U clean verify (painel-pedidos/)"
(cd "$raiz/painel-pedidos" && mvn -B -U clean verify)

echo "==> Gate do componente OK"
