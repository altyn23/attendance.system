package com.altynai.attendance.teacher;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.Attendance;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.AttendanceRepository;
import com.altynai.attendance.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TeacherController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/my-classes")
    public String myClassesPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "my-classes";
    }
    
    @GetMapping("/student-list")
    public String studentListPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("userId", userId);
        model.addAttribute("fullName", session.getAttribute("fullName"));
        model.addAttribute("role", role);
        
        return "student-list";
    }
    

    
    @GetMapping("/api/teacher/students")
    @ResponseBody
    public Map<String, Object> getTeacherStudents(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (userId == null) {
            result.put("error", "Unauthorized");
            return result;
        }

        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        Set<String> groups = getTeacherGroups(userId, role);

        List<User> registeredStudents = userRepository.findAll().stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .filter(u -> u.getRegistrationDate() != null)
                .toList();

        List<User> visibleStudents;
        if ("ADMIN".equals(role)) {
            visibleStudents = registeredStudents;
        } else if (groups.isEmpty()) {
            visibleStudents = List.of();
        } else {
            visibleStudents = registeredStudents.stream()
                    .filter(u -> u.getGroup() != null && groups.contains(u.getGroup()))
                    .toList();
        }

        Map<String, List<User>> studentsByGroup = visibleStudents.stream()
                .filter(u -> u.getGroup() != null && !u.getGroup().isBlank())
                .sorted(Comparator.comparing(User::getGroup).thenComparing(User::getLastName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.groupingBy(
                        User::getGroup,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<User> unassignedStudents = registeredStudents.stream()
                .filter(u -> u.getGroup() == null || u.getGroup().isBlank())
                .sorted(Comparator.comparing(User::getRegistrationDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<String> sortedGroups = new ArrayList<>(studentsByGroup.keySet());
        sortedGroups.sort(String::compareToIgnoreCase);

        result.put("students", visibleStudents);
        result.put("studentsByGroup", studentsByGroup);
        result.put("groups", sortedGroups);
        result.put("totalStudents", visibleStudents.size());
        result.put("unassignedStudents", "ADMIN".equals(role) ? unassignedStudents : List.of());
        result.put("unassignedCount", "ADMIN".equals(role) ? unassignedStudents.size() : 0);
        result.put("scopedByTeacherGroups", !"ADMIN".equals(role));
        
        return result;
    }
    
    @PostMapping("/api/teacher/add-student")
    @ResponseBody
    public Map<String, Object> addStudentToGroup(@RequestBody Map<String, String> req, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        String email = req.get("email");
        String firstName = req.get("firstName");
        String lastName = req.get("lastName");
        String group = req.get("group");
        String password = req.get("password");

        if (email == null || email.isBlank() || firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank() || group == null || group.isBlank()) {
            result.put("error", "Міндетті өрістерді толтырыңыз");
            return result;
        }

        if (userRepository.findByEmail(email).isPresent()) {
            result.put("error", "Бұл email тіркелген");
            return result;
        }
        
        User student = new User();
        student.setEmail(email);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setGroup(group);
        student.setRole("STUDENT");
        student.setPasswordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
            .encode(password != null ? password : "student123"));
        student.setRegistrationDate(java.time.LocalDateTime.now());
        
        userRepository.save(student);
        
        result.put("success", true);
        result.put("message", "Студент сәтті қосылды");
        result.put("student", student);
        
        return result;
    }
    
    @GetMapping("/api/teacher/group-stats/{group}")
    @ResponseBody
    public Map<String, Object> getGroupStatistics(@PathVariable String group, HttpSession session) {
        Map<String, Object> stats = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            stats.put("error", "Unauthorized");
            return stats;
        }

        if (!"ADMIN".equals(role)) {
            Set<String> teacherGroups = getTeacherGroups(userId, role);
            if (!teacherGroups.contains(group)) {
                stats.put("error", "Бұл топ бойынша мәліметке қолжетім жоқ");
                return stats;
            }
        }
        
        List<User> students = userRepository.findAll().stream()
            .filter(u -> "STUDENT".equals(u.getRole()))
            .filter(u -> group.equals(u.getGroup()))
            .collect(Collectors.toList());
        
        List<Attendance> groupAttendance = attendanceRepository.findByGroupAndDate(group, LocalDate.now());
        
        long presentCount = groupAttendance.stream().filter(a -> "PRESENT".equals(a.getStatus())).count();
        long lateCount = groupAttendance.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long absentCount = students.size() - presentCount - lateCount;
        
        stats.put("group", group);
        stats.put("totalStudents", students.size());
        stats.put("presentToday", presentCount);
        stats.put("lateToday", lateCount);
        stats.put("absentToday", absentCount);
        stats.put("attendanceRate", students.size() > 0 ? 
            (double)(presentCount + lateCount) / students.size() * 100 : 0);
        
        Map<String, Long> studentAttendance = new HashMap<>();
        for (User student : students) {
            long attended = attendanceRepository.findByStudentId(student.getId()).stream()
                .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
                .count();
            studentAttendance.put(student.getFullName(), attended);
        }
        
        List<Map.Entry<String, Long>> topStudents = studentAttendance.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        stats.put("topStudents", topStudents);
        
        return stats;
    }
    
    @PostMapping("/api/teacher/import-students")
    @ResponseBody
    public Map<String, Object> importStudents(@RequestBody List<Map<String, String>> students, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        int imported = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();
        
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
            new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        
        for (Map<String, String> studentData : students) {
            try {
                String email = studentData.get("email");
                String group = studentData.get("group");
                
                if (userRepository.findByEmail(email).isPresent()) {
                    failed++;
                    errors.add("Email " + email + " уже существует");
                    continue;
                }

                User student = new User();
                student.setEmail(email);
                student.setFirstName(studentData.get("firstName"));
                student.setLastName(studentData.get("lastName"));
                student.setGroup(group);
                student.setRole("STUDENT");
                student.setPasswordHash(encoder.encode(
                    studentData.getOrDefault("password", "student123")
                ));
                student.setRegistrationDate(java.time.LocalDateTime.now());
                
                userRepository.save(student);
                imported++;
                
            } catch (Exception e) {
                failed++;
                errors.add("Ошибка при импорте: " + e.getMessage());
            }
        }
        
        result.put("success", imported > 0);
        result.put("imported", imported);
        result.put("failed", failed);
        result.put("errors", errors);
        result.put("message", String.format("%d студент қосылды, %d қате", imported, failed));
        
        return result;
    }
    
    @PutMapping("/api/teacher/student-group/{studentId}")
    @ResponseBody
    public Map<String, Object> changeStudentGroup(
            @PathVariable String studentId, 
            @RequestBody Map<String, String> req,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            result.put("error", "Unauthorized");
            return result;
        }
        
        String newGroup = req.get("group");
        if (newGroup == null || newGroup.isBlank()) {
            result.put("error", "Топ атауы бос болмауы керек");
            return result;
        }

        if (!"ADMIN".equals(role)) {
            Set<String> teacherGroups = getTeacherGroups(userId, role);
            if (!teacherGroups.contains(newGroup)) {
                result.put("error", "Сіз тек өз сабақтарыңыздағы топтарға ғана ауыстыра аласыз");
                return result;
            }
        }
        
        Optional<User> studentOpt = userRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            result.put("error", "Студент табылмады");
            return result;
        }
        
        User student = studentOpt.get();
        String oldGroup = student.getGroup();
        student.setGroup(newGroup);
        userRepository.save(student);
        
        result.put("success", true);
        result.put("message", String.format("Студент %s тобынан %s тобына ауыстырылды", 
            oldGroup != null ? oldGroup : "белгісіз", newGroup));
        result.put("student", student);
        
        return result;
    }
    
    @GetMapping("/api/teacher/class-stats/{classId}")
    @ResponseBody  
    public Map<String, Object> getClassStatistics(@PathVariable String classId, HttpSession session) {
        Map<String, Object> stats = new HashMap<>();
        
        String role = (String) session.getAttribute("role");
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            stats.put("error", "Unauthorized");
            return stats;
        }
        
        Optional<Class> classOpt = classRepository.findById(classId);
        if (classOpt.isEmpty()) {
            stats.put("students", 0);
            stats.put("sessions", 0);
            stats.put("rate", 0);
            return stats;
        }
        
        Class cls = classOpt.get();
        
        List<User> students = userRepository.findAll().stream()
            .filter(u -> "STUDENT".equals(u.getRole()))
            .filter(u -> cls.getGroup() != null && cls.getGroup().equals(u.getGroup()))
            .collect(Collectors.toList());
        
        List<Attendance> classAttendances = attendanceRepository.findByClassId(classId);
        
        long sessions = classAttendances.stream()
            .map(Attendance::getDate)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        
        long totalPossible = students.size() * sessions;
        long actualAttended = classAttendances.stream()
            .filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus()))
            .count();
        
        double rate = totalPossible > 0 ? (double) actualAttended / totalPossible * 100 : 0;
        
        stats.put("students", students.size());
        stats.put("sessions", sessions);
        stats.put("rate", Math.round(rate));
        
        return stats;
    }

    private Set<String> getTeacherGroups(String userId, String role) {
        if ("ADMIN".equals(role)) {
            return classRepository.findAll().stream()
                    .map(Class::getGroup)
                    .filter(Objects::nonNull)
                    .filter(g -> !g.isBlank())
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        return classRepository.findByTeacherId(userId).stream()
                .map(Class::getGroup)
                .filter(Objects::nonNull)
                .filter(g -> !g.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
