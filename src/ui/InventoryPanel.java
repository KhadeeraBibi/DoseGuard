package ui;

import model.MedicineItem;
import service.DoseGuardService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InventoryPanel extends JPanel {
    private static final String[] MEDICINE_TYPES = {"Tablet", "Syrup", "Injection"};
    private static final String[] STOCK_OPERATIONS = {
            "Add Stock (+)", "Deduct Stock (-)", "Set Exact Quantity"
    };

    private final DoseGuardService service;
    private final DefaultTableModel tableModel;
    private final JTable inventoryTable;
    private final JLabel stockPill;
    private List<MedicineItem> displayedItems = new ArrayList<>();

    public InventoryPanel(DoseGuardService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top toolbar
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        leftTitle.setOpaque(false);
        JLabel sectionLabel = new JLabel("Stock Register");
        sectionLabel.setFont(UITheme.FONT_SECTION);
        sectionLabel.setForeground(UITheme.TEXT_PRIMARY);
        stockPill = UITheme.createCountPill("0 Items");
        leftTitle.add(sectionLabel);
        leftTitle.add(stockPill);
        topBar.add(leftTitle, BorderLayout.WEST);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);

        JButton addButton = UITheme.createPrimaryButton("Add Medicine", UITheme.IconType.PLUS);
        JButton updateButton = UITheme.createSecondaryButton("Update Quantity", UITheme.IconType.REFRESH);
        JButton removeButton = UITheme.createDangerButton("Remove", UITheme.IconType.TRASH);

        addButton.addActionListener(e -> openAddMedicineDialog());
        updateButton.addActionListener(e -> openUpdateQuantityDialog());
        removeButton.addActionListener(e -> handleRemoveMedicine());

        buttonBar.add(addButton);
        buttonBar.add(updateButton);
        buttonBar.add(removeButton);
        topBar.add(buttonBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Inventory table card
        tableModel = new DefaultTableModel(
                new String[]{"Medicine ID", "Medicine Name", "Type", "Available Quantity", "Stock Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        UITheme.styleTable(inventoryTable);
        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(105);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(235);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(155);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(135);
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.add(UITheme.wrapTableInScrollPane(inventoryTable), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshData() {
        String selectedId = null;
        MedicineItem current = getSelectedMedicine();
        if (current != null) {
            selectedId = current.getMedicineId();
        }

        displayedItems = new ArrayList<>(service.getInventory());
        tableModel.setRowCount(0);

        int alertItems = 0;
        int rowToSelect = -1;
        for (int i = 0; i < displayedItems.size(); i++) {
            MedicineItem item = displayedItems.get(i);
            String status = item.getStockStatus();
            if (!MedicineItem.STATUS_AVAILABLE.equals(status)) {
                alertItems++;
            }
            tableModel.addRow(new Object[]{
                    item.getMedicineId(),
                    item.getName(),
                    item.getItemType(),
                    item.getQuantityDisplay(),
                    status
            });
            if (selectedId != null && selectedId.equals(item.getMedicineId())) {
                rowToSelect = i;
            }
        }

        String summary = displayedItems.size() + (displayedItems.size() == 1 ? " Item" : " Items");
        if (alertItems > 0) {
            summary += "  •  " + alertItems + " Low / Out";
        }
        stockPill.setText(summary);

        if (rowToSelect >= 0) {
            inventoryTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    private MedicineItem getSelectedMedicine() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0 || row >= displayedItems.size()) {
            return null;
        }
        return displayedItems.get(row);
    }

    private void notifyFeedback(String message) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner instanceof MainFrame) {
            ((MainFrame) owner).showFeedback(message);
        }
    }

    private void openAddMedicineDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Add Medicine to Inventory", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        dialog.add(UITheme.createDialogHeader(
                "Add Medicine to Inventory",
                "Register a tablet, syrup, or injection with its current stock count"),
                BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JTextField nameField = UITheme.createTextField(23);
        JComboBox<String> typeCombo = new JComboBox<>(MEDICINE_TYPES);
        UITheme.styleComboBox(typeCombo);
        JTextField quantityField = UITheme.createTextField(23);

        JLabel quantityLabel = createFieldLabel("Available Quantity (tablets) *");
        typeCombo.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            if ("Syrup".equals(type)) {
                quantityLabel.setText("Available Quantity (ml) *");
            } else if ("Injection".equals(type)) {
                quantityLabel.setText("Available Quantity (vials) *");
            } else {
                quantityLabel.setText("Available Quantity (tablets) *");
            }
        });

        addFormRow(formPanel, 0, createFieldLabel("Medicine Name *"), nameField);
        addFormRow(formPanel, 1, createFieldLabel("Type *"), typeCombo);
        addFormRow(formPanel, 2, quantityLabel, quantityField);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton saveBtn = UITheme.createPrimaryButton("Add Medicine");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            String qtyText = quantityField.getText().trim();

            if (name.isEmpty() || qtyText.isEmpty()) {
                UITheme.showWarning(dialog, "Missing Fields", "Please enter both medicine name and quantity.");
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(qtyText);
            } catch (NumberFormatException ex) {
                UITheme.showError(dialog, "Invalid Quantity", "Please enter a valid whole number for quantity.");
                return;
            }

            try {
                MedicineItem added = service.addMedicine(name, type, quantity);
                dialog.dispose();
                notifyFeedback("Added " + added.getName() + " (" + added.getQuantityDisplay() + ")");
            } catch (IllegalArgumentException ex) {
                UITheme.showError(dialog, "Validation Error", ex.getMessage());
            } catch (IOException ex) {
                dialog.dispose();
                UITheme.showWarning(this, "Storage Warning",
                        "Medicine added in memory, but could not save to disk:\n" + ex.getMessage());
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

    private void openUpdateQuantityDialog() {
        MedicineItem item = getSelectedMedicine();
        if (item == null) {
            UITheme.showWarning(this, "No Selection", "Please select a medicine to update its quantity.");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Update Stock — " + item.getName(),
                JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        dialog.add(UITheme.createDialogHeader(
                "Update Stock — " + item.getName(),
                "Current stock: " + item.getQuantityDisplay() + " (" + item.getStockStatus() + ")"),
                BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JComboBox<String> operationCombo = new JComboBox<>(STOCK_OPERATIONS);
        UITheme.styleComboBox(operationCombo);

        JTextField amountField = UITheme.createTextField(20);

        addFormRow(formPanel, 0, createFieldLabel("Action"), operationCombo);
        addFormRow(formPanel, 1, createFieldLabel("Quantity (" + item.getUnit() + ") *"), amountField);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton updateBtn = UITheme.createPrimaryButton("Update Stock");

        cancelBtn.addActionListener(e -> dialog.dispose());
        updateBtn.addActionListener(e -> {
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                UITheme.showWarning(dialog, "Missing Quantity", "Please enter a quantity value.");
                return;
            }

            int amount;
            try {
                amount = Integer.parseInt(amountText);
            } catch (NumberFormatException ex) {
                UITheme.showError(dialog, "Invalid Quantity", "Please enter a valid whole number.");
                return;
            }

            int opIndex = operationCombo.getSelectedIndex();
            if ((opIndex == 0 || opIndex == 1) && amount <= 0) {
                UITheme.showError(dialog, "Invalid Quantity", "Quantity to add or deduct must be greater than zero.");
                return;
            }

            try {
                if (opIndex == 0) {
                    service.adjustMedicineQuantity(item.getMedicineId(), amount);
                } else if (opIndex == 1) {
                    service.adjustMedicineQuantity(item.getMedicineId(), -amount);
                } else {
                    service.setMedicineQuantity(item.getMedicineId(), amount);
                }
                dialog.dispose();
                notifyFeedback(item.getName() + " updated to " + item.getQuantityDisplay());
            } catch (IllegalArgumentException ex) {
                UITheme.showError(dialog, "Stock Update Failed", ex.getMessage());
            } catch (IOException ex) {
                dialog.dispose();
                UITheme.showWarning(this, "Storage Warning",
                        "Stock updated in memory, but could not save to disk:\n" + ex.getMessage());
            }
        });

        footer.add(cancelBtn);
        footer.add(updateBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(updateBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void handleRemoveMedicine() {
        MedicineItem item = getSelectedMedicine();
        if (item == null) {
            UITheme.showWarning(this, "No Selection", "Please select a medicine to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove " + item.getName() + " (" + item.getMedicineId() + ") from inventory?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.removeMedicine(item.getMedicineId());
            notifyFeedback("Removed " + item.getName() + " from inventory");
        } catch (IllegalArgumentException ex) {
            UITheme.showError(this, "Operation Failed", ex.getMessage());
        } catch (IOException ex) {
            UITheme.showWarning(this, "Storage Warning",
                    "Medicine removed in memory, but could not save to disk:\n" + ex.getMessage());
        }
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