package com.hnu.hnumessagemicroservice;

import jakarta.validation.constraints.NotBlank;

// EXTENDED LAB - 7: DTO fuer die Eingabe. Es enthaelt bewusst keine id, ein Aufrufer kann sie also
// gar nicht erst mitschicken. Damit ist Mass Assignment ausgeschlossen und message.setId(null) entfaellt.
public record MessageRequest(

        // EXTENDED LAB - 3: weder null noch leer noch nur Leerzeichen.
        // Geprueft wird das nur, wenn im Controller @Valid vor dem @RequestBody steht.
        @NotBlank(message = "Der Inhalt der Nachricht darf nicht leer sein")
        String content,

        // EXTENDED LAB - 5: optional; ist die ID gesetzt, wird die Nachricht diesem Benutzer zugeordnet
        Long userId) {
}
