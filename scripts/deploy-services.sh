
#!/bin/bash
set -o errexit

usage() {
    echo ""
    echo "usage: $0 [option]"
    echo ""
    echo "Choose one of the following options:"
    echo "  --azure            Deploy services in Azure"
    echo "  --local            Test services locally"
    echo ""
    echo "Example:"
    echo "  $0 --azure"
    echo "  $0 --local"
    echo ""
    exit 1
}

failed() {
    printf "💥 Script failed: %s\n\n" "$1"
    usage
    exit 1
}

# parse parameters

if [ $# -ne 1 ]; then
    usage
    exit 1
fi

azure_deployment=0
case "${1}"  in
    --azure) ;; #expected switch for azure deployment
    --local) ;; #expected switch for local deployment
    *) failed "Invalid option: $1";;
esac

printf "\n🤖  Starting deployments...\n\n"

printf '\n🎖️  Deploying Public API Service\n\n'
cd ./src/public-api-service
./service-deploy.sh ${1}

printf '\n ================================== \n\n'

printf '\n🎖️  Deploying Account Service\n\n'
cd ../account-service
./service-deploy.sh ${1}

printf '\n ================================== \n\n'

printf '\n🎖️  Deploying Transfer Service\n\n'
cd ../transfer-service
./service-deploy.sh ${1}
