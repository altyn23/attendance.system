package com.altynai.attendance.ai;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.model.QRSession;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.repository.QRSessionRepository;
import com.altynai.attendance.settings.SystemSettingsService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiInsightsService {
    private final AttendanceRepository attendanceRepository;
    private final ClassRepository classRepository;
    private final QRSessionRepository qrSessionRepository;
    private final UserRepository userRepository;
    private final SystemSettingsService systemSettingsService;

    public AiInsightsService(
            AttendanceRepository attendanceRepository,
            ClassRepository classRepository,
            QRSessionRepository qrSessionRepository,
            UserRepository userRepository,
            SystemSettingsService systemSettingsService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.classRepository = classRepository;
        this.qrSessionRepository = qrSessionRepository;
        this.userRepository = userRepository;
        this.systemSettingsService = systemSettingsService;
    }

    public List<Map<String, Object>> smartAlerts(String userId, String role) {
        if (userId == null || (!"ADMIN".equals(role) && !"TEACHER".equals(role))) {
            return List.of();
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
                        alerts.add(buildAlert("VERY_LATE", cls, student, "Student arrived extremely late", 76));
                    }
                    continue;
                }

                if (now.isAfter(classEnd)) {
                    alerts.add(buildAlert("ABSENT", cls, student, "Student missed the class", 92));
                } else if (now.isAfter(severeLateAt)) {
                    alerts.add(buildAlert("VERY_LATE", cls, student, "Student has not checked in yet", 74));
                }
            }
        }

        alerts.sort((a, b) -> Integer.compare((int) b.get("riskScore"), (int) a.get("riskScore")));
        return alerts;
    }

    public List<Map<String, Object>> riskPredictions(String userId, String role) {
        Scope scope = resolveScope(userId, role);
        if (!scope.allowed) {
            return List.of();
        }

        List<Attendance> scopedAttendance = scopedAttendance(scope);
        Map<String, User> students = scopedStudents(scope).stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        Map<String, List<Attendance>> byStudent = scopedAttendance.stream()
                .filter(a -> a.getStudentId() != null)
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        List<Map<String, Object>> predictions = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> entry : byStudent.entrySet()) {
            String studentId = entry.getKey();
            List<Attendance> records = entry.getValue();
            if (records.isEmpty()) {
                continue;
            }

            User student = students.get(studentId);
            long total = records.size();
            long absent = records.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
            long late = records.stream().filter(a -> "LATE".equals(a.getStatus())).count();
            long good = records.stream().filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus())).count();
            double attendanceRate = total > 0 ? (good * 100.0 / total) : 0.0;

            List<Attendance> sortedByDate = records.stream()
                    .filter(a -> a.getDate() != null)
                    .sorted(Comparator.comparing(Attendance::getDate).reversed())
                    .toList();
            int recentAbsences = 0;
            for (int i = 0; i < Math.min(5, sortedByDate.size()); i++) {
                if ("ABSENT".equals(sortedByDate.get(i).getStatus())) {
                    recentAbsences++;
                }
            }

            int score = (int) Math.round(
                    (absent * 100.0 / Math.max(total, 1)) * 0.55
                    + (late * 100.0 / Math.max(total, 1)) * 0.25
                    + (recentAbsences * 20) * 0.20
            );
            score = Math.max(0, Math.min(100, score));

            String level = score >= 75 ? "HIGH" : score >= 45 ? "MEDIUM" : "LOW";

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", studentId);
            item.put("studentName", student != null ? student.getFullName().trim() : safeName(records.get(0).getStudentName()));
            item.put("group", student != null ? defaultText(student.getGroup(), records.get(0).getGroup()) : defaultText(records.get(0).getGroup(), "-"));
            item.put("attendanceRate", round(attendanceRate));
            item.put("absentCount", absent);
            item.put("lateCount", late);
            item.put("totalCount", total);
            item.put("riskScore", score);
            item.put("riskLevel", level);
            item.put("recommendation", recommendationFor(level, attendanceRate));
            predictions.add(item);
        }

        predictions.sort((a, b) -> Integer.compare((int) b.get("riskScore"), (int) a.get("riskScore")));
        return predictions;
    }

    public List<Map<String, Object>> reasonClusters(String userId, String role) {
        Scope scope = resolveScope(userId, role);
        if (!scope.allowed) {
            return List.of();
        }

        List<Attendance> scoped = scopedAttendance(scope);
        if (scoped.isEmpty()) {
            return List.of();
        }

        long total = scoped.size();

        Map<String, Long> byDay = scoped.stream()
                .filter(a -> a.getDate() != null)
                .collect(Collectors.groupingBy(a -> a.getDate().getDayOfWeek().toString(), Collectors.counting()));
        Map<String, Long> byClass = scoped.stream()
                .collect(Collectors.groupingBy(a -> defaultText(a.getClassName(), "Unknown class"), Collectors.counting()));
        Map<String, Long> byStatus = scoped.stream()
                .collect(Collectors.groupingBy(a -> defaultText(a.getStatus(), "UNKNOWN"), Collectors.counting()));

        LocalTime morning = LocalTime.of(12, 0);
        Map<String, Long> byTime = scoped.stream()
                .collect(Collectors.groupingBy(a -> {
                    LocalTime t = a.getCheckInTime() != null ? a.getCheckInTime().toLocalTime() : LocalTime.NOON;
                    if (t.isBefore(morning)) return "MORNING";
                    if (t.isBefore(LocalTime.of(17, 0))) return "AFTERNOON";
                    return "EVENING";
                }, Collectors.counting()));

        List<Map<String, Object>> clusters = new ArrayList<>();
        clusters.add(cluster("DAY_PATTERN", topEntry(byDay), total, "Most issues happen on specific weekdays"));
        clusters.add(cluster("SUBJECT_PATTERN", topEntry(byClass), total, "Risk concentrates in a few subjects"));
        clusters.add(cluster("STATUS_PATTERN", topEntry(byStatus), total, "Dominant absence/late status pattern"));
        clusters.add(cluster("TIME_PATTERN", topEntry(byTime), total, "Attendance weakens in specific time slots"));
        return clusters;
    }

    public Map<String, Object> adminSummary(String userId, String role) {
        Scope scope = resolveScope(userId, role);
        if (!scope.allowed) {
            return Map.of("summary", "Unauthorized", "highlights", List.of());
        }

        List<Attendance> scoped = scopedAttendance(scope);
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate prevWeekStart = weekStart.minusWeeks(1);
        LocalDate prevWeekEnd = weekStart.minusDays(1);

        List<Attendance> thisWeek = scoped.stream()
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(weekStart))
                .toList();
        List<Attendance> prevWeek = scoped.stream()
                .filter(a -> a.getDate() != null && !a.getDate().isBefore(prevWeekStart) && !a.getDate().isAfter(prevWeekEnd))
                .toList();

        double thisRate = calcRate(thisWeek);
        double prevRate = calcRate(prevWeek);
        double delta = round(thisRate - prevRate);

        List<Map<String, Object>> risks = riskPredictions(userId, role).stream().limit(5).toList();
        List<Map<String, Object>> anomalies = qrAnomalies(userId, role).stream().limit(5).toList();

        String trend = delta >= 0 ? "improved" : "declined";
        String summary = String.format(
                "Weekly attendance %s by %.1f%%. Current week rate: %.1f%%, previous week: %.1f%%. High-risk students: %d, QR anomalies: %d.",
                trend,
                Math.abs(delta),
                thisRate,
                prevRate,
                risks.size(),
                anomalies.size()
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("thisWeekRate", thisRate);
        result.put("prevWeekRate", prevRate);
        result.put("delta", delta);
        result.put("highRiskStudents", risks);
        result.put("qrAnomalies", anomalies);
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    public Map<String, Object> teacherMessageDraft(String userId, String role, String studentId, String type, String lang, String tone) {
        Scope scope = resolveScope(userId, role);
        if (!scope.allowed) {
            return Map.of("message", "Unauthorized");
        }

        List<Map<String, Object>> risks = riskPredictions(userId, role);
        Map<String, Object> target = risks.stream()
                .filter(r -> Objects.equals(studentId, r.get("studentId")))
                .findFirst()
                .orElse(risks.stream().findFirst().orElse(Map.of()));

        String name = defaultText((String) target.get("studentName"), "Student");
        String grp = defaultText((String) target.get("group"), "-");
        int score = target.get("riskScore") instanceof Number ? ((Number) target.get("riskScore")).intValue() : 0;
        String level = defaultText((String) target.get("riskLevel"), "MEDIUM");

        boolean ru = "ru".equalsIgnoreCase(lang);
        String chosenTone = defaultText(tone, "supportive").toLowerCase(Locale.ROOT);
        String typeValue = defaultText(type, "GENERAL");

        String message;
        if (ru) {
            if ("strict".equals(chosenTone)) {
                message = String.format("%s, у вас критический риск по посещаемости (%d, %s). Необходимо обязательно посещать занятия и закрыть пропуски в ближайшие дни. Группа: %s.", name, score, level, grp);
            } else if ("neutral".equals(chosenTone)) {
                message = String.format("%s, у вас повышенный риск по посещаемости (%d, %s). Рекомендуем стабильно посещать занятия. Группа: %s.", name, score, level, grp);
            } else {
                message = String.format("%s, видим, что посещаемость просела (%d, %s). Давайте выровняем график: начните с ближайших пар и сообщите, если нужна помощь. Группа: %s.", name, score, level, grp);
            }
        } else {
            if ("strict".equals(chosenTone)) {
                message = String.format("%s, сіздің қатысуыңыз бойынша тәуекел өте жоғары (%d, %s). Сабақтарды міндетті түрде толық қатысып, жіберілген сабақтарды тез арада жабыңыз. Топ: %s.", name, score, level, grp);
            } else if ("neutral".equals(chosenTone)) {
                message = String.format("%s, қатысу көрсеткішіңіз бойынша тәуекел жоғары (%d, %s). Сабақтарға тұрақты қатысуды ұсынамыз. Топ: %s.", name, score, level, grp);
            } else {
                message = String.format("%s, қатысу сәл төмендеп тұр (%d, %s). Келесі сабақтардан бастап тұрақты қатысып көрейік, қажет болса көмектесеміз. Топ: %s.", name, score, level, grp);
            }
        }

        return Map.of(
                "studentId", target.getOrDefault("studentId", studentId),
                "studentName", name,
                "riskScore", score,
                "riskLevel", level,
                "type", typeValue,
                "tone", chosenTone,
                "lang", ru ? "ru" : "kz",
                "message", message
        );
    }

    public Map<String, Object> chat(String userId, String role, String query) {
        Scope scope = resolveScope(userId, role);
        if (!scope.allowed) {
            return Map.of("answer", "Unauthorized", "items", List.of());
        }

        String q = defaultText(query, "").toLowerCase(Locale.ROOT);
        List<Map<String, Object>> items;
        String answer;

        if (containsAny(q, "опаз", "кеш", "late")) {
            items = riskPredictions(userId, role).stream()
                    .filter(i -> ((Number) i.get("lateCount")).intValue() > 0)
                    .limit(5)
                    .toList();
            answer = "Top students with the most late arrivals.";
        } else if (containsAny(q, "не приш", "қатысп", "absent", "пропуск")) {
            items = riskPredictions(userId, role).stream()
                    .filter(i -> ((Number) i.get("absentCount")).intValue() > 0)
                    .limit(5)
                    .toList();
            answer = "Top students with absences.";
        } else if (containsAny(q, "анома", "аномал", "qr")) {
            items = qrAnomalies(userId, role).stream().limit(5).toList();
            answer = "QR anomaly list for recent sessions.";
        } else if (containsAny(q, "рис", "тәуек", "risk")) {
            items = riskPredictions(userId, role).stream().limit(5).toList();
            answer = "Highest risk students by current attendance patterns.";
        } else {
            items = reasonClusters(userId, role).stream().limit(4).toList();
            answer = "Top attendance behavior clusters by day/time/subject/status.";
        }

        return Map.of(
                "query", defaultText(query, ""),
                "answer", answer,
                "items", items,
                "generatedAt", LocalDateTime.now()
        );
    }

    public List<Map<String, Object>> qrAnomalies(String userId, String role) {
        Scope scope = resolveScope(userId, role);
        if (!scope.allowed) {
            return List.of();
        }

        Set<String> classIds = scopedClassIds(scope);
        List<QRSession> sessions = qrSessionRepository.findAll().stream()
                .filter(s -> s.getClassId() != null && classIds.contains(s.getClassId()))
                .toList();

        Map<String, List<Attendance>> bySession = attendanceRepository.findAll().stream()
                .filter(a -> a.getQrSessionId() != null)
                .collect(Collectors.groupingBy(Attendance::getQrSessionId));

        List<Map<String, Object>> anomalies = new ArrayList<>();
        for (QRSession session : sessions) {
            List<Attendance> records = bySession.getOrDefault(session.getId(), List.of());
            if (records.isEmpty() || session.getCreatedAt() == null) {
                continue;
            }

            long first20Sec = records.stream()
                    .filter(a -> a.getCheckInTime() != null)
                    .filter(a -> Duration.between(session.getCreatedAt(), a.getCheckInTime()).toSeconds() <= 20)
                    .count();

            long veryLate = records.stream()
                    .filter(a -> "LATE".equals(a.getStatus()))
                    .count();

            int severity = 0;
            List<String> reasons = new ArrayList<>();
            if (first20Sec >= 8) {
                severity += 50;
                reasons.add("Too many check-ins in the first 20 seconds");
            }
            if (veryLate >= Math.max(3, records.size() / 2)) {
                severity += 25;
                reasons.add("Unusually high late check-in ratio");
            }
            if (session.getExpiresAt() != null && session.getCreatedAt() != null && session.getExpiresAt().isBefore(session.getCreatedAt())) {
                severity += 35;
                reasons.add("Session expiration is earlier than creation time");
            }

            if (severity == 0) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sessionId", session.getId());
            item.put("classId", session.getClassId());
            item.put("className", defaultText(session.getClassName(), "-"));
            item.put("teacherId", defaultText(session.getTeacherId(), "-"));
            item.put("teacherName", defaultText(session.getTeacherName(), "-"));
            item.put("createdAt", session.getCreatedAt());
            item.put("checkIns", records.size());
            item.put("first20SecCheckIns", first20Sec);
            item.put("lateCheckIns", veryLate);
            item.put("severityScore", Math.min(severity, 100));
            item.put("severityLevel", severity >= 70 ? "HIGH" : "MEDIUM");
            item.put("reasons", reasons);
            anomalies.add(item);
        }

        anomalies.sort((a, b) -> Integer.compare(((Number) b.get("severityScore")).intValue(), ((Number) a.get("severityScore")).intValue()));
        return anomalies;
    }

    private Scope resolveScope(String userId, String role) {
        Scope scope = new Scope();
        scope.userId = userId;
        scope.role = role;
        scope.allowed = userId != null && ("ADMIN".equals(role) || "TEACHER".equals(role));
        if (scope.allowed && "TEACHER".equals(role)) {
            scope.classIds = classRepository.findByTeacherId(userId).stream()
                    .map(Class::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        return scope;
    }

    private List<User> scopedStudents(Scope scope) {
        List<User> students = userRepository.findByRole("STUDENT");
        if (!"TEACHER".equals(scope.role)) {
            return students;
        }

        Set<String> teacherGroups = classRepository.findByTeacherId(scope.userId).stream()
                .map(Class::getGroup)
                .filter(g -> g != null && !g.isBlank())
                .collect(Collectors.toSet());

        return students.stream()
                .filter(u -> u.getGroup() != null && teacherGroups.contains(u.getGroup()))
                .toList();
    }

    private List<Attendance> scopedAttendance(Scope scope) {
        if (!"TEACHER".equals(scope.role)) {
            return attendanceRepository.findAll();
        }

        if (scope.classIds == null || scope.classIds.isEmpty()) {
            return List.of();
        }

        return attendanceRepository.findAll().stream()
                .filter(a -> a.getClassId() != null && scope.classIds.contains(a.getClassId()))
                .toList();
    }

    private Set<String> scopedClassIds(Scope scope) {
        if (!"TEACHER".equals(scope.role)) {
            return classRepository.findAll().stream().map(Class::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return scope.classIds != null ? scope.classIds : Set.of();
    }

    private Map<String, Object> cluster(String clusterType, Map.Entry<String, Long> entry, long total, String insight) {
        String label = entry != null ? entry.getKey() : "N/A";
        long count = entry != null ? entry.getValue() : 0L;
        double share = total > 0 ? round(count * 100.0 / total) : 0.0;

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("clusterType", clusterType);
        item.put("label", label);
        item.put("count", count);
        item.put("sharePercent", share);
        item.put("insight", insight);
        return item;
    }

    private Map.Entry<String, Long> topEntry(Map<String, Long> map) {
        return map.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
    }

    private Map<String, Object> buildAlert(String type, Class cls, User student, String reason, int baseScore) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("key", LocalDate.now() + ":" + cls.getId() + ":" + student.getId() + ":" + type);
        alert.put("type", type);
        alert.put("severity", "ABSENT".equals(type) ? "HIGH" : "MEDIUM");
        alert.put("riskScore", baseScore);
        alert.put("studentId", student.getId());
        alert.put("studentName", student.getFullName());
        alert.put("studentEmail", student.getEmail());
        alert.put("group", defaultText(student.getGroup(), cls.getGroup()));
        alert.put("classId", cls.getId());
        alert.put("className", defaultText(cls.getName(), "-"));
        alert.put("teacherId", defaultText(cls.getTeacherId(), "-"));
        alert.put("teacherName", defaultText(cls.getTeacherName(), "-"));
        alert.put("reason", reason);
        return alert;
    }

    private LocalTime endTimeOrDefault(LocalTime endTime, LocalTime startTime) {
        if (endTime != null) {
            return endTime;
        }
        if (startTime == null) {
            return LocalTime.of(23, 59);
        }
        return startTime.plusMinutes(90);
    }

    private double calcRate(List<Attendance> records) {
        if (records == null || records.isEmpty()) {
            return 0.0;
        }
        long total = records.size();
        long good = records.stream().filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus())).count();
        return round(good * 100.0 / total);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private boolean containsAny(String query, String... tokens) {
        for (String token : tokens) {
            if (query.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private String recommendationFor(String level, double attendanceRate) {
        if ("HIGH".equals(level)) {
            return "Immediate intervention needed: contact student and parent/advisor, set weekly attendance target.";
        }
        if ("MEDIUM".equals(level)) {
            return "Monitor attendance weekly and send reminder before each class.";
        }
        if (attendanceRate >= 95) {
            return "Excellent consistency. Keep current attendance routine.";
        }
        return "Stable status. Continue current attendance behavior.";
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String safeName(String value) {
        if (value == null || value.isBlank()) {
            return "Student";
        }
        return value;
    }

    private static class Scope {
        String userId;
        String role;
        boolean allowed;
        Set<String> classIds;
    }
}
