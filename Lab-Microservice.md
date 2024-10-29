
# Lab: Erstellen eines einfachen Microservices mit Spring Boot und SQLite

In diesem Lab erstellen Sie einen einfachen Microservice mit Spring Boot. Der Service soll es ermöglichen, Nachrichten (Messages) zu speichern und wieder abzurufen. Die Nachrichten werden in einer SQLite-Datenbank gespeichert. Sie werden REST-API-Endpunkte erstellen, um Nachrichten mithilfe von **POST** und **GET** zu verwalten. 

## API-Endpunkte

1. **POST /api/messages**: Fügt eine neue Nachricht hinzu.
   - **Beschreibung**: Dieser Endpunkt speichert eine neue Nachricht in der Datenbank.
   - **Anfrage-Body**: JSON-Objekt, z. B. `{ "content": "Ihre Nachricht hier" }`
   - **Antwort**: Gibt das gespeicherte Nachrichtenobjekt als JSON zurück, inkl. automatisch generierter ID.

2. **GET /api/messages**: Gibt alle gespeicherten Nachrichten zurück.
   - **Beschreibung**: Dieser Endpunkt gibt alle gespeicherten Nachrichten aus der Datenbank zurück.
   - **Antwort**: JSON-Array aller Nachrichten, z. B. `[ { "id": 1, "content": "Ihre Nachricht hier" }, ... ]`

---

# Anleitung

In dieser Anleitung wird beschrieben, wie Sie einen Microservice mit Spring Boot erstellen. Der Service enthält einen **GET**- und einen **POST**-Endpunkt und verwendet **SQLite** als Datenbank. SQLite wird dabei mithilfe von **Spring Data JPA** angebunden.

## Voraussetzungen
- **IntelliJ IDEA** installiert
- **Java** installiert (mindestens Version 17, besser 21)

---

## Schritt 1: Projekt mit Spring Initializr konfigurieren

