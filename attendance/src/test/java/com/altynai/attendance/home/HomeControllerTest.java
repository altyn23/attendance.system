package com.altynai.attendance.home;

import com.altynai.attendance.news.News;
import com.altynai.attendance.news.NewsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HomeControllerTest {

    private HomeController controller;
    private NewsRepository newsRepository;

    @BeforeEach
    void setUp() {
        newsRepository = mock(NewsRepository.class);
        controller = new HomeController(newsRepository);
    }

    @Test
    void dashboard_redirectsToLogin_whenSessionIsMissing() {
        String view = controller.dashboard(new MockHttpSession(), new ConcurrentModel());
        assertEquals("redirect:/login", view);
    }

    @Test
    void dashboard_returnsDashboardAndModel_whenSessionExists() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "A001");
        session.setAttribute("email", "admin@college.edu.kz");
        session.setAttribute("fullName", "Admin User");
        session.setAttribute("role", "ADMIN");
        when(newsRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(new News()));

        Model model = new ConcurrentModel();
        String view = controller.dashboard(session, model);

        assertEquals("dashboard", view);
        assertEquals("A001", model.getAttribute("userId"));
        assertEquals("admin@college.edu.kz", model.getAttribute("email"));
        assertEquals("Admin User", model.getAttribute("fullName"));
        assertEquals("ADMIN", model.getAttribute("role"));
    }

    @Test
    void platformGuide_redirectsToLogin_whenSessionIsMissing() {
        String view = controller.platformGuide(new MockHttpSession(), new ConcurrentModel());
        assertEquals("redirect:/login", view);
    }

    @Test
    void platformGuide_returnsTemplate_whenSessionExists() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", "T001");
        session.setAttribute("fullName", "Teacher User");
        session.setAttribute("role", "TEACHER");

        Model model = new ConcurrentModel();
        String view = controller.platformGuide(session, model);

        assertEquals("platform-guide", view);
        assertEquals("Teacher User", model.getAttribute("fullName"));
        assertEquals("TEACHER", model.getAttribute("role"));
    }
}
