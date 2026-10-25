package com.hnu.hnumessagemicroservice;

// EXTENDED LAB - 5 und 7: Ausgabe fuer Benutzer. Die Nachrichten stehen bewusst nicht darin,
// sie gibt es ueber GET /api/users/{id}/messages.
public record UserResponse(Long id, String name) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName());
    }
}
