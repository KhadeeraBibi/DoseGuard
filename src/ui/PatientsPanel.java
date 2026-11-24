package ui;

import model.Medication;
import model.Patient;
import service.DoseGuardService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PatientsPanel extends JPanel {
    private final DoseGuardService service;
    private final DefaultTableModel tableModel;
    private final JTable patientsTable;
    private final JLabel countPill;
    private List<Patient> displayedPatients = new ArrayList<>();

    public PatientsPanel(DoseGuardService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top toolbar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        leftTitle.setOpaque(false);
        JLabel sectionLabel = new JLabel("Patient Directory");
        sectionLabel.setFont(UITheme.FONT_SECTION);
        sectionLabel.setForeground(UITheme.TEXT_PRIMARY);
        countPill = UITheme.createCountPill("0 Patients");
        leftTitle.add(sectionLabel);
        leftTitle.add(countPill);
        topBar.add(leftTitle, BorderLayout.WEST);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);

        JButton addButton = UITheme.createPrimaryButton("Add Patient", UITheme.IconType.PLUS);
        JButton viewButton = UITheme.createSecondaryButton("View Details", UITheme.IconType.EYE);
        JButton editButton = UITheme.createSecondaryButton("Edit Patient", UITheme.IconType.EDIT);
        JButton deleteButton = UITheme.createDangerButton("Delete", UITheme.IconType.TRASH);

        addButton.addActionListener(e -> openPatientFormDialog(null));
        viewButton.addActionListener(e -> openPatientDetailsDialog());
        editButton.addActionListener(e -> {
            Patient selected = getSelectedPatient();
            if (selected == null) {
                UITheme.showWarning(this, "No Selection", "Please select a patient to edit.");
                return;
            }
            openPatientFormDialog(selected);
        });
        deleteButton.addActionListener(e -> deleteSelectedPatient());

        buttonBar.add(addButton);
        buttonBar.add(viewButton);
        buttonBar.add(editButton);
        buttonBar.add(deleteButton);
        topBar.add(buttonBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Patients table card
        tableModel = new DefaultTableModel(
                new String[]{"Patient ID", "Patient Name", "Age", "Room Number", "Assigned Medications"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientsTable = new JTable(tableModel);
        UITheme.styleTable(patientsTable);
        patientsTable.getColumnModel().getColumn(0).setPreferredWidth(105);
        patientsTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        patientsTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        patientsTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        patientsTable.getColumnModel().getColumn(4).setPreferredWidth(155);

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.add(UITheme.wrapTableInScrollPane(patientsTable), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshData() {
        String selectedId = null;
        Patient currentlySelected = getSelectedPatient();
        if (currentlySelected != null) {
            selectedId = currentlySelected.getPatientId();
        }

        displayedPatients = new ArrayList<>(service.getPatients());
        tableModel.setRowCount(0);

        int count = displayedPatients.size();
        countPill.setText(count + (count == 1 ? " Patient" : " Patients"));

        int rowToSelect = -1;
        for (int i = 0; i < count; i++) {
            Patient p = displayedPatients.get(i);
            tableModel.addRow(new Object[]{
                    p.getPatientId(),
                    p.getName(),
                    p.getAge(),
                    p.getRoomNumber(),
                    service.getMedicationCountForPatient(p.getPatientId())
            });
            if (selectedId != null && selectedId.equals(p.getPatientId())) {
                rowToSelect = i;
            }
        }

        if (rowToSelect >= 0) {
            patientsTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    private Patient getSelectedPatient() {
        int row = patientsTable.getSelectedRow();
        if (row < 0 || row >= displayedPatients.size()) {
            return null;
        }
        return displayedPatients.get(row);
    }

    private void notifyFeedback(String message) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof MainFrame) {
            ((MainFrame) owner).showFeedback(message);
        }
    }

    private void openPatientFormDialog(Patient existing) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        boolean isEdit = existing != null;

        JDialog dialog = new JDialog(owner, isEdit ? "Edit Patient" : "Add New Patient",
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        dialog.add(UITheme.createDialogHeader(
                isEdit ? "Edit Patient Record" : "Register New Patient",
                isEdit ? "Update patient information for " + existing.getPatientId()
                        : "Enter the patient's details to begin tracking their medications"),
                BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JTextField nameField = UITheme.createTextField(23);
        JTextField ageField = UITheme.createTextField(23);
        JTextField roomField = UITheme.createTextField(23);

        if (isEdit) {
            nameField.setText(existing.getName());
            ageField.setText(String.valueOf(existing.getAge()));
            roomField.setText(existing.getRoomNumber());
        }

        addFormRow(formPanel, 0, "Patient Name *", nameField);
        addFormRow(formPanel, 1, "Age (1-120) *", ageField);
        addFormRow(formPanel, 2, "Room Number *", roomField);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton saveBtn = UITheme.createPrimaryButton(isEdit ? "Save Changes" : "Add Patient");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String room = roomField.getText().trim();

            if (name.isEmpty() || ageText.isEmpty() || room.isEmpty()) {
                UITheme.showWarning(dialog, "Missing Fields", "Please fill in all required fields.");
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException ex) {
                UITheme.showError(dialog, "Invalid Age", "Please enter a valid whole number for age.");
                return;
            }

            try {
                if (isEdit) {
                    service.updatePatient(existing.getPatientId(), name, age, room);
                    dialog.dispose();
                    notifyFeedback("Updated patient " + name);
                } else {
                    Patient added = service.addPatient(name, age, room);
                    dialog.dispose();
                    notifyFeedback("Registered " + added.getName() + " (" + added.getPatientId() + ")");
                }
            } catch (IllegalArgumentException ex) {
                UITheme.showError(dialog, "Validation Error", ex.getMessage());
            } catch (IOException ex) {
                dialog.dispose();
                UITheme.showWarning(this, "Storage Warning",
                        "Patient updated in memory, but could not save to disk:\n" + ex.getMessage());
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

    private void openPatientDetailsDialog() {
        Patient patient = getSelectedPatient();
        if (patient == null) {
            UITheme.showWarning(this, "No Selection", "Please select a patient to view details.");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Patient Details — " + patient.getPatientId(),
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(650, 460);
        dialog.setLocationRelativeTo(owner);

        dialog.add(UITheme.createDialogHeader(
                patient.getName(),
                "Patient ID: " + patient.getPatientId() + "   •   Age: " + patient.getAge()
                        + " years   •   Room: " + patient.getRoomNumber()),
                BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(UITheme.BG_APP);
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        DefaultTableModel medModel = new DefaultTableModel(
                new String[]{"Medicine Name", "Dosage", "Frequency", "Scheduled Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<Medication> assigned = service.getMedications(patient.getPatientId());
        for (Medication med : assigned) {
            medModel.addRow(new Object[]{
                    med.getMedicineName(),
                    med.getDosage(),
                    med.getFrequency(),
                    med.getScheduledTime(),
                    med.getStatus()
            });
        }

        JTable medTable = new JTable(medModel);
        UITheme.styleTable(medTable);
        medTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());

        JPanel medsCard = UITheme.createCardPanel();
        JPanel medsHeader = UITheme.createSectionHeader(
                "Assigned Medications (" + assigned.size() + ")",
                assigned.isEmpty() ? "No medications currently assigned to this patient"
                        : "Current daily schedule for " + patient.getName());
        medsHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        medsCard.add(medsHeader, BorderLayout.NORTH);
        medsCard.add(UITheme.wrapTableInScrollPane(medTable), BorderLayout.CENTER);
        content.add(medsCard, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(UITheme.BG_SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        footer.add(closeBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedPatient() {
        Patient patient = getSelectedPatient();
        if (patient == null) {
            UITheme.showWarning(this, "No Selection", "Please select a patient to delete.");
            return;
        }

        int medCount = service.getMedicationCountForPatient(patient.getPatientId());
        String extraWarning = medCount > 0
                ? "\nThis will also remove " + medCount + " assigned medication schedule(s)."
                : "";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete patient " + patient.getName() + " (" + patient.getPatientId() + ")?" + extraWarning,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deletePatient(patient.getPatientId());
            notifyFeedback("Removed patient " + patient.getName());
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, "Delete Failed", ex.getMessage());
        } catch (IOException ex) {
            UITheme.showWarning(this, "Storage Warning",
                    "Patient removed in memory, but could not save to disk:\n" + ex.getMessage());
        }
    }

    private void addFormRow(JPanel panel, int row, String labelText, JTextField field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 6, 14);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = row;

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_SMALL_BOLD);
        label.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }
}