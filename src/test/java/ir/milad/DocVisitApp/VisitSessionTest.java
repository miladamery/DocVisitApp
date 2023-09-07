package ir.milad.DocVisitApp;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisitSessionTest {

    LocalTime nineOClock = LocalTime.of(9, 0);
    LocalTime nine1_OClock = LocalTime.of(9,1);
    LocalTime nine_30_OClock = LocalTime.of(9, 30);

    private VisitSession visitSession;
    private List<Appointment> appointments;
    private final LocalDate date = LocalDate.of(2023, 8, 5);
    @BeforeEach
    public void setupVisitSession() {
        visitSession = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(14, 0, 0),
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

        var afterLastAppointmentTimePatient = new Patient("6", "M", "N", "2023-08-05");

        var time = LocalTime.of(9, 0);
        appointments = new ArrayList<>();
        for (Patient patient: patients) {
            appointments.add(visitSession.giveAppointment(patient, time, 1));
            time = time.plusMinutes(1);
        }
        appointments.add(visitSession.giveAppointment(afterLastAppointmentTimePatient, LocalTime.of(10, 30), 1));

    }

    @Nested
    class GiveAppointmentTest {
        @DisplayName("""
            GIVEN non-repetitive patient
            THEN should give a new Turn
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
            GIVEN repetitive patient
            THEN should return previous Turn 
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
                GIVEN numOfPersons > 1
                THEN next appointment time should be numOfPersons * session length
                """)
        @Test
        public void test3() {
            var vs = getVisitSession();
            var p1 = getPatient();
            var p2 = getPatient();
            var p3 = getPatient();

            var ap1 = vs.giveAppointment(p1, nineOClock, 3);
            var ap2 = vs.giveAppointment(p2, nine1_OClock, 5);
            var ap3 = vs.giveAppointment(p3, nineOClock.plusMinutes(2), 1);

            assertThat(ap1.getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
            assertThat(ap2.getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 24)));
            assertThat(ap3.getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 4)));
        }

        @DisplayName("""
                GIVEN giveAppointment
                WHEN lastAppointmentTime is after entryTime
                THEN should give an appointment with `visit time` == lastAppointmentTime
                AND lastAppointmentTime should increase by `session length`
                """)
        @Test
        public void test4() {
            var vs = getVisitSession();
            vs.setLastAppointmentTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30)));

            var p1 = getPatient();
            var p2 = getPatient();

            var ap1 = vs.giveAppointment(p1, nineOClock, 1);
            var ap2 = vs.giveAppointment(p2, nine1_OClock, 2);

            assertThat(ap1.getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 30)));
            assertThat(ap2.getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 38)));
            assertThat(vs.getLastAppointmentTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 54)));
        }

        @DisplayName("""
                GIVEN giveAppointment
                WHEN lastAppointmentTime is before entryTime
                THEN should give an appointment with `visit time` == entryTime
                AND lastAppointmentTime should increase by entryTime + `session length`
                """)
        @Test
        public void test5() {
            var vs = getVisitSession();

            var p1 = getPatient();
            var p2 = getPatient();

            var ap1 = vs.giveAppointment(p1, nine_30_OClock, 1);
            var ap2 = vs.giveAppointment(p2, nine_30_OClock.plusMinutes(5), 2);

            assertThat(ap1.getVisitTime()).isEqualTo(LocalDateTime.of(LocalTime.of(9, 30)));
            assertThat(ap2.getVisitTime()).isEqualTo(LocalDateTime.of(LocalTime.of(9, 38)));
            assertThat(vs.getLastAppointmentTime()).isEqualTo(LocalDateTime.of(LocalTime.of(9, 54)));
        }
    }

    @Nested
    class CheckInTest {
        @Test
        @DisplayName("""
            GIVEN check-in
            WHEN entryTime is equal to appointment visit time
            THEN appointments should be updated accordingly
            """)
        public void test1() {
            visitSession.checkIn(appointments.get(0).getId(), LocalTime.of(9, 0));

            assertThat(appointments.get(0).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
            assertThat(appointments.get(1).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 8)));
            assertThat(appointments.get(2).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 16)));
            assertThat(appointments.get(3).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 24)));
            assertThat(appointments.get(4).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 32)));
            assertThat(appointments.get(5).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 40)));
            assertThat(appointments.get(6).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 30)));
            assertThat(appointments.get(0).getStatus()).isEqualTo(AppointmentStatus.VISITING);
        }

        @Test
        @DisplayName("""
            GIVEN check-in
            WHEN entryTime is before appointment visit time
            THEN appointments should be updated accordingly
            """)
        public void test2() {
            visitSession.checkIn(appointments.get(1).getId(), LocalTime.of(9, 5));

            assertThat(appointments.get(0).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
            assertThat(appointments.get(1).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 5)));
            assertThat(appointments.get(2).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 13)));
            assertThat(appointments.get(3).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 21)));
            assertThat(appointments.get(4).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 29)));
            assertThat(appointments.get(5).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 37)));
            assertThat(appointments.get(6).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 27)));
        }

        @Test
        @DisplayName("""
            GIVEN check-in
            WHEN entryTime is after appointment visit time
            THEN appointments should be updated accordingly
            """)
        public void test3() {
            visitSession.checkIn(appointments.get(1).getId(), LocalTime.of(9, 15));

            assertThat(appointments.get(0).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)));
            assertThat(appointments.get(1).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 15)));
            assertThat(appointments.get(2).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 23)));
            assertThat(appointments.get(3).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 31)));
            assertThat(appointments.get(4).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 39)));
            assertThat(appointments.get(5).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 47)));
            assertThat(appointments.get(6).getVisitTime()).isEqualTo(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 37)));
        }
    }

    @Nested
    class DoneTest {
        @DisplayName("""
            GIVEN VisitSession with some patients in
            WHEN appointment is done
            AND done has happened earlier than estimated time
            THEN other appointments `visit time` should get lower
            """)
        @Test
        public void test1() {
            var vs = new VisitSession(
                    LocalDate.now(),
                    LocalTime.of(9, 0, 0),
                    LocalTime.of(14, 0, 0),
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

            var doneTime = LocalTime.of(9, 8);
            vs.checkIn(turns.get(0).getId(), doneTime);
            vs.done(turns.get(0).getId(), LocalTime.of(9, 0, 0));

            doneTime = LocalTime.of(9, 18);
            vs.checkIn(turns.get(1).getId(), LocalTime.of(9, 8, 0));
            vs.done(turns.get(1).getId(), doneTime);

            doneTime = LocalTime.of(9, 23);
            vs.checkIn(turns.get(2).getId(), LocalTime.of(9, 23, 0));
            vs.done(turns.get(2).getId(), doneTime);

            System.out.println();
        }

        @DisplayName("""
            GIVEN VisitSession with some patients in
            WHEN appointment is done
            AND done has happened later than estimated time
            THEN other appointments `visit time` should get higher
            """)
        @Test
        public void test2() {}

        @DisplayName("""
            GIVEN VisitSession with some patients in
            WHEN appointment is done
            AND done has happened exactly as estimated time
            THEN other appointments `visit time` should not change
            """)
        @Test
        public void test3() {}
    }

    @Nested
    class CancelAppointmentTest {
        @DisplayName("""
            GIVEN CancelAppointment
            WHEN appointment gets canceled AND entryTime is before appointment `visit time`
            THEN subsequent appointments `visit time` should get lower by appointment.numOfPersons * session length
            """)
        @Test
        /*@ParameterizedTest
        @ValueSource(longs = {1, 2, 3, 4, 5, 6, 7, 8, 9})*/
        public void test1() {
            var vs = new VisitSession(
                    LocalDate.now(),
                    LocalTime.of(9, 0, 0),
                    LocalTime.of(14, 0, 0),
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

            var time = LocalTime.of(9 ,0 ,0);
            var turns = new ArrayList<Appointment>();
            var i = 0;
            for (Patient patient: patients) {
                turns.add(vs.giveAppointment(patient, time, (i % 3) + 1));
                time = time.plusMinutes(1);
                i++;
            }

            vs.cancelAppointment(turns.get(1).getId(), AppointmentStatus.CANCELED, LocalTime.of(9, 5, 0));
            vs.cancelAppointment(turns.get(2).getId(), AppointmentStatus.CANCELED, LocalTime.of(9, 12, 0));
        }

        @DisplayName("""
            GIVEN CancelAppointment
            WHEN appointment gets canceled AND entryTime is equal to appointment `visit time`
            THEN subsequent appointments `visit time` should get lower by appointment.numOfPersons * session length
            """)
        @Test
        public void test2() {}
        @DisplayName("""
            GIVEN CancelAppointment
            WHEN appointment gets canceled AND entryTime is after appointment `visit time`
            THEN subsequent appointments `visit time` should get lower by 
            (appointment.numOfPersons * session length) - |appointment.visitTime - entryTime|
            """)
        @Test
        public void test3() {
        }

        @DisplayName("""
                GIVEN CancelAppointment
                WHEN appointment status == ON_HOLD
                THEN subsequent appointments should not change
                """)
        public void test4() {}
    }

    @Nested
    class OnHoldTest {
        @DisplayName("""
                GIVEN onHold operation
                WHEN entryTime is equal to appointment `visit time`
                THEN subsequent WAITING appointments `visit time` should get lower by
                appointment.numOfPersons * session length
                """)
        @Test
        public void test1() {}

        @DisplayName("""
                GIVEN onHold operation
                WHEN entryTime is after to appointment `visit time`
                THEN subsequent WAITING appointments `visit time` should get lower by
                appointment.numOfPersons * session length - |entryTime - appointment.visitTime|
                """)
        @Test
        public void test2() {}
    }

    @Nested
    class ResumeTest {
        @DisplayName("""
                GIVEN Resume operation                
                THEN subsequent WAITING appointments `visit time` should get higer by
                appointment.numOfPersons * session length
                AND appointment.visitTime should be set to now
                """)
        @Test
        public void test1() {}
    }

    @Nested
    class LastAppointmentTimeReliabilityTest {
        @Test
        @DisplayName("""
            WHEN last patient cancels or gets cancelled
            THEN visit session appointment giving time should be correct
            on next appointment
            """)
        public void test1() {}

        @Test
        @DisplayName("""
            WHEN last patient gets Done
            THEN visit session appointment giving time should be correct
            for next appointment
            """)
        public void test2() {}
    }

    @Nested
    class UpdateVisitSessionTest {
        @Test
        @DisplayName("""
            GIVEN update visit session
            WHEN appointments are not empty AND `from time` and `session length` did not change
            THEN operation should succeed and `to time` should get updated
            """)
        public void test1() {}

        @Test
        @DisplayName("""
            GIVEN update visit session
            WHEN appointments are not empty AND `from time` did change
            THEN operation should fail
            """)
        public void test2() {}

        @Test
        @DisplayName("""
            GIVEN update visit session
            WHEN appointments are not empty AND `session length` did change
            THEN operation should fail
            """)
        public void test3() {}

        @Test
        @DisplayName("""
                GIVEN update visit session
                WHEN appointments are empty And `from time` AND `session length` did change
                THEN operation should succeed AND `from time`, `session length` and `to time` should get updated
                """)
        public void test4() {}
    }

    @Nested
    class PrivateMethodsTest {}

    @DisplayName("""
           GIVEN invalid fromTime and toTime
           Should throw ApplicationException
            """)
    @Test
    public void test1() {
        assertThrows(ApplicationException.class, () -> {
            new VisitSession(
                    LocalDate.now(),
                    LocalTime.of(11, 0, 0),
                    LocalTime.of(10, 0, 0),
                    8);
        });
    }

    @DisplayName("""
            GIVEN full VisitSession
            WHEN giveTurn is called
            THEN should throw ApplicationException
            """)
    @Test
    public void test2() {
        var vs = new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(10, 0, 0),
                60
        );
        var p1 = new Patient("09300000000", "Milad", "Amery", "2023-08-05");
        var p2 = new Patient("09300000000", "Milad", "Amery2", "2023-08-05");
        vs.giveAppointment(p1, LocalTime.of(9, 5, 0), 1);
        assertThrows(ApplicationException.class
                , () -> vs.giveAppointment(p2, LocalTime.of(9, 5, 0).plusMinutes(5), 1)
        );
    }


    private VisitSession getVisitSession() {
        return new VisitSession(
                LocalDate.now(),
                LocalTime.of(9, 0, 0),
                LocalTime.of(14, 0, 0),
                8
        );
    }

    private Patient getPatient() {
        return Instancio.of(Patient.class)
                .set(field("dateOfBirth"), "2023-08-05")
                .create();
    }
}