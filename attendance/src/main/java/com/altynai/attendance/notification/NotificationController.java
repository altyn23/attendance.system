package com.altynai.attendance.notification;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.settings.SystemSettingsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class NotificationController {
    private final ClassRepository classRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final SystemSettingsService systemSettingsService;

    public NotificationController(
            ClassRepository classRepository,
            AttendanceRepository attendanceRepository,
            UserRepository userRepository,
            SystemSettingsService systemSettingsService
    ) {
        this.classRepository = classRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.systemSettingsService = systemSettingsService;
    }

    @GetMapping("/api/alerts/attendance")
    @ResponseBody
    public Map<String, Object> getAttendanceAlerts(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (userId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            res.put("error", "Forbidden");
            return res;
        }

        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        LocalDateTime now = LocalDateTime.now();
        int lateThresholdMinutes = Math.max(systemSettingsService.getSettings().getLateThreshold(), 1);

        List<Class> classes = "ADMIN".equals(role)
                ? classRepository.findByDayOfWeek(dayOfWeek)
                : classRepository.findByTeacherIdAndDayOfWeek(userId, dayOfWeek);

        List<Map<String, Object>> alerts = new ArrayList<>();

        for (Class cls : classes) {
            if (cls.getStartTime() == null || cls.getGroup() == null || cls.getGroup().isBlank()) {
                continue;
            }

            LocalDateTime classStart = LocalDateTime.of(today, cls.getStartTime());
            LocalDateTime classEnd = LocalDateTime.of(today, endTimeOrDefault(cls.getEndTime(), cls.getStartTime()));

            if (now.isBefore(classStart)) {
                continue;
            }

            List<User> groupStudents = userRepository.findByRoleAndGroup("STUDENT", cls.getGroup());
            if (groupStudents.isEmpty()) {
                continue;
            }

            List<Attendance> dayAttendance = attendanceRepository.findByClassIdAndDate(cls.getId(), today);
            Set<String> attendedIds = dayAttendance.stream()
                    .map(Attendance::getStudentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<String, Attendance> attendanceByStudent = dayAttendance.stream()
                    .filter(a -> a.getStudentId() != null)
                    .collect(Collectors.toMap(Attendance::getStudentId, a -> a, (a, b) -> a));

            LocalDateTime severeLateAt = classStart.plusMinutes(lateThresholdMinutes * 2L);

            for (User student : groupStudents) {
                if (student.getId() == null) {
                    continue;
                }

                Attendance existing = attendanceByStudent.get(student.getId());
                if (existing != null) {
                    if ("LATE".equals(existing.getStatus())
                            && existing.getCheckInTime() != null
                            && existing.getCheckInTime().isAfter(severeLateAt)) {
                        alerts.add(buildAlert("VERY_LATE", cls, student, today, "Өте кешігіп келді"));
                    }
                    continue;
                }

                if (now.isAfter(classEnd)) {
                    alerts.add(buildAlert("ABSENT", cls, student, today, "Сабаққа келмеді"));
                    continue;
                }

                if (now.isAfter(severeLateAt) && !attendedIds.contains(student.getId())) {
                    alerts.add(buildAlert("VERY_LATE", cls, student, today, "Өте қатты кешігіп жатыр"));
                }
            }
        }

        alerts.sort((a, b) -> {
            int wa = "ABSENT".equals(a.get("type")) ? 2 : 1;
            int wb = "ABSENT".equals(b.get("type")) ? 2 : 1;
            return Integer.compare(wb, wa);
        });

        res.put("success", true);
        res.put("alerts", alerts);
        res.put("generatedAt", LocalDateTime.now());
        return res;
    }

    private Map<String, Object> buildAlert(String type, Class cls, User student, LocalDate date, String message) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("key", date + ":" + cls.getId() + ":" + student.getId() + ":" + type);
        alert.put("type", type);
        alert.put("severity", "ABSENT".equals(type) ? "HIGH" : "MEDIUM");
        alert.put("studentId", student.getId());
        alert.put("studentName", student.getFullName());
        alert.put("studentEmail", student.getEmail());
        alert.put("classId", cls.getId());
        alert.put("className", cls.getName());
        alert.put("group", cls.getGroup());
        alert.put("teacherName", cls.getTeacherName() != null ? cls.getTeacherName() : "");
        alert.put("message", message);
        alert.put("date", date);
        return alert;
    }

    private LocalTime endTimeOrDefault(LocalTime endTime, LocalTime startTime) {
        if (endTime != null) {
            return endTime;
        }
        return startTime.plusMinutes(90);
    }
}
