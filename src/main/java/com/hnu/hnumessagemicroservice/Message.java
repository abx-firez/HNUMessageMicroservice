package com.hnu.hnumessagemicroservice;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    // EXTENDED LAB - 5: viele Nachrichten gehoeren zu einem Benutzer. Diese Seite besitzt die Beziehung,
    // in der Tabelle message entsteht dafuer die Fremdschluesselspalte user_id.
    // LAZY: der Benutzer wird erst geladen, wenn jemand auf ihn zugreift (siehe N+1 im MessageRepository).
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

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

    // EXTENDED LAB - 5
    public User getUser() {
        return user;
    }

    // EXTENDED LAB - 5
    public void setUser(User user) {
        this.user = user;
    }
}
