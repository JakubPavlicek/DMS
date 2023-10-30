# Document Management System

Centralized document storage for developers and teams to efficiently manage project-related files.
Designed with simplicity and accessibility in mind, this Software as a Service (SaaS) offers centralized document storage with seamless access through a RESTful API.

## Table of Contents

- [Build with](#built-with)
- [Modules](#modules)
- [API](#api)
- [Getting Started](#getting-started)
  - [Prerequisited](#prerequisites)
  - [Installation](#installation)
- [Development](#development)

## Built With

- ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
- ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
- ![OpenAPI](https://img.shields.io/badge/OpenAPI%20Initiative-6BA539.svg?style=for-the-badge&logo=OpenAPI-Initiative&logoColor=white)
- ![LiquiBase](https://img.shields.io/badge/Liquibase-2962FF.svg?style=for-the-badge&logo=Liquibase&logoColor=white)
- ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1.svg?style=for-the-badge&logo=PostgreSQL&logoColor=white)
- ![Oracle](https://img.shields.io/badge/Oracle-F80000.svg?style=for-the-badge&logo=Oracle&logoColor=white)
- ![MSSQL](https://img.shields.io/badge/Microsoft%20SQL%20Server-CC2927.svg?style=for-the-badge&logo=Microsoft-SQL-Server&logoColor=white)

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
    │   │       ├── filter                            // filtering in requests
    │   │       ├── hash                              // hasher for blobs
    │   │       ├── mapper                            // mapping between objects
    │   │       ├── repository                        // DAOs
    │   │       ├── service                           // service
    │   │       ├── sort                              // sorting in requests
    │   │       ├── specification                     // specification used for filtering
    │   │       └── DocumentManagerApplication.java   // Main application class
    │   └── resources
    │       ├── db.changelog                          // LiquiBase changelogs
    │       ├── static                                // OpenAPI files
    │       │   ├── example                           // OpenAPI examples
    │       │   ├── parameter                         // OpenAPI parameters
    │       │   ├── response                          // OpenAPI responses
    │       │   ├── schema                            // OpenAPI schemas
    │       │   └── openapi.yaml                      // main OpenAPI file 
    │       ├── application.yaml                      // SpringBoot + application config
    │       └── log4j2.yaml                           // Logger config
    └── test

```

## API

#### /documents
- `GET` : Retrieve a list of the documents

#### /documents/upload
- `POST` : Upload a document

#### /documents/{documentId}
- `GET` : Retrieve a document
- `PUT` : Upload new document version
- `DELETE` : Delete a document and all revisions associated to the document

#### /documents/{documentId}/download
- `GET` : Download a document

#### /document/{documentId}/move
- `PUT` : Move a document

#### /documents/{documentId}/revisions
- `GET` : Retrieve a list of document revisions

#### /documents/{documentId}/revisions/{revisionId}
- `PUT` : Switch document to a specific revision 

#### /documents/{documentId}/versions
- `GET` : Retrieve a list of document versions

#### /documents/{documentId}/versions/{version}
- `GET` : Retrieve a document with a specific version
- `PUT` : Switch document to a specific version 

#### /revisions 
- `GET` : Retrieve a list of revisions

#### /revisions/{documentId}
- `GET` : Retrieve a revision
- `DELETE` : Delete a revision

#### /revisions/{documentId}/download
- `GET` : Download a revision

## Getting Started

### Prerequisites

Requirments for the software and other tools to build

- Java 17+
- Maven
- PostgreSQL / Oracle / MS SQL

### Installation

1. Stažení aplikace: `git clone https://github.com/JakubPavlicek/DMS.git`
2. Přejděte do adresáře, kam jste si aplikaci stáhli a otevřete soubor **DocumentManager**
3. V souboru **src/main/resources/application.yaml** změňte hodnoty:
    - **storage.path** - Váš lokální adresář, kam se budou ukládat bloby souborů
    - **storage.directory-prefix-length** - Délka prefixu pro adresáře
    - **hash.algorithm** - Můžete nechat výchozí, či pozměnit na základě toho, jaký hashovací algoritmus chcete použít
    - **spring.profiles.active** - hodnota, představující, jakou databázi chcete použít:
      - **postgresql** - použije se PostgreSQL databáze
      - **oracle** - použije se Oracle databáze
      - **mssql** - použije se MS SQL databáze
    - dále je nutné změnit hodnoty pro databázi, kterou jste si vybrali a kterou chcete použít:
      - **spring.datasource.url** - url databáze
      - **spring.datasource.username** - přihlašovací jméno do databáze
      - **spring.datasource.password** - heslo do databáze
4. Spuštění aplikace:
   - Windows: `./mvnw.cmd spring-boot:run`
   - Linux: `./mvnw spring-boot:run`
   - macOS: `./mvnw spring-boot:run`
   - alternativa (v případě, že již máte nastaveny správne hodnoty v konfiguračním souboru **application.yaml**):
     - hodnota **spring-boot.run.profiles** znamená, jaký profil chcete nastavit (povolené hodnoty: **postgresql** / **mssql** / **oracle**)
       - Windows `./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgresql`
       - Linux `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgresql`
       - macOS `./mvnw spring-boot:run -Dspring-boot.run.profiles=postgresql`

## Development

This application is a Bachelor's thesis (in 2023), authored by Jakub Pavlíček from the University of West Bohemia in Pilsen - Faculty of Applied Sciences.