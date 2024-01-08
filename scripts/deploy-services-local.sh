#!/bin/sh
set -o errexit

printf "\n🤖  Starting local deployment...\n\n"

printf '\n🎖️  Deploying Money Transfer Service\n\n'
# cd ./src
sh ./local-deploy.sh


