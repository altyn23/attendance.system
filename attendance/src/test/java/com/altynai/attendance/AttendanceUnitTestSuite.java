package com.altynai.attendance;

import com.altynai.attendance.admin.AdminOperationsControllerTest;
import com.altynai.attendance.ai.AiInsightsServiceTest;
import com.altynai.attendance.auth.AuthControllerTest;
import com.altynai.attendance.controller.AttendanceControllerTest;
import com.altynai.attendance.controller.ClassControllerTest;
import com.altynai.attendance.home.HomeControllerTest;
import com.altynai.attendance.service.NotificationServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthControllerTest.class,
        ClassControllerTest.class,
        AttendanceControllerTest.class,
        HomeControllerTest.class,
        AdminOperationsControllerTest.class,
        AiInsightsServiceTest.class,
        NotificationServiceTest.class
})
public class AttendanceUnitTestSuite {
}
