# Document Management System ![Static Badge](https://img.shields.io/badge/coverage-90%25-green?label=coverage)

Centralized document storage for developers and teams to efficiently manage project-related files.
Designed with simplicity and accessibility in mind, this Software as a Service (SaaS) offers centralized document storage with seamless access through a RESTful API.

## Table of Contents

- [Built With](#built-with)
- [Modules](#modules)
- [API](#api)
- [First Steps](#first-steps)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Development](#development)

## Built With

- [![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/en/)
- [![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io)
- [![OpenAPI](https://img.shields.io/badge/OpenAPI%20Initiative-6BA539.svg?style=for-the-badge&logo=OpenAPI-Initiative&logoColor=white)](https://www.openapis.org)
- [![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36.svg?style=for-the-badge&logo=Apache-Maven&logoColor=white)](https://maven.apache.org)
- [![LiquiBase](https://img.shields.io/badge/Liquibase-2962FF.svg?style=for-the-badge&logo=Liquibase&logoColor=white)](https://www.liquibase.org)
- [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=for-the-badge&logo=PostgreSQL&logoColor=white)](https://www.postgresql.org)
- [![Oracle](https://img.shields.io/badge/Oracle-F80000.svg?style=for-the-badge&logo=Oracle&logoColor=white)](https://www.oracle.com/database/)
- [![MSSQL](https://img.shields.io/badge/Microsoft%20SQL%20Server-CC2927.svg?style=for-the-badge&logo=Microsoft-SQL-Server&logoColor=white)](https://www.microsoft.com/en-us/sql-server/sql-server-2022)

## Modules

```
└── src
    ├── main
    │   ├── java
    │   │   └── com.dms
    │   │       ├── config                            // Config and properties validation
    │   │       ├── controller                        // REST API controller
    │   │       ├── entity                            // Database tables
    │   │       ├── exception                         // Exceptions and exception handling
    │   │       ├── mapper                            // Mapping between objects
    │   │       │   ├── dto                           // DTO mappers
    │   │       │   └── entity                        // Entity mappers
    │   │       ├── repository                        // DAOs
    │   │       ├── service                           // Service
    │   │       ├── specification                     // Specifications used for filtering
    │   │       ├── util                              // Utility classes
    │   │       ├── validation                        // Validation annotations
    │   │       └── DocumentManagerApplication.java   // Main application class
    │   └── resources
    │       ├── certs                                 // Generated RSA keys
    │       ├── db.changelog                          // Liquibase changelogs
    │       ├── static                                // OpenAPI files
    │       │   ├── example                           // OpenAPI examples
    │       │   ├── parameter                         // OpenAPI parameters
    │       │   ├── path                              // OpenAPI paths
    │       │   │   ├── documents                     // /documents paths
    │       │   │   ├── auth                          // /auth paths
    │       │   │   ├── revisions                     // /revisions paths
    │       │   │   └── users                         // /users paths
    │       │   ├── requestBody                       // OpenAPI requestBodies
    │       │   ├── response                          // OpenAPI responses
    │       │   ├── schema                            // OpenAPI schemas
    │       │   ├── templates                         // OpenAPI generator templates
    │       │   └── openapi.yaml                      // Main OpenAPI file 
    │       ├── application.yaml                      // Application config
    │       └── log4j2.yaml                           // Logger config
    └── test
        └── java
            └── com.dms
                ├── integration                       // integration tests
                └── unit                              // unit tests
```

## API

#### /auth/token
- `POST` : Request access token

#### /documents
- `GET` : List documents

#### /documents/upload
- `POST` : Upload document

#### /documents/{documentId}
- `GET` : Get document
- `PUT` : Upload new document version
- `DELETE` : Delete document

#### /documents/{documentId}/archive
- `PUT` : Archive document

#### /documents/{documentId}/restore
- `PUT` : Restore document

#### /documents/{documentId}/download
- `GET` : Download document

#### /document/{documentId}/move
- `PUT` : Move document

#### /documents/{documentId}/revisions
- `GET` : List document revisions

#### /documents/{documentId}/revisions/{revisionId}
- `PUT` : Switch document to revision

#### /revisions 
- `GET` : List revisions

#### /revisions/{documentId}
- `GET` : Get revision
- `DELETE` : Delete revision

#### /revisions/{documentId}/download
- `GET` : Download revision

#### /users
- `POST` : Create user

#### /users/me
- `GET` : Get current user

#### /users/password
- `PUT` : Change user password

## First Steps

1. Create the user.
   - This is achieved by providing the name, email, and password to the /users endpoint.
2. Obtain the access token so that you can use the API.
   - This is achieved by providing the email and password to the /auth/token endpoint.
3. After obtaining the access token, you will need to provide it in the header of each subsequent request.

<img alt="First steps" src="API_first_steps.svg" width="400"/>

After starting the application, you can access the API specification at http://localhost:8080/swagger-ui/index.html.

## Getting Started

### Prerequisites

Requirements for the software and other tools to build

- git
- Java 17+
- Maven 3.9.6+
- PostgreSQL / Oracle / MS SQL

### Installation

1. **Download and Open**:
    - Clone the application: `git clone https://github.com/JakubPavlicek/DMS.git`
    - Navigate to the downloaded directory and open it.

2. **Configuration**:
    - In **src/main/resources/application.yaml**, update these values:
        - **storage.path**: Local directory for file storage.
        - **storage.subdirectory-prefix-length**: Subdirectory prefix length.
        - **hash.algorithm**: Hashing algorithm.
        - **archive.retention-period-days**: Document retention period.
        - **token.expiration.time**: JWT token expiration time.
        - **rsa.private-key**: Filepath for private key (must end with .pem).
        - **rsa.public-key**: Filepath for public key (must end with .pem).
        - **server.error.path**: Suffix in URL for errors.
        - **admin.name**: Name of the ADMIN user.
        - **admin.email**: Email of the ADMIN user.
        - **admin.password**: Password of the ADMIN user.
        - **spring.profiles.active**: Choose your database:
            - **h2** for H2.
            - **postgresql** for PostgreSQL.
            - **oracle** for Oracle.
            - **mssql** for MS SQL.
        - Update database settings in chosen profile:
            - **spring.datasource.url**: Database URL.
            - **spring.datasource.username**: Database username.
            - **spring.datasource.password**: Database password.

3. **Generate JavaDoc documentation and JaCoCo report** (optional):
    - **Windows**: `mvnw.cmd install`
    - **Linux** and **macOS**: `./mvnw install` 

4. **Start the Application**:
    - On **Windows**: `mvnw.cmd spring-boot:run`
    - On **Linux** and **macOS**: `./mvnw spring-boot:run`

5. **Alternative** (if configurations are set):
    - To set a specific profile, use the following command:
        - **Windows**: `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgresql`
        - **Linux** and **macOS**: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgresql`

## Development

This application is a Bachelor's thesis (2023/24), authored by Jakub Pavlíček from the University of West Bohemia in Pilsen - Faculty of Applied Sciences.