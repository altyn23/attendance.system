package com.altynai.attendance.teacher;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.model.ScheduleChangeRequest;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.repository.ScheduleChangeRequestRepository;
import com.altynai.attendance.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/ops")
public class TeacherOperationsController {
    private final ScheduleChangeRequestRepository requestRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TeacherOperationsController(
            ScheduleChangeRequestRepository requestRepository,
            ClassRepository classRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.requestRepository = requestRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/requests")
    public Map<String, Object> requestScheduleChange(
            @RequestBody Map<String, String> req,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();
        String teacherId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"TEACHER".equals(role)) {
            res.put("error", "Unauthorized");
            return res;
        }

        String classId = req.get("classId");
        String targetAdminId = req.get("targetAdminId");
        String reason = req.getOrDefault("reason", "").trim();
        String type = req.getOrDefault("type", "UPDATE").toUpperCase(Locale.ROOT);
        String newTeacherId = req.get("newTeacherId");
        String newRoom = req.get("newRoom");

        if (reason.isBlank()) {
            res.put("error", "Причина обязательна");
            return res;
        }

        Class cls = classRepository.findById(classId).orElse(null);
        if (cls == null || !teacherId.equals(cls.getTeacherId())) {
            res.put("error", "Сабақ табылмады немесе сізге тиесілі емес");
            return res;
        }

        User admin = userRepository.findById(targetAdminId).orElse(null);
        if (admin == null || !"ADMIN".equals(admin.getRole())) {
            res.put("error", "Таңдалған әкімші табылмады");
            return res;
        }

        User teacher = userRepository.findById(teacherId).orElse(null);
        ScheduleChangeRequest request = new ScheduleChangeRequest();
        request.setRequesterTeacherId(teacherId);
        request.setRequesterTeacherName(teacher != null ? teacher.getFullName().trim() : teacherId);
        request.setTargetAdminId(targetAdminId);
        request.setClassId(classId);
        request.setType(type);
        request.setReason(reason);
        request.setNewTeacherId(newTeacherId);
        request.setNewRoom(newRoom);
        request.setRequestedDate(parseDate(req.get("requestedDate")));
        request = requestRepository.save(request);

        notificationService.notifyUser(
                targetAdminId,
                "TEACHER_REQUEST",
                "Новая заявка от преподавателя",
                "Запрос на изменение пары: " + cls.getName() + ", причина: " + reason,
                request.getId()
        );

        res.put("success", true);
        res.put("request", request);
        return res;
    }

    @GetMapping("/requests")
    public Map<String, Object> myRequests(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String teacherId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"TEACHER".equals(role)) {
            res.put("error", "Unauthorized");
            return res;
        }

        res.put("success", true);
        res.put("requests", requestRepository.findByRequesterTeacherIdOrderByCreatedAtDesc(teacherId));
        return res;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
