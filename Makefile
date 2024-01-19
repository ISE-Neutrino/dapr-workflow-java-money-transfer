SHELL := /bin/bash

VERSION := 0.0.1
BUILD_INFO := Manual build

ENV_FILE := .env
ifeq ($(filter $(MAKECMDGOALS),config clean),)
	ifneq ($(strip $(wildcard $(ENV_FILE))),)
		ifneq ($(MAKECMDGOALS),config)
			include $(ENV_FILE)
			export
		endif
	endif
endif

.PHONY: help lint image push build run
.DEFAULT_GOAL := help

help: ## 💬 This help message :)
	@grep -E '[a-zA-Z_-]+:.*?## .*$$' $(firstword $(MAKEFILE_LIST)) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

####### LOCAL #############
start-local: ## 🧹 Setup local Kind Cluster
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/start-local-env.sh

start-aks: ## 🧹 Setup Azure K8s Cluster
	@echo -e "\e[34m$@\e[0m" || true
	@azd provision --environment azd-aks-workflow

deploy-local: ## 🚀 Deploy application resources locally
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/deploy-services.sh --local
	@echo -e "\e[34mYOU WILL NEED TO RUN \"make port-foward-local\" TO BE ABLE TO RUN TESTS\e[0m" || true

deploy-aks: ## 🚀 Deploy application resources in Azure
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/deploy-services.sh --azure


run-local: clean-local start-local deploy-local ## 💿 Run app locally

port-forward-local: ## ⏩ Forward the local port
	@echo -e "\e[34m$@\e[0m" || true
	@echo -e "\e[34mYOU WILL NEED TO START A NEW TERMINAL AND RUN  \"make test\"\e[0m" || true
	@kubectl port-forward service/public-api-service 8080:80 --pod-running-timeout=3m0s

dapr-dashboard: ## 🔬 Open the Dapr Dashboard
	@echo -e "\e[34m$@\e[0m" || true
	@dapr dashboard -k -p 9000 &
	
test: ## 🧪 Run tests, used for both local and aks development
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/test.sh

####### CLEAN #############
clean-local: ## 🧹 Clean up local files
	@echo -e "\e[34m$@\e[0m" || true
	@kind delete cluster --name azd-aks-workflow
	@docker rm kind-registry -f

clean-aks: ## 🧹 Clean up Azure AKS resources and deployments
	@echo -e "\e[34m$@\e[0m" || true
	@azd down --purge