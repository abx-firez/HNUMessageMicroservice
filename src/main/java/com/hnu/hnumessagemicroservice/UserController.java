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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// EXTENDED LAB - 5: Endpunkte zum Verwalten der Benutzer, aufgebaut wie der MessageController
@RestController
@RequestMapping("/api/users")
public class UserController {

    // EXTENDED LAB - 8
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public UserController(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        User user = new User();
        user.setName(request.name());
        User saved = userRepository.save(user);
        log.info("Benutzer {} angelegt", saved.getId());
        return UserResponse.from(saved);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return UserResponse.from(findUser(id));
    }

    // alle Nachrichten eines Benutzers, z. B. GET /api/users/1/messages
    @GetMapping("/{id}/messages")
    public List<MessageResponse> getMessagesOfUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Benutzer " + id + " nicht gefunden");
        }
        return messageRepository.findByUserId(id).stream()
                .map(MessageResponse::from)
                .toList();
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = findUser(id);
        user.setName(request.name());
        User saved = userRepository.save(user);
        log.info("Benutzer {} aktualisiert", id);
        return UserResponse.from(saved);
    }

    // loescht ueber cascade REMOVE auch alle Nachrichten des Benutzers
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userRepository.delete(findUser(id));
        log.info("Benutzer {} geloescht", id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Benutzer " + id + " nicht gefunden"));
    }
}
