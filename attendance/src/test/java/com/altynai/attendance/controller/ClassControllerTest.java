package com.altynai.attendance.controller;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.ClassRepository;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClassControllerTest {

    private ClassController controller;
    private ClassRepository classRepository;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        controller = new ClassController();
        classRepository = mock(ClassRepository.class);
        userRepository = mock(UserRepository.class);
        ReflectionTestUtils.setField(controller, "classRepository", classRepository);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
    }

    @AfterEach
    void tearDown() {
        reset(classRepository, userRepository);
    }

    @Test
    void createClass_rejectsTeacherRole() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "T001");
        session.setAttribute("role", "TEACHER");

        Map<String, Object> res = controller.createClass(new Class(), session);

        assertEquals("Сабақты тек әкімшілік тағайындайды", res.get("error"));
        verify(classRepository, never()).save(any(Class.class));
    }

    @Test
    void createClass_validatesRequiredFields() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");

        Class req = new Class();
        req.setName("Java");
        Map<String, Object> res = controller.createClass(req, session);

        assertEquals("Сабақ атауы, топ және аудитория міндетті", res.get("error"));
        verify(classRepository, never()).save(any(Class.class));
    }

    @Test
    void createClass_rejectsUnknownTeacher() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");

        Class req = new Class();
        req.setName("Java");
        req.setGroup("PO-2401");
        req.setRoom("401");
        req.setTeacherId("T999");
        when(userRepository.findById("T999")).thenReturn(Optional.empty());

        Map<String, Object> res = controller.createClass(req, session);
        assertEquals("Таңдалған оқытушы табылмады", res.get("error"));
    }

    @Test
    void createClass_adminSuccess() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");

        Class req = new Class();
        req.setName("Java");
        req.setGroup("PO-2401");
        req.setRoom("401");
        req.setTeacherId("T001");

        User teacher = new User();
        teacher.setId("T001");
        teacher.setRole("TEACHER");
        teacher.setFirstName("Аяулым");
        teacher.setLastName("Ахметова");

        when(userRepository.findById("T001")).thenReturn(Optional.of(teacher));
        when(classRepository.save(any(Class.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> res = controller.createClass(req, session);

        assertEquals(true, res.get("success"));
        verify(classRepository, times(1)).save(any(Class.class));
    }

    @Test
    void updateClass_rejectsTeacherRole() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "T001");
        session.setAttribute("role", "TEACHER");

        Class existing = new Class();
        existing.setId("C1");
        when(classRepository.findById("C1")).thenReturn(Optional.of(existing));

        Map<String, Object> res = controller.updateClass("C1", new Class(), session);
        assertEquals("Сабақты тек әкімшілік өзгерте алады", res.get("error"));
    }

    @Test
    void deleteClass_rejectsTeacherRole() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "T001");
        session.setAttribute("role", "TEACHER");

        Class existing = new Class();
        existing.setId("C1");
        when(classRepository.findById("C1")).thenReturn(Optional.of(existing));

        Map<String, Object> res = controller.deleteClass("C1", session);
        assertEquals("Сабақты тек әкімшілік жоя алады", res.get("error"));
        verify(classRepository, never()).deleteById("C1");
    }

    @Test
    void getGroups_returnsDistinctSortedList() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("role", "ADMIN");

        User s1 = new User(); s1.setGroup("PO-2402");
        User s2 = new User(); s2.setGroup("PO-2401");
        when(userRepository.findByRole("STUDENT")).thenReturn(List.of(s1, s2));

        Class c1 = new Class(); c1.setGroup("PO-2403");
        Class c2 = new Class(); c2.setGroup("PO-2402");
        when(classRepository.findAll()).thenReturn(List.of(c1, c2));

        Map<String, Object> res = controller.getGroups(session);
        assertEquals(true, res.get("success"));
        assertEquals(List.of("PO-2401", "PO-2402", "PO-2403"), res.get("groups"));
    }
}
