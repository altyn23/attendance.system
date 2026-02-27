(function () {
  const PREF_THEME = 'theme';
  const PREF_LANG = 'lang';

  const pairs = [
    ['Басты бет', 'Главная'],
    ['Шығу', 'Выход'],
    ['Профиль', 'Профиль'],
    ['Жаңалықтар', 'Новости'],
    ['Университет жаңалықтары', 'Новости университета'],
    ['Жаңалықтар жоқ', 'Новостей нет'],
    ['Әзірге жаңалықтар жарияланбаған', 'Пока новости не опубликованы'],
    ['Жаңалық қосу', 'Добавить новость'],
    ['Жаңа жаңалық қосу', 'Добавить новую новость'],
    ['Тақырып *', 'Заголовок *'],
    ['Мазмұны *', 'Содержание *'],
    ['Жаңалық мәтінін енгізіңіз...', 'Введите текст новости...'],
    ['мысалы: Деканат', 'например: Деканат'],
    ['Жариялау', 'Опубликовать'],
    ['Болдырмау', 'Отмена'],
    ['Жою', 'Удалить'],
    ['Әкімшілік', 'Администрация'],
    ['Менің профилім', 'Мой профиль'],
    ['Сабақ кестесі', 'Расписание занятий'],
    ['Апталық кесте', 'Недельное расписание'],
    ['Оқу күндерінің жоспары', 'План учебных дней'],
    ['Апталық', 'Недельный'],
    ['Тізім', 'Список'],
    ['Сабақ туралы', 'О занятии'],
    ['Уақыты', 'Время'],
    ['Аудитория', 'Аудитория'],
    ['Сипаттама', 'Описание'],
    ['Кредит', 'Кредит'],
    ['Кредиттер', 'Кредиты'],
    ['Оқытушы:', 'Преподаватель:'],
    ['Топ:', 'Группа:'],
    ['Аудитория:', 'Аудитория:'],
    ['Күн:', 'День:'],
    ['Уақыт:', 'Время:'],
    ['Сабақ атауы', 'Название занятия'],
    ['Басталу уақыты', 'Время начала'],
    ['Аяқталу уақыты', 'Время окончания'],
    ['Қатысу', 'Посещаемость'],
    ['Қатысу есебі', 'Отчет посещаемости'],
    ['Қатысу статистикасы', 'Статистика посещаемости'],
    ['Топтар бойынша тіркелген студенттер', 'Зарегистрированные студенты по группам'],
    ['Платформада тіркелген студенттер тізімі топтарға бөлініп көрсетіледі.', 'Список зарегистрированных студентов разделен по группам.'],
    ['Студент қосу', 'Добавить студента'],
    ['Жаңа студент қосу', 'Добавить нового студента'],
    ['Студентті іздеу: аты, тегі, email', 'Поиск студента: имя, фамилия, email'],
    ['Сұранысқа сай студент табылмады.', 'По запросу студенты не найдены.'],
    ['Аты', 'Имя'],
    ['Тегі', 'Фамилия'],
    ['Формат: Аты,Тегі,Email,Топ', 'Формат: Имя,Фамилия,Email,Группа'],
    ['Импорттау', 'Импортировать'],
    ['QR Сессиялар', 'QR Сессии'],
    ['QR сессия', 'QR сессия'],
    ['QR сессиялар', 'QR сессии'],
    ['Жаңа QR жасау', 'Создать новый QR'],
    ['Сабақты таңдап, QR сессияны бастаңыз.', 'Выберите занятие и запустите QR сессию.'],
    ['Сабақ', 'Занятие'],
    ['Сабақтар', 'Занятия'],
    ['Әлі QR сессия жоқ', 'Пока нет QR сессии'],
    ['Белсенді QR', 'Активный QR'],
    ['Белсенді сессиялар тізімі', 'Список активных сессий'],
    ['Push қосу', 'Включить Push'],
    ['Пәндер мен топтарды басқару', 'Управление предметами и группами'],
    ['Сабақтарды құрып, өңдеп, әр топ бойынша қатысу статистикасын бақылаңыз.', 'Создавайте и редактируйте занятия, отслеживайте посещаемость по группам.'],
    ['Жаңа сабақ', 'Новое занятие'],
    ['Жаңа сабақ қосу', 'Добавить новое занятие'],
    ['Әзірге сабақтар тізімі бос.', 'Список занятий пока пуст.'],
    ['Топ жоқ', 'Без группы'],
    ['Ескерту', 'Оповещение'],
    ['Кешігу', 'Опоздание'],
    ['Қатыспау', 'Отсутствие'],
    ['Барлық студент', 'Всего студентов'],
    ['Топ саны', 'Количество групп'],
    ['Топсыз студент', 'Студенты без группы'],
    ['Топсыз', 'Без группы'],
    ['Дүйсенбі', 'Понедельник'],
    ['Сейсенбі', 'Вторник'],
    ['Сәрсенбі', 'Среда'],
    ['Бейсенбі', 'Четверг'],
    ['Жұма', 'Пятница'],
    ['Сенбі', 'Суббота'],
    ['Қараша', 'Ноябрь'],
    ['Пайдаланушылар', 'Пользователи'],
    ['Жүйе есептері', 'Системные отчеты'],
    ['Баптаулар', 'Настройки'],
    ['Резерв', 'Резерв'],
    ['Қатысу есебі', 'Отчет посещаемости'],
    ['Менің сабақтарым', 'Мои занятия'],
    ['Студенттер', 'Студенты'],
    ['Сабақтар', 'Занятия'],
    ['Сабақ кестесі', 'Расписание'],
    ['QR жасау', 'Создать QR'],
    ['QR сканерлеу', 'Сканировать QR'],
    ['Қатысу статистикасы', 'Статистика посещаемости'],
    ['Басты', 'Главная'],
    ['Жеке мәліметтер', 'Личные данные'],
    ['Аккаунт мәліметтері', 'Данные аккаунта'],
    ['Статистика', 'Статистика'],
    ['Өзгерту', 'Изменить'],
    ['Сақтау', 'Сохранить'],
    ['Бас тарту', 'Отмена'],
    ['Өшіру', 'Удалить'],
    ['Аватар жүктеу', 'Загрузить аватар'],
    ['Кешігу/Қатыспау ескертулері', 'Оповещения о пропусках/опозданиях'],
    ['Ескерту жоқ', 'Оповещений нет'],
    ['Жүйе баптаулары', 'Системные настройки'],
    ['Тек әкімшіге қолжетімді параметрлер.', 'Параметры доступны только администратору.'],
    ['Резервтік көшірме', 'Резервная копия'],
    ['Қолмен резерв жасау үшін батырманы басыңыз.', 'Нажмите кнопку для ручного создания резервной копии.'],
    ['Кіру', 'Вход'],
    ['Жүйеге кіру', 'Вход в систему'],
    ['Құпиясөз', 'Пароль'],
    ['Көру', 'Показать'],
    ['Жасыру', 'Скрыть'],
    ['Тек тіркелген қолданушылар үшін', 'Только для зарегистрированных пользователей'],
    ['Email мен құпиясөзді енгізіңіз', 'Введите email и пароль'],
    ['Кіру қатесі', 'Ошибка входа'],
    ['Кіру сәтті', 'Вход выполнен'],
    ['Серверге қосылу мүмкін емес', 'Не удалось подключиться к серверу'],
    ['Күте тұрыңыз...', 'Подождите...'],
    ['Топ', 'Группа'],
    ['Оқытушы', 'Преподаватель'],
    ['Студент', 'Студент'],
    ['Админ', 'Админ'],
    ['Жалпы', 'Общее'],
    ['Соңғы жаңалықтар', 'Последние новости'],
    ['Таңдаңыз', 'Выберите'],
    ['Ұзақтығы', 'Длительность'],
    ['Секунд', 'Секунды'],
    ['Минут', 'Минуты'],
    ['Тіркелген күні', 'Дата регистрации'],
    ['Соңғы кіру', 'Последний вход'],
    ['Телефон', 'Телефон'],
    ['Кафедра/Бөлім', 'Кафедра/Отдел'],
    ['Рөлі', 'Роль'],
    ['Тема', 'Тема'],
    ['Light', 'Светлая'],
    ['Dark', 'Темная'],
    ['Барлығы', 'Все']
    ,['Хабарландырулар', 'Уведомления']
    ,['Хабарландыру жоқ', 'Уведомлений нет']
    ,['Платформа ережелері', 'Правила платформы']
    ,['Платформа ережелері және қолдану тәртібі', 'Правила платформы и порядок использования']
    ,['Платформа не үшін керек', 'Для чего нужна платформа']
    ,['Бұл жүйе сабаққа қатысуды ашық тіркеу, кестені басқару, өзгерістер туралы жедел хабарлау және әкімшілік шешімдерді жылдам қабылдау үшін қолданылады.', 'Эта система используется для прозрачной фиксации посещаемости, управления расписанием, быстрых уведомлений об изменениях и оперативных решений администрации.']
    ,['Негізгі рөлдер', 'Основные роли']
    ,['Студент: қатысу белгілеу, себеп жіберу, хабарландыру алу.', 'Студент: отмечает посещаемость, отправляет причину, получает уведомления.']
    ,['Оқытушы: тек өзіне тағайындалған сабақтармен жұмыс істеу, QR сессия ашу, әкімшілікке онлайн сұраныс жіберу.', 'Преподаватель: работает только со своими назначенными занятиями, открывает QR-сессии, отправляет онлайн-запросы администрации.']
    ,['Әкімші: сабақ/аудитория/уақыт тағайындау, өзгерістер енгізу, себептерді қарау және бекіту.', 'Администратор: назначает занятия/аудитории/время, вносит изменения, рассматривает и утверждает причины пропусков.']
    ,['Платформаны пайдалану ережелері', 'Правила использования платформы']
    ,['Әр қолданушы тек өз аккаунтымен кіруі керек.', 'Каждый пользователь должен входить только в свой аккаунт.']
    ,['Логин мен құпиясөзді үшінші тұлғаға беруге болмайды.', 'Нельзя передавать логин и пароль третьим лицам.']
    ,['Кестедегі өзгеріс тек себебімен сақталады және тарихта қалады.', 'Изменения в расписании сохраняются только с причиной и остаются в истории.']
    ,['Пропуск/кешігу бойынша жауап шынайы ақпаратпен толтырылуы керек.', 'Ответ по пропуску/опозданию должен содержать достоверную информацию.']
    ,['Жалған ақпарат берілген жағдайда әкімшілік тексеру жүргізе алады.', 'При ложной информации администрация может провести проверку.']
    ,['Техника және қолдану тәртібі', 'Технические правила и порядок использования']
    ,['Тіл: KZ/RU, тақырып: Light/Dark батырмаларымен ауысады.', 'Язык: KZ/RU, тема: Light/Dark переключается кнопками.']
    ,['QR қатысу белгілеу кезінде камераға рұқсат беру қажет.', 'При отметке посещаемости через QR необходимо разрешение камеры.']
    ,['Ақау болғанда бетті жаңартып, қайта кіру ұсынылады.', 'При сбоях рекомендуется обновить страницу и войти заново.']
    ,['Деректердің өзектілігі үшін кэшті сирек тазалап тұрыңыз.', 'Для актуальности данных периодически очищайте кэш.']
    ,['Маңызды өзгерістер «Хабарландырулар» бетінде көрсетіледі.', 'Важные изменения отображаются на странице «Уведомления».']
    ,['Онлайн өтінімдер қалай жұмыс істейді', 'Как работают онлайн-заявки']
    ,['Оқытушы сабақ ауыстыру/өзгертуге сұраныс жібереді. Әкімшілік шешім қабылдайды, содан кейін қатысы бар топтар мен оқытушыларға хабарлама автоматты түрде түседі.', 'Преподаватель отправляет запрос на перенос/изменение занятия. Администрация принимает решение, после чего связанным группам и преподавателям автоматически приходит уведомление.']
    ,['Біздің платформа сабаққа қатысуды, кестені және хабарландыруларды бір жерден басқаруға арналған.', 'Наша платформа нужна для управления посещаемостью, расписанием и уведомлениями в одном месте.']
    ,['Толығырақ', 'Подробнее']
    ,['Оқылмаған', 'Непрочитанные']
    ,['Оқыдым', 'Прочитано']
    ,['AI аналитика', 'AI аналитика']
    ,['Ескерту жоқ', 'Оповещений нет']
    ,['Дерек жоқ', 'Нет данных']
    ,['Топтар табылмады', 'Кластеры не найдены']
    ,['Қорытынды әлі жоқ', 'Сводка пока недоступна']
    ,['Элементтер жоқ', 'Элементов нет']
    ,['Аномалия жоқ', 'Аномалий нет']
    ,['AI Insights', 'AI аналитика']
    ,['Жаңарту', 'Обновить']
    ,['Сұрау', 'Запрос']
    ,['Сұрағыңызды жазыңыз...', 'Введите ваш вопрос...']
    ,['Кім қатты кешігіп жатыр немесе келмегенін көрсетеді.', 'Показывает, кто сильно опаздывает или не пришел.']
    ,['Студенттің тәуекел балын және деңгейін есептейді.', 'Считает риск-балл и уровень студента.']
    ,['Қатысу проблемасының топтарын шығарады.', 'Показывает кластеры проблем посещаемости.']
    ,['Апталық AI-сводка, өзгеріс және тәуекелдер.', 'Недельная AI-сводка, изменения и риски.']
    ,['Студентке дайын хабарлама (RU/KZ, tone).', 'Готовое сообщение студенту (RU/KZ, тон).']
    ,['QR сессиядағы күмәнді паттерндерді табады.', 'Находит подозрительные паттерны в QR сессиях.']
    ,['No alerts', 'Нет оповещений']
    ,['No data', 'Нет данных']
    ,['No clusters', 'Нет кластеров']
    ,['No summary available', 'Сводка недоступна']
    ,['No items', 'Нет элементов']
    ,['No anomalies', 'Нет аномалий']
  ];

  const kzToRu = Object.fromEntries(pairs);
  const ruToKz = Object.fromEntries(pairs.map(([kz, ru]) => [ru, kz]));

  function normalizeText(s) {
    return String(s || '').replace(/\s+/g, ' ').trim();
  }

  function translateValue(value, lang) {
    const normalized = normalizeText(value);
    if (!normalized) return value;
    if (lang === 'ru' && kzToRu[normalized]) return kzToRu[normalized];
    if (lang === 'kz' && ruToKz[normalized]) return ruToKz[normalized];
    return value;
  }

  function replacePhrases(value, lang) {
    let result = String(value || '');
    const dict = lang === 'ru' ? kzToRu : ruToKz;
    const keys = Object.keys(dict).sort((a, b) => b.length - a.length);
    keys.forEach((key) => {
      if (!key || !result.includes(key)) return;
      result = result.split(key).join(dict[key]);
    });
    return result;
  }

  function translateNodeTree(root, lang) {
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
      acceptNode(node) {
        if (!node.nodeValue || !node.nodeValue.trim()) return NodeFilter.FILTER_REJECT;
        const parent = node.parentElement;
        if (!parent) return NodeFilter.FILTER_REJECT;
        const tag = parent.tagName;
        if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'CODE') return NodeFilter.FILTER_REJECT;
        return NodeFilter.FILTER_ACCEPT;
      }
    });

    const textNodes = [];
    while (walker.nextNode()) textNodes.push(walker.currentNode);

    textNodes.forEach((node) => {
      const original = node.nodeValue;
      let translated = translateValue(original, lang);
      if (translated === original) translated = replacePhrases(original, lang);
      if (translated !== original) node.nodeValue = translated;
    });

    document.querySelectorAll('input[placeholder], textarea[placeholder]').forEach((el) => {
      const placeholder = el.getAttribute('placeholder');
      let translated = translateValue(placeholder, lang);
      if (translated === placeholder) translated = replacePhrases(placeholder, lang);
      if (translated) el.setAttribute('placeholder', translated);
    });

    document.querySelectorAll('input[type="button"][value], input[type="submit"][value]').forEach((el) => {
      const value = el.getAttribute('value');
      let translated = translateValue(value, lang);
      if (translated === value) translated = replacePhrases(value, lang);
      if (translated) el.setAttribute('value', translated);
    });

    document.querySelectorAll('[title]').forEach((el) => {
      const title = el.getAttribute('title');
      let translated = translateValue(title, lang);
      if (translated === title) translated = replacePhrases(title, lang);
      if (translated) el.setAttribute('title', translated);
    });

    if (document.title) {
      let translatedTitle = translateValue(document.title, lang);
      if (translatedTitle === document.title) translatedTitle = replacePhrases(document.title, lang);
      if (translatedTitle) document.title = translatedTitle;
    }
  }

  function applyTheme() {
    const theme = localStorage.getItem(PREF_THEME) || 'light';
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.classList.toggle('theme-dark', theme === 'dark');

    const globalBtn = document.getElementById('globalThemeBtn');
    if (globalBtn) {
      globalBtn.textContent = theme === 'dark' ? 'Dark' : 'Light';
      globalBtn.classList.toggle('active', theme === 'dark');
    }

    const localBtn = document.getElementById('themeBtn');
    if (localBtn) {
      localBtn.textContent = theme === 'dark' ? 'Dark' : 'Light';
      localBtn.classList.toggle('active', theme === 'dark');
    }
  }

  function toggleTheme() {
    const current = localStorage.getItem(PREF_THEME) || 'light';
    localStorage.setItem(PREF_THEME, current === 'dark' ? 'light' : 'dark');
    applyTheme();
  }

  function applyLang() {
    const lang = localStorage.getItem(PREF_LANG) || 'kz';
    document.documentElement.setAttribute('data-lang', lang);
    translateNodeTree(document.body, lang);

    const kzBtn = document.getElementById('globalLangKzBtn');
    const ruBtn = document.getElementById('globalLangRuBtn');
    if (kzBtn && ruBtn) {
      kzBtn.classList.toggle('active', lang === 'kz');
      ruBtn.classList.toggle('active', lang === 'ru');
    }

    const localKzBtn = document.getElementById('langKzBtn');
    const localRuBtn = document.getElementById('langRuBtn');
    if (localKzBtn && localRuBtn) {
      localKzBtn.classList.toggle('active', lang === 'kz');
      localRuBtn.classList.toggle('active', lang === 'ru');
    }
  }

  function setLang(lang) {
    localStorage.setItem(PREF_LANG, lang);
    applyLang();
  }

  function injectGlobalStyles() {
    if (document.getElementById('global-pref-style')) return;
    const style = document.createElement('style');
    style.id = 'global-pref-style';
    style.textContent = `
      [data-theme="dark"] body { background: linear-gradient(120deg,#050910 0%,#0f172a 60%,#111827 100%) !important; color:#e5e7eb !important; }
      [data-theme="light"] body { background: linear-gradient(120deg,#f0f9ff 0%,#e0f2fe 60%,#dbeafe 100%) !important; color:#1e293b !important; }
      [data-theme="dark"] .card, [data-theme="dark"] .panel, [data-theme="dark"] .welcome, [data-theme="dark"] .news, [data-theme="dark"] .stat-card, [data-theme="dark"] .modal-content, [data-theme="dark"] .modal-body, [data-theme="dark"] .table-wrap, [data-theme="dark"] .users-table, [data-theme="dark"] .report-table, [data-theme="dark"] .chart-card, [data-theme="dark"] .page-header, [data-theme="dark"] .empty-state, [data-theme="dark"] .profile-menu, [data-theme="dark"] .table-container, [data-theme="dark"] .content-card, [data-theme="dark"] .widget, [data-theme="dark"] .glass { background:#111827 !important; color:#e5e7eb !important; border-color:#263244 !important; }
      [data-theme="dark"] input, [data-theme="dark"] select, [data-theme="dark"] textarea { background:#0f172a !important; color:#e5e7eb !important; border-color:#334155 !important; }
      [data-theme="light"] .card, [data-theme="light"] .panel, [data-theme="light"] .welcome, [data-theme="light"] .news, [data-theme="light"] .stat-card, [data-theme="light"] .modal-content, [data-theme="light"] .modal-body, [data-theme="light"] .table-wrap, [data-theme="light"] .users-table, [data-theme="light"] .report-table, [data-theme="light"] .chart-card, [data-theme="light"] .page-header, [data-theme="light"] .empty-state, [data-theme="light"] .profile-menu, [data-theme="light"] .table-container, [data-theme="light"] .content-card, [data-theme="light"] .widget, [data-theme="light"] .glass { background:#ffffff !important; color:#1e293b !important; }
      [data-theme="dark"] .card h1, [data-theme="dark"] .card h2, [data-theme="dark"] .card h3, [data-theme="dark"] .card h4, [data-theme="dark"] .card h5, [data-theme="dark"] .card h6,
      [data-theme="dark"] .panel h1, [data-theme="dark"] .panel h2, [data-theme="dark"] .panel h3, [data-theme="dark"] .panel h4, [data-theme="dark"] .panel h5, [data-theme="dark"] .panel h6,
      [data-theme="dark"] .modal-content h1, [data-theme="dark"] .modal-content h2, [data-theme="dark"] .modal-content h3, [data-theme="dark"] .modal-content h4,
      [data-theme="dark"] .page-header h1, [data-theme="dark"] .page-header h2, [data-theme="dark"] .page-header h3, [data-theme="dark"] .empty-state h1, [data-theme="dark"] .empty-state h2, [data-theme="dark"] .empty-state h3,
      [data-theme="dark"] .card p, [data-theme="dark"] .panel p, [data-theme="dark"] .modal-content p,
      [data-theme="dark"] .card label, [data-theme="dark"] .panel label, [data-theme="dark"] .modal-content label,
      [data-theme="dark"] .card td, [data-theme="dark"] .panel td, [data-theme="dark"] .card th, [data-theme="dark"] .panel th,
      [data-theme="dark"] .card .title, [data-theme="dark"] .panel .title, [data-theme="dark"] .empty-title, [data-theme="dark"] .news-title, [data-theme="dark"] .modal-title { color:#e5e7eb !important; }
      [data-theme="light"] .card h1, [data-theme="light"] .card h2, [data-theme="light"] .card h3, [data-theme="light"] .card h4, [data-theme="light"] .card h5, [data-theme="light"] .card h6,
      [data-theme="light"] .panel h1, [data-theme="light"] .panel h2, [data-theme="light"] .panel h3, [data-theme="light"] .panel h4, [data-theme="light"] .panel h5, [data-theme="light"] .panel h6,
      [data-theme="light"] .modal-content h1, [data-theme="light"] .modal-content h2, [data-theme="light"] .modal-content h3, [data-theme="light"] .modal-content h4,
      [data-theme="light"] .page-header h1, [data-theme="light"] .page-header h2, [data-theme="light"] .page-header h3, [data-theme="light"] .empty-state h1, [data-theme="light"] .empty-state h2, [data-theme="light"] .empty-state h3,
      [data-theme="light"] .card p, [data-theme="light"] .panel p, [data-theme="light"] .modal-content p,
      [data-theme="light"] .card label, [data-theme="light"] .panel label, [data-theme="light"] .modal-content label,
      [data-theme="light"] .card td, [data-theme="light"] .panel td, [data-theme="light"] .card th, [data-theme="light"] .panel th,
      [data-theme="light"] .card .title, [data-theme="light"] .panel .title, [data-theme="light"] .empty-title, [data-theme="light"] .news-title, [data-theme="light"] .modal-title { color:#1f2937 !important; }
      [data-theme="dark"] .muted, [data-theme="dark"] .subtitle, [data-theme="dark"] .meta, [data-theme="dark"] .stat-label, [data-theme="dark"] .empty-text, [data-theme="dark"] .news-date, [data-theme="dark"] .news-author { color:#94a3b8 !important; opacity:1 !important; }
      [data-theme="light"] .muted, [data-theme="light"] .subtitle, [data-theme="light"] .meta, [data-theme="light"] .stat-label, [data-theme="light"] .empty-text, [data-theme="light"] .news-date, [data-theme="light"] .news-author { color:#64748b !important; opacity:1 !important; }
      .global-pref-switch { position: fixed; right: 14px; bottom: 14px; z-index: 9999; display:flex; gap:8px; flex-wrap:wrap; background: rgba(15,23,42,.78); border:1px solid rgba(255,255,255,.2); border-radius: 12px; padding: 8px; }
      [data-theme="light"] .global-pref-switch { background: rgba(255,255,255,.92); border-color: #cbd5e1; }
      .global-pref-switch button { border:none; border-radius:8px; padding:6px 10px; font-size:12px; cursor:pointer; font-weight:700; background:#334155; color:#f8fafc; }
      [data-theme="light"] .global-pref-switch button { background:#1e40af; color:#fff; }
      .global-pref-switch button.active { outline:2px solid #22d3ee; }
    `;
    document.head.appendChild(style);
  }

  function injectGlobalSwitcher() {
    const hasLocalControls = document.getElementById('themeBtn') || document.getElementById('langKzBtn') || document.getElementById('langRuBtn');
    if (hasLocalControls || document.querySelector('.global-pref-switch')) return;

    const wrap = document.createElement('div');
    wrap.className = 'global-pref-switch';
    wrap.innerHTML = `
      <button id="globalThemeBtn" type="button">Dark</button>
      <button id="globalLangKzBtn" type="button">KZ</button>
      <button id="globalLangRuBtn" type="button">RU</button>
    `;

    document.body.appendChild(wrap);

    document.getElementById('globalThemeBtn').addEventListener('click', toggleTheme);
    document.getElementById('globalLangKzBtn').addEventListener('click', function () { setLang('kz'); });
    document.getElementById('globalLangRuBtn').addEventListener('click', function () { setLang('ru'); });
  }

  function init() {
    injectGlobalStyles();
    injectGlobalSwitcher();
    applyTheme();
    applyLang();

    const observer = new MutationObserver(function () {
      const lang = localStorage.getItem(PREF_LANG) || 'kz';
      translateNodeTree(document.body, lang);
    });
    observer.observe(document.body, { childList: true, subtree: true });
  }

  window.toggleTheme = toggleTheme;
  window.setLang = setLang;
  window.applyTheme = applyTheme;
  window.applyLang = applyLang;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
