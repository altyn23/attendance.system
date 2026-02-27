package com.altynai.attendance.admin;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.AbsenceExplanation;
import com.altynai.attendance.model.AcademicGroup;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.model.ClassChangeLog;
import com.altynai.attendance.model.ScheduleChangeRequest;
import com.altynai.attendance.repository.AbsenceExplanationRepository;
import com.altynai.attendance.repository.AcademicGroupRepository;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassChangeLogRepository;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.repository.ScheduleChangeRequestRepository;
import com.altynai.attendance.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/ops")
public class AdminOperationsController {
    private final UserRepository userRepository;
    private final AcademicGroupRepository groupRepository;
    private final ClassRepository classRepository;
    private final ScheduleChangeRequestRepository changeRequestRepository;
    private final ClassChangeLogRepository classChangeLogRepository;
    private final AbsenceExplanationRepository absenceExplanationRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    public AdminOperationsController(
            UserRepository userRepository,
            AcademicGroupRepository groupRepository,
            ClassRepository classRepository,
            ScheduleChangeRequestRepository changeRequestRepository,
            ClassChangeLogRepository classChangeLogRepository,
            AbsenceExplanationRepository absenceExplanationRepository,
            AttendanceRepository attendanceRepository,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.classRepository = classRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.classChangeLogRepository = classChangeLogRepository;
        this.absenceExplanationRepository = absenceExplanationRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/reference-data")
    public Map<String, Object> referenceData(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }

        List<AcademicGroup> groups = groupRepository.findAll().stream()
                .sorted(Comparator.comparing(AcademicGroup::getGroupCode, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<User> teachers = userRepository.findByRole("TEACHER").stream()
                .sorted(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
        List<User> admins = userRepository.findByRole("ADMIN").stream()
                .sorted(Comparator.comparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        List<String> subjects = teachers.stream()
                .map(User::getSubject)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        List<String> rooms = classRepository.findAll().stream()
                .map(Class::getRoom)
                .filter(Objects::nonNull)
                .filter(r -> !r.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        res.put("success", true);
        res.put("groups", groups);
        res.put("teachers", teachers);
        res.put("admins", admins);
        res.put("subjects", subjects);
        res.put("rooms", rooms);
        return res;
    }

    @GetMapping("/requests")
    public Map<String, Object> scheduleRequests(
            @RequestParam(required = false) String status,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }
        String adminId = (String) session.getAttribute("userId");

        List<ScheduleChangeRequest> requests;
        if (status != null && !status.isBlank()) {
            requests = changeRequestRepository.findByStatusOrderByCreatedAtDesc(status.toUpperCase(Locale.ROOT));
        } else {
            requests = changeRequestRepository.findByTargetAdminIdOrderByCreatedAtDesc(adminId);
        }
        res.put("success", true);
        res.put("requests", requests);
        return res;
    }

    @PostMapping("/requests/{requestId}/decision")
    public Map<String, Object> decideRequest(
            @PathVariable String requestId,
            @RequestBody Map<String, String> req,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }

        String decision = req.getOrDefault("decision", "").toUpperCase(Locale.ROOT);
        String comment = req.getOrDefault("comment", "");
        String adminId = (String) session.getAttribute("userId");

        ScheduleChangeRequest changeRequest = changeRequestRepository.findById(requestId).orElse(null);
        if (changeRequest == null) {
            res.put("error", "Request not found");
            return res;
        }
        if (!"PENDING".equals(changeRequest.getStatus())) {
            res.put("error", "Request already processed");
            return res;
        }

        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) {
            res.put("error", "decision must be APPROVED or REJECTED");
            return res;
        }

        changeRequest.setStatus(decision);
        changeRequest.setAdminComment(comment);
        changeRequest.setProcessedAt(LocalDateTime.now());
        changeRequestRepository.save(changeRequest);

        if ("APPROVED".equals(decision)) {
            applyClassChange(changeRequest.getClassId(), changeRequest.getType(), changeRequest.getReason(),
                    adminId, changeRequest.getNewTeacherId(), changeRequest.getNewRoom(), changeRequest.getRequestedDate());
        }

        notificationService.notifyUser(
                changeRequest.getRequesterTeacherId(),
                "REQUEST_" + decision,
                "Запрос по расписанию обработан",
                "Ваш запрос " + requestId + " -> " + decision + ". " + comment,
                requestId
        );

        res.put("success", true);
        res.put("request", changeRequest);
        return res;
    }

    @PostMapping("/class-change")
    public Map<String, Object> classChange(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }

        String classId = req.get("classId");
        String action = req.getOrDefault("action", "UPDATE").toUpperCase(Locale.ROOT);
        String reason = req.getOrDefault("reason", "").trim();
        String newTeacherId = req.get("newTeacherId");
        String newRoom = req.get("newRoom");
        LocalDate effectiveDate = parseDate(req.get("effectiveDate"));

        if (classId == null || classId.isBlank()) {
            res.put("error", "classId is required");
            return res;
        }
        if (reason.isBlank()) {
            res.put("error", "Причина обязательна для любых изменений");
            return res;
        }

        Class changed = applyClassChange(
                classId,
                action,
                reason,
                (String) session.getAttribute("userId"),
                newTeacherId,
                newRoom,
                effectiveDate
        );
        if (changed == null) {
            res.put("error", "Class not found");
            return res;
        }

        res.put("success", true);
        res.put("class", changed);
        return res;
    }

    @PostMapping("/absence/request")
    public Map<String, Object> requestAbsenceExplanation(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role) && !"TEACHER".equals(role)) {
            res.put("error", "Unauthorized");
            return res;
        }

