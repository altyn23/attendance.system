package com.altynai.attendance.service;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.SystemNotification;
import com.altynai.attendance.repository.SystemNotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final SystemNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(SystemNotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyUser(String userId, String type, String title, String message, String relatedEntityId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        SystemNotification notification = new SystemNotification();
        notification.setRecipientUserId(userId);
        notification.setRecipientRole(user.getRole());
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityId(relatedEntityId);
        notificationRepository.save(notification);
    }

    public void notifyGroupStudents(String groupCode, String type, String title, String message, String relatedEntityId) {
        if (groupCode == null || groupCode.isBlank()) {
            return;
        }
        List<User> students = userRepository.findByRoleAndGroup("STUDENT", groupCode);
        for (User student : students) {
            notifyUser(student.getId(), type, title, message, relatedEntityId);
        }
    }
}
