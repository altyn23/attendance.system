package com.altynai.attendance.controller;

import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.ClassRepository;
import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import jakarta.annotation.PostConstruct;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostConstruct
    public void initTestData() {
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
            
            System.out.println("✅ Тестовые занятия созданы: " + testClasses.size());
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
    
    @PostMapping
    public Map<String, Object> createClass(@RequestBody Class newClass, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        
        String userId = (String) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        
        if (!"TEACHER".equals(role) && !"ADMIN".equals(role)) {
            res.put("error", "Тек оқытушылар сабақ қоса алады");
            return res;
        }
        
        User teacher = userRepository.findById(userId).orElse(null);
        if (teacher != null) {
            newClass.setTeacherId(userId);
            newClass.setTeacherName(teacher.getFullName());
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
        
        Optional<Class> classOpt = classRepository.findById(id);
        if (classOpt.isEmpty()) {
            res.put("error", "Сабақ табылмады");
            return res;
        }
        
        Class existingClass = classOpt.get();
        
        if (!"ADMIN".equals(role) && !userId.equals(existingClass.getTeacherId())) {
            res.put("error", "Сізде бұл сабақты өзгертуге құқық жоқ");
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
        
        Optional<Class> classOpt = classRepository.findById(id);
        if (classOpt.isEmpty()) {
            res.put("error", "Сабақ табылмады");
            return res;
        }
        
        Class existingClass = classOpt.get();
        
        if (!"ADMIN".equals(role) && !userId.equals(existingClass.getTeacherId())) {
            res.put("error", "Сізде бұл сабақты жоюға құқық жоқ");
            return res;
        }
        
        classRepository.deleteById(id);
        res.put("success", true);
        res.put("message", "Сабақ сәтті жойылды");
        
        return res;
    }
}
