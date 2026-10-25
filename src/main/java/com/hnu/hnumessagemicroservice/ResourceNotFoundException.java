package com.hnu.hnumessagemicroservice;

// EXTENDED LAB - 4: fachliche Ausnahme "gibt es nicht". Sie kennt keinen HTTP-Statuscode,
// die Uebersetzung in 404 macht zentral der GlobalExceptionHandler.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
