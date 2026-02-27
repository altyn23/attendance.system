package com.altynai.attendance.ai;

import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.repository.QRSessionRepository;
import com.altynai.attendance.settings.SystemSettingsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AiInsightsServiceTest {

    @Test
    void unauthorizedRequestsReturnSafeDefaults() {
        AiInsightsService service = buildService();

        assertTrue(service.smartAlerts(null, "ADMIN").isEmpty());
        assertTrue(service.riskPredictions(null, "ADMIN").isEmpty());
        assertTrue(service.reasonClusters(null, "ADMIN").isEmpty());
        assertTrue(service.qrAnomalies(null, "ADMIN").isEmpty());

        Map<String, Object> summary = service.adminSummary(null, "ADMIN");
        assertEquals("Unauthorized", summary.get("summary"));

        Map<String, Object> draft = service.teacherMessageDraft(null, "ADMIN", null, "GENERAL", "ru", "neutral");
        assertEquals("Unauthorized", draft.get("message"));
    }

    @Test
    void teacherScopeWithNoAssignedClassesReturnsEmptyRisk() {
        ClassRepository classRepository = mock(ClassRepository.class);
        AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
        QRSessionRepository qrSessionRepository = mock(QRSessionRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        SystemSettingsService settingsService = mock(SystemSettingsService.class);

        when(classRepository.findByTeacherId("teacher-1")).thenReturn(List.of());
        when(userRepository.findByRole("STUDENT")).thenReturn(List.of());

        AiInsightsService service = new AiInsightsService(
                attendanceRepository,
                classRepository,
                qrSessionRepository,
                userRepository,
                settingsService
        );

        assertTrue(service.riskPredictions("teacher-1", "TEACHER").isEmpty());
        assertTrue(service.reasonClusters("teacher-1", "TEACHER").isEmpty());
    }

    private AiInsightsService buildService() {
        return new AiInsightsService(
                mock(AttendanceRepository.class),
                mock(ClassRepository.class),
                mock(QRSessionRepository.class),
                mock(UserRepository.class),
                mock(SystemSettingsService.class)
        );
    }
}
