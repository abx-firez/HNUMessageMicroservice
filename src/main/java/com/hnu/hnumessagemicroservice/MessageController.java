package com.hnu.hnumessagemicroservice;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController                      // markiert die Klasse als REST-Controller
@RequestMapping("/api/messages")     // Basis-URL fuer alle Endpunkte dieser Klasse
public class MessageController {

    // EXTENDED LAB - 8
    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageRepository messageRepository;

    // EXTENDED LAB - 5
    private final UserRepository userRepository;

    // Constructor Injection: Spring reicht die Repositories beim Erzeugen herein
    public MessageController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // EXTENDED LAB - 3: @Valid loest die Pruefung von @NotBlank aus, ohne @Valid passiert nichts.
    // EXTENDED LAB - 7: MessageRequest statt Message als Eingabe, MessageResponse statt Message als Ausgabe.
    // Die Zeile message.setId(null) aus dem Basis-Lab ist damit ueberfluessig, die ID kommt nie von aussen.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)          // 201 statt 200
    public MessageResponse createMessage(@Valid @RequestBody MessageRequest request) {
        Message message = new Message();
        message.setContent(request.content());
        message.setUser(findUserOrNull(request.userId()));
        Message saved = messageRepository.save(message);

        // EXTENDED LAB - 8: die ID ja, den Nachrichtentext nicht. Er kann personenbezogene Daten enthalten,
        // und Logdateien werden oft laenger aufbewahrt und von mehr Leuten gelesen als die Datenbank.
        log.info("Nachricht {} angelegt", saved.getId());
        return MessageResponse.from(saved);
    }

    @GetMapping
    public List<MessageResponse> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(MessageResponse::from)
                .toList();
    }

    // EXTENDED LAB - 6: wird nur gewaehlt, wenn der Parameter q in der URL steht, z. B. GET /api/messages?q=hallo.
    // Ohne q greift weiterhin getAllMessages().
    @GetMapping(params = "q")
    public List<MessageResponse> searchMessages(@RequestParam String q) {
        return messageRepository.findByContentContainingIgnoreCase(q).stream()
                .map(MessageResponse::from)
                .toList();
    }

    // EXTENDED LAB - 1: PUT ersetzt die Nachricht vollstaendig. Fehlt userId im Body,
    // ist die Nachricht danach keinem Benutzer mehr zugeordnet.
    // EXTENDED LAB - 4: existiert die ID nicht, wird daraus im GlobalExceptionHandler ein 404.
    @PutMapping("/{id}")
    public MessageResponse updateMessage(@PathVariable Long id, @Valid @RequestBody MessageRequest request) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nachricht " + id + " nicht gefunden"));
        message.setContent(request.content());
        message.setUser(findUserOrNull(request.userId()));
        Message saved = messageRepository.save(message);

        // EXTENDED LAB - 8
        log.info("Nachricht {} aktualisiert", id);
        return MessageResponse.from(saved);
    }

    // EXTENDED LAB - 2: 204 No Content, die Antwort hat keinen Body.
    // EXTENDED LAB - 4: existiert die ID nicht, gibt es 404 statt eines stillen Erfolgs.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long id) {
        if (!messageRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nachricht " + id + " nicht gefunden");
        }
        messageRepository.deleteById(id);

        // EXTENDED LAB - 8
        log.info("Nachricht {} geloescht", id);
    }

    // EXTENDED LAB - 5: userId ist optional. Ist sie angegeben, muss der Benutzer aber existieren.
    private User findUserOrNull(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer " + userId + " nicht gefunden"));
    }
}
