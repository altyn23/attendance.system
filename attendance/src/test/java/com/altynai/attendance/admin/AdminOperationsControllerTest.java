package com.altynai.attendance.admin;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.AbsenceExplanation;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.*;
import com.altynai.attendance.service.NotificationService;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AdminOperationsControllerTest {

    private UserRepository userRepository;
    private AcademicGroupRepository groupRepository;
    private ClassRepository classRepository;
    private ScheduleChangeRequestRepository requestRepository;
    private ClassChangeLogRepository classChangeLogRepository;
    private AbsenceExplanationRepository absenceExplanationRepository;
    private AttendanceRepository attendanceRepository;
    private NotificationService notificationService;
    private AdminOperationsController controller;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        groupRepository = mock(AcademicGroupRepository.class);
        classRepository = mock(ClassRepository.class);
        requestRepository = mock(ScheduleChangeRequestRepository.class);
        classChangeLogRepository = mock(ClassChangeLogRepository.class);
        absenceExplanationRepository = mock(AbsenceExplanationRepository.class);
        attendanceRepository = mock(AttendanceRepository.class);
        notificationService = mock(NotificationService.class);

        controller = new AdminOperationsController(
                userRepository,
                groupRepository,
                classRepository,
                requestRepository,
                classChangeLogRepository,
                absenceExplanationRepository,
                attendanceRepository,
                notificationService
        );
    }

    @Test
    void classChange_requiresAdmin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "TEACHER");
        Map<String, Object> res = controller.classChange(Map.of(), session);
        assertEquals("Unauthorized", res.get("error"));
    }

    @Test
    void classChange_requiresReason() {
        MockHttpSession session = adminSession();
        Map<String, Object> res = controller.classChange(Map.of("classId", "C1"), session);
        assertEquals("Причина обязательна для любых изменений", res.get("error"));
    }

    @Test
    void classChange_returnsNotFoundWhenClassMissing() {
        MockHttpSession session = adminSession();
        when(classRepository.findById("C404")).thenReturn(Optional.empty());
        Map<String, Object> res = controller.classChange(
                Map.of("classId", "C404", "reason", "test"),
                session
        );
        assertEquals("Class not found", res.get("error"));
    }

    @Test
    void classChange_updatesRoomAndTeacherAndSendsNotifications() {
        MockHttpSession session = adminSession();
        Class cls = new Class();
        cls.setId("C1");
        cls.setName("Java");
        cls.setGroup("PO-2401");
        cls.setRoom("401");
        cls.setTeacherId("T001");
        cls.setTeacherName("Old Teacher");

        User newTeacher = new User();
        newTeacher.setId("T002");
        newTeacher.setRole("TEACHER");
        newTeacher.setFirstName("New");
        newTeacher.setLastName("Teacher");

        when(classRepository.findById("C1")).thenReturn(Optional.of(cls));
        when(userRepository.findById("T002")).thenReturn(Optional.of(newTeacher));
        when(classRepository.save(any(Class.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> res = controller.classChange(
                Map.of(
                        "classId", "C1",
                        "action", "UPDATE",
                        "reason", "Қажеттілік",
                        "newTeacherId", "T002",
                        "newRoom", "410"
                ),
                session
        );

        assertEquals(true, res.get("success"));
        assertEquals("T002", cls.getTeacherId());
        assertEquals("410", cls.getRoom());
        verify(classChangeLogRepository, times(1)).save(any());
        verify(notificationService, atLeast(2)).notifyUser(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(notificationService, times(1)).notifyGroupStudents(eq("PO-2401"), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void requestAbsenceExplanation_requiresAttendanceId() {
        MockHttpSession session = adminSession();
        Map<String, Object> res = controller.requestAbsenceExplanation(Map.of(), session);
        assertEquals("attendanceId is required", res.get("error"));
    }

    @Test
    void requestAbsenceExplanation_rejectsNonAbsentStatus() {
        MockHttpSession session = adminSession();
        Attendance attendance = new Attendance();
        attendance.setId("A1");
        attendance.setStatus("PRESENT");
        when(attendanceRepository.findById("A1")).thenReturn(Optional.of(attendance));

        Map<String, Object> res = controller.requestAbsenceExplanation(Map.of("attendanceId", "A1"), session);
        assertEquals("Запрос причины доступен только для ABSENT", res.get("error"));
    }

    @Test
    void reviewAbsence_validatesDecision() {
        MockHttpSession session = adminSession();
        Map<String, Object> res = controller.reviewAbsence("ID1", Map.of("decision", "MAYBE"), session);
        assertEquals("decision must be APPROVED or REJECTED", res.get("error"));
    }

    @Test
    void reviewAbsence_successPath() {
        MockHttpSession session = adminSession();
        AbsenceExplanation item = new AbsenceExplanation();
        item.setId("EX1");
        item.setStudentId("S001");
        when(absenceExplanationRepository.findById("EX1")).thenReturn(Optional.of(item));

        Map<String, Object> res = controller.reviewAbsence(
                "EX1",
                Map.of("decision", "APPROVED", "comment", "ok"),
                session
        );

        assertEquals(true, res.get("success"));
        assertEquals("APPROVED", ((AbsenceExplanation) res.get("item")).getStatus());
        verify(absenceExplanationRepository, times(1)).save(any(AbsenceExplanation.class));
        verify(notificationService, times(1)).notifyUser(eq("S001"), eq("ABSENCE_REVIEW"), anyString(), anyString(), eq("EX1"));
    }

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");
        return session;
    }
}
