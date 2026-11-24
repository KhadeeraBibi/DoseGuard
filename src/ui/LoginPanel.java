package ui;

import model.Caretaker;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {
    private final AuthService authService;
    private final Consumer<Caretaker> onLoginSuccess;

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JCheckBox showPasswordCheck;
    private final JLabel statusBanner;
    private final char defaultPasswordEcho;

    public LoginPanel(AuthService authService, Consumer<Caretaker> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_APP);

        JPanel authShell = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        authShell.setOpaque(false);
        authShell.setPreferredSize(new Dimension(760, 440));
        authShell.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        // Left minimal brand panel
        authShell.add(createBrandPanel(), BorderLayout.WEST);

        // Right sign-in form
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(UITheme.BG_SURFACE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(32, 42, 32, 42));

        JPanel formStack = new JPanel();
        formStack.setOpaque(false);
        formStack.setLayout(new BoxLayout(formStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(UITheme.FONT_HERO);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);

        statusBanner = new JLabel(" ");
        statusBanner.setFont(UITheme.FONT_SMALL_BOLD);
        statusBanner.setOpaque(true);
        statusBanner.setAlignmentX(LEFT_ALIGNMENT);
        statusBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        statusBanner.setVisible(false);

        JLabel userLabel = createFieldLabel("Username");
        usernameField = UITheme.createTextField(22);
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel passLabel = createFieldLabel("Password");
        passwordField = UITheme.createPasswordField(22);
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        defaultPasswordEcho = passwordField.getEchoChar();

        showPasswordCheck = new JCheckBox("Show password");
        showPasswordCheck.setFont(UITheme.FONT_SMALL);
        showPasswordCheck.setForeground(UITheme.TEXT_SECONDARY);
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setFocusPainted(false);
        showPasswordCheck.setAlignmentX(LEFT_ALIGNMENT);
        showPasswordCheck.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPasswordCheck.addActionListener(e ->
                passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : defaultPasswordEcho));

        JButton loginButton = UITheme.createPrimaryButton("Login", UITheme.IconType.LOCK);
        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginButton.setPreferredSize(new Dimension(320, 40));

        ActionListener submitAction = e -> attemptLogin();
        loginButton.addActionListener(submitAction);
        usernameField.addActionListener(submitAction);
        passwordField.addActionListener(submitAction);

        // Subtle Register Caretaker row
        JPanel registerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        registerRow.setOpaque(false);
        registerRow.setAlignmentX(LEFT_ALIGNMENT);
        registerRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(14, 0, 0, 0)
        ));

        JLabel newHereLabel = new JLabel("New caretaker?  ");
        newHereLabel.setFont(UITheme.FONT_SMALL);
        newHereLabel.setForeground(UITheme.TEXT_SECONDARY);

        JButton registerLink = new JButton("Register Caretaker");
        registerLink.setFont(UITheme.FONT_SMALL_BOLD);
        registerLink.setForeground(UITheme.PRIMARY);
        registerLink.setContentAreaFilled(false);
        registerLink.setBorderPainted(false);
        registerLink.setFocusPainted(false);
        registerLink.setOpaque(false);
        registerLink.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> openRegistrationDialog());

        registerRow.add(newHereLabel);
        registerRow.add(registerLink);

        formStack.add(title);
        formStack.add(Box.createVerticalStrut(14));
        formStack.add(statusBanner);
        formStack.add(Box.createVerticalStrut(10));
        formStack.add(userLabel);
        formStack.add(Box.createVerticalStrut(6));
        formStack.add(usernameField);
        formStack.add(Box.createVerticalStrut(14));
        formStack.add(passLabel);
        formStack.add(Box.createVerticalStrut(6));
        formStack.add(passwordField);
        formStack.add(Box.createVerticalStrut(8));
        formStack.add(showPasswordCheck);
        formStack.add(Box.createVerticalStrut(18));
        formStack.add(loginButton);
        formStack.add(Box.createVerticalStrut(22));
        formStack.add(registerRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        formContainer.add(formStack, gbc);

        authShell.add(formContainer, BorderLayout.CENTER);
        add(authShell, new GridBagConstraints());
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle healthcare motif in top-right corner
                g2.setColor(new Color(255, 255, 255, 16));
                g2.setStroke(new BasicStroke(1.2f));
                int cx = getWidth() - 48;
                int cy = 58;
                g2.drawOval(cx - 42, cy - 42, 84, 84);
                g2.drawOval(cx - 24, cy - 24, 48, 48);
                g2.drawLine(cx - 11, cy, cx + 11, cy);
                g2.drawLine(cx, cy - 11, cx, cy + 11);

                // Subtle bottom sage-teal accent line
                g2.setColor(new Color(111, 194, 184, 65));
                g2.fillRect(0, getHeight() - 4, getWidth(), 4);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(295, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(32, 28, 32, 28));

        JPanel brandStack = new JPanel();
        brandStack.setOpaque(false);
        brandStack.setLayout(new BoxLayout(brandStack, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel(UITheme.createLogoIcon(46));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel appName = new JLabel("DoseGuard");
        appName.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Medication & Care");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.SIDEBAR_MUTED);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        brandStack.add(logo);
        brandStack.add(Box.createVerticalStrut(14));
        brandStack.add(appName);
        brandStack.add(Box.createVerticalStrut(4));
        brandStack.add(subtitle);

        panel.add(brandStack, new GridBagConstraints());
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.FONT_SMALL_BOLD);
        label.setForeground(UITheme.TEXT_PRIMARY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {
            showBanner("Please enter both username and password.", true);
            return;
        }

        if (!authService.hasRegisteredCaretakers()) {
            showBanner("No caretaker accounts yet. Click Register Caretaker.", true);
            passwordField.setText("");
            return;
        }

        Caretaker authenticated = authService.authenticate(username, password);
        if (authenticated != null) {
            hideBanner();
            passwordField.setText("");
            showPasswordCheck.setSelected(false);
            passwordField.setEchoChar(defaultPasswordEcho);
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(authenticated);
            }
        } else {
            showBanner("Invalid username or password.", true);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }

    private void openRegistrationDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Register Caretaker", JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);

        dialog.add(UITheme.createDialogHeader(
                "Register Caretaker",
                "Create a personal caretaker account for DoseGuard"), BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.BG_SURFACE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 14, 24));

        JTextField fullNameField = UITheme.createTextField(22);
        JTextField regUsernameField = UITheme.createTextField(22);
        JPasswordField regPasswordField = UITheme.createPasswordField(22);
        JPasswordField confirmPasswordField = UITheme.createPasswordField(22);

        char regEcho = regPasswordField.getEchoChar();
        JCheckBox showRegPasswords = new JCheckBox("Show passwords");
        showRegPasswords.setFont(UITheme.FONT_SMALL);
        showRegPasswords.setForeground(UITheme.TEXT_SECONDARY);
        showRegPasswords.setOpaque(false);
        showRegPasswords.setFocusPainted(false);
        showRegPasswords.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showRegPasswords.addActionListener(e -> {
            char echo = showRegPasswords.isSelected() ? (char) 0 : regEcho;
            regPasswordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        addDialogRow(formPanel, 0, "Full Name *", fullNameField);
        addDialogRow(formPanel, 1, "Username *", regUsernameField);
        addDialogRow(formPanel, 2, "Password *", regPasswordField);
        addDialogRow(formPanel, 3, "Confirm Password *", confirmPasswordField);

        GridBagConstraints checkGbc = new GridBagConstraints();
        checkGbc.gridx = 1;
        checkGbc.gridy = 4;
        checkGbc.anchor = GridBagConstraints.WEST;
        checkGbc.insets = new Insets(4, 0, 2, 0);
        formPanel.add(showRegPasswords, checkGbc);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        footer.setBackground(UITheme.BG_APP);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        JButton createBtn = UITheme.createPrimaryButton("Create Account");

        cancelBtn.addActionListener(e -> dialog.dispose());
        createBtn.addActionListener(e -> {
            try {
                Caretaker created = authService.registerCaretaker(
                        fullNameField.getText(),
                        regUsernameField.getText(),
                        regPasswordField.getPassword(),
                        confirmPasswordField.getPassword()
                );
                dialog.dispose();
                usernameField.setText(created.getUsername());
                passwordField.setText("");
                showBanner("Account created for " + created.getFullName() + ". You can now log in.", false);
                SwingUtilities.invokeLater(passwordField::requestFocusInWindow);
            } catch (IllegalArgumentException ex) {
                UITheme.showWarning(dialog, "Registration Error", ex.getMessage());
            } catch (IOException ex) {
                UITheme.showError(dialog, "Storage Error",
                        "Could not save caretaker account:\n" + ex.getMessage());
            }
        });

        footer.add(cancelBtn);
        footer.add(createBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(createBtn);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void addDialogRow(JPanel panel, int row, String labelText, JTextField field) {
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

    private void showBanner(String message, boolean isError) {
        statusBanner.setText(message);
        if (isError) {
            statusBanner.setForeground(UITheme.ERROR_TEXT);
            statusBanner.setBackground(UITheme.ERROR_BG);
            statusBanner.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.ERROR_BORDER, 1),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)
            ));
        } else {
            statusBanner.setForeground(UITheme.SUCCESS_TEXT);
            statusBanner.setBackground(UITheme.SUCCESS_BG);
            statusBanner.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.SUCCESS_BORDER, 1),
                    BorderFactory.createEmptyBorder(7, 10, 7, 10)
            ));
        }
        statusBanner.setVisible(true);
        revalidate();
        repaint();
    }

    private void hideBanner() {
        statusBanner.setVisible(false);
    }

    public void resetForm() {
        hideBanner();
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheck.setSelected(false);
        passwordField.setEchoChar(defaultPasswordEcho);
        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
    }

    public void focusDefaultInput() {
        SwingUtilities.invokeLater(() -> {
            if (usernameField.getText().trim().isEmpty()) {
                usernameField.requestFocusInWindow();
            } else {
                passwordField.requestFocusInWindow();
            }
        });
    }
}