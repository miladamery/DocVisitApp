package ir.milad.DocVisitApp.domain.visit_session.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DoctorGivingAppointmentServiceTest {

    @Test
    @DisplayName("""
            When doctor wants to give appointment
            And entry time is before or equal to session start
            Then appointment visit time should start from visit session from time 
            and continues according to session length           
            """)
    public void test1() {

    }

    @Test
    @DisplayName("""
            When doctor wants to give appointment
            And entry time is after session start
            Then appointment visit time should start from LocalTime.now
            and continues according to session length          
            """)
    public void test2() {

    }
}