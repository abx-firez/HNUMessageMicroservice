package com.hnu.hnumessagemicroservice;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // EXTENDED LAB - 5: N+1-Problem vermeiden.
    // Die Antwort enthaelt den Namen des Benutzers. Ohne @EntityGraph laedt Hibernate zuerst alle Nachrichten
    // (1 Abfrage) und danach beim Zugriff auf getUser().getName() jeden Benutzer einzeln nach (N Abfragen).
    // Mit @EntityGraph wird der Benutzer per JOIN in derselben Abfrage mitgeladen. Mit show-sql=true
    // sehen Sie in der Konsole den Unterschied: ein einziges select statt 1 + N.
    @Override
    @EntityGraph(attributePaths = "user")
    List<Message> findAll();

    // EXTENDED LAB - 6: Spring Data leitet die Abfrage aus dem Methodennamen ab,
    // daraus wird: ... where upper(content) like upper('%teil%')
    @EntityGraph(attributePaths = "user")
    List<Message> findByContentContainingIgnoreCase(String teil);

    // EXTENDED LAB - 5: alle Nachrichten eines Benutzers, abgeleitet aus dem Pfad user.id
    @EntityGraph(attributePaths = "user")
    List<Message> findByUserId(Long userId);
}
