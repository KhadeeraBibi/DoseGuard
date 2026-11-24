package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Medication implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_GIVEN = "Given";
    public static final String STATUS_MISSED = "Missed";

    public static final String TYPE_TABLET = "Tablet";
    public static final String TYPE_SYRUP = "Syrup";
    public static final String TYPE_INJECTION = "Injection";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final String medicationId;
    private final String patientId;
    private String medicineName;
    private String medicineType;
    private String injectionRoute;
    private String dosage;
    private String frequency;
    private String scheduledTime;
    private String status;
    private LocalDate lastAdministeredDate;
    private LocalDate lastUpdatedDate;

    public Medication(String medicationId, String patientId, String medicineName,
                      String dosage, String frequency, String scheduledTime) {
        this(medicationId, patientId, medicineName, TYPE_TABLET, dosage, frequency, scheduledTime, "");
    }

    public Medication(String medicationId, String patientId, String medicineName, String medicineType,
                      String dosage, String frequency, String scheduledTime, String injectionRoute) {
        if (medicationId == null || medicationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Medication ID cannot be empty.");
        }
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("A patient must be selected.");
        }
        this.medicationId = medicationId.trim();
        this.patientId = patientId.trim();
        setMedicineName(medicineName);
        setMedicineType(medicineType);
        setInjectionRoute(injectionRoute);
        setDosage(dosage);
        setFrequency(frequency);
        setScheduledTime(scheduledTime);
        this.status = STATUS_PENDING;
        this.lastUpdatedDate = LocalDate.now();
    }

    public String getMedicationId() {
        return medicationId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        if (medicineName == null || medicineName.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        this.medicineName = medicineName.trim();
    }

    public String getMedicineType() {
        return (medicineType == null || medicineType.trim().isEmpty()) ? TYPE_TABLET : medicineType;
    }

    public void setMedicineType(String medicineType) {
        if (medicineType == null || medicineType.trim().isEmpty()) {
            this.medicineType = TYPE_TABLET;
            return;
        }
        String trimmed = medicineType.trim();
        if (!TYPE_TABLET.equalsIgnoreCase(trimmed)
                && !TYPE_SYRUP.equalsIgnoreCase(trimmed)
                && !TYPE_INJECTION.equalsIgnoreCase(trimmed)) {
            throw new IllegalArgumentException("Medicine type must be Tablet, Syrup, or Injection.");
        }
        if (TYPE_INJECTION.equalsIgnoreCase(trimmed)) {
            this.medicineType = TYPE_INJECTION;
        } else if (TYPE_SYRUP.equalsIgnoreCase(trimmed)) {
            this.medicineType = TYPE_SYRUP;
        } else {
            this.medicineType = TYPE_TABLET;
        }
    }

    public boolean isInjection() {
        return TYPE_INJECTION.equalsIgnoreCase(getMedicineType());
    }

    public String getInjectionRoute() {
        return injectionRoute != null ? injectionRoute : "";
    }

    public void setInjectionRoute(String injectionRoute) {
        if (isInjection()) {
            if (injectionRoute == null || injectionRoute.trim().isEmpty()) {
                throw new IllegalArgumentException("Injection route is required for injections.");
            }
            this.injectionRoute = injectionRoute.trim();
        } else {
            this.injectionRoute = "";
        }
    }

    public String getTypeDisplay() {
        if (isInjection() && !getInjectionRoute().isEmpty()) {
            return TYPE_INJECTION + " (" + extractRouteCode(getInjectionRoute()) + ")";
        }
        return getMedicineType();
    }

    private String extractRouteCode(String route) {
        int start = route.indexOf('(');
        int end = route.indexOf(')');
        if (start >= 0 && end > start + 1) {
            return route.substring(start + 1, end);
        }
        return route;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        if (dosage == null || dosage.trim().isEmpty()) {
            throw new IllegalArgumentException("Dosage is required (e.g., 500 mg, 10 ml, or 2 ml).");
        }
        this.dosage = dosage.trim();
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            throw new IllegalArgumentException("Frequency is required.");
        }
        this.frequency = frequency.trim();
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        if (scheduledTime == null || scheduledTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Scheduled time is required in HH:mm format (e.g., 08:30).");
        }
        String trimmed = scheduledTime.trim();
        try {
            LocalTime parsed = LocalTime.parse(trimmed, TIME_FORMATTER);
            this.scheduledTime = parsed.format(TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Scheduled time must be in 24-hour HH:mm format (e.g., 08:00 or 20:30).");
        }
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getLastAdministeredDate() {
        return lastAdministeredDate;
    }

    public void syncForDate(LocalDate today) {
        if (today == null) {
            return;
        }
        if (lastUpdatedDate == null || !lastUpdatedDate.equals(today)) {
            this.status = STATUS_PENDING;
            this.lastAdministeredDate = null;
            this.lastUpdatedDate = today;
        }
    }

    public boolean isAdministeredToday(LocalDate today) {
        return today != null
                && STATUS_GIVEN.equals(status)
                && today.equals(lastAdministeredDate);
    }

    public void markAsGiven(LocalDate today) {
        syncForDate(today);
        if (isAdministeredToday(today)) {
            throw new IllegalStateException("This scheduled dose has already been marked as Given today.");
        }
        this.status = STATUS_GIVEN;
        this.lastAdministeredDate = today;
        this.lastUpdatedDate = today;
    }

    public void markAsMissed(LocalDate today) {
        syncForDate(today);
        if (isAdministeredToday(today)) {
            throw new IllegalStateException("This dose was already marked as Given today and cannot be marked Missed.");
        }
        if (STATUS_MISSED.equals(status) && today.equals(lastUpdatedDate)) {
            throw new IllegalStateException("This scheduled dose is already marked as Missed today.");
        }
        this.status = STATUS_MISSED;
        this.lastUpdatedDate = today;
    }
}