#!/bin/bash

set -o errexit

echo "📀 - Post-Provision hook - Installing DAPR and Redis components in AKS..." 

# load environment variables from azd deployment
source <(azd env get-values)

# updating kubeconfig with cluster credentials
az aks get-credentials --resource-group $AZURE_RESOURCE_GROUP_NAME --name $AZURE_AKS_CLUSTER_NAME --overwrite-existing

az config set extension.use_dynamic_install=yes_without_prompt

# check if DAPR namespace exists on cluster, and skip installation if it does
if [ $(kubectl get namespaces | grep -c "^dapr-system ") -eq "0" ]; then
    echo "- Installing DAPR on AKS..." 
    az k8s-extension create --cluster-type managedClusters \
        --cluster-name $AZURE_AKS_CLUSTER_NAME \
        --resource-group $AZURE_RESOURCE_GROUP_NAME \
        --name myDaprExtension \
        --extension-type Microsoft.Dapr
else
    echo "\t - DAPR already installed on AKS..."
fi 

printf '\n📀 Deploy Dapr Dashboard\n\n'
helm repo add dapr https://dapr.github.io/helm-charts/
helm repo update
helm install dapr-dashboard dapr/dapr-dashboard

printf '\n📀 Deploy Redis on AKS\n\n'
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install redis --set architecture=standalone bitnami/redis

printf '\n🚀 Deploy state store component backed Redis\n\n'
kubectl apply -f ./local/components/state.yaml --wait=true


printf "\n🎉 AKS environment setup completed!\n\n"