package com.altynai.attendance.auth;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    private AuthController controller;
    private UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeAll
    static void beforeAll() {
        System.out.println("AuthControllerTest started");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("AuthControllerTest finished");
    }

    @BeforeEach
    void setUp() {
        controller = new AuthController();
        userRepository = mock(UserRepository.class);
        ReflectionTestUtils.setField(controller, "userRepository", userRepository);
    }

    @AfterEach
    void tearDown() {
        reset(userRepository);
    }

    @Test
    void register_requiresAdminSession() {
        MockHttpSession session = new MockHttpSession();
        Map<String, Object> res = controller.register(Map.of(), session);

        assertTrue(res.containsKey("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_validatesMandatoryFields() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ADMIN");

        Map<String, Object> res = controller.register(Map.of("email", "x@mail.com"), session);

        assertEquals("Барлық міндетті өрістерді толтырыңыз", res.get("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_rejectsInvalidEmail() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ADMIN");

        Map<String, String> req = Map.of(
                "email", "bad-email",
                "password", "123456",
                "firstName", "A",
                "lastName", "B"
        );
        Map<String, Object> res = controller.register(req, session);

        assertEquals("Қате email форматы", res.get("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_rejectsDuplicateEmail() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ADMIN");
        when(userRepository.findByEmail("s001@college.edu.kz")).thenReturn(Optional.of(new User()));

        Map<String, String> req = Map.of(
                "email", "s001@college.edu.kz",
                "password", "123456",
                "firstName", "A",
                "lastName", "B"
        );
        Map<String, Object> res = controller.register(req, session);

        assertEquals("Бұл email тіркелген", res.get("error"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_successfulFlow() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ADMIN");
        when(userRepository.findByEmail("new@college.edu.kz")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("U-1");
            return u;
        });

        Map<String, String> req = Map.of(
                "email", "new@college.edu.kz",
                "password", "Strong123!",
                "firstName", "New",
                "lastName", "User",
                "role", "TEACHER"
        );
        Map<String, Object> res = controller.register(req, session);

        assertEquals(true, res.get("success"));
        assertEquals("U-1", res.get("userId"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_failsForUnknownEmail() {
        MockHttpSession session = new MockHttpSession();
        when(userRepository.findByEmail("none@mail.com")).thenReturn(Optional.empty());

        Map<String, Object> res = controller.login(Map.of("email", "none@mail.com", "password", "x"), session);

        assertEquals("Қате email немесе құпиясөз", res.get("error"));
        assertNull(session.getAttribute("userId"));
    }

    @Test
    void login_failsForWrongPassword() {
        MockHttpSession session = new MockHttpSession();
        User u = new User();
        u.setEmail("u@mail.com");
        u.setPasswordHash(encoder.encode("Strong123!"));
        when(userRepository.findByEmail("u@mail.com")).thenReturn(Optional.of(u));

        Map<String, Object> res = controller.login(Map.of("email", "u@mail.com", "password", "wrong"), session);

        assertEquals("Қате email немесе құпиясөз", res.get("error"));
        assertNull(session.getAttribute("userId"));
    }

    @Test
    void login_setsSessionOnSuccess() {
        MockHttpSession session = new MockHttpSession();
        User u = new User();
        u.setId("S001");
        u.setEmail("s001@college.edu.kz");
        u.setFirstName("Аружан");
        u.setLastName("Абишева");
        u.setRole("STUDENT");
        u.setPasswordHash(encoder.encode("Strong123!"));
        when(userRepository.findByEmail("s001@college.edu.kz")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenReturn(u);

        Map<String, Object> res = controller.login(
                Map.of("email", "s001@college.edu.kz", "password", "Strong123!"),
                session
        );

        assertEquals(true, res.get("success"));
        assertEquals("S001", session.getAttribute("userId"));
        assertEquals("STUDENT", session.getAttribute("role"));
        assertNotNull(session.getAttribute("fullName"));
    }

    @Test
    void allUsers_requiresAdminRole() {
        MockHttpSession session = new MockHttpSession();
        Object res = controller.allUsers(session);
        assertTrue(res instanceof Map<?, ?>);
        assertEquals("Unauthorized", ((Map<?, ?>) res).get("error"));
    }

    @Test
    void allUsers_adminGetsList() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("role", "ADMIN");
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));

        Object res = controller.allUsers(session);

        assertTrue(res instanceof List<?>);
        assertEquals(2, ((List<?>) res).size());
    }
}
