package com.hnu.HNUMessageMicroservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // EXTENDED LAB - 6
    List<Message> findByContentContaining(String infix);
}