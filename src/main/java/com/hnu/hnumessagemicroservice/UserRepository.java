package com.hnu.hnumessagemicroservice;

import org.springframework.data.jpa.repository.JpaRepository;

// EXTENDED LAB - 5
public interface UserRepository extends JpaRepository<User, Long> {
}
