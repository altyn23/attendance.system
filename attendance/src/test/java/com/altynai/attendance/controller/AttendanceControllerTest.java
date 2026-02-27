package com.altynai.attendance.controller;

import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.repository.AttendanceRepository;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AttendanceControllerTest {

    private AttendanceController controller;
    private AttendanceRepository attendanceRepository;

    @BeforeEach
    void setUp() {
        controller = new AttendanceController();
        attendanceRepository = mock(AttendanceRepository.class);
        ReflectionTestUtils.setField(controller, "attendanceRepository", attendanceRepository);
    }

    @AfterEach
    void tearDown() {
        reset(attendanceRepository);
    }

    @Test
    void getAllAttendance_returnsEmptyForUnauthorized() {
        List<Attendance> res = controller.getAllAttendance(new MockHttpSession());
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void getAllAttendance_returnsAllForAdmin() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");
        when(attendanceRepository.findAll()).thenReturn(List.of(new Attendance(), new Attendance()));

        List<Attendance> res = controller.getAllAttendance(session);
        assertEquals(2, res.size());
        verify(attendanceRepository, times(1)).findAll();
    }

    @Test
    void getAllAttendance_returnsTeacherScope() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "T001");
        session.setAttribute("role", "TEACHER");
        when(attendanceRepository.findByTeacherId("T001")).thenReturn(List.of(new Attendance()));

        List<Attendance> res = controller.getAllAttendance(session);
        assertEquals(1, res.size());
        verify(attendanceRepository, times(1)).findByTeacherId("T001");
    }

    @Test
    void getAllAttendance_returnsStudentScope() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "S001");
        session.setAttribute("role", "STUDENT");
        when(attendanceRepository.findByStudentId("S001")).thenReturn(List.of(new Attendance()));

        List<Attendance> res = controller.getAllAttendance(session);
        assertEquals(1, res.size());
        verify(attendanceRepository, times(1)).findByStudentId("S001");
    }

    @Test
    void getStudentStats_forbiddenForOtherStudent() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "S001");
        session.setAttribute("role", "STUDENT");

        Map<String, Object> stats = controller.getStudentStats("S999", session);
        assertEquals("Forbidden", stats.get("error"));
    }

    @Test
    void getStudentStats_calculatesValues() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");

        Attendance a1 = new Attendance(); a1.setStatus("PRESENT");
        Attendance a2 = new Attendance(); a2.setStatus("LATE");
        Attendance a3 = new Attendance(); a3.setStatus("ABSENT");
        when(attendanceRepository.findByStudentId("S001")).thenReturn(List.of(a1, a2, a3));

        Map<String, Object> stats = controller.getStudentStats("S001", session);
        assertEquals(3, stats.get("total"));
        assertEquals(1L, stats.get("present"));
        assertEquals(1L, stats.get("late"));
        assertEquals(1L, stats.get("absent"));
        assertEquals(66.66666666666666, stats.get("rate"));
    }

    @Test
    void getTodayAttendance_forTeacherFiltersByTeacherId() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "T001");
        session.setAttribute("role", "TEACHER");

        Attendance own = new Attendance();
        own.setTeacherId("T001");
        own.setDate(LocalDate.now());
        Attendance other = new Attendance();
        other.setTeacherId("T999");
        other.setDate(LocalDate.now());

        when(attendanceRepository.findByDate(LocalDate.now())).thenReturn(List.of(own, other));

        List<Attendance> res = controller.getTodayAttendance(session);
        assertEquals(1, res.size());
        assertEquals("T001", res.get(0).getTeacherId());
        assertFalse(res.stream().anyMatch(a -> "T999".equals(a.getTeacherId())));
    }
}
