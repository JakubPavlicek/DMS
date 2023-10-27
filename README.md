# DMS

## Document Management System

Jedná se o aplikaci typu SaaS (Software as a Service) pro centrální uložení dokumentů vzniklých při vývoji aplikací.

## Vývoj

Tato aplikace je Bakalářskou prací (v roce 2023), autorem je Jakub Pavlíček ze Západočeské univerzity v Plzni - Fakulta aplikovaných věd.

## Endpointy

### /documents [GET]

- získání souborů

### /documents/upload [POST]

- nahrání souboru

### /documents/{documentId} [GET]

- získání informace o souboru

### /documents/{documentId} [PUT]

- nahrání změny souboru

### /documents/{documentId} [DELETE]

- smazání souboru

### /documents/{documentId}/download [GET]

- stažení souboru

### /document/{documentId}/move [PUT]

- přesunutí souboru

### /documents/{documentId}/revisions [GET]

- získání revizí souboru

### /documents/{documentId}/revisions/{revisionId} [GET]

- přepnutí se na revizi

### /documents/{documentId}/versions [GET]

- získání verzí souboru

### /documents/{documentId}/versions/{version} [GET]

- získání souboru dané verze

### /documents/{documentId}/versions/{version} [PUT]

- přepnutí se na verzi souboru

### /revisions [GET]

- získání informací o všech revizích

### /revisions/{documentId} [GET]

- získání informace o revizi

### /revisions/{documentId} [DELETE]

- smazání revize

### /revisions/{documentId}/download [GET]

- stažení revize

## Moduly

### src

- zdrojové kódy aplikace

### src/main

- 2 package:
  - **java** - Java kód
  - **resources** - konfigurační soubory

### src/main/java/com/dms

#### config

- konfigurace Springu + přepravky na hodnoty v konfiguračním souboru

#### controller

- jednotlivé endpointy REST-API

#### dto

- data transfer objects

#### entity

- tabulky databáze

#### exception

- odchytávání výjimek
- výjimky

#### hash

- hashovací algoritmus

#### repository

- přístup k databázi

#### service

- business logic

#### specification

- specifikace k filtrování hodnot

#### validation

- validace hodnot z requestu

### src/main/resources

#### db.changelog

- LiquiBase soubory

#### static

- OpenAPI

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