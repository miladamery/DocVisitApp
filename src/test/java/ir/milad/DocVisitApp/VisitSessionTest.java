package ir.milad.DocVisitApp;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisitSessionTest {

    LocalTime nineOClock = LocalTime.of(9, 0, 0);
    LocalTime nine1_OClock = LocalTime.of(9,1,0);

    @DisplayName("""
            Given non-repetitive patient
            Then should give a new Turn
            """)
    @Test
    public void test1() {
        var vs = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(10, 0, 0),
                8
        );

        vs.giveAppointment(new Patient("09300000000", "Milad", "Amery", "2023-08-05"), nineOClock, 1);
        vs.giveAppointment(new Patient("09300000000", "Milad", "Amery", "2023-08-06"), nine1_OClock, 1);
        assertEquals(vs.getAppointments().size(), 2);
    }

    @DisplayName("""
            Given repetitive patient
            Then should return previous Turn 
            """)
    @Test
    public void test2() {
        var vs = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(10, 0, 0),
                8
        );

        var p1 = new Patient("09300000000", "Milad", "Amery", "2023-08-05");
        var p2 = new Patient("09300000000", "Milad", "Amery", "2023-08-05");
        var t1 = vs.giveAppointment(p1, nine1_OClock, 1);
        var t2 = vs.giveAppointment(p2, nine1_OClock, 1);

        var t3 = vs.giveAppointment(p1, LocalTime.of(9,2, 0), 1);
        var t4 = vs.giveAppointment(p2, LocalTime.of(9,4, 0), 1);
        assertEquals(t1, t3);
        assertEquals(t2, t4);
    }

    @DisplayName("""
           Given invalid fromTime and toTime
           Should throw ApplicationException
            """)
    @Test
    public void test3() {
        assertThrows(ApplicationException.class, () -> {
            new VisitSession(
                    LocalDate.now(),
                    LocalTime.of(11, 0, 0),
                    LocalTime.of(10, 0, 0),
                    8
            );
        });
    }

    @DisplayName("""
            Given full VisitSession
            When giveTurn is called
            Then should throw ApplicationException
            """)
    @Test
    public void test4() {
        var vs = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(10, 0, 0),
                60
        );
        var p1 = new Patient("09300000000", "Milad", "Amery", "2023-08-05");
        var p2 = new Patient("09300000000", "Milad", "Amery", "2023-08-05");
        vs.giveAppointment(p1, LocalTime.of(9, 5, 0), 1);
        assertThrows(ApplicationException.class
                , () -> vs.giveAppointment(p2, LocalTime.of(9, 5, 0).plusMinutes(5), 1)
        );
    }

    @DisplayName("""
            Given VisitSession with some patients in
            When someone cancells check in
            Then other patients appointment should get updated
            """)
    @Test
    public void test5() {
        var vs = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(12, 0, 0),
                8
        );

        var patients = Arrays.asList(
                new Patient("0", "A", "B", "2023-08-05"),
                new Patient("1", "C", "D", "2023-08-05"),
                new Patient("2", "E", "F", "2023-08-05"),
                new Patient("3", "G", "H", "2023-08-05"),
                new Patient("4", "I", "J", "2023-08-05"),
                new Patient("5", "K", "L", "2023-08-05")
        );

        var time = LocalTime.of(9, 0);
        var turns = new ArrayList<Appointment>();
        for (Patient patient: patients) {
            turns.add(vs.giveAppointment(patient, time, 1));
            time = time.plusMinutes(1);
        }

        vs.cancelAppointment(turns.get(3).getId());

        assertEquals(turns.get(3).getStatus(), AppointmentStatus.CANCELED);
        assertEquals(turns.get(4).getTurnNumber(), 4);
        assertEquals(turns.get(4).getVisitTime(), LocalTime.of(9, 24));
        assertEquals(turns.get(4).getTurnsToAwait(), 3);

        assertEquals(turns.get(5).getTurnNumber(), 5);
        assertEquals(turns.get(5).getVisitTime(), LocalTime.of(9, 32));
        assertEquals(turns.get(5).getTurnsToAwait(), 4);

        assertEquals(vs.getLastAppointmentTime(), LocalTime.of(9, 40));
    }

    @DisplayName("""
            Given VisitSession with some patients in
            When appointment is done
            Then other patients appointment should get updated
            """)
    @Test
    public void test6() {
        var vs = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(12, 0, 0),
                8
        );

        var patients = Arrays.asList(
                new Patient("0", "A", "B", "2023-08-05"),
                new Patient("1", "C", "D", "2023-08-05"),
                new Patient("2", "E", "F", "2023-08-05"),
                new Patient("3", "G", "H", "2023-08-05"),
                new Patient("4", "I", "J", "2023-08-05"),
                new Patient("5", "K", "L", "2023-08-05")
        );

        var time = LocalTime.of(9, 0);
        var turns = new ArrayList<Appointment>();
        for (Patient patient: patients) {
            turns.add(vs.giveAppointment(patient, time, 1));
            time = time.plusMinutes(1);
        }
        vs.checkIn(turns.get(0).getId());
        vs.done(turns.get(0).getId(), turns.get(0).getVisitTime().plusMinutes(vs.getSessionLength()).toLocalTime());

        vs.checkIn(turns.get(1).getId());
        vs.done(turns.get(1).getId(), turns.get(1).getVisitTime().plusMinutes(vs.getSessionLength()).toLocalTime());

        vs.checkIn(turns.get(2).getId());
        vs.done(turns.get(2).getId(), turns.get(2).getVisitTime().plusMinutes(vs.getSessionLength()).toLocalTime());

        vs.checkIn(turns.get(3).getId());
        vs.done(turns.get(3).getId(), turns.get(3).getVisitTime().plusMinutes(vs.getSessionLength() * 2).toLocalTime());

        vs.checkIn(turns.get(4).getId());
        vs.done(turns.get(4).getId(), turns.get(4).getVisitTime().plusMinutes(vs.getSessionLength() - 3).toLocalTime());

        assertEquals(turns.get(0).getVisitTime(), LocalTime.of(9, 0));
        assertEquals(turns.get(1).getVisitTime(), LocalTime.of(9, 8));
        assertEquals(turns.get(2).getVisitTime(), LocalTime.of(9, 16));
        assertEquals(turns.get(3).getVisitTime(), LocalTime.of(9, 24));
        assertEquals(turns.get(4).getVisitTime(), LocalTime.of(9, 40));
        assertEquals(turns.get(5).getVisitTime(), LocalTime.of(9, 45));
    }
}