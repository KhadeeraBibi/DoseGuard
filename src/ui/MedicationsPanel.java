package ui;

import model.AdministrationLog;
import model.Medication;
import model.MedicineItem;
import model.Patient;
import service.DoseGuardService;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MedicationsPanel extends JPanel {
    private static final String[] MEDICINE_TYPES = {
            Medication.TYPE_TABLET, Medication.TYPE_SYRUP, Medication.TYPE_INJECTION
    };
    private static final String[] INJECTION_ROUTES = {
            "Intramuscular (IM)", "Intravenous (IV)", "Subcutaneous (SC)", "Intradermal (ID)"
    };
    private static final String[] INJECTION_SITES = {
            "Left Upper Arm (Deltoid)",
            "Right Upper Arm (Deltoid)",
            "Left Thigh",
            "Right Thigh",
            "Abdomen",
            "Left Glute",
            "Right Glute",
            "IV Cannula"
    };
    private static final String[] FREQUENCIES = {
            "Once Daily", "Twice Daily", "Three Times Daily", "Before Meals", "After Meals", "As Needed"
    };

    private final DoseGuardService service;
    private final DefaultTableModel tableModel;
    private final JTable medicationsTable;
    private final JLabel statusPill;
    private List<Medication> displayedMedications = new ArrayList<>();

    public MedicationsPanel(DoseGuardService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top toolbar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        leftTitle.setOpaque(false);
        JLabel sectionLabel = new JLabel("Scheduled Doses");
        sectionLabel.setFont(UITheme.FONT_SECTION);
        sectionLabel.setForeground(UITheme.TEXT_PRIMARY);
        statusPill = UITheme.createCountPill("0 Scheduled");
        leftTitle.add(sectionLabel);
        leftTitle.add(statusPill);
        topBar.add(leftTitle, BorderLayout.WEST);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);

        JButton addButton = UITheme.createPrimaryButton("Add Medication", UITheme.IconType.PLUS);
        JButton markGivenButton = UITheme.createSecondaryButton("Mark as Given", UITheme.IconType.CHECK_CIRCLE);
        JButton markMissedButton = UITheme.createSecondaryButton("Mark as Missed", UITheme.IconType.CLOCK);
        JButton historyButton = UITheme.createSecondaryButton("View History", UITheme.IconType.HISTORY);
        JButton removeButton = UITheme.createDangerButton("Remove", UITheme.IconType.TRASH);

        addButton.addActionListener(e -> openAddMedicationDialog());
        markGivenButton.addActionListener(e -> handleMarkAsGiven());
        markMissedButton.addActionListener(e -> handleMarkAsMissed());
        historyButton.addActionListener(e -> openHistoryDialog());
        removeButton.addActionListener(e -> handleRemoveMedication());

        buttonBar.add(addButton);
        buttonBar.add(markGivenButton);
        buttonBar.add(markMissedButton);
        buttonBar.add(historyButton);
        buttonBar.add(removeButton);
        topBar.add(buttonBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Medications table card
        tableModel = new DefaultTableModel(
                new String[]{"Patient Name", "Medicine Name", "Type", "Dosage", "Scheduled Time", "Administration Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        medicationsTable = new JTable(tableModel);
        UITheme.styleTable(medicationsTable);
        medicationsTable.getColumnModel().getColumn(0).setPreferredWidth(175);
        medicationsTable.getColumnModel().getColumn(1).setPreferredWidth(175);
        medicationsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        medicationsTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        medicationsTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        medicationsTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        medicationsTable.getColumnModel().getColumn(5).setCellRenderer(UITheme.createStatusRenderer());

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.add(UITheme.wrapTableInScrollPane(medicationsTable), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshData() {
        String selectedId = null;
        Medication current = getSelectedMedication();
        if (current != null) {
            selectedId = current.getMedicationId();
        }

        displayedMedications = new ArrayList<>(service.getMedications());
        tableModel.setRowCount(0);

        int pendingCount = 0;
        int rowToSelect = -1;
        for (int i = 0; i < displayedMedications.size(); i++) {
            Medication med = displayedMedications.get(i);
            if (Medication.STATUS_PENDING.equals(med.getStatus())) {
                pendingCount++;
            }
            tableModel.addRow(new Object[]{
                    service.getPatientNameById(med.getPatientId()),
                    med.getMedicineName(),
                    med.getTypeDisplay(),
                    med.getDosage(),
                    med.getScheduledTime(),
                    med.getStatus()
            });
            if (selectedId != null && selectedId.equals(med.getMedicationId())) {
                rowToSelect = i;
            }
        }

        statusPill.setText(displayedMedications.size() + " Scheduled  \u2022  " + pendingCount + " Pending");

        if (rowToSelect >= 0) {
            medicationsTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    private Medication getSelectedMedication() {
        int row = medicationsTable.getSelectedRow();
        if (row < 0 || row >= displayedMedications.size()) {
            return null;
        }
        return displayedMedications.get(row);
    }

    private void notifyFeedback(String message) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof MainFrame) {
            ((MainFrame) owner).showFeedback(message);
        }
    }

    private void openAddMedicationDialog() {
        List<Patient> patients = service.getPatients();
        if (patients.isEmpty()) {
            UITheme.showWarning(this, "No Patients Registered",
                    "Please register at least one patient in the Patients section before adding medications.");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Add Medication Schedule", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        dialog.add(UITheme.createDialogHeader(
                "Assign Medication Schedule",
                "Pick a medicine from Inventory (or type a custom name) to link automatic stock deduction"),
                BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JComboBox<Patient> patientCombo = new JComboBox<>(patients.toArray(new Patient[0]));
        UITheme.styleComboBox(patientCombo);

        // Editable Inventory-Linked Dropdown (unlimited items, auto-scrollable)
        DefaultComboBoxModel<String> medicineModel = new DefaultComboBoxModel<>();
        Set<String> uniqueNames = new LinkedHashSet<>();
        for (MedicineItem item : service.getInventory()) {
            if (uniqueNames.add(item.getName())) {
                medicineModel.addElement(item.getName());
            }
        }
        JComboBox<String> medicineCombo = new JComboBox<>(medicineModel);
        medicineCombo.setEditable(true);
        medicineCombo.setSelectedItem("");
        UITheme.styleComboBox(medicineCombo);

        JLabel stockHintLabel = new JLabel(
                uniqueNames.isEmpty()
                        ? "Tip: Add medicines in Medicine Inventory to enable auto-deduction."
                        : "Select from Inventory or type a custom medicine name.");
        stockHintLabel.setFont(UITheme.FONT_SMALL);
        stockHintLabel.setForeground(UITheme.TEXT_SECONDARY);

        JComboBox<String> typeCombo = new JComboBox<>(MEDICINE_TYPES);
        UITheme.styleComboBox(typeCombo);

        JLabel routeLabel = createFieldLabel("Injection Route *");
        JComboBox<String> routeCombo = new JComboBox<>(INJECTION_ROUTES);
        UITheme.styleComboBox(routeCombo);
        routeLabel.setVisible(false);
        routeCombo.setVisible(false);

        JLabel dosageLabel = createFieldLabel("Dosage (e.g. 500 mg) *");
        JTextField dosageField = UITheme.createTextField(23);

        JComboBox<String> frequencyCombo = new JComboBox<>(FREQUENCIES);
        UITheme.styleComboBox(frequencyCombo);

        JTextField timeField = UITheme.createTextField(23);
        timeField.setToolTipText("Use 24-hour HH:mm format, e.g., 08:00 or 20:30");

        // Sync live stock hint and auto-select Medicine Type when matching an inventory item
        Runnable syncInventoryHint = () -> {
            Object raw = medicineCombo.getEditor().getItem();
            String entered = raw != null ? raw.toString().trim() : "";
            if (entered.isEmpty()) {
                stockHintLabel.setText(uniqueNames.isEmpty()
                        ? "Tip: Add medicines in Medicine Inventory to enable auto-deduction."
                        : "Select from Inventory or type a custom medicine name.");
                stockHintLabel.setForeground(UITheme.TEXT_SECONDARY);
                return;
            }
            MedicineItem matched = service.findMatchingInventoryItem(
                    entered, (String) typeCombo.getSelectedItem());
            if (matched != null) {
                if (MedicineItem.STATUS_OUT_OF_STOCK.equals(matched.getStockStatus())) {
                    stockHintLabel.setText("Inventory: " + matched.getItemType() + " - Out of Stock (0 remaining)");
                    stockHintLabel.setForeground(UITheme.ERROR_TEXT);
                } else if (MedicineItem.STATUS_LOW_STOCK.equals(matched.getStockStatus())) {
                    stockHintLabel.setText("Inventory: " + matched.getItemType() + " - Low Stock (" + matched.getQuantityDisplay() + ")");
                    stockHintLabel.setForeground(UITheme.WARNING_TEXT);
                } else {
                    stockHintLabel.setText("Inventory linked: " + matched.getItemType() + " (" + matched.getQuantityDisplay() + " available)");
                    stockHintLabel.setForeground(UITheme.SUCCESS_TEXT);
                }
            } else {
                stockHintLabel.setText("Custom entry (not in Inventory - stock will not auto-deduct).");
                stockHintLabel.setForeground(UITheme.TEXT_MUTED);
            }
        };

        medicineCombo.addActionListener(e -> {
            Object selected = medicineCombo.getSelectedItem();
            if (selected != null) {
                MedicineItem matched = service.findMedicineByName(selected.toString().trim());
                if (matched != null) {
                    typeCombo.setSelectedItem(matched.getItemType());
                }
            }
            syncInventoryHint.run();
        });

        Component editorComp = medicineCombo.getEditor().getEditorComponent();
        if (editorComp instanceof JTextField) {
            ((JTextField) editorComp).getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    syncInventoryHint.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    syncInventoryHint.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    syncInventoryHint.run();
                }
            });
        }

        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();
            boolean isInjection = Medication.TYPE_INJECTION.equals(selectedType);
            routeLabel.setVisible(isInjection);
            routeCombo.setVisible(isInjection);

            if (Medication.TYPE_SYRUP.equals(selectedType)) {
                dosageLabel.setText("Dosage (e.g. 10 ml) *");
            } else if (isInjection) {
                dosageLabel.setText("Dosage (e.g. 2 ml / 10 units) *");
            } else {
                dosageLabel.setText("Dosage (e.g. 500 mg) *");
            }
            syncInventoryHint.run();
            dialog.pack();
        });

        addFormRow(formPanel, 0, createFieldLabel("Patient *"), patientCombo);
        addFormRow(formPanel, 1, createFieldLabel("Medicine Name *"), medicineCombo);

        GridBagConstraints hintGbc = new GridBagConstraints();
        hintGbc.gridx = 1;
        hintGbc.gridy = 2;
        hintGbc.anchor = GridBagConstraints.WEST;
        hintGbc.fill = GridBagConstraints.HORIZONTAL;
        hintGbc.insets = new Insets(0, 0, 8, 0);
        formPanel.add(stockHintLabel, hintGbc);

        addFormRow(formPanel, 3, createFieldLabel("Medicine Type *"), typeCombo);
        addFormRow(formPanel, 4, routeLabel, routeCombo);
        addFormRow(formPanel, 5, dosageLabel, dosageField);
        addFormRow(formPanel, 6, createFieldLabel("Frequency *"), frequencyCombo);
        addFormRow(formPanel, 7, createFieldLabel("Scheduled Time (HH:mm) *"), timeField);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton saveBtn = UITheme.createPrimaryButton("Add Medication");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            Patient selectedPatient = (Patient) patientCombo.getSelectedItem();
            Object rawMedicine = medicineCombo.getEditor().getItem();
            String medicineName = rawMedicine != null ? rawMedicine.toString().trim() : "";
            String medicineType = (String) typeCombo.getSelectedItem();
            String injectionRoute = Medication.TYPE_INJECTION.equals(medicineType)
                    ? (String) routeCombo.getSelectedItem() : "";
            String dosage = dosageField.getText().trim();
            String frequency = (String) frequencyCombo.getSelectedItem();
            String scheduledTime = timeField.getText().trim();

            if (selectedPatient == null || medicineName.isEmpty()
                    || dosage.isEmpty() || scheduledTime.isEmpty()) {
                UITheme.showWarning(dialog, "Missing Fields", "Please complete all required fields.");
                return;
            }

            try {
                service.addMedication(
                        selectedPatient.getPatientId(), medicineName, medicineType,
                        dosage, frequency, scheduledTime, injectionRoute);
                dialog.dispose();
                notifyFeedback("Scheduled " + medicineName + " (" + medicineType + ") at " + scheduledTime);
            } catch (IllegalArgumentException ex) {
                UITheme.showError(dialog, "Validation Error", ex.getMessage());
            } catch (IOException ex) {
                dialog.dispose();
                UITheme.showWarning(this, "Storage Warning",
                        "Medication added in memory, but could not save to disk:\n" + ex.getMessage());
            }
        });

        footer.add(cancelBtn);
        footer.add(saveBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(saveBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void handleMarkAsGiven() {
        Medication med = getSelectedMedication();
        if (med == null) {
            UITheme.showWarning(this, "No Selection", "Please select a scheduled medication first.");
            return;
        }

        if (Medication.STATUS_GIVEN.equals(med.getStatus())) {
            UITheme.showWarning(this, "Duplicate Administration Prevented",
                    "This scheduled dose has already been marked as Given today.");
            return;
        }

        MedicineItem stockItem = service.findMatchingInventoryItem(med.getMedicineName(), med.getMedicineType());
        if (stockItem != null && stockItem.getQuantity() <= 0) {
            UITheme.showWarning(this, "Out of Stock",
                    stockItem.getName() + " (" + stockItem.getItemType()
                            + ") is currently Out of Stock in Medicine Inventory.\n"
                            + "Please restock it in the Medicine Inventory tab before administering.");
            return;
        }

        // Injections go through the dedicated injection administration verification dialog
        if (med.isInjection()) {
            openInjectionAdminDialog(med);
            return;
        }

        try {
            AdministrationLog log = service.markMedicationAsGiven(med.getMedicationId(), "Oral");
            String inventoryNote = service.getLastAdministrationInventoryNote();
            notifyFeedback("Dose given: " + log.getMedicineName() + " at " + log.getFormattedTime());

            StringBuilder message = new StringBuilder();
            message.append("Recorded as Given:\n")
                    .append("Patient: ").append(log.getPatientName()).append("\n")
                    .append("Medicine: ").append(log.getMedicineName())
                    .append(" (").append(log.getMedicineType()).append(", ").append(log.getDosage()).append(")\n")
                    .append("Date & Time: ").append(log.getFormattedDate()).append(" at ").append(log.getFormattedTime());
            if (inventoryNote != null && !inventoryNote.isEmpty()) {
                message.append("\n\n").append(inventoryNote);
            }

            UITheme.showInfo(this, "Medication Administered", message.toString());
        } catch (IllegalStateException ex) {
            UITheme.showWarning(this, "Cannot Administer Dose", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, "Operation Failed", ex.getMessage());
        } catch (IOException ex) {
            UITheme.showWarning(this, "Storage Warning",
                    "Medication marked as Given in memory, but could not save to disk:\n" + ex.getMessage());
        }
    }

    private void openInjectionAdminDialog(Medication med) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String patientName = service.getPatientNameById(med.getPatientId());

        JDialog dialog = new JDialog(owner, "Administer Injection", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        dialog.add(UITheme.createDialogHeader(
                "Injection Administration Verification",
                patientName + "  \u2022  " + med.getMedicineName() + " (" + med.getDosage() + ")"),
                BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JComboBox<String> routeCombo = new JComboBox<>(INJECTION_ROUTES);
        UITheme.styleComboBox(routeCombo);
        if (!med.getInjectionRoute().isEmpty()) {
            routeCombo.setSelectedItem(med.getInjectionRoute());
        }

        JComboBox<String> siteCombo = new JComboBox<>(INJECTION_SITES);
        UITheme.styleComboBox(siteCombo);

        JCheckBox sterileCheck = new JCheckBox("Verified sterile syringe, needle, and prescribed dosage");
        styleCheckBox(sterileCheck);

        JCheckBox siteCheck = new JCheckBox("Confirmed patient identity and cleaned injection site");
        styleCheckBox(siteCheck);

        addFormRow(formPanel, 0, createFieldLabel("Injection Route *"), routeCombo);
        addFormRow(formPanel, 1, createFieldLabel("Injection Site *"), siteCombo);

        GridBagConstraints checkGbc = new GridBagConstraints();
        checkGbc.gridx = 0;
        checkGbc.gridwidth = 2;
        checkGbc.anchor = GridBagConstraints.WEST;
        checkGbc.fill = GridBagConstraints.HORIZONTAL;

        checkGbc.gridy = 2;
        checkGbc.insets = new Insets(12, 0, 4, 0);
        formPanel.add(sterileCheck, checkGbc);

        checkGbc.gridy = 3;
        checkGbc.insets = new Insets(2, 0, 4, 0);
        formPanel.add(siteCheck, checkGbc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton confirmBtn = UITheme.createPrimaryButton("Confirm Injection Given", UITheme.IconType.CHECK_CIRCLE);

        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmBtn.addActionListener(e -> {
            if (!sterileCheck.isSelected() || !siteCheck.isSelected()) {
                UITheme.showWarning(dialog, "Safety Verification Required",
                        "Please confirm both safety checks before recording the injection as Given.");
                return;
            }

            String route = (String) routeCombo.getSelectedItem();
            String site = (String) siteCombo.getSelectedItem();
            String details = route + " - " + site;

            try {
                AdministrationLog log = service.markMedicationAsGiven(med.getMedicationId(), details);
                String inventoryNote = service.getLastAdministrationInventoryNote();
                dialog.dispose();
                notifyFeedback("Injection logged: " + log.getMedicineName() + " (" + site + ")");

                StringBuilder message = new StringBuilder();
                message.append("Recorded as Given:\n")
                        .append("Patient: ").append(log.getPatientName()).append("\n")
                        .append("Injection: ").append(log.getMedicineName()).append(" (").append(log.getDosage()).append(")\n")
                        .append("Route & Site: ").append(log.getAdministrationDetails()).append("\n")
                        .append("Date & Time: ").append(log.getFormattedDate()).append(" at ").append(log.getFormattedTime());
                if (inventoryNote != null && !inventoryNote.isEmpty()) {
                    message.append("\n\n").append(inventoryNote);
                }

                UITheme.showInfo(this, "Injection Administered", message.toString());
            } catch (IllegalStateException ex) {
                dialog.dispose();
                UITheme.showWarning(this, "Cannot Administer Dose", ex.getMessage());
            } catch (IllegalArgumentException ex) {
                UITheme.showError(dialog, "Operation Failed", ex.getMessage());
            } catch (IOException ex) {
                dialog.dispose();
                UITheme.showWarning(this, "Storage Warning",
                        "Injection marked as Given in memory, but could not save to disk:\n" + ex.getMessage());
            }
        });

        footer.add(cancelBtn);
        footer.add(confirmBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(confirmBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(UITheme.FONT_SMALL);
        checkBox.setForeground(UITheme.TEXT_PRIMARY);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void handleMarkAsMissed() {
        Medication med = getSelectedMedication();
        if (med == null) {
            UITheme.showWarning(this, "No Selection", "Please select a scheduled medication first.");
            return;
        }

        try {
            AdministrationLog log = service.markMedicationAsMissed(med.getMedicationId());
            notifyFeedback("Marked missed: " + log.getMedicineName() + " (" + log.getPatientName() + ")");
        } catch (IllegalStateException ex) {
            UITheme.showWarning(this, "Action Not Allowed", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, "Operation Failed", ex.getMessage());
        } catch (IOException ex) {
            UITheme.showWarning(this, "Storage Warning",
                    "Medication marked as Missed in memory, but could not save to disk:\n" + ex.getMessage());
        }
    }

    private void handleRemoveMedication() {
        Medication med = getSelectedMedication();
        if (med == null) {
            UITheme.showWarning(this, "No Selection", "Please select a medication schedule to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove scheduled medication " + med.getMedicineName() + " (" + med.getScheduledTime() + ") for "
                        + service.getPatientNameById(med.getPatientId()) + "?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteMedication(med.getMedicationId());
            notifyFeedback("Removed schedule for " + med.getMedicineName());
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, "Operation Failed", ex.getMessage());
        } catch (IOException ex) {
            UITheme.showWarning(this, "Storage Warning",
                    "Medication removed in memory, but could not save to disk:\n" + ex.getMessage());
        }
    }

    private void openHistoryDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Medication Administration History",
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(900, 480);
        dialog.setLocationRelativeTo(owner);

        List<AdministrationLog> logs = service.getAdministrationLogs();

        dialog.add(UITheme.createDialogHeader(
                "Administration History (" + logs.size() + ")",
                logs.isEmpty() ? "No administration records logged yet"
                        : "Timestamped record of oral and injection administrations (most recent first)"),
                BorderLayout.NORTH);

        DefaultTableModel historyModel = new DefaultTableModel(
                new String[]{"Date", "Time", "Patient", "Medicine", "Type", "Dosage", "Route / Site", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (AdministrationLog log : logs) {
            historyModel.addRow(new Object[]{
                    log.getFormattedDate(),
                    log.getFormattedTime(),
                    log.getPatientName(),
                    log.getMedicineName(),
                    log.getMedicineType(),
                    log.getDosage(),
                    log.getAdministrationDetails(),
                    log.getStatus()
            });
        }

        JTable historyTable = new JTable(historyModel);
        UITheme.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(92);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(78);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(125);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(76);
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(190);
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(95);
        historyTable.getColumnModel().getColumn(7).setCellRenderer(UITheme.createStatusRenderer());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BG_APP);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel card = UITheme.createCardPanel();
        card.add(UITheme.wrapTableInScrollPane(historyTable), BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(UITheme.BG_SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(wrapper, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_SMALL_BOLD);
        label.setForeground(UITheme.TEXT_PRIMARY);
        return label;
    }

    private void addFormRow(JPanel panel, int row, JLabel label, Component input) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(input, gbc);
    }
}