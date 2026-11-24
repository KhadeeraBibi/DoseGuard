package service;

import model.AdministrationLog;
import model.InjectionMedicine;
import model.Medication;
import model.MedicineItem;
import model.Patient;
import model.SyrupMedicine;
import model.TabletMedicine;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DoseGuardService implements DataManager {
    private static final String DEFAULT_DATA_PATH = "data" + File.separator + "doseguard_data.dat";

    private final String dataFilePath;
    private final List<Patient> patients = new ArrayList<>();
    private final List<Medication> medications = new ArrayList<>();
    private final List<AdministrationLog> logs = new ArrayList<>();
    private final List<MedicineItem> inventory = new ArrayList<>();
    private final List<Runnable> changeListeners = new ArrayList<>();

    private int nextPatientNumber = 101;
    private int nextMedicationNumber = 101;
    private int nextInventoryNumber = 101;
    private int nextLogNumber = 1001;

    private boolean dataLoadFailed = false;
    private String lastLoadError = null;

    public DoseGuardService() {
        this(DEFAULT_DATA_PATH);
    }

    public DoseGuardService(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    public String getDataFilePath() {
        return dataFilePath;
    }

    public void addDataChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    private void notifyDataChanged() {
        Runnable notifier = () -> {
            for (Runnable listener : new ArrayList<>(changeListeners)) {
                listener.run();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            notifier.run();
        } else {
            SwingUtilities.invokeLater(notifier);
        }
    }

    // ==================== Persistence (DataManager) ====================

    @Override
    public boolean loadData() throws IOException, ClassNotFoundException {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            dataLoadFailed = false;
            lastLoadError = null;
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object raw = ois.readObject();
            if (!(raw instanceof DataSnapshot)) {
                throw new IOException("Unrecognized data format in " + dataFilePath);
            }
            DataSnapshot snapshot = (DataSnapshot) raw;

            patients.clear();
            medications.clear();
            logs.clear();
            inventory.clear();

            if (snapshot.patients != null) {
                patients.addAll(snapshot.patients);
            }
            if (snapshot.medications != null) {
                medications.addAll(snapshot.medications);
            }
            if (snapshot.logs != null) {
                logs.addAll(snapshot.logs);
            }
            if (snapshot.inventory != null) {
                inventory.addAll(snapshot.inventory);
            }

            nextPatientNumber = Math.max(101, snapshot.nextPatientNumber);
            nextMedicationNumber = Math.max(101, snapshot.nextMedicationNumber);
            nextInventoryNumber = Math.max(101, snapshot.nextInventoryNumber);
            nextLogNumber = Math.max(1001, snapshot.nextLogNumber);

            syncCountersWithExistingData();
            refreshDailyStatuses();

            dataLoadFailed = false;
            lastLoadError = null;
            return true;
        } catch (IOException | ClassNotFoundException | ClassCastException ex) {
            dataLoadFailed = true;
            lastLoadError = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            if (ex instanceof IOException) {
                throw (IOException) ex;
            }
            if (ex instanceof ClassNotFoundException) {
                throw (ClassNotFoundException) ex;
            }
            throw new IOException("Corrupted data file: " + lastLoadError, ex);
        }
    }

    @Override
    public void saveData() throws IOException {
        if (dataLoadFailed) {
            throw new IOException(
                    "Auto-save is paused to prevent overwriting the unreadable data file ("
                            + dataFilePath + "). Please repair or move that file and restart.");
        }

        File targetFile = new File(dataFilePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Could not create data directory: " + parentDir.getAbsolutePath());
        }

        DataSnapshot snapshot = new DataSnapshot(
                new ArrayList<>(patients),
                new ArrayList<>(medications),
                new ArrayList<>(logs),
                new ArrayList<>(inventory),
                nextPatientNumber,
                nextMedicationNumber,
                nextInventoryNumber,
                nextLogNumber
        );

        File tempFile = new File(dataFilePath + ".tmp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(snapshot);
            oos.flush();
        }

        Path source = tempFile.toPath();
        Path destination = targetFile.toPath();
        try {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicNotSupported) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public boolean isDataLoadFailed() {
        return dataLoadFailed;
    }

    @Override
    public String getLastLoadError() {
        return lastLoadError;
    }

    private void syncCountersWithExistingData() {
        for (Patient p : patients) {
            nextPatientNumber = Math.max(nextPatientNumber, extractNumericSuffix(p.getPatientId()) + 1);
        }
        for (Medication m : medications) {
            nextMedicationNumber = Math.max(nextMedicationNumber, extractNumericSuffix(m.getMedicationId()) + 1);
        }
        for (MedicineItem item : inventory) {
            nextInventoryNumber = Math.max(nextInventoryNumber, extractNumericSuffix(item.getMedicineId()) + 1);
        }
        for (AdministrationLog log : logs) {
            nextLogNumber = Math.max(nextLogNumber, extractNumericSuffix(log.getLogId()) + 1);
        }
    }

    private int extractNumericSuffix(String id) {
        if (id == null) {
            return 0;
        }
        int dash = id.lastIndexOf('-');
        if (dash >= 0 && dash < id.length() - 1) {
            try {
                return Integer.parseInt(id.substring(dash + 1));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private String generateUniquePatientId() {
        String id;
        do {
            id = "PAT-" + nextPatientNumber++;
        } while (findPatientById(id) != null);
        return id;
    }

    private String generateUniqueMedicationId() {
        String id;
        do {
            id = "MED-" + nextMedicationNumber++;
        } while (findMedicationById(id) != null);
        return id;
    }

    private String generateUniqueInventoryId() {
        String id;
        do {
            id = "INV-" + nextInventoryNumber++;
        } while (findMedicineById(id) != null);
        return id;
    }

    private String generateUniqueLogId() {
        return "LOG-" + nextLogNumber++;
    }

    public void refreshDailyStatuses() {
        LocalDate today = LocalDate.now();
        for (Medication med : medications) {
            med.syncForDate(today);
        }
    }

    // ==================== Patient Management ====================

    public List<Patient> getPatients() {
        List<Patient> copy = new ArrayList<>(patients);
        copy.sort(Comparator.comparing(Patient::getPatientId));
        return Collections.unmodifiableList(copy);
    }

    public Patient findPatientById(String patientId) {
        if (patientId == null) {
            return null;
        }
        for (Patient p : patients) {
            if (p.getPatientId().equals(patientId)) {
                return p;
            }
        }
        return null;
    }

    public String getPatientNameById(String patientId) {
        Patient patient = findPatientById(patientId);
        return patient != null ? patient.getName() : "Unknown Patient";
    }

    public Patient addPatient(String name, int age, String roomNumber) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required.");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        for (Patient existing : patients) {
            if (existing.getName().equalsIgnoreCase(name.trim())
                    && existing.getRoomNumber().equalsIgnoreCase(roomNumber.trim())) {
                throw new IllegalArgumentException(
                        "A patient with the same name in Room " + roomNumber.trim() + " already exists.");
            }
        }

        Patient patient = new Patient(generateUniquePatientId(), name, age, roomNumber);
        patients.add(patient);
        notifyDataChanged();
        saveData();
        return patient;
    }

    public void updatePatient(String patientId, String name, int age, String roomNumber) throws IOException {
        Patient patient = findPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Selected patient could not be found.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required.");
        }
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        for (Patient existing : patients) {
            if (!existing.getPatientId().equals(patientId)
                    && existing.getName().equalsIgnoreCase(name.trim())
                    && existing.getRoomNumber().equalsIgnoreCase(roomNumber.trim())) {
                throw new IllegalArgumentException(
                        "Another patient with the same name in Room " + roomNumber.trim() + " already exists.");
            }
        }

        patient.setName(name);
        patient.setAge(age);
        patient.setRoomNumber(roomNumber);
        notifyDataChanged();
        saveData();
    }

    public void deletePatient(String patientId) throws IOException {
        Patient patient = findPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Selected patient could not be found.");
        }
        patients.remove(patient);
        medications.removeIf(med -> med.getPatientId().equals(patientId));
        notifyDataChanged();
        saveData();
    }

    public int getMedicationCountForPatient(String patientId) {
        int count = 0;
        for (Medication med : medications) {
            if (med.getPatientId().equals(patientId)) {
                count++;
            }
        }
        return count;
    }

    // ==================== Medication & Administration ====================

    // Overloaded method #1: returns all scheduled medications
    public List<Medication> getMedications() {
        refreshDailyStatuses();
        List<Medication> copy = new ArrayList<>(medications);
        copy.sort(Comparator.comparing(Medication::getScheduledTime)
                .thenComparing(m -> getPatientNameById(m.getPatientId())));
        return Collections.unmodifiableList(copy);
    }

    // Overloaded method #2: returns medications for a specific patient
    public List<Medication> getMedications(String patientId) {
        refreshDailyStatuses();
        List<Medication> result = new ArrayList<>();
        if (patientId == null) {
            return Collections.unmodifiableList(result);
        }
        for (Medication med : medications) {
            if (med.getPatientId().equals(patientId)) {
                result.add(med);
            }
        }
        result.sort(Comparator.comparing(Medication::getScheduledTime));
        return Collections.unmodifiableList(result);
    }

    public Medication findMedicationById(String medicationId) {
        if (medicationId == null) {
            return null;
        }
        for (Medication med : medications) {
            if (med.getMedicationId().equals(medicationId)) {
                return med;
            }
        }
        return null;
    }

    // Overloaded addMedication #1 (backward-compatible default)
    public Medication addMedication(String patientId, String medicineName, String dosage,
                                    String frequency, String scheduledTime) throws IOException {
        return addMedication(patientId, medicineName, Medication.TYPE_TABLET,
                dosage, frequency, scheduledTime, "");
    }

    // Overloaded addMedication #2 (supports Tablet, Syrup, and Injection with route)
    public Medication addMedication(String patientId, String medicineName, String medicineType,
                                    String dosage, String frequency, String scheduledTime,
                                    String injectionRoute) throws IOException {
        Patient patient = findPatientById(patientId);
        if (patient == null) {
            throw new IllegalArgumentException("Please select a valid patient.");
        }

        Medication candidate = new Medication(
                generateUniqueMedicationId(), patientId, medicineName, medicineType,
                dosage, frequency, scheduledTime, injectionRoute);

        for (Medication existing : medications) {
            if (existing.getPatientId().equals(patientId)
                    && existing.getMedicineName().equalsIgnoreCase(candidate.getMedicineName())
                    && existing.getScheduledTime().equals(candidate.getScheduledTime())) {
                throw new IllegalArgumentException(
                        "Duplicate schedule: " + candidate.getMedicineName()
                                + " is already scheduled at " + candidate.getScheduledTime()
                                + " for " + patient.getName() + ".");
            }
        }

        medications.add(candidate);
        notifyDataChanged();
        saveData();
        return candidate;
    }

    public void deleteMedication(String medicationId) throws IOException {
        Medication med = findMedicationById(medicationId);
        if (med == null) {
            throw new IllegalArgumentException("Selected medication could not be found.");
        }
        medications.remove(med);
        notifyDataChanged();
        saveData();
    }

    // Overloaded markMedicationAsGiven #1 (standard oral Tablet/Syrup)
    public AdministrationLog markMedicationAsGiven(String medicationId) throws IOException {
        return markMedicationAsGiven(medicationId, "Oral");
    }

    // Overloaded markMedicationAsGiven #2 (supports Injection route & site details)
    public AdministrationLog markMedicationAsGiven(String medicationId, String administrationDetails) throws IOException {
        Medication med = findMedicationById(medicationId);
        if (med == null) {
            throw new IllegalArgumentException("Selected medication could not be found.");
        }

        LocalDate today = LocalDate.now();
        if (hasGivenLogToday(medicationId, today)) {
            throw new IllegalStateException("This scheduled dose has already been marked as Given today.");
        }

        String details = (administrationDetails == null || administrationDetails.trim().isEmpty())
                ? (med.isInjection() ? med.getInjectionRoute() : "Oral")
                : administrationDetails.trim();

        med.markAsGiven(today);

        AdministrationLog log = new AdministrationLog(
                generateUniqueLogId(),
                med.getMedicationId(),
                med.getPatientId(),
                getPatientNameById(med.getPatientId()),
                med.getMedicineName(),
                med.getMedicineType(),
                med.getDosage(),
                med.getScheduledTime(),
                details,
                today,
                LocalTime.now(),
                Medication.STATUS_GIVEN
        );
        logs.add(log);
        notifyDataChanged();
        saveData();
        return log;
    }

    public AdministrationLog markMedicationAsMissed(String medicationId) throws IOException {
        Medication med = findMedicationById(medicationId);
        if (med == null) {
            throw new IllegalArgumentException("Selected medication could not be found.");
        }

        LocalDate today = LocalDate.now();
        med.markAsMissed(today);

        String details = med.isInjection() ? med.getInjectionRoute() : "Oral";

        AdministrationLog log = new AdministrationLog(
                generateUniqueLogId(),
                med.getMedicationId(),
                med.getPatientId(),
                getPatientNameById(med.getPatientId()),
                med.getMedicineName(),
                med.getMedicineType(),
                med.getDosage(),
                med.getScheduledTime(),
                details,
                today,
                LocalTime.now(),
                Medication.STATUS_MISSED
        );
        logs.add(log);
        notifyDataChanged();
        saveData();
        return log;
    }

    private boolean hasGivenLogToday(String medicationId, LocalDate today) {
        for (AdministrationLog log : logs) {
            if (log.getMedicationId().equals(medicationId)
                    && today.equals(log.getDate())
                    && Medication.STATUS_GIVEN.equals(log.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public List<AdministrationLog> getAdministrationLogs() {
        List<AdministrationLog> copy = new ArrayList<>(logs);
        Collections.reverse(copy);
        return Collections.unmodifiableList(copy);
    }

    public List<AdministrationLog> getAdministrationLogs(String patientId) {
        List<AdministrationLog> filtered = new ArrayList<>();
        if (patientId == null) {
            return Collections.unmodifiableList(filtered);
        }
        for (int i = logs.size() - 1; i >= 0; i--) {
            AdministrationLog log = logs.get(i);
            if (patientId.equals(log.getPatientId())) {
                filtered.add(log);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    // ==================== Medicine Inventory ====================

    public List<MedicineItem> getInventory() {
        List<MedicineItem> copy = new ArrayList<>(inventory);
        copy.sort(Comparator.comparing(MedicineItem::getName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(copy);
    }

    public MedicineItem findMedicineById(String medicineId) {
        if (medicineId == null) {
            return null;
        }
        for (MedicineItem item : inventory) {
            if (item.getMedicineId().equals(medicineId)) {
                return item;
            }
        }
        return null;
    }

    public MedicineItem findMedicineByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String target = name.trim();
        for (MedicineItem item : inventory) {
            if (item.getName().equalsIgnoreCase(target)) {
                return item;
            }
        }
        return null;
    }

    public MedicineItem addMedicine(String name, String type, int quantity) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine type is required.");
        }
        String trimmedName = name.trim();
        String trimmedType = type.trim();

        for (MedicineItem existing : inventory) {
            if (existing.getName().equalsIgnoreCase(trimmedName)
                    && existing.getItemType().equalsIgnoreCase(trimmedType)) {
                throw new IllegalArgumentException(
                        trimmedName + " (" + trimmedType + ") already exists in inventory. Use Update Quantity instead.");
            }
        }

        String id = generateUniqueInventoryId();
        MedicineItem item;
        if ("Syrup".equalsIgnoreCase(trimmedType)) {
            item = new SyrupMedicine(id, trimmedName, quantity);
        } else if ("Injection".equalsIgnoreCase(trimmedType)) {
            item = new InjectionMedicine(id, trimmedName, quantity);
        } else if ("Tablet".equalsIgnoreCase(trimmedType)) {
            item = new TabletMedicine(id, trimmedName, quantity);
        } else {
            throw new IllegalArgumentException("Unsupported medicine type: " + trimmedType);
        }

        inventory.add(item);
        notifyDataChanged();
        saveData();
        return item;
    }

    public void setMedicineQuantity(String medicineId, int newQuantity) throws IOException {
        MedicineItem item = findMedicineById(medicineId);
        if (item == null) {
            throw new IllegalArgumentException("Selected medicine could not be found.");
        }
        item.setQuantity(newQuantity);
        notifyDataChanged();
        saveData();
    }

    public void adjustMedicineQuantity(String medicineId, int delta) throws IOException {
        MedicineItem item = findMedicineById(medicineId);
        if (item == null) {
            throw new IllegalArgumentException("Selected medicine could not be found.");
        }
        item.adjustQuantity(delta);
        notifyDataChanged();
        saveData();
    }

    public void removeMedicine(String medicineId) throws IOException {
        MedicineItem item = findMedicineById(medicineId);
        if (item == null) {
            throw new IllegalArgumentException("Selected medicine could not be found.");
        }
        inventory.remove(item);
        notifyDataChanged();
        saveData();
    }

    // ==================== Dashboard Summaries & Alerts ====================

    public int getTotalPatientsCount() {
        return patients.size();
    }

    public int getScheduledTodayCount() {
        refreshDailyStatuses();
        return medications.size();
    }

    public int getAdministeredTodayCount() {
        refreshDailyStatuses();
        int count = 0;
        for (Medication med : medications) {
            if (Medication.STATUS_GIVEN.equals(med.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public int getPendingTodayCount() {
        refreshDailyStatuses();
        int count = 0;
        for (Medication med : medications) {
            if (Medication.STATUS_PENDING.equals(med.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public List<Medication> getUpcomingMedications() {
        refreshDailyStatuses();
        List<Medication> sorted = new ArrayList<>(medications);
        sorted.sort((a, b) -> {
            int priorityA = statusPriority(a.getStatus());
            int priorityB = statusPriority(b.getStatus());
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            return a.getScheduledTime().compareTo(b.getScheduledTime());
        });
        return Collections.unmodifiableList(sorted);
    }

    private int statusPriority(String status) {
        if (Medication.STATUS_PENDING.equals(status)) {
            return 0;
        }
        if (Medication.STATUS_MISSED.equals(status)) {
            return 1;
        }
        return 2;
    }

    public List<Medication> getPendingMedications() {
        refreshDailyStatuses();
        List<Medication> pending = new ArrayList<>();
        for (Medication med : medications) {
            if (Medication.STATUS_PENDING.equals(med.getStatus())) {
                pending.add(med);
            }
        }
        pending.sort(Comparator.comparing(Medication::getScheduledTime));
        return Collections.unmodifiableList(pending);
    }

    public List<MedicineItem> getLowStockMedicines() {
        List<MedicineItem> low = new ArrayList<>();
        for (MedicineItem item : inventory) {
            if (MedicineItem.STATUS_LOW_STOCK.equals(item.getStockStatus())) {
                low.add(item);
            }
        }
        low.sort(Comparator.comparing(MedicineItem::getName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(low);
    }

    public List<MedicineItem> getOutOfStockMedicines() {
        List<MedicineItem> out = new ArrayList<>();
        for (MedicineItem item : inventory) {
            if (MedicineItem.STATUS_OUT_OF_STOCK.equals(item.getStockStatus())) {
                out.add(item);
            }
        }
        out.sort(Comparator.comparing(MedicineItem::getName, String.CASE_INSENSITIVE_ORDER));
        return Collections.unmodifiableList(out);
    }

    public int getTotalAlertCount() {
        return getPendingMedications().size()
                + getLowStockMedicines().size()
                + getOutOfStockMedicines().size();
    }

    // ==================== Serialization Container ====================

    private static class DataSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        final List<Patient> patients;
        final List<Medication> medications;
        final List<AdministrationLog> logs;
        final List<MedicineItem> inventory;
        final int nextPatientNumber;
        final int nextMedicationNumber;
        final int nextInventoryNumber;
        final int nextLogNumber;

        DataSnapshot(List<Patient> patients, List<Medication> medications,
                     List<AdministrationLog> logs, List<MedicineItem> inventory,
                     int nextPatientNumber, int nextMedicationNumber,
                     int nextInventoryNumber, int nextLogNumber) {
            this.patients = patients;
            this.medications = medications;
            this.logs = logs;
            this.inventory = inventory;
            this.nextPatientNumber = nextPatientNumber;
            this.nextMedicationNumber = nextMedicationNumber;
            this.nextInventoryNumber = nextInventoryNumber;
            this.nextLogNumber = nextLogNumber;
        }
    }
}