        String attendanceId = req.get("attendanceId");
        if (attendanceId == null || attendanceId.isBlank()) {
            res.put("error", "attendanceId is required");
            return res;
        }

        Attendance attendance = attendanceRepository.findById(attendanceId).orElse(null);
        if (attendance == null) {
            res.put("error", "Attendance not found");
            return res;
        }
        if (!"ABSENT".equals(attendance.getStatus())) {
            res.put("error", "Запрос причины доступен только для ABSENT");
            return res;
        }

        AbsenceExplanation existing = absenceExplanationRepository.findByAttendanceId(attendanceId).orElse(null);
        if (existing != null) {
            res.put("success", true);
            res.put("explanation", existing);
            return res;
        }

        AbsenceExplanation explanation = new AbsenceExplanation();
        explanation.setAttendanceId(attendance.getId());
        explanation.setStudentId(attendance.getStudentId());
        explanation.setStudentName(attendance.getStudentName());
        explanation.setClassId(attendance.getClassId());
        explanation.setClassName(attendance.getClassName());
        explanation.setClassDate(attendance.getDate());
        explanation.setGroup(attendance.getGroup());
        explanation.setReasonType("PENDING");
        explanation.setStatus("REQUESTED");
        explanation.setUpdatedAt(LocalDateTime.now());
        explanation = absenceExplanationRepository.save(explanation);

        notificationService.notifyUser(
                attendance.getStudentId(),
                "ABSENCE_REASON_REQUIRED",
                "Требуется объяснение пропуска",
                "Укажите причину пропуска по предмету " + attendance.getClassName() + " (" + attendance.getDate() + ")",
                explanation.getId()
        );

