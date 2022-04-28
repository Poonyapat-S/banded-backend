package com.javainuse.classes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Integer> {
	List<DirectMessage> findBySender(User sender);
	List<DirectMessage> findByRecipient(User recipient);
	List<DirectMessage> findBySenderOrRecipient(User sender, User recipient);
	List<DirectMessage> findBySenderAndRecipient(User sender, User recipient);
}
