#!/bin/bash
set -o errexit

printf "\n🤖 Starting local environment...\n\n"

printf '\n📀 Create registry container unless it already exists\n\n'
reg_name='kind-registry'
reg_port='5001'
if [ "$(docker inspect -f '{{.State.Running}}' "${reg_name}" 2>/dev/null || true)" != 'true' ]; then
  docker run \
    -d --restart=always -p "127.0.0.1:${reg_port}:5000" --name "${reg_name}" \
    registry:2
fi

printf '\n📀 Create kind cluster called: azd-aks-workflow\n\n'
kind create cluster --name azd-aks-workflow --config ./local/kind-cluster-config.yaml

printf '\n📀 Connect the registry to the cluster network if not already connected\n'
if [ "$(docker inspect -f='{{json .NetworkSettings.Networks.kind}}' "${reg_name}")" = 'null' ]; then
  docker network connect "kind" "${reg_name}"
fi

printf '\n📀 Map the local registry to cluster\n\n'
kubectl apply -f ./local/deployments/config-map.yaml --wait=true


printf '\n📀 Deploy Redis\n\n'
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install redis -n default --set architecture=standalone bitnami/redis

printf '\n📀 Init Dapr\n\n'
dapr init --kubernetes --wait --timeout 600

printf '\n📀 Deploy Dapr Dashboard\n\n'
helm repo add dapr https://dapr.github.io/helm-charts/
helm repo update
helm install dapr-dashboard dapr/dapr-dashboard

printf '\n📀 Deploy state store component backed Redis\n\n'
kubectl apply -f ./local/components/state.yaml --wait=true


printf "\n🎉 Local environment setup completed!\n\n"