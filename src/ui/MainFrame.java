package ui;

import model.Caretaker;
import service.AuthService;
import service.DoseGuardService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private static final String ROOT_LOGIN = "LOGIN";
    private static final String ROOT_WORKSPACE = "WORKSPACE";

    private static final String CARD_DASHBOARD = "Dashboard";
    private static final String CARD_PATIENTS = "Patients";
    private static final String CARD_MEDICATIONS = "Medications";
    private static final String CARD_INVENTORY = "Medicine Inventory";

    private static final DateTimeFormatter HEADER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy");

    private final DoseGuardService service;

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel rootContainer = new JPanel(rootLayout);
    private final LoginPanel loginPanel;

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(contentLayout);

    private final DashboardPanel dashboardPanel;
    private final PatientsPanel patientsPanel;
    private final MedicationsPanel medicationsPanel;
    private final InventoryPanel inventoryPanel;

    private final Map<String, SidebarNavButton> navButtons = new LinkedHashMap<>();
    private final Map<String, String> sectionSubtitles = new LinkedHashMap<>();

    private final JLabel pageTitleLabel = new JLabel(CARD_DASHBOARD);
    private final JLabel pageSubtitleLabel = new JLabel();
    private final JLabel alertBadgeLabel = new JLabel();
    private final JLabel feedbackToastLabel = new JLabel();
    private final JLabel headerCaretakerLabel = new JLabel();
    private final JLabel sidebarCaretakerLabel = new JLabel("Caretaker");
    private final Timer toastTimer;

    private Caretaker currentCaretaker;

    public MainFrame(DoseGuardService service, AuthService authService) {
        this.service = service;

        sectionSubtitles.put(CARD_DASHBOARD, "Overview of today's care schedule, daily progress, and alerts");
        sectionSubtitles.put(CARD_PATIENTS, "Registered patients and their assigned medication schedules");
        sectionSubtitles.put(CARD_MEDICATIONS, "Daily medication rounds, administration status, and logs");
        sectionSubtitles.put(CARD_INVENTORY, "Tablet and syrup stock quantities with automatic status alerts");

        setTitle("DoseGuard — Medication & Care Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 740);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        toastTimer = new Timer(3600, e -> feedbackToastLabel.setVisible(false));
        toastTimer.setRepeats(false);

        dashboardPanel = new DashboardPanel(service);
        patientsPanel = new PatientsPanel(service);
        medicationsPanel = new MedicationsPanel(service);
        inventoryPanel = new InventoryPanel(service);

        contentPanel.setBackground(UITheme.BG_APP);
        contentPanel.add(dashboardPanel, CARD_DASHBOARD);
        contentPanel.add(patientsPanel, CARD_PATIENTS);
        contentPanel.add(medicationsPanel, CARD_MEDICATIONS);
        contentPanel.add(inventoryPanel, CARD_INVENTORY);

        JPanel workspacePanel = new JPanel(new BorderLayout());
        workspacePanel.setBackground(UITheme.BG_APP);
        workspacePanel.add(createSidebar(), BorderLayout.WEST);

        JPanel rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(UITheme.BG_APP);
        rightWrapper.add(createTopHeader(), BorderLayout.NORTH);
        rightWrapper.add(contentPanel, BorderLayout.CENTER);
        workspacePanel.add(rightWrapper, BorderLayout.CENTER);

        loginPanel = new LoginPanel(authService, this::handleLoginSuccess);

        rootContainer.add(loginPanel, ROOT_LOGIN);
        rootContainer.add(workspacePanel, ROOT_WORKSPACE);
        add(rootContainer, BorderLayout.CENTER);

        service.addDataChangeListener(this::refreshAllViews);

        switchSection(CARD_DASHBOARD);
        refreshAllViews();
        showLoginScreen();
    }

    private void showLoginScreen() {
        rootLayout.show(rootContainer, ROOT_LOGIN);
        loginPanel.focusDefaultInput();
    }

    private void handleLoginSuccess(Caretaker caretaker) {
        this.currentCaretaker = caretaker;
        String displayName = caretaker.getFullName();
        headerCaretakerLabel.setText(displayName);
        headerCaretakerLabel.setToolTipText("Signed in as " + displayName + " (@" + caretaker.getUsername() + ")");
        sidebarCaretakerLabel.setText(displayName);

        switchSection(CARD_DASHBOARD);
        refreshAllViews();
        rootLayout.show(rootContainer, ROOT_WORKSPACE);
        showFeedback("Welcome, " + displayName);
    }

    private void handleLogout() {
        String namePrompt = currentCaretaker != null ? " (" + currentCaretaker.getFullName() + ")" : "";
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Sign out of DoseGuard" + namePrompt + " and return to the login screen?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            currentCaretaker = null;
            feedbackToastLabel.setVisible(false);
            toastTimer.stop();
            loginPanel.resetForm();
            rootLayout.show(rootContainer, ROOT_LOGIN);
        }
    }

    public void showFeedback(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        feedbackToastLabel.setText(message);
        feedbackToastLabel.setVisible(true);
        toastTimer.restart();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(236, 0));
        sidebar.setBackground(UITheme.SIDEBAR_BG);

        // Brand header
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.SIDEBAR_DIVIDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 16)
        ));

        brandPanel.add(new JLabel(UITheme.createLogoIcon(30)), BorderLayout.WEST);

        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));

        JLabel appName = new JLabel("DoseGuard");
        appName.setFont(UITheme.FONT_BRAND);
        appName.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Care Management");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.SIDEBAR_MUTED);

        brandText.add(appName);
        brandText.add(subtitle);
        brandPanel.add(brandText, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation section
        JPanel navContainer = new JPanel();
        navContainer.setOpaque(false);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(BorderFactory.createEmptyBorder(18, 0, 14, 0));

        JLabel navHeading = new JLabel("WORKSPACE");
        navHeading.setFont(UITheme.FONT_MICRO);
        navHeading.setForeground(UITheme.SIDEBAR_MUTED);
        navHeading.setBorder(BorderFactory.createEmptyBorder(0, 22, 8, 16));
        navHeading.setAlignmentX(LEFT_ALIGNMENT);
        navContainer.add(navHeading);

        addNavButton(navContainer, CARD_DASHBOARD, UITheme.IconType.DASHBOARD);
        addNavButton(navContainer, CARD_PATIENTS, UITheme.IconType.PATIENTS);
        addNavButton(navContainer, CARD_MEDICATIONS, UITheme.IconType.MEDICATIONS);
        addNavButton(navContainer, CARD_INVENTORY, UITheme.IconType.INVENTORY);

        navContainer.add(Box.createVerticalGlue());
        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom caretaker & logout footer
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.SIDEBAR_DIVIDER),
                BorderFactory.createEmptyBorder(14, 16, 16, 16)
        ));

        JPanel profileRow = new JPanel(new BorderLayout(10, 0));
        profileRow.setOpaque(false);
        profileRow.setAlignmentX(LEFT_ALIGNMENT);
        profileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel avatar = new JLabel(UITheme.createIcon(UITheme.IconType.USER, 18, UITheme.SIDEBAR_ACCENT));
        avatar.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        profileRow.add(avatar, BorderLayout.WEST);

        JPanel userMeta = new JPanel();
        userMeta.setOpaque(false);
        userMeta.setLayout(new BoxLayout(userMeta, BoxLayout.Y_AXIS));

        sidebarCaretakerLabel.setFont(UITheme.FONT_BODY_BOLD);
        sidebarCaretakerLabel.setForeground(Color.WHITE);

        JLabel storageStatus = new JLabel(
                service.isDataLoadFailed() ? "Storage: Read-Only" : "Local Storage: Active");
        storageStatus.setFont(UITheme.FONT_MICRO);
        storageStatus.setForeground(
                service.isDataLoadFailed() ? new Color(247, 196, 128) : UITheme.SIDEBAR_MUTED);

        userMeta.add(sidebarCaretakerLabel);
        userMeta.add(storageStatus);
        profileRow.add(userMeta, BorderLayout.CENTER);

        SidebarLogoutButton logoutButton = new SidebarLogoutButton("Logout");
        logoutButton.setAlignmentX(LEFT_ALIGNMENT);
        logoutButton.addActionListener(e -> handleLogout());

        footer.add(profileRow);
        footer.add(Box.createVerticalStrut(12));
        footer.add(logoutButton);
        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavButton(JPanel container, String sectionKey, UITheme.IconType iconType) {
        SidebarNavButton button = new SidebarNavButton(sectionKey, iconType);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.addActionListener(e -> switchSection(sectionKey));
        navButtons.put(sectionKey, button);
        container.add(button);
        container.add(Box.createVerticalStrut(3));
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 68));
        header.setBackground(UITheme.BG_SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setBorder(BorderFactory.createEmptyBorder(13, 0, 10, 0));

        pageTitleLabel.setFont(UITheme.FONT_PAGE_TITLE);
        pageTitleLabel.setForeground(UITheme.TEXT_PRIMARY);

        pageSubtitleLabel.setFont(UITheme.FONT_SMALL);
        pageSubtitleLabel.setForeground(UITheme.TEXT_SECONDARY);

        titleStack.add(pageTitleLabel);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(pageSubtitleLabel);
        header.add(titleStack, BorderLayout.WEST);

        JPanel rightMeta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 18));
        rightMeta.setOpaque(false);

        feedbackToastLabel.setFont(UITheme.FONT_SMALL_BOLD);
        feedbackToastLabel.setForeground(UITheme.SUCCESS_TEXT);
        feedbackToastLabel.setBackground(UITheme.SUCCESS_BG);
        feedbackToastLabel.setOpaque(true);
        feedbackToastLabel.setIcon(UITheme.createIcon(UITheme.IconType.CHECK_CIRCLE, 14, UITheme.SUCCESS_TEXT));
        feedbackToastLabel.setIconTextGap(6);
        feedbackToastLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.SUCCESS_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        feedbackToastLabel.setVisible(false);
        rightMeta.add(feedbackToastLabel);

        JLabel dateLabel = new JLabel(
                LocalDate.now().format(HEADER_DATE_FORMAT),
                UITheme.createIcon(UITheme.IconType.CALENDAR, 14, UITheme.TEXT_SECONDARY),
                SwingConstants.LEFT
        );
        dateLabel.setIconTextGap(6);
        dateLabel.setFont(UITheme.FONT_SMALL);
        dateLabel.setForeground(UITheme.TEXT_SECONDARY);
        rightMeta.add(dateLabel);

        alertBadgeLabel.setFont(UITheme.FONT_SMALL_BOLD);
        alertBadgeLabel.setOpaque(true);
        alertBadgeLabel.setIconTextGap(6);
        alertBadgeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        alertBadgeLabel.setToolTipText("View care and stock alerts on Dashboard");
        alertBadgeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switchSection(CARD_DASHBOARD);
            }
        });
        rightMeta.add(alertBadgeLabel);

        // Logged-in caretaker badge in application header
        headerCaretakerLabel.setFont(UITheme.FONT_SMALL_BOLD);
        headerCaretakerLabel.setForeground(UITheme.PRIMARY);
        headerCaretakerLabel.setBackground(UITheme.SAGE_SOFT);
        headerCaretakerLabel.setOpaque(true);
        headerCaretakerLabel.setIcon(UITheme.createIcon(UITheme.IconType.USER, 14, UITheme.PRIMARY));
        headerCaretakerLabel.setIconTextGap(6);
        headerCaretakerLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.SAGE_BORDER, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        rightMeta.add(headerCaretakerLabel);

        header.add(rightMeta, BorderLayout.EAST);
        return header;
    }

    private void switchSection(String sectionKey) {
        contentLayout.show(contentPanel, sectionKey);
        pageTitleLabel.setText(sectionKey);
        pageSubtitleLabel.setText(sectionSubtitles.getOrDefault(sectionKey, ""));

        for (Map.Entry<String, SidebarNavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(sectionKey));
        }

        if (CARD_DASHBOARD.equals(sectionKey)) {
            dashboardPanel.refreshData();
        } else if (CARD_PATIENTS.equals(sectionKey)) {
            patientsPanel.refreshData();
        } else if (CARD_MEDICATIONS.equals(sectionKey)) {
            medicationsPanel.refreshData();
        } else if (CARD_INVENTORY.equals(sectionKey)) {
            inventoryPanel.refreshData();
        }
    }

    public void refreshAllViews() {
        dashboardPanel.refreshData();
        patientsPanel.refreshData();
        medicationsPanel.refreshData();
        inventoryPanel.refreshData();
        updateAlertBadge();
    }

    private void updateAlertBadge() {
        int alertCount = service.getTotalAlertCount();
        if (alertCount > 0) {
            alertBadgeLabel.setText(alertCount == 1 ? "1 Alert" : alertCount + " Alerts");
            alertBadgeLabel.setIcon(UITheme.createIcon(UITheme.IconType.ALERT_TRIANGLE, 14, UITheme.WARNING_TEXT));
            alertBadgeLabel.setForeground(UITheme.WARNING_TEXT);
            alertBadgeLabel.setBackground(UITheme.WARNING_BG);
            alertBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.WARNING_BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        } else {
            alertBadgeLabel.setText("All Clear");
            alertBadgeLabel.setIcon(UITheme.createIcon(UITheme.IconType.CHECK_CIRCLE, 14, UITheme.SUCCESS_TEXT));
            alertBadgeLabel.setForeground(UITheme.SUCCESS_TEXT);
            alertBadgeLabel.setBackground(UITheme.SUCCESS_BG);
            alertBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.SUCCESS_BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        }
    }

    private static class SidebarNavButton extends JButton {
        private final Icon inactiveIcon;
        private final Icon activeIcon;
        private boolean active = false;
        private boolean hovered = false;

        SidebarNavButton(String text, UITheme.IconType iconType) {
            super(text);
            this.inactiveIcon = UITheme.createIcon(iconType, 16, UITheme.SIDEBAR_MUTED);
            this.activeIcon = UITheme.createIcon(iconType, 16, UITheme.SIDEBAR_ACCENT);

            setIcon(inactiveIcon);
            setIconTextGap(12);
            setHorizontalAlignment(SwingConstants.LEFT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(236, 44));
            setFont(UITheme.FONT_BODY);
            setForeground(UITheme.SIDEBAR_TEXT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            setFont(active ? UITheme.FONT_BODY_BOLD : UITheme.FONT_BODY);
            setForeground(active ? Color.WHITE : UITheme.SIDEBAR_TEXT);
            setIcon(active ? activeIcon : inactiveIcon);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(UITheme.SIDEBAR_ACTIVE);
                g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 6, 6);
                g2.setColor(UITheme.SIDEBAR_ACCENT);
                g2.fillRoundRect(10, 9, 3, getHeight() - 18, 2, 2);
            } else if (hovered) {
                g2.setColor(UITheme.SIDEBAR_HOVER);
                g2.fillRoundRect(10, 2, getWidth() - 20, getHeight() - 4, 6, 6);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SidebarLogoutButton extends JButton {
        private boolean hovered = false;

        SidebarLogoutButton(String text) {
            super(text, UITheme.createIcon(UITheme.IconType.LOGOUT, 15, UITheme.SIDEBAR_TEXT));
            setFont(UITheme.FONT_SMALL_BOLD);
            setForeground(UITheme.SIDEBAR_TEXT);
            setIconTextGap(8);
            setHorizontalAlignment(SwingConstants.CENTER);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setPreferredSize(new Dimension(204, 36));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? new Color(41, 82, 90) : new Color(28, 62, 69));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
            g2.setColor(UITheme.SIDEBAR_DIVIDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}