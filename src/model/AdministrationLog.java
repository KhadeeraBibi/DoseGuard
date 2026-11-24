package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AdministrationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final String logId;
    private final String medicationId;
    private final String patientId;
    private final String patientName;
    private final String medicineName;
    private final String medicineType;
    private final String dosage;
    private final String scheduledTime;
    private final String administrationDetails;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public AdministrationLog(String logId, String medicationId, String patientId,
                             String patientName, String medicineName, String dosage,
                             String scheduledTime, LocalDate date, LocalTime time, String status) {
        this(logId, medicationId, patientId, patientName, medicineName,
                Medication.TYPE_TABLET, dosage, scheduledTime, "Oral", date, time, status);
    }

    public AdministrationLog(String logId, String medicationId, String patientId,
                             String patientName, String medicineName, String medicineType,
                             String dosage, String scheduledTime, String administrationDetails,
                             LocalDate date, LocalTime time, String status) {
        this.logId = logId;
        this.medicationId = medicationId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.medicineName = medicineName;
        this.medicineType = (medicineType == null || medicineType.trim().isEmpty())
                ? Medication.TYPE_TABLET : medicineType.trim();
        this.dosage = dosage;
        this.scheduledTime = scheduledTime;
        this.administrationDetails = (administrationDetails == null || administrationDetails.trim().isEmpty())
                ? "-" : administrationDetails.trim();
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getLogId() {
        return logId;
    }

    public String getMedicationId() {
        return medicationId;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getMedicineType() {
        return (medicineType == null || medicineType.trim().isEmpty())
                ? Medication.TYPE_TABLET : medicineType;
    }

    public String getDosage() {
        return dosage;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public String getAdministrationDetails() {
        return (administrationDetails == null || administrationDetails.trim().isEmpty())
                ? "-" : administrationDetails;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getFormattedDate() {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    public String getFormattedTime() {
        return time != null ? time.format(TIME_FORMATTER) : "-";
    }

    public String getStatus() {
        return status;
    }
}