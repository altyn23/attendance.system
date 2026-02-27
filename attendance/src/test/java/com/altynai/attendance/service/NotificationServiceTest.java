package com.altynai.attendance.service;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.SystemNotification;
import com.altynai.attendance.repository.SystemNotificationRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private SystemNotificationRepository notificationRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;

    @BeforeAll
    static void beforeAll() {
        System.out.println("NotificationServiceTest started");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("NotificationServiceTest finished");
    }

    @BeforeEach
    void setUp() {
        notificationRepository = mock(SystemNotificationRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = new NotificationService(notificationRepository, userRepository);
    }

    @AfterEach
    void tearDown() {
        reset(notificationRepository, userRepository);
    }

    @Test
    void notifyUser_savesNotificationWhenRecipientExists() {
        User user = new User();
        user.setId("S001");
        user.setRole("STUDENT");
        when(userRepository.findById("S001")).thenReturn(Optional.of(user));

        notificationService.notifyUser("S001", "SCHEDULE_CHANGE", "Title", "Message", "cls-1");

        ArgumentCaptor<SystemNotification> captor = ArgumentCaptor.forClass(SystemNotification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        SystemNotification saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("S001", saved.getRecipientUserId());
        assertEquals("STUDENT", saved.getRecipientRole());
        assertEquals("SCHEDULE_CHANGE", saved.getType());
        assertTrue(saved.getCreatedAt() != null);
    }

    @Test
    void notifyUser_doesNotSaveWhenUserMissing() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        notificationService.notifyUser("missing", "TYPE", "Title", "Message", "x");

        verify(notificationRepository, never()).save(any(SystemNotification.class));
        assertFalse(userRepository.findById("missing").isPresent());
    }

    @Test
    void notifyGroupStudents_sendsForEveryStudentInGroup() {
        User st1 = new User();
        st1.setId("S001");
        st1.setRole("STUDENT");

        User st2 = new User();
        st2.setId("S002");
        st2.setRole("STUDENT");

        when(userRepository.findByRoleAndGroup("STUDENT", "PO-2401")).thenReturn(List.of(st1, st2));
        when(userRepository.findById("S001")).thenReturn(Optional.of(st1));
        when(userRepository.findById("S002")).thenReturn(Optional.of(st2));

        notificationService.notifyGroupStudents("PO-2401", "ABSENCE", "Title", "Msg", "att-1");

        verify(notificationRepository, times(2)).save(any(SystemNotification.class));
        verify(userRepository, times(1)).findByRoleAndGroup("STUDENT", "PO-2401");
    }
}
