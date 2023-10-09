# DMS

## Document Management System

Jedná se o aplikaci typu SaaS (Software as a Service) pro centrální uložení dokumentů vzniklých při vývoji aplikací.

## Vývoj

Tato aplikace je Bakalářskou prací (v roce 2023), autorem je Jakub Pavlíček ze Západočeské univerzity v Plzni - Fakulta aplikovaných věd.

## Moduly

### src

- zdrojové kódy aplikace

### src/main

- 2 package:
  - **java** - Java kód
  - **resources** - konfigurační soubory

### src/main/java/com/dms

#### controller

- jednotlivé endpointy REST-API

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

#### storage

- blob storage, kam se budou ukládat bloby souborů

## Spuštění aplikace

1. Stažení aplikace: `git clone https://github.com/JakubPavlicek/DMS.git`
2. Přejděte do adresáře, kam jste si aplikaci stáhli a otevřete soubor **DocumentManager**
3. Spuštění aplikace:
   - Windows: `./mvnw.cmd spring-boot:run`
   - Linux: `./mvnw spring-boot:run`