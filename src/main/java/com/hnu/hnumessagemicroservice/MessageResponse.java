package com.hnu.hnumessagemicroservice;

// EXTENDED LAB - 7: DTO fuer die Ausgabe. Der Controller gibt nie die Entitaet selbst zurueck,
// dadurch bestimmt dieser Typ und nicht das Datenbankschema, wie das JSON aussieht.
// Nebenbei ist so auch die JSON-Endlosschleife aus Aufgabe 5 (Message -> User -> Messages -> ...) kein Thema,
// weil hier nur id und Name des Benutzers stehen, nicht der Benutzer selbst.
public record MessageResponse(Long id, String content, Long userId, String userName) {

    public static MessageResponse from(Message message) {
        User user = message.getUser();
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                user != null ? user.getId() : null,
                user != null ? user.getName() : null);
    }
}
