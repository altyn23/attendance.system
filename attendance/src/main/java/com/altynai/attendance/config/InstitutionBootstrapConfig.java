package com.altynai.attendance.config;

import com.altynai.attendance.account.User;
import com.altynai.attendance.account.UserRepository;
import com.altynai.attendance.model.AcademicGroup;
import com.altynai.attendance.model.Class;
import com.altynai.attendance.repository.AcademicGroupRepository;
import com.altynai.attendance.repository.ClassRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class InstitutionBootstrapConfig {
    private final UserRepository userRepository;
    private final AcademicGroupRepository groupRepository;
    private final ClassRepository classRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final boolean enabled;

    public InstitutionBootstrapConfig(
            UserRepository userRepository,
            AcademicGroupRepository groupRepository,
            ClassRepository classRepository,
            @Value("${app.bootstrap-institution.enabled:true}") boolean enabled
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.classRepository = classRepository;
        this.enabled = enabled;
    }

    @PostConstruct
    public void seedInstitutionData() {
        if (!enabled) {
            return;
        }
        seedTeachers();
        seedAdmins();
        seedGroups();
        seedStudents();
        seedBaseClasses();
    }

    private void seedTeachers() {
        List<Map<String, String>> teachers = List.of(
                teacher("T001", "Ахметова", "Аяулым", "a.akhmetova@college.edu.kz", "AyauT001!", "+7 700 301 11 01", "2001-03-14", "Разработка мобильных приложений", "401", "PO-2401"),
                teacher("T002", "Сарсенов", "Нурасыл", "n.sarsenov@college.edu.kz", "NuraT002!", "+7 700 301 11 02", "2000-07-22", "Программирование на Java", "402", "PO-2402"),
                teacher("T003", "Бекенова", "Мадина", "m.bekenova@college.edu.kz", "MadiT003!", "+7 700 301 11 03", "2002-01-09", "Программирование на Python", "403", "PO-2403"),
                teacher("T004", "Жумабеков", "Алихан", "a.zhumabekov@college.edu.kz", "AliT004!", "+7 700 301 11 04", "2001-11-30", "Базы данных и SQL", "404", "PO-2404"),
                teacher("T005", "Иманова", "Томирис", "t.imanova@college.edu.kz", "TomiT005!", "+7 700 301 11 05", "2003-05-17", "Микроконтроллеры и Arduino", "405", "PO-2405"),
                teacher("T006", "Касымов", "Данияр", "d.kassymov@college.edu.kz", "DaniT006!", "+7 700 301 11 06", "2000-09-03", "Веб-программирование", "406", "PO-2406"),
                teacher("T007", "Абдуллина", "Аружан", "a.abdullina@college.edu.kz", "AruT007!", "+7 700 301 11 07", "2004-02-26", "Алгоритмы и структуры данных", "407", "PO-2407"),
                teacher("T008", "Муратов", "Ернур", "e.muratov@college.edu.kz", "ErnuT008!", "+7 700 301 11 08", "2002-08-11", "Компьютерные сети", "408", "PO-2408"),
                teacher("T009", "Тулегенова", "Алина", "a.tulegenova@college.edu.kz", "AlinT009!", "+7 700 301 11 09", "2001-12-05", "Информационная безопасность", "409", "PO-2409"),
                teacher("T010", "Омаров", "Расул", "r.omarov@college.edu.kz", "RasuT010!", "+7 700 301 11 10", "2003-04-19", "Инженерия программного обеспечения", "410", "PO-2410")
        );

        for (Map<String, String> t : teachers) {
            User user = userRepository.findById(t.get("id")).orElseGet(User::new);
            user.setId(t.get("id"));
            user.setExternalId(t.get("id"));
            user.setRole("TEACHER");
            user.setLastName(t.get("lastName"));
            user.setFirstName(t.get("firstName"));
            user.setEmail(t.get("email"));
            user.setPhone(t.get("phone"));
            user.setBirthDate(LocalDate.parse(t.get("birthDate")));
            user.setSubject(t.get("subject"));
            user.setCabinet(t.get("cabinet"));
            user.setCuratorGroup(t.get("curatorGroup"));
            user.setDepartment("Программное обеспечение");
            user.setRegistrationDate(Optional.ofNullable(user.getRegistrationDate()).orElse(LocalDateTime.now()));
            user.setPasswordHash(encoder.encode(t.get("password")));
            userRepository.save(user);
        }
    }

    private void seedAdmins() {
        List<Map<String, String>> admins = List.of(
                admin("A001", "Заместитель директора по учебной работе", "Нурпеисова", "Динара", "d.nurpeisova@college.edu.kz", "AdminA001!", "+7 700 401 21 01", "1992-06-18"),
                admin("A002", "Заведующий учебной частью", "Исабеков", "Мейиржан", "m.isabekov@college.edu.kz", "AdminA002!", "+7 700 401 21 02", "1990-10-07"),
                admin("A003", "Диспетчер расписания", "Кенжебаева", "Асем", "a.kenzhebaeva@college.edu.kz", "AdminA003!", "+7 700 401 21 03", "1995-01-25"),
                admin("A004", "Методист учебного отдела", "Сабитова", "Гульмира", "g.sabitova@college.edu.kz", "AdminA004!", "+7 700 401 21 04", "1993-09-12")
        );

        for (Map<String, String> a : admins) {
            User user = userRepository.findById(a.get("id")).orElseGet(User::new);
            user.setId(a.get("id"));
            user.setExternalId(a.get("id"));
            user.setRole("ADMIN");
            user.setPosition(a.get("position"));
            user.setLastName(a.get("lastName"));
            user.setFirstName(a.get("firstName"));
            user.setEmail(a.get("email"));
            user.setPhone(a.get("phone"));
            user.setBirthDate(LocalDate.parse(a.get("birthDate")));
            user.setRegistrationDate(Optional.ofNullable(user.getRegistrationDate()).orElse(LocalDateTime.now()));
            user.setPasswordHash(encoder.encode(a.get("password")));
            userRepository.save(user);
        }
    }

    private void seedGroups() {
        for (int i = 1; i <= 10; i++) {
            String code = String.format("PO-24%02d", i);
            AcademicGroup group = groupRepository.findById(code).orElseGet(AcademicGroup::new);
            group.setId(code);
            group.setGroupCode(code);
            group.setGroupNameRu("Программное обеспечение");
            group.setGroupNameKz("Бағдарламалық қамтамасыз ету");
            group.setCourse(1);
            group.setCuratorTeacherId(String.format("T%03d", i));
            groupRepository.save(group);
        }
    }

    private void seedStudents() {
        Map<String, String[]> names = parseStudentNames();
        for (int i = 1; i <= 200; i++) {
            String sid = String.format("S%03d", i);
            String group = String.format("PO-24%02d", ((i - 1) / 20) + 1);
            String[] fio = names.getOrDefault(sid, new String[]{"Student", sid});
            String email = ("s" + String.format("%03d", i) + "@college.edu.kz").toLowerCase(Locale.ROOT);
            User student = userRepository.findById(sid)
                    .orElseGet(() -> userRepository.findByEmail(email).orElseGet(User::new));
            if (student.getId() == null || student.getId().isBlank()) {
                student.setId(sid);
            }
            student.setExternalId(sid);
            student.setRole("STUDENT");
            student.setLastName(fio[0]);
            student.setFirstName(fio[1]);
            student.setEmail(email);
            student.setPhone(buildStudentPhone(i));
            student.setGroup(group);
            student.setDepartment("Программное обеспечение");
            student.setBirthDate(buildStudentDob(i));
            student.setRegistrationDate(Optional.ofNullable(student.getRegistrationDate()).orElse(LocalDateTime.now()));
            student.setPasswordHash(encoder.encode("Stud" + sid + "!"));
            userRepository.save(student);
        }
    }

    private void seedBaseClasses() {
        for (int i = 1; i <= 10; i++) {
            String teacherId = String.format("T%03d", i);
            String groupCode = String.format("PO-24%02d", i);
            List<Class> groupClasses = classRepository.findByGroup(groupCode);
            if (!groupClasses.isEmpty()) {
                continue;
            }

            User teacher = userRepository.findById(teacherId).orElse(null);
            if (teacher == null) {
                continue;
            }

            Class cls = new Class();
            cls.setName(teacher.getSubject() != null ? teacher.getSubject() : "Профильная дисциплина");
            cls.setTeacherId(teacher.getId());
            cls.setTeacherName(teacher.getFullName().trim());
            cls.setRoom(teacher.getCabinet() != null ? teacher.getCabinet() : "401");
            cls.setDayOfWeek(DayOfWeek.of((i % 5) + 1));
            cls.setStartTime(LocalTime.of(8 + (i % 4) * 2, 30));
            cls.setEndTime(LocalTime.of(10 + (i % 4) * 2, 0));
            cls.setGroup(groupCode);
            cls.setDepartment("Программное обеспечение");
            cls.setSemester("2025-2026");
            cls.setCredits(3);
            cls.setDescription("Базовое назначение администрацией");
            classRepository.save(cls);
        }
    }

    private String buildStudentPhone(int i) {
        String n = String.format("%04d", i);
        return "+7 700 501 " + n.substring(0, 2) + " " + n.substring(2, 4);
    }

    private LocalDate buildStudentDob(int i) {
        int year = (i % 2 == 0) ? 2008 : 2007;
        int month = (i % 12) + 1;
        int day = (i % 28) + 1;
        return LocalDate.of(year, month, day);
    }

    private Map<String, String> teacher(
            String id, String lastName, String firstName, String email, String password, String phone,
            String birthDate, String subject, String cabinet, String curatorGroup
    ) {
        Map<String, String> t = new HashMap<>();
        t.put("id", id);
        t.put("lastName", lastName);
        t.put("firstName", firstName);
        t.put("email", email);
        t.put("password", password);
        t.put("phone", phone);
        t.put("birthDate", birthDate);
        t.put("subject", subject);
        t.put("cabinet", cabinet);
        t.put("curatorGroup", curatorGroup);
        return t;
    }

    private Map<String, String> admin(
            String id, String position, String lastName, String firstName, String email, String password, String phone, String birthDate
    ) {
        Map<String, String> a = new HashMap<>();
        a.put("id", id);
        a.put("position", position);
        a.put("lastName", lastName);
        a.put("firstName", firstName);
        a.put("email", email);
        a.put("password", password);
        a.put("phone", phone);
        a.put("birthDate", birthDate);
        return a;
    }

    private Map<String, String[]> parseStudentNames() {
        Map<String, String[]> result = new HashMap<>();
        String[] lines = STUDENT_NAMES_RAW.strip().split("\\R");
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length != 3) {
                continue;
            }
            result.put(parts[0].trim(), new String[]{parts[1].trim(), parts[2].trim()});
        }
        return result;
    }

    private static final String STUDENT_NAMES_RAW = """
S001|Абишева|Аружан
S002|Нургалиев|Алибек
S003|Тлеуберген|Дана
S004|Ермеков|Санжар
S005|Касымова|Томирис
S006|Сарсенбай|Диас
S007|Жусупова|Айзере
S008|Омаров|Ерасыл
S009|Муратова|Малика
S010|Бекзатулы|Адиль
S011|Бекмуратова|Алина
S012|Рахимов|Мирас
S013|Турсын|Аяна
S014|Куанышбек|Нурислам
S015|Ибраева|Назерке
S016|Смагулов|Арман
S017|Жаксылыкова|Инжу
S018|Токтар|Рамазан
S019|Сарсембаева|Амина
S020|Кенжетаев|Нурали
S021|Омарова|Айлин
S022|Елубаев|Бекзат
S023|Нуржан|Асель
S024|Абдрахманов|Ильяс
S025|Каримова|Меруерт
S026|Султанов|Ислам
S027|Айтжанова|Дильназ
S028|Кабдолов|Алихан
S029|Талгатова|Жанель
S030|Баймурат|Еркебулан
S031|Ногайбаева|Сабина
S032|Рустемов|Дамир
S033|Байтасова|Амина
S034|Тлеген|Мади
S035|Серикова|Айгерим
S036|Мухтаров|Нурсултан
S037|Амангелдиева|Мадина
S038|Калибек|Алишер
S039|Бекенова|Нурайым
S040|Сапарбай|Расул
S041|Касенова|Аяулым
S042|Турсынбек|Диан
S043|Сариева|Жансая
S044|Оралов|Азамат
S045|Мусина|Томирис
S046|Досанов|Бекарыс
S047|Алиева|Аружан
S048|Есенгали|Нурадил
S049|Бекбаева|Алина
S050|Кабылов|Ернар
S051|Нуртаева|Мадина
S052|Оразбек|Арсен
S053|Серикбаева|Назым
S054|Рымбеков|Исламбек
S055|Аскарова|Даяна
S056|Ержанов|Дамир
S057|Жанибекова|Айкерим
S058|Сагындык|Мирас
S059|Тажиева|Самина
S060|Бекетов|Нурсаяд
S061|Куатова|Асем
S062|Жумахан|Адилет
S063|Тулегенова|Аяжан
S064|Исенов|Санат
S065|Шарипова|Медина
S066|Байкенов|Ерсултан
S067|Ниязова|Аиша
S068|Куанышев|Диас
S069|Саинова|Жанель
S070|Ермекбай|Алинур
S071|Базарбаева|Ару
S072|Сейитов|Мансур
S073|Утегенова|Нурай
S074|Турсунов|Бекнур
S075|Кенжебаева|Инкар
S076|Жолдасов|Артур
S077|Амирханова|Дильназ
S078|Оспанов|Нуртас
S079|Абилова|Лейла
S080|Дауренов|Рамис
S081|Сарсенова|Аяна
S082|Молдабек|Ержан
S083|Тажибаева|Аружан
S084|Кабенов|Нурасыл
S085|Иманова|Дина
S086|Умирзаков|Бекарыс
S087|Нурланова|Айару
S088|Жанбыршин|Дастан
S089|Серикова|Гаухар
S090|Сулейменов|Азиз
S091|Турсынова|Мадина
S092|Омарбек|Бекзат
S093|Бекетова|Алинур
S094|Есенали|Алишер
S095|Кайратова|Нурай
S096|Сапаров|Дамир
S097|Ногаева|Амина
S098|Байдаулет|Мирас
S099|Аханова|Даяна
S100|Рахатов|Нурбол
S101|Усенова|Ажар
S102|Мукашев|Ерсин
S103|Токсанбаева|Жанерке
S104|Адилов|Султан
S105|Сейтова|Аружан
S106|Бахытжан|Ильяс
S107|Ниязбекова|Томирис
S108|Кенесов|Диас
S109|Абдуллина|Аделя
S110|Нурымов|Алишер
S111|Кабдрахманова|Самина
S112|Жумагул|Еркежан
S113|Исаева|Дильназ
S114|Сариев|Нурторе
S115|Бектасова|Амина
S116|Даулетов|Арман
S117|Тлеуова|Айганым
S118|Ергалиев|Нурислам
S119|Мырзабекова|Лаура
S120|Калиев|Рустам
S121|Токенова|Айым
S122|Ахметов|Нуржан
S123|Садвакасова|Айару
S124|Жолдыбаев|Мади
S125|Куанышбаева|Асемай
S126|Нурбеков|Ернар
S127|Кабылова|Инкар
S128|Сапаров|Азамат
S129|Оразаева|Жания
S130|Иманкул|Бекзат
S131|Досжанова|Алина
S132|Баймагамбетов|Санжар
S133|Смайлова|Назерке
S134|Таженов|Нурасыл
S135|Ермекова|Гульназ
S136|Кенжебек|Арсен
S137|Абенова|Дана
S138|Султанбек|Нуртас
S139|Бекова|Ляззат
S140|Маратов|Рауан
S141|Жумабаева|Айнура
S142|Исмагулов|Данияр
S143|Абдикаримова|Жанар
S144|Муканов|Адильхан
S145|Аманова|Айлин
S146|Базаров|Ербол
S147|Сарсенбаева|Аружан
S148|Кудайберген|Мирас
S149|Шаймерденова|Карина
S150|Тынышбаев|Алан
S151|Алипова|Самал
S152|Досымов|Рамазан
S153|Утегенова|Дильмира
S154|Есимханов|Нурболат
S155|Кайсарова|Амира
S156|Маханов|Бекнур
S157|Ергалиева|Нурайым
S158|Сабитов|Ислам
S159|Аханова|Мадина
S160|Оспанбек|Нурислам
S161|Кенжетаева|Асия
S162|Бекназаров|Адилет
S163|Смагулова|Аружан
S164|Нурсеитов|Дамир
S165|Абдулина|Адель
S166|Мырзахмет|Ернар
S167|Байтурсынова|Назгуль
S168|Касенов|Бекарыс
S169|Айдарова|Айша
S170|Ерланов|Нурасыл
S171|Кабенова|Динара
S172|Сарбасов|Ильяс
S173|Талгатова|Амира
S174|Орынбасар|Азамат
S175|Куанышбаева|Лейла
S176|Жанабаев|Арсен
S177|Уразова|Асем
S178|Токтасын|Рауан
S179|Есенбаева|Инжу
S180|Мусрепов|Нурлан
S181|Алимжанова|Аружан
S182|Сейтказин|Нурислам
S183|Касымбекова|Томирис
S184|Бекмурза|Диас
S185|Оразбаева|Малика
S186|Сапаргалиев|Арман
S187|Нургалиева|Аяла
S188|Жумагазы|Бекзат
S189|Айманова|Гульназ
S190|Турсынбеков|Дамир
S191|Умирбекова|Сабина
S192|Кабылдин|Нурасыл
S193|Байжанова|Амина
S194|Ермекулы|Санжар
S195|Тлеуова|Назерке
S196|Нурсапа|Алишер
S197|Досмухамбетова|Дина
S198|Садыков|Еркебулан
S199|Искакова|Асем
S200|Маратбек|Нурболат
""";
}
