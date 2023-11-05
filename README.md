# Document Management System

Centralized document storage for developers and teams to efficiently manage project-related files.
Designed with simplicity and accessibility in mind, this Software as a Service (SaaS) offers centralized document storage with seamless access through a RESTful API.

## Table of Contents

- [Build With](#built-with)
- [Modules](#modules)
- [API](#api)
- [Getting Started](#getting-started)
  - [Prerequisited](#prerequisites)
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
    │   │       ├── config                            // config and properties validation
    │   │       ├── controller                        // REST-API controller
    │   │       ├── entity                            // database tables
    │   │       ├── exception                         // exceptions and exception handling
    │   │       ├── hash                              // hasher for blobs
    │   │       ├── mapper                            // mapping between objects
    │   │       │   ├── dto                           // dto mappers
    │   │       │   └── entity                        // entity mappers
    │   │       ├── repository                        // DAOs
    │   │       ├── service                           // service
    │   │       ├── specification                     // specification used for filtering
    │   │       ├── validation                        // validation annotations
    │   │       └── DocumentManagerApplication.java   // main application class
    │   └── resources
    │       ├── db.changelog                          // LiquiBase changelogs
    │       ├── static                                // OpenAPI files
    │       │   ├── example                           // OpenAPI examples
    │       │   ├── parameter                         // OpenAPI parameters
    │       │   ├── path                              // OpenAPI paths
    │       │   │   ├── documents                     // /documents paths
    │       │   │   └── revisions                     // /revisions paths
    │       │   ├── requestBody                       // OpenAPI requestBodies
    │       │   ├── schema                            // OpenAPI schemas
    │       │   ├── templates                         // OpenAPI generator templates
    │       │   └── openapi.yaml                      // main OpenAPI file 
    │       ├── application.yaml                      // SpringBoot + application config
    │       └── log4j2.yaml                           // Logger config
    └── test
```

## API

#### /documents
- `GET` : List documents

#### /documents/upload
- `POST` : Upload document

#### /documents/{documentId}
- `GET` : Get document
- `PUT` : Upload new document version
- `DELETE` : Delete document

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

## Getting Started

### Prerequisites

Requirements for the software and other tools to build

- git
- Java 17+
- Maven
- PostgreSQL / Oracle / MS SQL

### Installation

1. **Download and Open**:
    - Clone the application: `git clone https://github.com/JakubPavlicek/DMS.git`
    - Navigate to the downloaded directory and open **DocumentManager**.

2. **Configuration**:
    - In **src/main/resources/application.yaml**, update these values:
        - **storage.path**: Local directory for file storage.
        - **storage.directory-prefix-length**: Directory prefix length.
        - **hash.algorithm**: Hashing algorithm (optional).
        - **spring.profiles.active**: Choose your database:
            - **postgresql** for PostgreSQL.
            - **oracle** for Oracle.
            - **mssql** for MS SQL.
        - Update database settings:
            - **spring.datasource.url**: Database URL.
            - **spring.datasource.username**: Database username.
            - **spring.datasource.password**: Database password.

3. **Start the Application**:
    - On **Windows**: `./mvnw.cmd spring-boot:run`
    - On **Linux** and **macOS**: `./mvnw spring-boot:run`

4. **Alternative** (if configurations are set):
    - To set a specific profile, use the following command:
        - **Windows**: `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgresql`
        - **Linux** and **macOS**: `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgresql`

## Development

This application is a Bachelor's thesis (in 2023), authored by Jakub Pavlíček from the University of West Bohemia in Pilsen - Faculty of Applied Sciences.