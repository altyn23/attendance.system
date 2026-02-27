package com.altynai.attendance.controller;

import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/classes")
public class ClassController {
    private static final Logger log = LoggerFactory.getLogger(ClassController.class);

    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Value("${app.seed-demo-classes.enabled:true}")
    private boolean seedDemoClassesEnabled;
    
    @PostConstruct
    public void initTestData() {
        if (!seedDemoClassesEnabled) {
            return;
        }

        if (classRepository.count() == 0) {
            List<Class> testClasses = new ArrayList<>();
            
            Class class1 = new Class();
            class1.setName("Программалау");
            class1.setRoom("305");
            class1.setDayOfWeek(DayOfWeek.MONDAY);
            class1.setStartTime(LocalTime.of(9, 0));
            class1.setEndTime(LocalTime.of(10, 30));
            class1.setGroup("ИС-2101");
            class1.setDepartment("Ақпараттық жүйелер");
            class1.setSemester("2024-Fall");
            class1.setCredits(3);
            class1.setDescription("Java программалау тілі");
            testClasses.add(class1);
            
            Class class2 = new Class();
            class2.setName("Деректер қоры");
            class2.setRoom("412");
            class2.setDayOfWeek(DayOfWeek.TUESDAY);
            class2.setStartTime(LocalTime.of(11, 0));
            class2.setEndTime(LocalTime.of(12, 30));
            class2.setGroup("ИС-2101");
            class2.setDepartment("Ақпараттық жүйелер");
            class2.setSemester("2024-Fall");
            class2.setCredits(3);
            class2.setDescription("SQL және NoSQL деректер қоры");
            testClasses.add(class2);
            
            Class class3 = new Class();
            class3.setName("Веб әзірлеу");
            class3.setRoom("208");
            class3.setDayOfWeek(DayOfWeek.WEDNESDAY);
            class3.setStartTime(LocalTime.of(14, 0));
            class3.setEndTime(LocalTime.of(15, 30));
            class3.setGroup("ИС-2102");
            class3.setDepartment("Ақпараттық жүйелер");
            class3.setSemester("2024-Fall");
            class3.setCredits(4);
            class3.setDescription("Frontend және Backend әзірлеу");
            testClasses.add(class3);
            
            Class class4 = new Class();
            class4.setName("Мобильді қосымша");
            class4.setRoom("310");
            class4.setDayOfWeek(DayOfWeek.THURSDAY);
            class4.setStartTime(LocalTime.of(9, 0));
            class4.setEndTime(LocalTime.of(10, 30));
            class4.setGroup("ИС-2103");
            class4.setDepartment("Ақпараттық жүйелер");
            class4.setSemester("2024-Fall");
            class4.setCredits(3);
            class4.setDescription("Android және iOS әзірлеу");
            testClasses.add(class4);
            
            Class class5 = new Class();
            class5.setName("Жасанды интеллект");
            class5.setRoom("501");
            class5.setDayOfWeek(DayOfWeek.FRIDAY);
            class5.setStartTime(LocalTime.of(13, 0));
            class5.setEndTime(LocalTime.of(14, 30));
            class5.setGroup("ИС-2101");
            class5.setDepartment("Ақпараттық жүйелер");
            class5.setSemester("2024-Fall");
            class5.setCredits(4);
            class5.setDescription("Machine Learning және AI негіздері");
            testClasses.add(class5);
            
            List<User> teachers = userRepository.findAll().stream()
                .filter(u -> "TEACHER".equals(u.getRole()) || "ADMIN".equals(u.getRole()))
                .toList();
            
            String teacherId = null;
            String teacherName = null;
            
            if (!teachers.isEmpty()) {
                User teacher = teachers.get(0);
                teacherId = teacher.getId();
                teacherName = teacher.getFullName();
            }
            
            for (Class cls : testClasses) {
                if (teacherId != null) {
                    cls.setTeacherId(teacherId);
                    cls.setTeacherName(teacherName);
                }
                classRepository.save(cls);
            }
            
            log.info("Demo classes created: {}", testClasses.size());
        }
    }
    
    @GetMapping
    public List<Class> getAllClasses() {
        return classRepository.findAll();
    }
    
    @GetMapping("/teacher/{teacherId}")
    public List<Class> getTeacherClasses(@PathVariable String teacherId) {
        return classRepository.findByTeacherId(teacherId);
    }
    
    @GetMapping("/group/{group}")
    public List<Class> getGroupClasses(@PathVariable String group) {
        return classRepository.findByGroup(group);
    }

    @GetMapping("/teachers")
    public Map<String, Object> getTeachers(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || (!"TEACHER".equals(role) && !"ADMIN".equals(role))) {
            res.put("error", "Unauthorized");
            return res;
        }

        List<Map<String, String>> teachers = userRepository.findAll().stream()
                .filter(u -> "TEACHER".equals(u.getRole()))
                .map(u -> {
                    Map<String, String> item = new HashMap<>();
                    item.put("id", u.getId());
                    item.put("fullName", u.getFullName().trim());
                    item.put("email", u.getEmail());
                    return item;
                })
                .sorted(Comparator.comparing(i -> i.get("fullName"), String.CASE_INSENSITIVE_ORDER))
                .toList();

