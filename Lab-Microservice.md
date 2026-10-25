# Lab: Erstellen eines einfachen Microservices mit Spring Boot und SQLite

In diesem Lab erstellen Sie einen einfachen Microservice mit Spring Boot. Der Service soll es ermöglichen, Nachrichten (Messages) zu speichern und wieder abzurufen. Die Nachrichten werden in einer SQLite-Datenbank gespeichert. Sie werden REST-API-Endpunkte erstellen, um Nachrichten mithilfe von **POST** und **GET** zu verwalten.

## Lernziele

Nach diesem Lab können Sie

- ein Spring-Boot-Projekt mit dem Spring Initializr anlegen und die Projektstruktur erklären,
- eine JPA-Entität auf eine Datenbanktabelle abbilden,
- ein Spring-Data-Repository verwenden, ohne SQL zu schreiben,
- REST-Endpunkte mit `@RestController` bereitstellen und mit einem REST-Client testen,
- benennen, welche Rolle Build-Werkzeug, eingebetteter Webserver und O/R-Mapper dabei spielen.

## KI-Regel für dieses Lab

Es gilt die Regel von der Aufgabenfolie im Foliensatz *05 Implementierung*. 

## API-Endpunkte

1. **POST /api/messages**: Fügt eine neue Nachricht hinzu.
   - **Beschreibung**: Dieser Endpunkt speichert eine neue Nachricht in der Datenbank.
   - **Anfrage-Body**: JSON-Objekt, z. B. `{ "content": "Ihre Nachricht hier" }`
   - **Antwort**: HTTP 201 Created, im Body das gespeicherte Nachrichtenobjekt als JSON, inkl. automatisch generierter ID.

2. **GET /api/messages**: Gibt alle gespeicherten Nachrichten zurück.
   - **Beschreibung**: Dieser Endpunkt gibt alle gespeicherten Nachrichten aus der Datenbank zurück.
   - **Antwort**: HTTP 200, JSON-Array aller Nachrichten, z. B. `[ { "id": 1, "content": "Ihre Nachricht hier" }, ... ]`

---

# Anleitung

In dieser Anleitung wird beschrieben, wie Sie einen Microservice mit Spring Boot erstellen. Der Service enthält einen **GET**- und einen **POST**-Endpunkt und verwendet **SQLite** als Datenbank. SQLite wird dabei mithilfe von **Spring Data JPA** angebunden.

## Voraussetzungen

- **IntelliJ IDEA** installiert
- **JDK 21 oder 25** installiert (beides sind LTS-Versionen). Prüfen mit `java -version`.
- Ein REST-Client: **Bruno** (empfohlen, speichert die Requests als Dateien im Projekt und ist damit versionierbar) oder **Postman**. `curl` genügt auch und ist überall vorhanden.
- Ein Maven-Download ist **nicht** nötig: Das vom Initializr erzeugte Projekt enthält den Maven Wrapper (`mvnw` bzw. `mvnw.cmd`).

---

## Schritt 1: Projekt mit Spring Initializr konfigurieren

