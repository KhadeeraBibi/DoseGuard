package ui;

import model.AdministrationLog;
import model.Medication;
import model.MedicineItem;
import service.DoseGuardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

public class DashboardPanel extends JPanel {
    private static final int DESKTOP_BREAKPOINT = 920;
    private static final int PHONE_BREAKPOINT = 540;

    private final DoseGuardService service;

    private final JLabel totalPatientsValue = new JLabel("0");
    private final JLabel scheduledTodayValue = new JLabel("0");
    private final JLabel administeredTodayValue = new JLabel("0");
    private final JLabel pendingTodayValue = new JLabel("0");

    private final JLabel progressTextLabel = new JLabel("No doses scheduled today");
    private final CompletionProgressBar completionBar = new CompletionProgressBar();

    private final DefaultTableModel scheduleModel;
    private final DefaultTableModel alertsModel;
    private final DefaultTableModel activityModel;
    private final JLabel alertSummaryLabel = new JLabel("No active alerts.");

    private final JPanel containerPanel;
    private final JPanel metricsRow;
    private final JPanel bodyPanel;
    private final JPanel scheduleCard;
    private final JPanel rightColumn;

    private int lastLayoutMode = -1; // 0=phone, 1=tablet, 2=desktop

    public DashboardPanel(DoseGuardService service) {
        this.service = service;
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_APP);

        containerPanel = new JPanel(new BorderLayout(0, 16));
        containerPanel.setBackground(UITheme.BG_APP);
        containerPanel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        // Top 4 KPI summary cards
        metricsRow = new JPanel(new GridLayout(1, 4, 14, 14));
        metricsRow.setOpaque(false);
        metricsRow.add(createMetricCard(
                "Total Patients", "Registered under care", totalPatientsValue,
                UITheme.IconType.PATIENTS, UITheme.PRIMARY, UITheme.PRIMARY_SOFT));
        metricsRow.add(createMetricCard(
                "Scheduled Today", "Total daily doses", scheduledTodayValue,
                UITheme.IconType.CALENDAR, UITheme.SAGE, UITheme.SAGE_SOFT));
        metricsRow.add(createMetricCard(
                "Administered", "Completed today", administeredTodayValue,
                UITheme.IconType.CHECK_CIRCLE, UITheme.SUCCESS_TEXT, UITheme.SUCCESS_BG));
        metricsRow.add(createMetricCard(
                "Pending Doses", "Awaiting administration", pendingTodayValue,
                UITheme.IconType.CLOCK, UITheme.WARNING_TEXT, UITheme.WARNING_BG));
        containerPanel.add(metricsRow, BorderLayout.NORTH);

        // Main workspace
        bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setOpaque(false);

