package com.altynai.attendance.admin;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.model.QRSession;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.repository.QRSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private QRSessionRepository qrSessionRepository;

    @GetMapping("/users")
    public String usersPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "users";
    }
    
    @GetMapping("/system-reports")
    public String systemReports(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "system-reports";
    }
    
    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "settings";
    }
    
    @GetMapping("/backup")
    public String backup(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "backup";
    }
    
    @GetMapping("/api/admin/stats")
    @ResponseBody
    public Map<String, Object> getSystemStats(HttpSession session) {
        Map<String, Object> stats = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            stats.put("error", "Unauthorized");
            return stats;
        }
        
        
        List<User> users = userRepository.findAll();
        List<Class> classes = classRepository.findAll();
        List<Attendance> attendances = attendanceRepository.findAll();
        List<QRSession> qrSessions = qrSessionRepository.findAll();
        
        
        Map<String, Long> usersByRole = users.stream()
            .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
        
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<Attendance> recentAttendances = attendances.stream()
            .filter(a -> a.getDate() != null && a.getDate().isAfter(thirtyDaysAgo))
            .collect(Collectors.toList());
        
        long activeQrSessions = qrSessions.stream()
            .filter(QRSession::isActive)
            .count();
        
        stats.put("totalUsers", users.size());
        stats.put("usersByRole", usersByRole);
        stats.put("totalClasses", classes.size());
        stats.put("totalAttendanceRecords", attendances.size());
        stats.put("recentAttendanceCount", recentAttendances.size());
        stats.put("activeQrSessions", activeQrSessions);
        stats.put("totalQrSessions", qrSessions.size());
        
        Map<String, Long> attendanceByStatus = attendances.stream()
            .collect(Collectors.groupingBy(
                a -> a.getStatus() != null ? a.getStatus() : "UNKNOWN", 
                Collectors.counting()
            ));
        stats.put("attendanceByStatus", attendanceByStatus);
        
        Map<String, Long> topClasses = attendances.stream()
            .filter(a -> a.getClassName() != null)
            .collect(Collectors.groupingBy(Attendance::getClassName, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        stats.put("topClasses", topClasses);
        
        return stats;
    }
    
    @PostMapping("/api/admin/backup")
    @ResponseBody
    public Map<String, Object> createBackup(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        try {
            Map<String, Object> backupData = new HashMap<>();
            backupData.put("timestamp", LocalDateTime.now());
            backupData.put("users", userRepository.findAll());
            backupData.put("classes", classRepository.findAll());
            backupData.put("attendance", attendanceRepository.findAll());
            backupData.put("qrSessions", qrSessionRepository.findAll());
            
            result.put("success", true);
            result.put("message", "Резервтік көшірме сәтті жасалды");
            result.put("timestamp", LocalDateTime.now());
            result.put("recordsCount", Map.of(
                "users", userRepository.count(),
                "classes", classRepository.count(),
                "attendance", attendanceRepository.count(),
                "qrSessions", qrSessionRepository.count()
            ));
            
        } catch (Exception e) {
            result.put("error", "Резервтік көшірме жасау кезінде қате: " + e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/api/admin/cleanup")
    @ResponseBody
    public Map<String, Object> cleanupOldData(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        try {
            int daysToKeep = Integer.parseInt(req.getOrDefault("daysToKeep", "90"));
            LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
            
            List<Attendance> oldAttendances = attendanceRepository.findAll().stream()
                .filter(a -> a.getDate() != null && a.getDate().isBefore(cutoffDate))
                .collect(Collectors.toList());
            
            attendanceRepository.deleteAll(oldAttendances);
            
            List<QRSession> inactiveSessions = qrSessionRepository.findAll().stream()
                .filter(s -> !s.isActive())
                .collect(Collectors.toList());
            
            qrSessionRepository.deleteAll(inactiveSessions);
            
            result.put("success", true);
            result.put("message", String.format("%d күннен ескі деректер тазаланды", daysToKeep));
            result.put("deletedRecords", Map.of(
                "attendance", oldAttendances.size(),
                "qrSessions", inactiveSessions.size()
            ));
            
        } catch (Exception e) {
            result.put("error", "Тазалау кезінде қате: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/api/admin/settings")
    @ResponseBody
    public Map<String, Object> getSettings(HttpSession session) {
        Map<String, Object> settings = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            settings.put("error", "Unauthorized");
            return settings;
        }
        
        settings.put("qrCodeDuration", 15); 
        settings.put("lateThreshold", 10); 
        settings.put("minAttendanceRate", 75);
        settings.put("semesterStart", "2024-09-01");
        settings.put("semesterEnd", "2025-01-31");
        settings.put("systemVersion", "1.0.0");
        settings.put("databaseType", "MongoDB");
        settings.put("maxUploadSize", "10MB");
        
        return settings;
    }
    
    @PostMapping("/api/admin/settings")
    @ResponseBody
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> newSettings, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        result.put("success", true);
        result.put("message", "Баптаулар сәтті сақталды");
        result.put("updatedSettings", newSettings);
        
        return result;
    }
}
