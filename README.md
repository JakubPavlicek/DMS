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