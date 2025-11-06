package com.altynai.attendance.controller;

import com.altynai.attendance.model.QRSession;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.repository.QRSessionRepository;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class QRCodeController {

    @Autowired
    private QRSessionRepository qrSessionRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/qr-scan")
    public String qrScanPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"STUDENT".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "qr-scan";
    }
    
    @GetMapping("/generate-qr")
    public String generateQRPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        List<com.altynai.attendance.model.Class> classes;
        if ("ADMIN".equals(role)) {
            classes = classRepository.findAll();
        } else {
            classes = classRepository.findByTeacherId(userId);
        }
        
        model.addAttribute("classes", classes);
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "generate-qr";
    }
    
    @PostMapping("/api/qr/generate")
    @ResponseBody
    public Map<String, Object> generateQR(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            res.put("error", "Авторизацияны қайта өтіңіз");
            return res;
        }
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            res.put("error", "Тек оқытушылар QR код жасай алады");
            return res;
        }
        
        String classId = req.get("classId");
        String room = req.get("room");
        int duration = req.containsKey("duration") ? Integer.parseInt(req.get("duration")) : 15;
        
        Optional<com.altynai.attendance.model.Class> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) {
            res.put("error", "Сабақ табылмады");
            return res;
        }
        
        com.altynai.attendance.model.Class classInfo = classOpt.get();
        User teacher = userRepository.findById(userId).orElse(null);
        
        QRSession qrSession = new QRSession();
        qrSession.setClassId(classId);
        qrSession.setClassName(classInfo.getName());
        qrSession.setTeacherId(userId);
        qrSession.setTeacherName(teacher != null ? teacher.getFullName() : "");
        qrSession.setRoom(room != null ? room : classInfo.getRoom());
        qrSession.setQrCode(generateUniqueCode());
        qrSession.setExpiresAt(LocalDateTime.now().plusMinutes(duration));
        
        qrSession = qrSessionRepository.save(qrSession);
        
        res.put("success", true);
        res.put("qrCode", qrSession.getQrCode());
        res.put("sessionId", qrSession.getId());
        res.put("expiresAt", qrSession.getExpiresAt().toString());
        res.put("message", "QR код сәтті жасалды");
        
        return res;
    }
    
    @PostMapping("/api/qr/scan")
    @ResponseBody
    public Map<String, Object> scanQR(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            res.put("error", "Авторизацияны қайта өтіңіз");
            return res;
        }
        
        if (!"STUDENT".equals(role)) {
            res.put("error", "Тек студенттер QR код сканерлей алады");
            return res;
        }
        
        String qrCode = req.get("qrCode");
        
        Optional<QRSession> sessionOpt = qrSessionRepository.findByQrCodeAndActive(qrCode, true);
        if (sessionOpt.isEmpty()) {
            res.put("error", "QR код жарамсыз немесе мерзімі өтіп кеткен");
            return res;
        }
        
        QRSession qrSession = sessionOpt.get();
        
        if (!qrSession.isActive()) {
            res.put("error", "QR кодтың мерзімі аяқталған");
            qrSession.setActive(false);
            qrSessionRepository.save(qrSession);
            return res;
        }
        
        Optional<Attendance> existingAttendance = attendanceRepository.findByStudentIdAndQrSessionId(userId, qrSession.getId());
        if (existingAttendance.isPresent()) {
            res.put("error", "Сіз бұл сабаққа қатысуды тіркегенсіз");
            return res;
        }
        
        User student = userRepository.findById(userId).orElse(null);
        if (student == null) {
            res.put("error", "Студент мәліметтері табылмады");
            return res;
        }
        
        Attendance attendance = new Attendance();
        attendance.setStudentId(userId);
        attendance.setStudentName(student.getFullName());
        attendance.setStudentEmail(student.getEmail());
        attendance.setClassId(qrSession.getClassId());
        attendance.setClassName(qrSession.getClassName());
        attendance.setTeacherId(qrSession.getTeacherId());
        attendance.setTeacherName(qrSession.getTeacherName());
        attendance.setQrSessionId(qrSession.getId());
        attendance.setGroup(student.getGroup());
        attendance.setSemester("2024-Fall"); // TODO: Сделать динамическим
        
        long minutesSinceCreation = java.time.Duration.between(qrSession.getCreatedAt(), LocalDateTime.now()).toMinutes();
        if (minutesSinceCreation > 10) {
            attendance.setStatus("LATE");
            attendance.setNotes("Кешігіп келді");
        }
        
        attendanceRepository.save(attendance);
        
        qrSession.addScannedStudent(userId);
        qrSessionRepository.save(qrSession);
        
        res.put("success", true);
        res.put("message", "Қатысу сәтті тіркелді!");
        res.put("className", qrSession.getClassName());
        res.put("room", qrSession.getRoom());
        res.put("status", attendance.getStatus());
        
        return res;
    }
    
    @GetMapping("/api/qr/sessions")
    @ResponseBody
    public Map<String, Object> getActiveSessions(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            res.put("error", "Авторизацияны қайта өтіңіз");
            return res;
        }
        
        List<QRSession> sessions = qrSessionRepository.findByTeacherId(userId);
        List<Map<String, Object>> activeSessions = new ArrayList<>();
        
        for (QRSession qrSession : sessions) {
            if (qrSession.isActive()) {
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("id", qrSession.getId());
                sessionData.put("className", qrSession.getClassName());
                sessionData.put("room", qrSession.getRoom());
                sessionData.put("qrCode", qrSession.getQrCode());
                sessionData.put("createdAt", qrSession.getCreatedAt());
                sessionData.put("expiresAt", qrSession.getExpiresAt());
                sessionData.put("scannedCount", qrSession.getScannedStudentIds().size());
                activeSessions.add(sessionData);
            }
        }
        
        res.put("success", true);
        res.put("sessions", activeSessions);
        
        return res;
    }
    
    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
