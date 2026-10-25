package com.hnu.hnumessagemicroservice;

import jakarta.validation.constraints.NotBlank;

// EXTENDED LAB - 5 und 7: Eingabe fuer Benutzer, ohne id und ohne Nachrichtenliste
public record UserRequest(

        // EXTENDED LAB - 3
        @NotBlank(message = "Der Name darf nicht leer sein")
        String name) {
}
