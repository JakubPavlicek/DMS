# DMS

## Document Management System

Jedná se o aplikaci typu SaaS (Software as a Service) pro centrální uložení dokumentů vzniklých při vývoji aplikací.

## Vývoj

Tato aplikace je Bakalářskou prací (v roce 2023), autorem je Jakub Pavlíček ze Západočeské univerzity v Plzni - Fakulta aplikovaných věd.

## Moduly

```
└── src
    ├── main
    │   ├── java
    │   │   └── com.dms
    │   │       ├── config                              // configurations and properties validation
    │   │       ├── controller                          // REST-API controller
    │   │       ├── entity                              // database tables
    │   │       ├── exception                           // exceptions and exception handling
    │   │       ├── filter                              // filtering in requests
    │   │       ├── hash                                // hasher for blobs
    │   │       ├── mapper                              // mapping between objects
    │   │       ├── repository                          // DAOs
    │   │       ├── service                             // service
    │   │       ├── sort                                // sorting in requests
    │   │       ├── specification                       // specification used for filtering
    │   │       └── DocumentManagerApplication.java     // Main application class
    │   └── resources
    │       ├── db.changelog                            // LiquiBase changelogs
    │       ├── static                                  // OpenAPI files
    │       │   ├── example                             // OpenAPI examples
    │       │   ├── parameter                           // OpenAPI parameters
    │       │   ├── response                            // OpenAPI responses
    │       │   ├── schema                              // OpenAPI schemas
    │       │   └── openapi.yaml                        // main OpenAPI file 
    │       ├── application.yaml                        // SpringBoot + application config
    │       └── log4j2.yaml                             // Logger config
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

#### /documents/{documentId}/versions [GET]
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

## Spuštění aplikace

1. Stažení aplikace: `git clone https://github.com/JakubPavlicek/DMS.git`
2. Přejděte do adresáře, kam jste si aplikaci stáhli a otevřete soubor **DocumentManager**
3. V souboru **src/main/resources/application.yaml** změňte hodnoty:
    - **storage.path** - Váš lokální adresář, kam se budou ukládat bloby souborů
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