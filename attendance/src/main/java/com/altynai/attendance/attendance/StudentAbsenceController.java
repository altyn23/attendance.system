package com.altynai.attendance.attendance;

import com.altynai.attendance.model.AbsenceExplanation;
import com.altynai.attendance.repository.AbsenceExplanationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/student/absence")
public class StudentAbsenceController {
    private final AbsenceExplanationRepository absenceExplanationRepository;

    public StudentAbsenceController(AbsenceExplanationRepository absenceExplanationRepository) {
        this.absenceExplanationRepository = absenceExplanationRepository;
    }

    @GetMapping("/my")
    public Map<String, Object> myAbsenceRequests(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String studentId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            res.put("error", "Unauthorized");
            return res;
        }
        res.put("success", true);
        res.put("items", absenceExplanationRepository.findByStudentIdOrderByCreatedAtDesc(studentId));
        return res;
    }

    @PostMapping("/{id}/reply")
    public Map<String, Object> reply(
            @PathVariable String id,
            @RequestBody Map<String, String> req,
            HttpSession session
    ) {
        Map<String, Object> res = new HashMap<>();
        String studentId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            res.put("error", "Unauthorized");
            return res;
        }

        AbsenceExplanation explanation = absenceExplanationRepository.findById(id).orElse(null);
        if (explanation == null || !studentId.equals(explanation.getStudentId())) {
            res.put("error", "Not found");
            return res;
        }

        String reasonType = req.getOrDefault("reasonType", "").toUpperCase(Locale.ROOT);
        String reasonText = req.getOrDefault("reasonText", "").trim();

        if (reasonType.isBlank()) {
            res.put("error", "reasonType is required");
            return res;
        }

        if ("MEDICAL".equals(reasonType)) {
            LocalDate medicalFrom = parseDate(req.get("medicalFrom"));
            LocalDate medicalTo = parseDate(req.get("medicalTo"));
            if (medicalFrom == null || medicalTo == null) {
                res.put("error", "Для MEDICAL укажите medicalFrom и medicalTo");
                return res;
            }
            explanation.setMedicalFrom(medicalFrom);
            explanation.setMedicalTo(medicalTo);
        }

        explanation.setReasonType(reasonType);
        explanation.setReasonText(reasonText);
        explanation.setAttachmentUrl(req.get("attachmentUrl"));
        explanation.setStatus("SUBMITTED");
        explanation.setUpdatedAt(LocalDateTime.now());
        absenceExplanationRepository.save(explanation);

        res.put("success", true);
        res.put("item", explanation);
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