1. Öffnen Sie [Spring Initializr](https://start.spring.io/).
2. Konfigurieren Sie die folgenden Einstellungen:

   - **Projekttyp**: Maven Project
   - **Sprache**: Java
   - **Spring Boot Version**: die neueste stabile **4.x**-Version (ohne den Zusatz `SNAPSHOT` oder `M1`, `M2` und so weiter)
   - **Gruppen-ID**: `com.hnu`
   - **Artefakt**: `hnu-message-microservice`
   - **Name**: `HNUMessageMicroservice`
   - **Package name**: `com.hnu.hnumessagemicroservice`
   - **Packaging**: Jar
   - **Java-Version**: 21 oder 25 (dieselbe, die auf Ihrem Rechner installiert ist)

3. **Abhängigkeiten hinzufügen**:

   - **Spring Web** (für die REST-API)
   - **Spring Data JPA** (für die Datenbank-Interaktion)
   - **Validation** (wird erst im erweiterten Teil gebraucht, ist aber nachträglich lästig zu ergänzen)

4. Klicken Sie auf **Generate** und entpacken Sie das heruntergeladene ZIP-Archiv. Öffnen Sie das Projekt in Ihrer Entwicklungsumgebung.

> **Zum Artefaktnamen:** Maven-Artefakt-IDs schreibt man üblicherweise klein und mit Bindestrichen (`hnu-message-microservice`), Java-Klassennamen dagegen in CamelCase. Das ist dieselbe „Coding Convention", über die in der Vorlesung gesprochen wird, hier können Sie sie gleich anwenden.

---

## Schritt 2: Projekt in IntelliJ öffnen

1. **Starten Sie IntelliJ IDEA** und wählen Sie *File → Open*.
2. **Projektverzeichnis auswählen**: Navigieren Sie zum entpackten Projekt und wählen Sie das Hauptverzeichnis aus, das, in dem die `pom.xml` liegt.
3. IntelliJ erkennt das Maven-Projekt und lädt die Abhängigkeiten herunter. Das dauert beim ersten Mal einige Minuten. Falls eine Nachfrage erscheint, bestätigen Sie den Import bzw. *Trust Project*.
4. **Projekt starten**: Öffnen Sie `src/main/java/com/hnu/hnumessagemicroservice/HnuMessageMicroserviceApplication.java` und klicken Sie auf das grüne Startsymbol neben der `main`-Methode.

An dieser Stelle startet die Anwendung bereits, sie kann nur noch nichts. Werfen Sie trotzdem einen Blick in die Konsole: Dort steht, dass ein **Tomcat** auf Port 8080 gestartet wurde. Dieser Webserver ist in Spring Boot eingebettet; Sie müssen nichts installieren und nichts deployen.

---

## Schritt 3: SQLite-Datenbank konfigurieren

### SQLite-JDBC-Treiber hinzufügen

Spring Boot unterstützt SQLite nicht ab Werk. Sie brauchen zwei Abhängigkeiten: den JDBC-Treiber und das Hibernate-Modul, das den SQLite-Dialekt enthält. Tragen Sie sie in der `pom.xml` innerhalb von `<dependencies>` ein:

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.53.4.0</version>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>
```

Beim zweiten Eintrag steht bewusst **keine** Version: Diese verwaltet Spring Boot für Sie, damit die Hibernate-Version zum Rest des Frameworks passt. Beim SQLite-Treiber müssen Sie die Version selbst angeben, die aktuelle finden Sie auf [Maven Central](https://central.sonatype.com/artifact/org.xerial/sqlite-jdbc).

> **Warum nicht einfach die JAR-Datei herunterladen wie in Datenbanken im 1. Semester?** Weil das Build-Werkzeug die Bibliothek samt ihrer eigenen Abhängigkeiten lädt, die Version im Repository festhält und damit reproduzierbar macht. Und weil sich nur so automatisiert prüfen lässt, ob eine Ihrer Abhängigkeiten eine bekannte Sicherheitslücke hat.

### Datenbankkonfiguration

Öffnen Sie `src/main/resources/application.properties` und tragen Sie ein:

```properties
spring.application.name=HNUMessageMicroservice

# Verbindung zur SQLite-Datei
spring.datasource.url=jdbc:sqlite:sample.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect

# Hibernate legt die Tabellen beim Start an und passt sie an die Entitaeten an
spring.jpa.hibernate.ddl-auto=update

# Erzeugtes SQL im Log mitlesen, ist sehr hilfreich, um das O/R-Mapping zu verstehen
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Erklärungen zur Konfiguration**

1. **SQLite** ist eine leichtgewichtige, serverlose SQL-Datenbank, die keine Installation erfordert und als einzelne Datei gespeichert wird. Für Übungen und lokale Entwicklung ist das ideal. Für den Produktiveinsatz nimmt man PostgreSQL oder MySQL. Wie das aussieht, zeigt Anhang B: Dort läuft PostgreSQL in einem Container, und Sie müssen ebenfalls nichts installieren.

2. **Die einzelnen Einträge**
   - `spring.datasource.url=jdbc:sqlite:sample.db` legt die Datei `sample.db` im Arbeitsverzeichnis an, also meist im Projektverzeichnis.
   - `spring.datasource.driver-class-name=org.sqlite.JDBC` benennt den JDBC-Treiber, über den die Verbindung läuft.
   - `spring.jpa.database-platform` legt den Dialekt fest. Er übersetzt die generischen Hibernate-Befehle in SQLite-SQL. Jede Datenbank hat ihre Eigenheiten, und der Dialekt kapselt sie.
   - `spring.jpa.hibernate.ddl-auto=update` sorgt dafür, dass Hibernate die Tabellen passend zu Ihren Entitäten erzeugt und ändert. **Das ist eine Bequemlichkeit für die Übung.** In Produktion arbeitet man mit Migrationswerkzeugen wie Flyway oder Liquibase, weil `update` weder Spalten löscht noch nachvollziehbar dokumentiert, was wann geändert wurde.
   - `spring.jpa.show-sql` und `format_sql` zeigen das von Hibernate erzeugte SQL in der Konsole. Schauen Sie da später wirklich hin: Sie sehen dann, dass aus `repository.save(...)` ein `insert into message ...` wird.

### Die Datenbankdatei nicht einchecken

Legen Sie in der `.gitignore` (im Projektwurzelverzeichnis) folgende Zeilen an bzw. ergänzen Sie sie:

```gitignore
target/
*.db
.env
```

Die Datenbankdatei ist ein Build-Ergebnis, kein Quellcode! Sie gehört nicht ins Repository! Dasselbe gilt für alles, was Zugangsdaten enthält.

---

## Schritt 4: Entität erstellen

Erstellen Sie im Paket `com.hnu.hnumessagemicroservice` eine neue Klasse `Message.java`. Diese Klasse repräsentiert eine Entität, die in der SQLite-Datenbank gespeichert wird:

```java
package com.hnu.hnumessagemicroservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // JPA benoetigt einen parameterlosen Konstruktor
    public Message() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
```

**Zwei Punkte, die wichtig sind:**

- **Der parameterlose Konstruktor ist Pflicht.** JPA erzeugt Instanzen zur Laufzeit über Reflection und braucht ihn dafür. Solange Sie keinen weiteren Konstruktor schreiben, erzeugt Java ihn automatisch. Sobald Sie aber einen Konstruktor mit Parametern ergänzen, müssen Sie den leeren selbst wieder hinschreiben. Deshalb steht er hier von Anfang an da.
- **`GenerationType.IDENTITY`** überlässt die Vergabe der ID der Datenbank. Bei SQLite ist das die `INTEGER PRIMARY KEY`-Spalte. Mit `AUTO` würde Hibernate stattdessen eine Sequenz emulieren und dafür eine zusätzliche Hilfstabelle anlegen. Für SQLite, MySQL und PostgreSQL ist `IDENTITY` die passende Wahl.

Getter und Setter werden hier für zwei verschiedene Dinge gebraucht: Jackson benutzt sie beim Umwandeln von und nach JSON, und sie sind die übliche Form für eine Bean-Klasse. Für das Mapping auf die Tabelle selbst genügen Hibernate bereits die Felder, weil `@Id` am Feld steht.

---

## Schritt 5: Repository erstellen

Erstellen Sie ein Interface `MessageRepository.java` für den Datenbankzugriff:

```java
package com.hnu.hnumessagemicroservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
```

Mehr ist das nicht. Sie schreiben keine Implementierung, Spring Data erzeugt sie zur Laufzeit.

Die Typparameter sind `<Entität, Typ des Primärschlüssels>`, in dieser Reihenfolge.

**Hinweis:** Schauen Sie sich das `JpaRepository`-Interface einmal genauer an (in IntelliJ: Strg-Klick auf den Namen). Sie werden sehen, wie viele Methoden bereits vorhanden sind, nämlich alle gängigen CRUD-Operationen. Eigene Abfragen kommen später dazu, indem Sie eine Methode nach einem festen Namensschema deklarieren, zum Beispiel `List<Message> findByContent(String content);`.

---

## Schritt 6: Controller erstellen

Erstellen Sie den Controller `MessageController.java`, um die REST-Endpunkte für **POST** und **GET** zu implementieren:

```java
package com.hnu.hnumessagemicroservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController                      // markiert die Klasse als REST-Controller
@RequestMapping("/api/messages")     // Basis-URL fuer alle Endpunkte dieser Klasse
public class MessageController {

    private final MessageRepository messageRepository;

    // Constructor Injection: Spring reicht das Repository beim Erzeugen herein
    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)          // 201 statt 200
    public Message createMessage(@RequestBody Message message) {
        message.setId(null);                     // die ID vergibt die Datenbank, nicht der Aufrufer
        return messageRepository.save(message);
    }

    @GetMapping
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
```

- **POST /api/messages** → legt eine Nachricht an, antwortet mit **201 Created**
- **GET /api/messages** → gibt alle Nachrichten zurück, antwortet mit **200 OK**

**Drei Entwurfsentscheidungen, die hier bewusst so getroffen sind:**

1. **Constructor Injection statt `@Autowired` am Feld.** Das Feld ist dadurch `final` und kann nach dem Erzeugen nicht mehr verändert werden; die Abhängigkeit ist an der Signatur sichtbar statt versteckt; und die Klasse lässt sich im Test ohne Spring instanziieren, indem man eine Attrappe hereingibt. Das ist das Dependency-Inversion-Prinzip aus der Vorlesung in seiner alltäglichen Form. Bei genau einem Konstruktor braucht Spring dafür keine Annotation.

2. **`message.setId(null)`.** Ohne diese Zeile könnte ein Aufrufer `{"id": 3, "content": "Text"}` schicken und damit einen fremden Datensatz überschreiben. Der Fachbegriff dafür ist *Mass Assignment*; in den OWASP Top 10 steht die zugehörige Kategorie seit Jahren auf Platz eins. Die saubere Lösung ist ein eigenes DTO für die Anfrage, siehe dazu die erweiterte Aufgabe 7.

3. **201 statt 200.** Wer eine Ressource anlegt, antwortet mit *201 Created*. Vollständig wäre zusätzlich ein `Location`-Header mit der URL der neuen Ressource:

   ```java
   @PostMapping
   public ResponseEntity<Message> createMessage(@RequestBody Message message) {
       message.setId(null);
       Message saved = messageRepository.save(message);
       return ResponseEntity
               .created(URI.create("/api/messages/" + saved.getId()))
               .body(saved);
   }
   ```

---

## Schritt 7: Anwendung starten

Starten Sie die Anwendung in IntelliJ über die `main()`-Methode oder auf der Konsole im Projektverzeichnis:

```bash
./mvnw spring-boot:run
```

Unter Windows: `mvnw.cmd spring-boot:run`

**Falls Port 8080 belegt ist**, bricht der Start mit einer entsprechenden Fehlermeldung ab. Sie können den Port in der `application.properties` ändern:

```properties
server.port=7890
```

Merken Sie sich den Port, denn in Schritt 8 müssen Sie ihn in den URLs einsetzen.

### Was in der Konsole steht

Lesen Sie die Startausgabe einmal bewusst durch, dort steht mehr, als man denkt:

- **Tomcat started on port 8080**: der eingebettete Webserver. Er ist Teil Ihrer Anwendung, Sie deployen nichts in einen externen Server.
- **Hibernate: create table if not exists message**: hier arbeitet `ddl-auto=update`.
- **spring.jpa.open-in-view is enabled by default**: eine Warnung, die Spring Boot immer ausgibt. Sie bedeutet, dass die Datenbankverbindung bis zum Ende der HTTP-Antwort offen bleibt. Für dieses Lab belassen wir die Voreinstellung. Merken Sie sich nur, dass in größeren Anwendungen `spring.jpa.open-in-view=false` die übliche Einstellung ist.

---

## Schritt 8: Die Endpunkte testen

Setzen Sie in den folgenden URLs Ihren Port ein, falls Sie ihn geändert haben.

### Mit curl

```bash
# Nachricht anlegen
curl -i -X POST http://localhost:8080/api/messages \
     -H "Content-Type: application/json" \
     -d '{"content":"Hallo HNU"}'

# Alle Nachrichten abrufen
curl http://localhost:8080/api/messages
```

`-i` zeigt zusätzlich die Antwortzeile und die Header. Dort sehen Sie das `HTTP/1.1 201`.

### Mit Bruno oder Postman

1. **POST /api/messages**
   - Body-Typ: JSON
   - Body: `{ "content": "Ihre Nachricht hier" }`
   - Erwartete Antwort: **201 Created**, im Body das Objekt inkl. automatisch vergebener `id`

2. **GET /api/messages**
   - Erwartete Antwort: **200 OK** mit einem JSON-Array. Beim ersten Aufruf ist es leer, legen Sie also zuerst per POST etwas an.

Den GET-Endpunkt können Sie auch im Browser aufrufen: `http://localhost:8080/api/messages`. Für POST geht das nicht, weil der Browser über die Adressleiste nur GET-Anfragen stellt. Dafür gibt es REST-Clients.

> Request, die Sie mit Bruno erstellen sollten Sie in einer Bruno-Collection ablegen und ebenfalls versionieren, z.B. in Git. Dann hat jedes Teammitglied dieselben Testaufrufe, und Änderungen an der API sind in der Versionsgeschichte sichtbar. Das ist speziell im späteren SE-Projekt ein direkter Benefit.

---

## Schritt 9: In die Datenbank schauen

Jetzt lohnt sich der Blick auf die andere Seite des O/R-Mappings.

- In IntelliJ (Ultimate): Reiter *Database* → **+** → *Data Source* → *SQLite* → Datei `sample.db` auswählen. Die Tabelle `message` ist dort sichtbar, mit genau den Spalten `id` und `content`.
- Alternativ mit dem kostenlosen [DB Browser for SQLite](https://sqlitebrowser.org/).

Vergleichen Sie, was Sie sehen, mit Ihrer Java-Klasse: eine Klasse wurde eine Tabelle, ein Attribut eine Spalte, ein Objekt eine Zeile. Das ist O/R-Mapping in einem Satz.

---

Gratulation :-) Sie haben nun Ihren ersten Microservice in einem der weltweit am weitesten verbreiteten Frameworks geschrieben.

---

## Lab - extended: Erweiterungen des Labs, zu bearbeiten Zuhause, ebenfalls ohne KI!

1. **PUT /api/messages/{id}**: Implementieren Sie einen PUT-Endpunkt, um eine Nachricht zu aktualisieren. Testen Sie ihn mit Bruno/Postman.
   *Hinweis:* `@PutMapping("/{id}")` und `@PathVariable Long id`.

2. **DELETE /api/messages/{id}**: Implementieren Sie einen DELETE-Endpunkt. Antworten Sie mit **204 No Content**, wenn gelöscht wurde.

3. **Validierung**: Stellen Sie sicher, dass eine Nachricht nicht leer sein darf.
   *Hinweis:* Dafür brauchen Sie die Abhängigkeit **Validation** aus Schritt 1 (`spring-boot-starter-validation`). Dann `@NotBlank` am Feld `content` und `@Valid` vor dem `@RequestBody`-Parameter. Ohne `@Valid` passiert nichts, das ist der häufigste Stolperstein.

4. **Fehlerbehandlung**: Geben Sie verständliche Fehlermeldungen mit passenden Statuscodes zurück:
   1. Ungültige Anfrage (z. B. leerer Inhalt) → **400**
   2. Nicht gefundene Ressource (ID existiert nicht) → **404**
   3. Interner Fehler (z. B. Datenbankfehler) → **500**

   *Hinweis:* Für den Einzelfall genügt `throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nachricht nicht gefunden")`. Sauberer ist eine zentrale Klasse mit `@RestControllerAdvice` und `@ExceptionHandler`-Methoden. Dann steht die Fehlerbehandlung an einer Stelle statt in jedem Controller. Überlegen Sie dabei: Welche Information darf in der Fehlermeldung stehen und welche nicht? Ein Stacktrace gehört ins Log, nicht in die HTTP-Antwort.

5. **Weitere Entitäten**: Fügen Sie eine Entität `User` hinzu und verknüpfen Sie sie mit `Message`. Implementieren Sie Endpunkte, um Benutzer und Nachrichten zu verwalten.
   *Hinweise:* `@ManyToOne` auf der Seite von `Message`, `@OneToMany(mappedBy = "user")` auf der Seite von `User`. Zwei Fallen erwarten Sie dort: eine Endlosschleife beim Erzeugen des JSON (beide Seiten verweisen aufeinander, blenden Sie also eine Seite mit `@JsonIgnore` aus oder verwenden Sie ein DTO) und das **N+1-Problem** (eine Abfrage für die Liste, dann eine weitere pro Zeile). Schauen Sie mit `show-sql=true` in die Konsole und zählen Sie die Statements.

6. **Suchfunktion**: Ergänzen Sie einen GET-Endpunkt mit Suchparameter, der alle Nachrichten zurückgibt, die den Suchbegriff enthalten.
   *Hinweis:* Sie brauchen dafür keine Implementierung zu schreiben. Deklarieren Sie im Repository
   `List<Message> findByContentContainingIgnoreCase(String teil);`
   und im Controller `@GetMapping(params = "q")` mit `@RequestParam String q`. Spring Data leitet die Abfrage aus dem Methodennamen ab.

7. **DTO statt Entität**: Führen Sie einen eigenen Typ `MessageRequest` für die Eingabe und `MessageResponse` für die Ausgabe ein, in Java 21 jeweils ein `record`. Die Entität verlässt damit nie die Fachschicht. Danach brauchen Sie die Zeile `message.setId(null)` nicht mehr: Die ID lässt sich gar nicht erst von außen setzen.

8. **Logging** (Anschluss an den Logging-Teil der Vorlesung): Ergänzen Sie im Controller
   `private static final Logger log = LoggerFactory.getLogger(MessageController.class);`
   und protokollieren Sie das Anlegen einer Nachricht. Achten Sie darauf, *was* Sie loggen: die ID ja, einen vollständigen Nachrichtentext eher nicht.

---

## [Optional] Anhang A: H2 statt SQLite

Falls die SQLite-Einbindung bei Ihnen nicht läuft oder Sie ohne Zusatz-Abhängigkeiten arbeiten wollen: **H2** ist eine Datenbank in Java, die Spring Boot vollständig selbst verwaltet.

1. Abhängigkeit im Initializr: **H2 Database** (oder in der `pom.xml` `com.h2database:h2` mit `<scope>runtime</scope>`, ohne Versionsangabe).
2. Die beiden SQLite-Abhängigkeiten und die drei SQLite-Zeilen in der `application.properties` entfallen.
3. Stattdessen:

```properties
spring.datasource.url=jdbc:h2:file:./data/messages
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

Einen Dialekt müssen Sie nicht angeben, den erkennt Spring Boot selbst. Unter `http://localhost:8080/h2-console` erreichen Sie eine Weboberfläche, in der Sie die Tabellen ansehen und SQL absetzen können. Tragen Sie dort als JDBC-URL denselben Wert ein wie oben.

---

## [Optional] Anhang B: PostgreSQL im Container

Diese Variante schlägt die Brücke zum Container-Teil der Vorlesung und entspricht dem, was Sie im Projekt tatsächlich tun werden: dieselbe Datenbank wie in Produktion, lokal gestartet, ohne Installation.

1. Abhängigkeit im Initializr: **PostgreSQL Driver** statt der beiden SQLite-Einträge.
2. Legen Sie im Projektverzeichnis eine `compose.yaml` an:

```yaml
services:
  db:
    image: postgres:17
    environment:
      POSTGRES_DB: messages
      POSTGRES_USER: app
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - db-daten:/var/lib/postgresql/data

volumes:
  db-daten:
```

3. Daneben eine Datei `.env` mit `DB_PASSWORD=geheim` und zusätzlich eine `.env.example` mit `DB_PASSWORD=` als Vorlage. Die `.env` wird **nicht** eingecheckt, die `.env.example` schon.
4. In der `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/messages
spring.datasource.username=app
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

5. Datenbank starten mit `docker compose up -d`, später aufräumen mit `docker compose down`. Mit `docker compose down -v` verschwinden auch die gespeicherten Daten.

Ihre Java-Klassen ändern sich dabei **nicht**. Darin liegt der Gewinn der Abstraktion durch JPA: Die Fachlogik kennt die konkrete Datenbank nicht.

---

## Anhang C: Häufige Fehler

| Symptom | Ursache und Lösung |
|---|---|
| `Port 8080 was already in use` | Eine andere Anwendung läuft noch. Alte Instanz beenden oder `server.port` ändern (Schritt 7) |
| `Unable to determine Dialect` oder eine Fehlermeldung mit `SQLiteDialect` | Die Abhängigkeit `hibernate-community-dialects` fehlt oder die Zeile `spring.jpa.database-platform` ist falsch geschrieben. Nach Änderungen an der `pom.xml` die Maven-Abhängigkeiten neu laden |
| `Table "MESSAGE" not found` | `spring.jpa.hibernate.ddl-auto=update` fehlt in der `application.properties` |
| POST liefert **415 Unsupported Media Type** | Der Header `Content-Type: application/json` fehlt in der Anfrage |
| POST liefert **400** ohne erkennbaren Grund | Der Body ist kein gültiges JSON. Typisch sind typografische Anführungszeichen („ ") statt `"`, wenn der Text aus einem Textverarbeitungsprogramm kopiert wurde |
| GET liefert `[]`, obwohl Sie etwas angelegt haben | Die Anwendung wurde zwischendurch mit einem anderen Arbeitsverzeichnis gestartet und hat eine zweite `sample.db` angelegt. Im Projekt nach `*.db` suchen |
| Die Anwendung startet, aber `/api/messages` liefert **404** | Der Controller liegt nicht unterhalb des Pakets der Hauptklasse. Spring sucht Komponenten nur dort |