        scheduleModel = new DefaultTableModel(
                new String[]{"Time", "Patient Name", "Medicine", "Dosage", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable scheduleTable = new JTable(scheduleModel);
        UITheme.styleTable(scheduleTable);
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(64);
        scheduleTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        scheduleTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        scheduleTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        scheduleTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        scheduleTable.getColumnModel().getColumn(4).setCellRenderer(UITheme.createStatusRenderer());

        scheduleCard = UITheme.createCardPanel();
        JPanel scheduleTop = new JPanel(new BorderLayout(0, 10));
        scheduleTop.setOpaque(false);
        scheduleTop.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        scheduleTop.add(UITheme.createSectionHeader(
                "Today's Medication Schedule",
                "Pending doses appear first in chronological order"), BorderLayout.NORTH);

        JPanel progressStrip = new JPanel(new BorderLayout(12, 0));
        progressStrip.setBackground(UITheme.BG_SUBTLE);
        progressStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        progressTextLabel.setFont(UITheme.FONT_SMALL_BOLD);
        progressTextLabel.setForeground(UITheme.TEXT_SECONDARY);
        progressStrip.add(progressTextLabel, BorderLayout.WEST);
        progressStrip.add(completionBar, BorderLayout.CENTER);
        scheduleTop.add(progressStrip, BorderLayout.SOUTH);

        scheduleCard.add(scheduleTop, BorderLayout.NORTH);
        scheduleCard.add(UITheme.wrapTableInScrollPane(scheduleTable), BorderLayout.CENTER);

        rightColumn = new JPanel(new GridBagLayout());
        rightColumn.setOpaque(false);

        alertsModel = new DefaultTableModel(new String[]{"Type", "Details"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable alertsTable = new JTable(alertsModel);
        UITheme.styleTable(alertsTable);
        alertsTable.setRowHeight(35);
        alertsTable.getColumnModel().getColumn(0).setPreferredWidth(104);
        alertsTable.getColumnModel().getColumn(0).setMaxWidth(120);
        alertsTable.getColumnModel().getColumn(0).setCellRenderer(UITheme.createStatusRenderer());
        alertsTable.getColumnModel().getColumn(1).setPreferredWidth(210);

        JPanel alertsCard = UITheme.createCardPanel();
        JPanel alertsHeader = UITheme.createSectionHeader(
                "Care & Stock Alerts",
                "Pending medications, low stock, and out-of-stock items");
        alertsHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        alertsCard.add(alertsHeader, BorderLayout.NORTH);
        alertsCard.add(UITheme.wrapTableInScrollPane(alertsTable), BorderLayout.CENTER);

        alertSummaryLabel.setFont(UITheme.FONT_SMALL);
        alertSummaryLabel.setForeground(UITheme.TEXT_SECONDARY);
        alertSummaryLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        alertsCard.add(alertSummaryLabel, BorderLayout.SOUTH);

        activityModel = new DefaultTableModel(new String[]{"Time", "Patient", "Medicine", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable activityTable = new JTable(activityModel);
        UITheme.styleTable(activityTable);
        activityTable.setRowHeight(34);
        activityTable.getColumnModel().getColumn(0).setPreferredWidth(72);
        activityTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        activityTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        activityTable.getColumnModel().getColumn(3).setPreferredWidth(82);
        activityTable.getColumnModel().getColumn(3).setCellRenderer(UITheme.createStatusRenderer());

        JPanel activityCard = UITheme.createCardPanel();
        JPanel activityHeader = UITheme.createSectionHeader(
                "Recent Activity",
                "Latest logged medication administrations");
        activityHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        activityCard.add(activityHeader, BorderLayout.NORTH);
        activityCard.add(UITheme.wrapTableInScrollPane(activityTable), BorderLayout.CENTER);

        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.gridx = 0;
        rightGbc.fill = GridBagConstraints.BOTH;
        rightGbc.weightx = 1.0;

        rightGbc.gridy = 0;
        rightGbc.weighty = 0.56;
        rightGbc.insets = new Insets(0, 0, 14, 0);
        rightColumn.add(alertsCard, rightGbc);

        rightGbc.gridy = 1;
        rightGbc.weighty = 0.44;
        rightGbc.insets = new Insets(0, 0, 0, 0);
        rightColumn.add(activityCard, rightGbc);

        containerPanel.add(bodyPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        applyResponsiveLayout(1100);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveLayout(getWidth());
            }
        });
    }

    private void applyResponsiveLayout(int width) {
        if (width <= 0) {
            return;
        }
        int mode = width < PHONE_BREAKPOINT ? 0 : (width < DESKTOP_BREAKPOINT ? 1 : 2);
        if (mode == lastLayoutMode) {
            return;
        }
        lastLayoutMode = mode;

        if (mode == 2) {
            metricsRow.setLayout(new GridLayout(1, 4, 14, 14));
            containerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        } else if (mode == 1) {
            metricsRow.setLayout(new GridLayout(2, 2, 12, 12));
            containerPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        } else {
            metricsRow.setLayout(new GridLayout(4, 1, 10, 10));
            containerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        }

        bodyPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        if (mode == 2) {
            scheduleCard.setPreferredSize(null);
            rightColumn.setPreferredSize(null);

            gbc.gridy = 0;
            gbc.weighty = 1.0;

            gbc.gridx = 0;
            gbc.weightx = 0.58;
            gbc.insets = new Insets(0, 0, 0, 16);
            bodyPanel.add(scheduleCard, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.42;
            gbc.insets = new Insets(0, 0, 0, 0);
            bodyPanel.add(rightColumn, gbc);
        } else {
            scheduleCard.setPreferredSize(new Dimension(0, 320));
            rightColumn.setPreferredSize(new Dimension(0, 440));

            gbc.gridx = 0;
            gbc.weightx = 1.0;

            gbc.gridy = 0;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(0, 0, 14, 0);
            bodyPanel.add(scheduleCard, gbc);

            gbc.gridy = 1;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(0, 0, 0, 0);
            bodyPanel.add(rightColumn, gbc);
        }

        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private JPanel createMetricCard(String title, String caption, JLabel valueLabel,
                                    UITheme.IconType iconType, Color accentColor, Color badgeBg) {
        JPanel card = UITheme.createCardPanel();

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL_BOLD);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        topRow.add(titleLabel, BorderLayout.WEST);

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(UITheme.createIconBadge(iconType, accentColor, badgeBg, 32));
        topRow.add(badgeWrap, BorderLayout.EAST);

        JPanel bottomStack = new JPanel();
        bottomStack.setOpaque(false);
        bottomStack.setLayout(new BoxLayout(bottomStack, BoxLayout.Y_AXIS));
        bottomStack.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        valueLabel.setFont(UITheme.FONT_METRIC);
        valueLabel.setForeground(UITheme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(UITheme.FONT_MICRO);
        captionLabel.setForeground(UITheme.TEXT_MUTED);
        captionLabel.setAlignmentX(LEFT_ALIGNMENT);

        bottomStack.add(valueLabel);
        bottomStack.add(Box.createVerticalStrut(2));
        bottomStack.add(captionLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(bottomStack, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        int totalPatients = service.getTotalPatientsCount();
        int scheduledToday = service.getScheduledTodayCount();
        int administeredToday = service.getAdministeredTodayCount();
        int pendingToday = service.getPendingTodayCount();

        totalPatientsValue.setText(String.valueOf(totalPatients));
        scheduledTodayValue.setText(String.valueOf(scheduledToday));
        administeredTodayValue.setText(String.valueOf(administeredToday));
        pendingTodayValue.setText(String.valueOf(pendingToday));

        if (scheduledToday == 0) {
            progressTextLabel.setText("Daily Progress: No doses scheduled yet");
            completionBar.setProgress(0.0);
        } else {
            double ratio = (double) administeredToday / scheduledToday;
            int pct = (int) Math.round(ratio * 100);
            progressTextLabel.setText("Daily Progress: " + administeredToday + " of "
                    + scheduledToday + " administered (" + pct + "%)");
            completionBar.setProgress(ratio);
        }

        scheduleModel.setRowCount(0);
        List<Medication> upcoming = service.getUpcomingMedications();
        for (Medication med : upcoming) {
            scheduleModel.addRow(new Object[]{
                    med.getScheduledTime(),
                    service.getPatientNameById(med.getPatientId()),
                    med.getMedicineName(),
                    med.getDosage(),
                    med.getStatus()
            });
        }

        alertsModel.setRowCount(0);

        List<MedicineItem> outOfStock = service.getOutOfStockMedicines();
        for (MedicineItem item : outOfStock) {
            alertsModel.addRow(new Object[]{
                    MedicineItem.STATUS_OUT_OF_STOCK,
                    item.getName() + " (" + item.getItemType() + " - 0 remaining)"
            });
        }

        List<MedicineItem> lowStock = service.getLowStockMedicines();
        for (MedicineItem item : lowStock) {
            alertsModel.addRow(new Object[]{
                    MedicineItem.STATUS_LOW_STOCK,
                    item.getName() + " (" + item.getQuantityDisplay() + " left)"
            });
        }

        List<Medication> pendingMeds = service.getPendingMedications();
        for (Medication med : pendingMeds) {
            alertsModel.addRow(new Object[]{
                    Medication.STATUS_PENDING,
                    med.getScheduledTime() + " - " + med.getMedicineName()
                            + " (" + service.getPatientNameById(med.getPatientId()) + ")"
            });
        }

        int totalAlerts = alertsModel.getRowCount();
        if (totalAlerts == 0) {
            alertSummaryLabel.setText("All schedules and inventory levels are up to date.");
            alertSummaryLabel.setForeground(UITheme.SUCCESS_TEXT);
        } else {
            alertSummaryLabel.setText(totalAlerts + " active " + (totalAlerts == 1 ? "alert" : "alerts")
                    + " requiring caretaker attention.");
            alertSummaryLabel.setForeground(UITheme.TEXT_SECONDARY);
        }

        activityModel.setRowCount(0);
        List<AdministrationLog> logs = service.getAdministrationLogs();
        int limit = Math.min(6, logs.size());
        for (int i = 0; i < limit; i++) {
            AdministrationLog log = logs.get(i);
            activityModel.addRow(new Object[]{
                    log.getFormattedTime(),
                    log.getPatientName(),
                    log.getMedicineName(),
                    log.getStatus()
            });
        }
    }

    private static class CompletionProgressBar extends JComponent {
        private double progress = 0.0;

        CompletionProgressBar() {
            setPreferredSize(new Dimension(100, 12));
        }

        void setProgress(double progress) {
            this.progress = Math.max(0.0, Math.min(1.0, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int barH = 8;
            int y = (getHeight() - barH) / 2;
            int w = getWidth();

            g2.setColor(UITheme.BORDER);
            g2.fillRoundRect(0, y, w, barH, barH, barH);

            int fillW = (int) Math.round(w * progress);
            if (fillW > 0) {
                g2.setColor(progress >= 1.0 ? UITheme.SUCCESS_TEXT : UITheme.PRIMARY);
                g2.fillRoundRect(0, y, Math.max(barH, fillW), barH, barH, barH);
            }
            g2.dispose();
        }
    }
}