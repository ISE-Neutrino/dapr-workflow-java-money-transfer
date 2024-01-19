#!/bin/bash

set -o errexit

usage() {
    echo "This script is not meant to be called directly. It's part of a flow"
    echo "and should be called from \"scripts/deploy-services.sh\""
    echo ""
    exit 1
}

failed() {
    printf "💥 Script failed: %s\n\n" "$1"
    usage
    exit 1
}

if [ $# -ne 1 ]; then
    failed "missing or wrong parameters"
    exit 1
elif [ "${1}" == "--azure" ]; then
    printf "\n🤖  Starting Azure deployments...\n\n"
    # get values from azd environment
    source <(azd env get-values)
    containerRegistry="${AZURE_CONTAINER_REGISTRY_NAME}.azurecr.io"

elif [ "${1}" == "--local" ]; then
    printf "\n🤖  Starting local deployments...\n\n"
    containerRegistry="localhost:5001"
else
    failed "invalid parameter: ${1}"
    exit 1
fi

serviceName="transfer-service"
version=$(date +%Y.%m.%d.%H.%M.%S)
imageName="${containerRegistry}/${serviceName}":"${version}"
printf "\n🛖  Releasing version: %s\n\n" "${imageName}"

# check if service deployment exists on cluster, deleting if it does
if [ $(kubectl get deployments | grep -c "^${serviceName}") -eq "1" ]; then
  printf "\n☢️  Attempting to delete existing deployment %s\n\n" "${serviceName}"
  kubectl delete deployment "${serviceName}"
fi

printf "\n🏗️  Building docker image\n\n"
docker build -t ${imageName} .

printf "\n🚚  Pushing docker image to container registry\n\n"
docker push ${imageName}

printf "\n🚀  Deploying to cluster\n\n"
cat <<EOF | kubectl apply -f -

apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${serviceName}
  labels:
    app: ${serviceName}
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ${serviceName}
  template:
    metadata:
      labels:
        app: ${serviceName}
      annotations:
        dapr.io/enabled: "true"
        dapr.io/app-id: "${serviceName}"
        dapr.io/app-port: "8080"
        dapr.io/enable-api-logging: "true"
    spec:
      containers:
      - name: node
        image: ${imageName}
        env:
        - name: APP_PORT
          value: "8080"
        - name: APP_VERSION
          value: "${version}"
        ports:
        - containerPort: 80
        imagePullPolicy: Always
        resources:
            limits:
              cpu: "512m"
              memory: "512Mi"
            requests:
              cpu: "100m"
              memory: "128Mi"
EOF


printf "\n🎉  Deployment complete\n\n"