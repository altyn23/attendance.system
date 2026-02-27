package com.altynai.attendance.controller;

import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @GetMapping("/my-attendance")
    public String myAttendancePage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            return "redirect:/dashboard";
        }
        
        List<Attendance> attendances = attendanceRepository.findByStudentIdOrderByDateDescCheckInTimeDesc(userId);
        
        int totalClasses = attendances.size();
        long presentCount = attendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long lateCount = attendances.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long absentCount = attendances.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        
        double attendanceRate = totalClasses > 0 ? (double)(presentCount + lateCount) / totalClasses * 100 : 0;

        List<Attendance> recentAttendances = attendances.stream().limit(20).toList();
        Map<String, Object> trend = buildTrendData(attendances);
        Map<String, Object> subjectStats = buildSubjectStats(attendances);
        
        model.addAttribute("attendances", recentAttendances);
        model.addAttribute("totalClasses", totalClasses);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("lateCount", lateCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("attendanceRate", String.format("%.1f", attendanceRate));
        model.addAttribute("attendanceTrendLabels", trend.get("labels"));
        model.addAttribute("attendanceTrendValues", trend.get("values"));
        model.addAttribute("subjectLabels", subjectStats.get("labels"));
        model.addAttribute("subjectValues", subjectStats.get("values"));
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "my-attendance";
    }
    
    @GetMapping("/attendance-report")
    public String attendanceReportPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        List<Attendance> attendances;
        if ("TEACHER".equals(role)) {
            attendances = attendanceRepository.findByTeacherId(userId);
        } else {
            attendances = attendanceRepository.findAll();
        }
        
        Map<String, List<Attendance>> attendanceByClass = attendances.stream()
            .collect(Collectors.groupingBy(Attendance::getClassName));
        
        model.addAttribute("attendanceByClass", attendanceByClass);
        model.addAttribute("totalRecords", attendances.size());
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "attendance-report";
    }
    
    @GetMapping("/api/attendance/stats/{studentId}")
    @ResponseBody
    public Map<String, Object> getStudentStats(@PathVariable String studentId, HttpSession session) {
        Map<String, Object> stats = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) {
            stats.put("error", "Unauthorized");
            return stats;
        }
        if (!"ADMIN".equals(role) && !"TEACHER".equals(role) && !userId.equals(studentId)) {
            stats.put("error", "Forbidden");
            return stats;
        }
        
        List<Attendance> attendances = attendanceRepository.findByStudentId(studentId);
        
        int totalClasses = attendances.size();
        long presentCount = attendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long lateCount = attendances.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long absentCount = attendances.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        
        stats.put("total", totalClasses);
        stats.put("present", presentCount);
        stats.put("late", lateCount);
        stats.put("absent", absentCount);
        stats.put("rate", totalClasses > 0 ? (double)(presentCount + lateCount) / totalClasses * 100 : 0);
        
        return stats;
    }
    
    @GetMapping("/api/attendance/today")
    @ResponseBody
    public List<Attendance> getTodayAttendance(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) {
            return List.of();
        }
        
        LocalDate today = LocalDate.now();
        
        if ("STUDENT".equals(role)) {
            return attendanceRepository.findByStudentIdAndDate(userId, today);
        } else if ("TEACHER".equals(role)) {
            return attendanceRepository.findByDate(today).stream()
                .filter(a -> userId.equals(a.getTeacherId()))
                .collect(Collectors.toList());
        } else {
            return attendanceRepository.findByDate(today);
        }
    }

    @GetMapping("/api/attendance/all")
    @ResponseBody
    public List<Attendance> getAllAttendance(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) {
            return List.of();
        }

        if ("ADMIN".equals(role)) {
            return attendanceRepository.findAll();
        }
        if ("TEACHER".equals(role)) {
            return attendanceRepository.findByTeacherId(userId);
        }
        return attendanceRepository.findByStudentId(userId);
    }

    private Map<String, Object> buildTrendData(List<Attendance> attendances) {
        Map<LocalDate, List<Attendance>> byDay = attendances.stream()
                .filter(a -> a.getDate() != null)
                .collect(Collectors.groupingBy(Attendance::getDate));

        List<LocalDate> datesWithData = byDay.keySet().stream().sorted().toList();
        if (datesWithData.isEmpty()) {
            List<String> labels = List.of("Бүгін");
            List<Double> values = List.of(0.0);
            return Map.of("labels", labels, "values", values);
        }

        int fromIndex = Math.max(0, datesWithData.size() - 10);
        List<LocalDate> recentDates = datesWithData.subList(fromIndex, datesWithData.size());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        long total = 0;
        long good = 0;

        for (LocalDate day : recentDates) {
            labels.add(day.format(formatter));
            List<Attendance> dayAttendances = byDay.get(day);
            total += dayAttendances.size();
            good += dayAttendances.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                    .count();
            values.add(total > 0 ? (double) good * 100 / total : 0.0);
        }

        if (values.size() == 1) {
            labels = List.of(labels.get(0), labels.get(0));
            values = List.of(values.get(0), values.get(0));
        }

        return Map.of("labels", labels, "values", values);
    }

    private Map<String, Object> buildSubjectStats(List<Attendance> attendances) {
        Map<String, List<Attendance>> byClass = attendances.stream()
                .filter(a -> a.getClassId() != null && !a.getClassId().isBlank())
                .collect(Collectors.groupingBy(Attendance::getClassId));

        List<Map.Entry<String, List<Attendance>>> entries = byClass.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(6)
                .toList();

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> entry : entries) {
            List<Attendance> classAttendances = entry.getValue();
            String className = classAttendances.get(0).getClassName() != null
                    ? classAttendances.get(0).getClassName()
                    : entry.getKey();
            long good = classAttendances.stream()
                    .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                    .count();
            labels.add(className);
            values.add((double) good * 100 / classAttendances.size());
        }
        return Map.of("labels", labels, "values", values);
    }
}
