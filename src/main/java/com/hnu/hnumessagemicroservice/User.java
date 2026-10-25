package com.hnu.hnumessagemicroservice;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

// EXTENDED LAB - 5: zweite Entitaet, ein Benutzer hat beliebig viele Nachrichten.
@Entity
@Table(name = "users")   // "user" ist in vielen Datenbanken (z. B. PostgreSQL) ein reserviertes Wort
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // mappedBy = "user": die Beziehung gehoert dem Feld user in Message, hier steht nur die Rueckrichtung,
    // in der Tabelle users entsteht dafuer keine Spalte.
    // cascade REMOVE: wird ein Benutzer geloescht, werden seine Nachrichten mitgeloescht. Sonst blieben
    // Nachrichten mit einer user_id zurueck, die auf keinen Benutzer mehr zeigt.
    // Die JSON-Endlosschleife (User -> Messages -> User -> ...) entsteht nicht, weil die Controller
    // nie die Entitaet, sondern immer ein DTO zurueckgeben (Aufgabe 7). Ohne DTO braeuchte es hier @JsonIgnore.
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Message> messages = new ArrayList<>();

    // JPA benoetigt einen parameterlosen Konstruktor
    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