        res.put("success", true);
        res.put("explanation", explanation);
        return res;
    }

    @GetMapping("/absence")
    public Map<String, Object> absenceForAdmin(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String status,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }

        List<AbsenceExplanation> items = (status == null || status.isBlank())
                ? absenceExplanationRepository.findAll()
                : absenceExplanationRepository.findByStatusOrderByCreatedAtDesc(status);

        if (group != null && !group.isBlank()) {
            items = items.stream().filter(i -> group.equals(i.getGroup())).toList();
        }
        items = items.stream()
                .sorted(Comparator.comparing(AbsenceExplanation::getCreatedAt, Comparator.reverseOrder()))
                .toList();

        res.put("success", true);
        res.put("items", items);
        return res;
    }

    @PostMapping("/absence/{id}/review")
    public Map<String, Object> reviewAbsence(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("error", "Unauthorized");
            return res;
        }
        String decision = req.getOrDefault("decision", "").toUpperCase(Locale.ROOT);
        String comment = req.getOrDefault("comment", "");
        if (!"APPROVED".equals(decision) && !"REJECTED".equals(decision)) {
            res.put("error", "decision must be APPROVED or REJECTED");
            return res;
        }

        AbsenceExplanation item = absenceExplanationRepository.findById(id).orElse(null);
        if (item == null) {
            res.put("error", "Not found");
            return res;
        }
        item.setStatus(decision);
        item.setReviewComment(comment);
        item.setReviewedByAdminId((String) session.getAttribute("userId"));
        item.setUpdatedAt(LocalDateTime.now());
        absenceExplanationRepository.save(item);

        notificationService.notifyUser(
                item.getStudentId(),
                "ABSENCE_REVIEW",
                "Результат по объяснительной",
                "Ваше объяснение по пропуску: " + decision,
                item.getId()
        );

        res.put("success", true);
        res.put("item", item);
        return res;
    }

    private Class applyClassChange(
            String classId,
            String action,
            String reason,
            String actorUserId,
            String newTeacherId,
            String newRoom,
            LocalDate effectiveDate
    ) {
        Class cls = classRepository.findById(classId).orElse(null);
        if (cls == null) {
            return null;
        }

        String oldTeacherId = cls.getTeacherId();
        String oldRoom = cls.getRoom();

        if ("REPLACE_TEACHER".equals(action) || "UPDATE".equals(action)) {
            if (newTeacherId != null && !newTeacherId.isBlank()) {
                User newTeacher = userRepository.findById(newTeacherId).orElse(null);
                if (newTeacher != null && "TEACHER".equals(newTeacher.getRole())) {
                    cls.setTeacherId(newTeacher.getId());
                    cls.setTeacherName(newTeacher.getFullName().trim());
                }
            }
        }
        if ("CHANGE_ROOM".equals(action) || "UPDATE".equals(action)) {
            if (newRoom != null && !newRoom.isBlank()) {
                cls.setRoom(newRoom);
            }
        }
        if ("CANCEL".equals(action)) {
            String marker = " [CANCELLED: " + reason + "]";
            cls.setDescription((cls.getDescription() == null ? "" : cls.getDescription()) + marker);
        }
        cls.setUpdatedAt(LocalDateTime.now());
        classRepository.save(cls);

        ClassChangeLog log = new ClassChangeLog();
        log.setClassId(classId);
        log.setAction(action);
        log.setReason(reason);
        log.setChangedByUserId(actorUserId);
        log.setOldTeacherId(oldTeacherId);
        log.setNewTeacherId(cls.getTeacherId());
        log.setOldRoom(oldRoom);
        log.setNewRoom(cls.getRoom());
        log.setEffectiveDate(effectiveDate);
        classChangeLogRepository.save(log);

        notificationService.notifyUser(
                cls.getTeacherId(),
                "SCHEDULE_CHANGE",
                "Изменение в расписании",
                "По вашей паре (" + cls.getName() + ") внесено изменение: " + action + ". Причина: " + reason,
                classId
        );
        notificationService.notifyGroupStudents(
                cls.getGroup(),
                "SCHEDULE_CHANGE",
                "Изменение расписания",
                "По группе " + cls.getGroup() + " изменена пара " + cls.getName()
                        + ". Новая аудитория: " + cls.getRoom() + ". Причина: " + reason,
                classId
        );

        if (oldTeacherId != null && !oldTeacherId.equals(cls.getTeacherId())) {
            notificationService.notifyUser(
                    oldTeacherId,
                    "TEACHER_REPLACED",
                    "Вы заменены на паре",
                    "По паре " + cls.getName() + " назначен другой преподаватель. Причина: " + reason,
                    classId
            );
        }
        return cls;
    }

    private boolean isAdmin(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("role"));
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