1. Öffnen Sie [Spring Initializr](https://start.spring.io/).
2. Konfigurieren Sie die folgenden Einstellungen:

   - **Projekttyp**: Maven Project
   - **Sprache**: Java
   - **Spring Boot Version**: Neueste stabile Version
   - **Gruppen-ID**: `com.hnu`
   - **Artefakt**: `HNUMessageMicroservice`
   - **Name**: `HNUMessageMicroservice`
   - **Package name**: `com.hnu.hnumessagemicroservice`
   - **Packaging**: Jar
   - **Java-Version**: Wählen Sie Version 21 

3. **Abhängigkeiten hinzufügen**:

   - **Spring Web** (für die REST-API)
   - **Spring Data JPA** (für die Datenbank-Interaktion)

4. Klicken Sie auf **Generate** und entpacken Sie das heruntergeladene ZIP-Archiv. Öffnen Sie das Projekt in Ihrer Entwicklungsumgebung.

---

## Schritt 2: Projekt in IntelliJ öffnen

1. **Starten Sie IntelliJ IDEA**.
   - Falls IntelliJ IDEA bereits geöffnet ist, klicken Sie auf "File" und wählen Sie "Open...".
   
2. **Projektverzeichnis auswählen**:
   - Navigieren Sie zum Verzeichnis, in dem Sie das Spring Initializr-Projekt entpackt haben.
   - Wählen Sie das Hauptverzeichnis des Projekts (dort, wo die `pom.xml`-Datei liegt) und klicken Sie auf "OK".

3. **Projekt als Maven-Projekt importieren**:
   - IntelliJ wird Sie fragen, ob das Projekt als Maven-Projekt importiert werden soll. Bestätigen Sie dies, damit alle Abhängigkeiten automatisch heruntergeladen werden.

4. **Projekt starten**:
   - Öffnen Sie `src/main/java` im Projekt-Baum und navigieren Sie zur Hauptklasse des Projekts (z. B. `HNUMessageMicroserviceApplication.java`).
   - Klicken Sie auf das grüne Startsymbol neben der `main`-Methode, um die Anwendung auszuführen.

---

## Schritt 3: SQLite-Datenbank konfigurieren

### SQLite-JDBC-Treiber hinzufügen

Da Spring Boot SQLite nicht nativ unterstützt, müssen Sie den SQLite-JDBC-Treiber manuell in der `pom.xml`-Datei hinzufügen. Fügen Sie dort die folgende Abhängigkeiten ein (achten Sie darauf, dass Sie es in den richtigen Bereich der `pom.xml`-Datei einfügen):

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.47.0.0</version>
</dependency>

<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>
```

Diese Abhängigkeit lädt den JDBC-Treiber für SQLite herunter und ermöglicht es Spring Boot, eine Verbindung zur SQLite-Datenbank herzustellen. Die aktuellste Version des Treibers finden Sie auf der [Maven Central Repository-Seite](https://search.maven.org/artifact/org.xerial/sqlite-jdbc). 

### Datenbankkonfiguration

Öffnen Sie die Datei `src/main/resources/application.properties` und fügen Sie folgende Konfiguration für SQLite ein:

```properties
spring.application.name=HNUMessageMicroservice
spring.datasource.url=jdbc:sqlite:sample.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.sql.init.mode=always
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
```

**Erklärungen zur SQLite-Konfiguration**:

1. **SQLite** ist eine leichtgewichtige, serverlose, self-contained SQL-Datenbank, die keine Installation erfordert und direkt als Datei gespeichert wird. Sie eignet sich sehr gut für einfache Anwendungen oder Entwicklungsumgebungen. Für den Produktiveinsatz oder größere Anwendungen sind andere Datenbanken wie MySQL oder PostgreSQL besser geeignet. Diese können mit Spring Boot ebenfalls einfach integriert werden. Hierfür müssen Sie lediglich die entsprechenden Abhängigkeiten und Konfigurationen in der `application.properties`-Datei hinzufügen.

2. **Konfigurationsdetails**:
   - `spring.datasource.url=jdbc:sqlite:sample.db`: Gibt die Datenbank-URL an. In diesem Fall wird eine `sample.db`-Datei erstellt, in der die SQLite-Daten gespeichert werden.
   - `spring.datasource.driver-class-name=org.sqlite.JDBC`: Der JDBC-Treiber für SQLite, um die Verbindung zu SQLite herzustellen.
   - `spring.sql.init.mode=always`: Die Datenbank wird bei jedem Start initialisiert.
   - `spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect`: Teilt Spring mit, wie SQLite-SQL-Befehle verarbeitet werden sollen.
   - `spring.jpa.hibernate.ddl-auto=update`: Hibernate erstellt oder aktualisiert automatisch die Datenbanktabellen, um Änderungen in den Entitäten widerzuspiegeln.

In der `application.properties`-Datei können Sie auch andere Konfigurationen wie den Serverport, den Anwendungsnamen oder die Logging-Einstellungen festlegen. Weitere Informationen finden Sie in der offiziellen [Spring Boot-Dokumentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config).

---

## Schritt 4: Entität erstellen

Erstellen Sie im Paket `com.hnu.hnumessagemicroservice` eine neue Klasse namens `Message.java`. Diese Klasse repräsentiert eine Entität, die in der lokalen SQLite Datenbank gespeichert wird:

```java
package com.hnu.hnumessagemicroservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String content;

    // Wichtig: Getter und Setter müssen vorhanden sein, damit Spring Data JPA die Entität korrekt verarbeiten kann
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

---

## Schritt 5: Repository erstellen

Erstellen Sie ein Interface `MessageRepository.java` für den Datenbankzugriff:

```java
package com.hnu.hnumessagemicroservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
```
Hinweis: Schauen Sie sich gerne das JpaRepository Interface etwas genauer an. Sie werden feststellen wie viele Methoden bereits implementiert sind und wie einfach es ist, eigene Methoden zu implementieren. Alle gängigen CRUD-Operationen sind bereits vorhanden.

---

## Schritt 6: Controller erstellen

Erstellen Sie den Controller `MessageController.java`, um die REST-Endpunkte für **POST** und **GET** zu implementieren:

```java
package com.hnu.hnumessagemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Spring-Annotation, die den Controller als REST-Controller markiert
@RequestMapping("/api/messages") // Basis-URL für alle Endpunkte in diesem Controller
public class MessageController {

    @Autowired // Automatische Verdrahtung des MessageRepository
    private MessageRepository messageRepository;

    @PostMapping // POST-Endpunkt zum Erstellen einer neuen Nachricht
    public Message createMessage(@RequestBody Message message) {
        return messageRepository.save(message);
    }

    @GetMapping // GET-Endpunkt zum Abrufen aller Nachrichten
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }
}
```

- **POST /api/messages**: Fügt eine neue Nachricht hinzu.
- **GET /api/messages**: Gibt alle gespeicherten Nachrichten zurück.

---

## Schritt 7: Anwendung starten

Starten Sie die Anwendung in IntelliJ über die main()-Methode oder durch den folgenden Befehl auf der Konsole:

```bash
mvn spring-boot:run
```

Hinweis: Sollte der Port 8080 bereits belegt sein erhalten Sie beim Start der Anwendung eine entsprechende Fehlermeldung. Sie können den Port in der `application.properties`-Datei z.B. auf 7890 wie folgt ändern:

```properties
server.port=7890
```

Ansonsten werfen Sie bitte unbedingt einen Blick auf die Ausgabe auf der Konsole. Sie sehen, dass hier ein Tomcat-Server gestartet wird. Dieser Server ist in Spring Boot bereits integriert und wird automatisch gestartet, wenn Sie Ihre Anwendung ausführen. Tomcat ist ein weit verbreiteter Webserver und Servlet-Container, der in Java-Anwendungen eingesetzt wird. Damit haben Sie einen Webserver, der Ihre REST-API-Endpunkte bereitstellt und den Sie über den Browser oder einen REST-Client ansprechen können. 

---

## Schritt 8: Die Endpunkte testen

Wechseln Sie zu einem REST-Client wie **Bruno** oder **Postman** und testen Sie die Endpunkte:

1. **POST /api/messages**:
   - **Anfrage-Body**: `{ "content": "Ihre Nachricht hier" }`
   - **Antwort**: Gibt das gespeicherte Nachrichtenobjekt als JSON zurück, inkl. automatisch generierter ID.

2. **GET /api/messages**:
   - **Antwort**: Gibt alle gespeicherten Nachrichten als JSON-Array zurück. Sollte beim ersten Aufruf noch leer sein. Fügen Sie also zuerst einige Nachrichten mittels POST hinzu.


Natürlich können Sie auch den Browser verwenden, um zumindest den GET-Endpunkt zu testen. Geben Sie einfach die URL `http://localhost:8080/api/messages` in die Adressleiste ein, um alle Nachrichten anzuzeigen.

---

Gratulation :-), Sie haben nun Ihren ersten Microservice in einem der weltweit am meisten verbreitetsten Frameworks geschrieben. 
