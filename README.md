# Demo Project - Movie & Actor Management

Ovo je demo Java Spring Boot projekt koji omogućuje upravljanje filmovima i glumcima putem REST API-ja. Projekt koristi OpenAPI specifikaciju za generiranje API sučelja i DTO objekata te uključuje sigurnosni sloj baziran na JWT tokenima.

## 📦 Tehnologije korištene

- Java 21
- Spring Boot
- Spring Security (JWT + Refresh token)
- JPA / Hibernate
- PostgreSQL (hostano na [Render](https://render.com))
- Gradle
- OpenAPI Generator
- Log4j2
- Postman (kolekcija za testiranje)

## 🚀 Pokretanje projekta

1. Očisti build (opcionalno):

```bash
./gradlew clean
```

2. Pokreni aplikaciju:

```bash
./gradlew bootRun
```

> 🔧 Projekt se automatski povezuje na udaljenu PostgreSQL bazu hostanu na Renderu. Nije potreban lokalni setup baze.

## 🗃️ Baza podataka

- Baza se automatski generira prvim pokretanjem aplikacije zahvaljujući `spring.jpa.generate-ddl=true`.
- Nema potrebe za ručnim pokretanjem SQL skripti.
- Baza je udaljena i hostana na Render servisu.
- Svi entiteti koriste soft delete (`active = 0` znači neaktivan).

## 🔐 Autentifikacija i sigurnost

- Prijava se vrši putem `/auth/authenticate`, a za osvježavanje tokena koristi se `/auth/refresh`.
- U odgovoru se vraća `token` i `refreshToken`, gdje `token` treba postaviti kao Bearer token za sve ostale pozive.
- U projektu su definirani korisnici: **admin** i **korisnik**, no uloga (rola) trenutno nije implementirana i nije korištena u logici aplikacije.

## 📬 Testiranje API-ja putem Postmana

📁 Uključena Postman kolekcija: `Comping.postman_collection.json`

Sadrži:

- Autentikaciju i dobivanje tokena
- Dodavanje, dohvat, ažuriranje i brisanje:
    - 🎥 Filmova (`/movies`)
    - 👤 Glumaca (`/actors`)
- Primjer poziva s i bez tokena
- Refresh token endpoint

## Jedinični testovi

```bash
./gradlew test
```

- `CoreServiceTest` — testira poslovnu logiku (mockovi)

## 📁 Struktura projekta

- `api/` – OpenAPI generirani interfejsi i modeli
- `domain/` – JPA entiteti
- `dto/` – Mapperi za pretvorbu između entiteta i DTO-a
- `services/` – Poslovna logika (CoreService)
- `repository/` – JPA repozitoriji
- `config/` – Sigurnosna i aplikacijska konfiguracija

## 📝 Napomena

- Korišten je `AbstractDomain` za sve entitete s audit poljima i soft-delete logikom (`active = 0`)
- M:M veza `Movie <-> Actor` implementirana je kroz eksplicitni entitet `MovieActor`
- OpenAPI YAML koristi se za automatsko generiranje REST sloja i modela (interfejs + DTO)

---

© 2025 – Comping Demo Project
