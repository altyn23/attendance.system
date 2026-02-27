package com.altynai.attendance.notification;

import com.altynai.attendance.model.SystemNotification;
import com.altynai.attendance.repository.SystemNotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class SystemNotificationController {
    private final SystemNotificationRepository notificationRepository;

    public SystemNotificationController(SystemNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/my")
    public Map<String, Object> myNotifications(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        res.put("success", true);
        res.put("unreadCount", notificationRepository.countByRecipientUserIdAndReadFalse(userId));
        res.put("items", notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId));
        return res;
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable String id, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        SystemNotification notification = notificationRepository.findById(id).orElse(null);
        if (notification == null || !userId.equals(notification.getRecipientUserId())) {
            res.put("error", "Not found");
            return res;
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
        res.put("success", true);
        return res;
    }
}
