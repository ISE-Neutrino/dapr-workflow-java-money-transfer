#!/bin/bash

set -o errexit

# FRONT_END_IP=localhost:8080
printf "\n🧪 Getting front end IP from cluster configuration\n\n"
FRONT_END_IP=$(kubectl get service public-api-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

if [ -z "$FRONT_END_IP" ]; then
  echo "⚠  Could not get front end IP - Assigning local IP"
  FRONT_END_IP=localhost:8080
fi

echo "Front end IP: $FRONT_END_IP"

# Create aacounts
echo -e "\n🧪 Creating accounts"
acc1=$(curl -X POST -s\
  http://${FRONT_END_IP}/accounts \
  -H 'Content-Type: application/json' \
  -d '{
    "owner": "A",
    "amount": 100
}' | jq)

acc2=$(curl -X POST -s\
  http://${FRONT_END_IP}/accounts \
  -H 'Content-Type: application/json' \
  -d '{
    "owner": "B",
    "amount": 100
}' | jq)


#show account balances
curl -X GET -s\
    http://${FRONT_END_IP}/accounts/A \
    -H 'Content-Type: application/json' | jq

curl -X GET -s\
    http://${FRONT_END_IP}/accounts/B \
    -H 'Content-Type: application/json' | jq


# transfer money
echo -e "\n🧪 Transfering money"
transfer=$(curl -X POST -s\
  http://${FRONT_END_IP}/transfers \
  -H 'Content-Type: application/json' \
  -d '{
    "sender": "A",
    "receiver": "B",
    "amount": 23
}' | jq)

# echo $transfer | jq

transferId=$(echo $transfer | jq -r '.transferId')
echo "TransferId: $transferId"

# Show results
echo -e "\n🧪 Showing results"
curl -X GET -s\
  http://${FRONT_END_IP}/transfers/${transferId} \
  -H 'Content-Type: application/json' | jq

curl -X GET -s\
    http://${FRONT_END_IP}/accounts/A \
    -H 'Content-Type: application/json' | jq

curl -X GET -s\
    http://${FRONT_END_IP}/accounts/B \
    -H 'Content-Type: application/json' | jq