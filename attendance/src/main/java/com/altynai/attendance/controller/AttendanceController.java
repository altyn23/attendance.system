package com.altynai.attendance.controller;

import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private UserRepository userRepository;

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
        
        List<Attendance> attendances = attendanceRepository.findByStudentId(userId);
        
        int totalClasses = attendances.size();
        long presentCount = attendances.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long lateCount = attendances.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long absentCount = attendances.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        
        double attendanceRate = totalClasses > 0 ? (double)(presentCount + lateCount) / totalClasses * 100 : 0;
        
        model.addAttribute("attendances", attendances);
        model.addAttribute("totalClasses", totalClasses);
        model.addAttribute("presentCount", presentCount);
        model.addAttribute("lateCount", lateCount);
        model.addAttribute("absentCount", absentCount);
        model.addAttribute("attendanceRate", String.format("%.1f", attendanceRate));
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
    public Map<String, Object> getStudentStats(@PathVariable String studentId) {
        Map<String, Object> stats = new HashMap<>();
        
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
}
