package com.hnu.hnumessagemicroservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

// EXTENDED LAB - 4: zentrale Fehlerbehandlung fuer alle Controller.
//
// Alle Fehlerantworten haben das standardisierte Format "Problem Details" (RFC 9457), z. B.
//   { "type": "about:blank", "title": "Nicht gefunden", "status": 404,
//     "detail": "Nachricht 42 nicht gefunden", "instance": "/api/messages/42" }
//
// Die Klasse erbt von ResponseEntityExceptionHandler. Der kennt die Fehler, die Spring selbst erzeugt
// (ungueltiges JSON -> 400, falscher Content-Type -> 415, falsche HTTP-Methode -> 405, ...) und
// beantwortet sie mit dem richtigen Statuscode. Ohne diese Basisklasse wuerden sie im Auffang-Handler
// fuer Exception landen und faelschlich als 500 gemeldet.
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: @Valid ist fehlgeschlagen. Die Meldung nennt jedes ungueltige Feld,
    // z. B. "content: Der Inhalt der Nachricht darf nicht leer sein".
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Ungueltige Anfrage");
        return ResponseEntity.badRequest().body(problem);
    }

    // 404: die angefragte Nachricht bzw. der Benutzer existiert nicht
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Nicht gefunden");
        return problem;
    }

    // 500: alles Unerwartete, z. B. ein Datenbankfehler.
    // Der Stacktrace gehoert ins Log, nicht in die HTTP-Antwort: Tabellennamen, SQL oder Klassennamen
    // helfen einem Angreifer mehr als dem Aufrufer. Der bekommt deshalb nur eine allgemeine Meldung.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unerwarteter Fehler", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Es ist ein interner Fehler aufgetreten. Bitte versuchen Sie es spaeter erneut.");
        problem.setTitle("Interner Fehler");
        return problem;
    }
}
