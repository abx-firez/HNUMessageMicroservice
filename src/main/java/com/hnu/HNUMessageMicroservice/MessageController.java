package com.hnu.HNUMessageMicroservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.logging.LogManager;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Value("${meineSuperAdresse}")
    private String msa;

    @Autowired
    private MessageRepository messageRepository;

    // EXTENDED LAB - 5
    @Autowired
    private UserRepository userRepository;

    // EXTENDED LAB - 6
    @PostMapping
    public Message createMessage(@RequestBody Message message, @RequestParam Long userId) {
        logger.info("Creating a new message: {}", message.getContent());
        // EXTENDED LAB - 3 und 4, Bad Request 400
        if (message.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must not be empty");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id " + userId));
        message.setUser(user);
        return messageRepository.save(message);
    }

    @GetMapping
    public List<Message> getAllMessages() {
        // EXTENDED LAB - 4, 500
        try {
            logger.info("Retrieving all messages");
            return messageRepository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while retrieving messages", e);
        }
    }

    // EXTENDED LAB - 1
    @PutMapping("/{id}")
    public Message updateMessage(@PathVariable Long id, @RequestBody Message messageDetails) {
        logger.debug("Updating message with id {}", id);
        Optional<Message> optionalMessage = messageRepository.findById(id);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            message.setContent(messageDetails.getContent());
            return messageRepository.save(message);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found with id " + id);
        }
    }

    // EXTENDED LAB - 2
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id) {
        // EXTENDED LAB - 4, 404 und 500
        try {
            if (!messageRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found with id " + id);
            }
            logger.info("Deleting message with id {}", id);
            messageRepository.deleteById(id);
        } catch (Exception e) {
            logger.error("An error occurred while deleting the message", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the message", e);
        }
    }

    // EXTENDED LAB - 5
    // Optional: Ein Endpunkt um alle Messages für einen User abzufragen
    @GetMapping("/user/{userId}")
    public List<Message> getMessagesByUserId(@PathVariable Long userId) {
        logger.debug("Retrieving messages for user with id {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id " + userId));
        return user.getMessages();
    }

    // EXTENDED LAB - 6
    @GetMapping("/search")
    public List<Message> searchMessages(@RequestParam String query) {
        logger.debug("Searching for messages containing {}", query);
        return messageRepository.findByContentContaining(query);
    }
}