# Java Workflow Sample using DAPR
Initial playgroud to work with DAPR workflows in Java

This sample is a simple money transfer application built on Java Spring Boot, leveraging DAPR workflows to manage the creation of accounts and facilitate money transfers between accounts. The project focuses on simplicity, reliability, and extensibility, utilizing DAPR for state management and implementing various workflow patterns.

## Features

- REST API Endpoints
    - Create Accounts
    - Get Account Information
    - Transfer Money between Accounts
    - Get Transfer Request Information

- State Store
    - Utilizes a Redis key-value map as the state store for efficient data management.

- DAPR Workflows
    - Leverages DAPR workflows to handle the creation of accounts and money transfer functionalities.
    - Implements workflow patterns such as Chain and Fanout for enhanced functionality.
- Java Version:
    - Developed using Java 17.

## Getting Started

### Prerequisites
- Java 17
- [Springboot](https://spring.io/projects/spring-boot)
- [DAPR CLI](https://docs.dapr.io/getting-started/install-dapr-cli/)
- Redis

Alternatively, you can use DevContainers and Visual Studio Code for local development. Opening the project within a devContainer will automatically install all the required tools and extensions.


## Execution

We use Make commands to automate the build and deployment process. You can run the following command to see the available commands:

```bash
make help
```
The following commands are available:
```bash
help                 💬 This help message :)
init-dapr            🧹 Initialize Dapr
stop-dapr            🧹 Uninstall Dapr
build                🧹 Build application
clean                🧹 Clean compilation files
start-client         🚀 Start client
run                  💿 Run app locally
dapr-dashboard       🔬 Open the Dapr Dashboard
test                 🧪 Run tests in local development

```

### Initialize DAPR

```bash
make init-dapr  ## needed only for the first time
```

### Run the application locally

```bash
make run
```

### Access the DAPR Dashboard:

```bash
make dapr-dashboard
```

## Usage

### REST API Endpoints

- Create Account:
    - Endpoint: **POST /create**
    - Request Body: JSON with account details.
        ```json
        {
            "owner": "B",
            "amount": "100"
        }
        ```

- Get Account Information:
    - Endpoint: **GET /accounts/{accountId}**
    - Returns JSON with account details.

- Transfer Money:
    - Endpoint: **POST /transfer**
    - Request Body: JSON with transfer details (source, destination, amount).
        ```json
        {
            "sender": "A",
            "receiver": "B",
            "amount": "23"
        }
        ```

- Get Transfer Information:
    - Endpoint: **GET /transfers/{transferId}**
    - Returns JSON with transfer details.
