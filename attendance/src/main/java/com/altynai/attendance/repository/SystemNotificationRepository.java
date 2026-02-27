package com.altynai.attendance.repository;

import com.altynai.attendance.model.SystemNotification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SystemNotificationRepository extends MongoRepository<SystemNotification, String> {
    List<SystemNotification> findByRecipientUserIdOrderByCreatedAtDesc(String recipientUserId);
    long countByRecipientUserIdAndReadFalse(String recipientUserId);
}