        res.put("success", true);
        res.put("teachers", teachers);
        return res;
    }

    @GetMapping("/groups")
    public Map<String, Object> getGroups(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null || (!"TEACHER".equals(role) && !"ADMIN".equals(role))) {
            res.put("error", "Unauthorized");
            return res;
        }

        Set<String> studentGroups = userRepository.findByRole("STUDENT").stream()
                .map(User::getGroup)
                .filter(g -> g != null && !g.isBlank())
                .collect(Collectors.toSet());

        Set<String> classGroups = classRepository.findAll().stream()
                .map(Class::getGroup)
                .filter(g -> g != null && !g.isBlank())
                .collect(Collectors.toSet());

        List<String> groups = new ArrayList<>();
        groups.addAll(studentGroups);
        groups.addAll(classGroups);
        groups = groups.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        res.put("success", true);
        res.put("groups", groups);
        return res;
    }
    
    @PostMapping
    public Map<String, Object> createClass(@RequestBody Class newClass, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        
        if (!"ADMIN".equals(role)) {
            res.put("error", "Сабақты тек әкімшілік тағайындайды");
            return res;
        }

        if (newClass.getName() == null || newClass.getName().isBlank()
                || newClass.getGroup() == null || newClass.getGroup().isBlank()
                || newClass.getRoom() == null || newClass.getRoom().isBlank()) {
            res.put("error", "Сабақ атауы, топ және аудитория міндетті");
            return res;
        }

        String teacherId = newClass.getTeacherId();
        if (teacherId != null && !teacherId.isBlank()) {
            User selectedTeacher = userRepository.findById(teacherId).orElse(null);
            if (selectedTeacher == null || !"TEACHER".equals(selectedTeacher.getRole())) {
                res.put("error", "Таңдалған оқытушы табылмады");
                return res;
            }
            newClass.setTeacherId(selectedTeacher.getId());
            newClass.setTeacherName(selectedTeacher.getFullName().trim());
        } else {
            newClass.setTeacherId(null);
            newClass.setTeacherName(null);
        }
        
        Class savedClass = classRepository.save(newClass);
        res.put("success", true);
        res.put("class", savedClass);
        res.put("message", "Сабақ сәтті қосылды");
        
        return res;
    }
    
    @PutMapping("/{id}")
    public Map<String, Object> updateClass(@PathVariable String id, @RequestBody Class updatedClass, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        
        Optional<Class> classOpt = classRepository.findById(id);
        if (classOpt.isEmpty()) {
            res.put("error", "Сабақ табылмады");
            return res;
        }
        
        Class existingClass = classOpt.get();
        
        if (!"ADMIN".equals(role)) {
            res.put("error", "Сабақты тек әкімшілік өзгерте алады");
            return res;
        }
        
        existingClass.setName(updatedClass.getName());
        existingClass.setRoom(updatedClass.getRoom());
        existingClass.setDayOfWeek(updatedClass.getDayOfWeek());
        existingClass.setStartTime(updatedClass.getStartTime());
        existingClass.setEndTime(updatedClass.getEndTime());
        existingClass.setGroup(updatedClass.getGroup());
        existingClass.setDepartment(updatedClass.getDepartment());
        existingClass.setSemester(updatedClass.getSemester());
        existingClass.setCredits(updatedClass.getCredits());
        existingClass.setDescription(updatedClass.getDescription());

        String teacherId = updatedClass.getTeacherId();
        if (teacherId != null && !teacherId.isBlank()) {
            User selectedTeacher = userRepository.findById(teacherId).orElse(null);
            if (selectedTeacher == null || !"TEACHER".equals(selectedTeacher.getRole())) {
                res.put("error", "Таңдалған оқытушы табылмады");
                return res;
            }
            existingClass.setTeacherId(selectedTeacher.getId());
            existingClass.setTeacherName(selectedTeacher.getFullName().trim());
        } else {
            existingClass.setTeacherId(null);
            existingClass.setTeacherName(null);
        }

        existingClass.setUpdatedAt(java.time.LocalDateTime.now());
        
        Class savedClass = classRepository.save(existingClass);
        res.put("success", true);
        res.put("class", savedClass);
        res.put("message", "Сабақ сәтті жаңартылды");
        
        return res;
    }
    
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteClass(@PathVariable String id, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        if (userId == null) {
            res.put("error", "Unauthorized");
            return res;
        }
        
        Optional<Class> classOpt = classRepository.findById(id);
        if (classOpt.isEmpty()) {
            res.put("error", "Сабақ табылмады");
            return res;
        }
        
        Class existingClass = classOpt.get();
        
        if (!"ADMIN".equals(role)) {
            res.put("error", "Сабақты тек әкімшілік жоя алады");
            return res;
        }
        
        classRepository.deleteById(id);
        res.put("success", true);
        res.put("message", "Сабақ сәтті жойылды");
        
        return res;
    }
}
