package com.hnu.HNUMessageMicroservice;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String content;

    // EXTENDED LAB - 5
    @ManyToOne
    /**
     * @JsonBackReference wird verwendet, um eine unendliche Rekursion während der JSON-Serialisierung zu verhindern.
     * In einer bidirektionalen Beziehung, wenn das übergeordnete Objekt (User) serialisiert wird,
     * werden die untergeordneten Objekte (Messages) serialisiert und dann versucht, das übergeordnete Objekt erneut zu serialisieren,
     * was zu einer Endlosschleife führt. @JsonBackReference teilt Jackson mit, die Rückreferenz
     * während der Serialisierung zu ignorieren und somit die Schleife zu durchbrechen.
     *
     */
    @JsonBackReference
    private User user;

    // Getters and Setters
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}