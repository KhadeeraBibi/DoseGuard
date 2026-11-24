package ui;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

public final class UITheme {
    // Primary: muted teal / deep medical green
    public static final Color PRIMARY = new Color(15, 107, 114);
    public static final Color PRIMARY_HOVER = new Color(11, 86, 92);
    public static final Color PRIMARY_SOFT = new Color(231, 244, 244);
    public static final Color PRIMARY_BORDER = new Color(184, 219, 221);

    // Secondary: soft sage green
    public static final Color SAGE = new Color(78, 126, 115);
    public static final Color SAGE_SOFT = new Color(236, 244, 240);
    public static final Color SAGE_BORDER = new Color(201, 222, 213);

    // Sidebar palette
    public static final Color SIDEBAR_BG = new Color(21, 50, 56);
    public static final Color SIDEBAR_ACTIVE = new Color(30, 86, 94);
    public static final Color SIDEBAR_HOVER = new Color(29, 66, 73);
    public static final Color SIDEBAR_DIVIDER = new Color(37, 72, 80);
    public static final Color SIDEBAR_TEXT = new Color(212, 226, 229);
    public static final Color SIDEBAR_MUTED = new Color(149, 177, 184);
    public static final Color SIDEBAR_ACCENT = new Color(111, 194, 184);

    // Neutrals: warm off-white canvas + crisp white cards
    public static final Color BG_APP = new Color(246, 247, 245);
    public static final Color BG_SURFACE = Color.WHITE;
    public static final Color BG_SUBTLE = new Color(250, 251, 249);
    public static final Color TABLE_HEADER_BG = new Color(243, 246, 245);
    public static final Color TABLE_STRIPE = new Color(250, 251, 250);
    public static final Color BORDER = new Color(221, 227, 225);
    public static final Color INPUT_BORDER = new Color(206, 215, 212);

    // Text hierarchy
    public static final Color TEXT_PRIMARY = new Color(31, 41, 51);
    public static final Color TEXT_SECONDARY = new Color(94, 109, 119);
    public static final Color TEXT_MUTED = new Color(132, 146, 156);

    // Semantic status colours
    public static final Color SUCCESS_TEXT = new Color(30, 107, 69);
    public static final Color SUCCESS_BG = new Color(229, 244, 236);
    public static final Color SUCCESS_BORDER = new Color(185, 226, 204);

    public static final Color WARNING_TEXT = new Color(154, 96, 8);
    public static final Color WARNING_BG = new Color(253, 243, 223);
    public static final Color WARNING_BORDER = new Color(240, 213, 158);

    public static final Color ERROR_TEXT = new Color(163, 48, 48);
    public static final Color ERROR_BG = new Color(252, 234, 234);
    public static final Color ERROR_BORDER = new Color(235, 187, 187);

    // Typography
    public static final String FONT_FAMILY = resolveFontFamily();
    public static final Font FONT_BRAND = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_HERO = new Font(FONT_FAMILY, Font.BOLD, 22);
    public static final Font FONT_PAGE_TITLE = new Font(FONT_FAMILY, Font.BOLD, 18);
    public static final Font FONT_SECTION = new Font(FONT_FAMILY, Font.BOLD, 14);
    public static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_MICRO = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_METRIC = new Font(FONT_FAMILY, Font.BOLD, 25);

    public enum IconType {
        DASHBOARD,
        PATIENTS,
        MEDICATIONS,
        INVENTORY,
        LOGOUT,
        PLUS,
        EDIT,
        TRASH,
        EYE,
        CHECK_CIRCLE,
        CLOCK,
        ALERT_TRIANGLE,
        HISTORY,
        CALENDAR,
        USER,
        LOCK,
        REFRESH
    }

    private UITheme() {
    }

    private static String resolveFontFamily() {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        if (Arrays.asList(available).contains("Segoe UI")) {
            return "Segoe UI";
        }
        return Font.SANS_SERIF;
    }

    // ==================== Buttons ====================

    public static JButton createPrimaryButton(String text) {
        return createPrimaryButton(text, null);
    }

    public static JButton createPrimaryButton(String text, IconType iconType) {
        Icon icon = iconType != null ? createIcon(iconType, 15, Color.WHITE) : null;
        return new FlatButton(text, icon, PRIMARY, PRIMARY_HOVER, Color.WHITE, PRIMARY);
    }

    public static JButton createSecondaryButton(String text) {
        return createSecondaryButton(text, null);
    }

    public static JButton createSecondaryButton(String text, IconType iconType) {
        Icon icon = iconType != null ? createIcon(iconType, 15, PRIMARY) : null;
        return new FlatButton(text, icon, BG_SURFACE, SAGE_SOFT, TEXT_PRIMARY, BORDER);
    }

    public static JButton createDangerButton(String text) {
        return createDangerButton(text, null);
    }

    public static JButton createDangerButton(String text, IconType iconType) {
        Icon icon = iconType != null ? createIcon(iconType, 15, ERROR_TEXT) : null;
        return new FlatButton(text, icon, BG_SURFACE, ERROR_BG, ERROR_TEXT, ERROR_BORDER);
    }

    // ==================== Cards & Headers ====================

    public static JPanel createCardPanel() {
        JPanel panel = new RoundedPanel(6, BG_SURFACE, BORDER);
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return panel;
    }

    public static JPanel createSectionHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_SECTION);
        titleLabel.setForeground(TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(FONT_SMALL);
            subLabel.setForeground(TEXT_SECONDARY);
            header.add(subLabel, BorderLayout.SOUTH);
        }
        return header;
    }

    public static JPanel createDialogHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setBackground(SAGE_SOFT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, SAGE_BORDER),
                BorderFactory.createEmptyBorder(14, 22, 14, 22)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 15));
        titleLabel.setForeground(TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.NORTH);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            JLabel subLabel = new JLabel(subtitle);
            subLabel.setFont(FONT_SMALL);
            subLabel.setForeground(TEXT_SECONDARY);
            header.add(subLabel, BorderLayout.SOUTH);
        }
        return header;
    }

    public static JLabel createCountPill(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL_BOLD);
        label.setForeground(SAGE);
        label.setBackground(SAGE_SOFT);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SAGE_BORDER, 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        return label;
    }

    public static JPanel createIconBadge(IconType type, Color iconColor, Color bgColor, int boxSize) {
        JPanel badge = new RoundedPanel(6, bgColor, bgColor);
        badge.setPreferredSize(new Dimension(boxSize, boxSize));
        badge.setMinimumSize(new Dimension(boxSize, boxSize));
        badge.setMaximumSize(new Dimension(boxSize, boxSize));
        badge.setLayout(new BorderLayout());

        JLabel iconLabel = new JLabel(createIcon(type, Math.max(14, boxSize / 2), iconColor));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badge.add(iconLabel, BorderLayout.CENTER);
        return badge;
    }

    // ==================== Inputs ====================

    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleInputComponent(field);
        return field;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInputComponent(field);
        return field;
    }

    private static void styleInputComponent(JTextField field) {
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_SURFACE);
        field.setCaretColor(PRIMARY);

        Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(INPUT_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );
        Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        );

        field.setBorder(normalBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(focusBorder);
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(normalBorder);
            }
        });
    }

    public static <T> void styleComboBox(JComboBox<T> comboBox) {
        comboBox.setFont(FONT_BODY);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBackground(BG_SURFACE);
        comboBox.setPreferredSize(new Dimension(comboBox.getPreferredSize().width, 35));
    }

    // ==================== Tables & Status Pills ====================

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_SURFACE);
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 239, 237));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(PRIMARY_SOFT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 38));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, false, false, row, column);
                label.setFont(FONT_MICRO);
                label.setForeground(TEXT_SECONDARY);
                label.setBackground(TABLE_HEADER_BG);
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setText(value != null ? value.toString().toUpperCase() : "");
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        tbl, value, isSelected, false, row, column);
                label.setFont(FONT_BODY);
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (isSelected) {
                    label.setBackground(PRIMARY_SOFT);
                    label.setForeground(TEXT_PRIMARY);
                } else {
                    label.setBackground(row % 2 == 0 ? BG_SURFACE : TABLE_STRIPE);
                    label.setForeground(TEXT_PRIMARY);
                }
                return label;
            }
        });
    }

    public static JScrollPane wrapTableInScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(BG_SURFACE);
        return scrollPane;
    }

    public static DefaultTableCellRenderer createStatusRenderer() {
        return new StatusPillRenderer();
    }

    // ==================== Vector Icons (Lucide-Style Java2D) ====================

    public static Icon createLogoIcon() {
        return createLogoIcon(30);
    }

    public static Icon createLogoIcon(int size) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                double s = size;
                // Shield silhouette
                Path2D shield = new Path2D.Double();
                shield.moveTo(x + s * 0.50, y + s * 0.06);
                shield.lineTo(x + s * 0.88, y + s * 0.20);
                shield.lineTo(x + s * 0.88, y + s * 0.52);
                shield.curveTo(x + s * 0.88, y + s * 0.76, x + s * 0.68, y + s * 0.89, x + s * 0.50, y + s * 0.95);
                shield.curveTo(x + s * 0.32, y + s * 0.89, x + s * 0.12, y + s * 0.76, x + s * 0.12, y + s * 0.52);
                shield.lineTo(x + s * 0.12, y + s * 0.20);
                shield.closePath();

                g2.setColor(new Color(38, 146, 152));
                g2.fill(shield);

                g2.setColor(new Color(111, 194, 184));
                g2.setStroke(new BasicStroke((float) Math.max(1.2, s * 0.045)));
                g2.draw(shield);

                // Clean medical cross centered inside shield
                g2.setColor(Color.WHITE);
                int barThick = Math.max(3, (int) Math.round(s * 0.14));
                int barLen = Math.max(10, (int) Math.round(s * 0.44));
                int cx = x + size / 2;
                int cy = y + (int) Math.round(s * 0.49);

                g2.fillRoundRect(cx - barThick / 2, cy - barLen / 2, barThick, barLen, 2, 2);
                g2.fillRoundRect(cx - barLen / 2, cy - barThick / 2, barLen, barThick, 2, 2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static Icon createIcon(IconType type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                float strokeWidth = Math.max(1.4f, size / 10.5f);
                g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                double u = size / 16.0;
                g2.translate(x, y);

                switch (type) {
                    case DASHBOARD:
                        g2.draw(new RoundRectangle2D.Double(2 * u, 2 * u, 5 * u, 5 * u, 1.5 * u, 1.5 * u));
                        g2.draw(new RoundRectangle2D.Double(9 * u, 2 * u, 5 * u, 5 * u, 1.5 * u, 1.5 * u));
                        g2.draw(new RoundRectangle2D.Double(2 * u, 9 * u, 5 * u, 5 * u, 1.5 * u, 1.5 * u));
                        g2.draw(new RoundRectangle2D.Double(9 * u, 9 * u, 5 * u, 5 * u, 1.5 * u, 1.5 * u));
                        break;

                    case PATIENTS:
                        g2.draw(new Ellipse2D.Double(4 * u, 2.5 * u, 4.5 * u, 4.5 * u));
                        Path2D body = new Path2D.Double();
                        body.moveTo(1.5 * u, 13.5 * u);
                        body.curveTo(1.5 * u, 10.5 * u, 3.5 * u, 9.2 * u, 6.25 * u, 9.2 * u);
                        body.curveTo(9 * u, 9.2 * u, 11 * u, 10.5 * u, 11 * u, 13.5 * u);
                        g2.draw(body);
                        g2.draw(new Ellipse2D.Double(10.2 * u, 3.5 * u, 3.3 * u, 3.3 * u));
                        Path2D body2 = new Path2D.Double();
                        body2.moveTo(11.8 * u, 9.5 * u);
                        body2.curveTo(13.6 * u, 9.8 * u, 14.6 * u, 11 * u, 14.6 * u, 13.2 * u);
                        g2.draw(body2);
                        break;

                    case MEDICATIONS:
                        Graphics2D pillG = (Graphics2D) g2.create();
                        pillG.rotate(Math.toRadians(-45), 8 * u, 8 * u);
                        pillG.draw(new RoundRectangle2D.Double(2.2 * u, 5.2 * u, 11.6 * u, 5.6 * u, 5.6 * u, 5.6 * u));
                        pillG.draw(new Line2D.Double(8 * u, 5.2 * u, 8 * u, 10.8 * u));
                        pillG.dispose();
                        break;

                    case INVENTORY:
                        Path2D box = new Path2D.Double();
                        box.moveTo(8 * u, 1.8 * u);
                        box.lineTo(13.8 * u, 4.8 * u);
                        box.lineTo(13.8 * u, 11.2 * u);
                        box.lineTo(8 * u, 14.2 * u);
                        box.lineTo(2.2 * u, 11.2 * u);
                        box.lineTo(2.2 * u, 4.8 * u);
                        box.closePath();
                        g2.draw(box);
                        g2.draw(new Line2D.Double(2.2 * u, 4.8 * u, 8 * u, 7.8 * u));
                        g2.draw(new Line2D.Double(13.8 * u, 4.8 * u, 8 * u, 7.8 * u));
                        g2.draw(new Line2D.Double(8 * u, 7.8 * u, 8 * u, 14.2 * u));
                        break;

                    case LOGOUT:
                        Path2D door = new Path2D.Double();
                        door.moveTo(6.5 * u, 2.5 * u);
                        door.lineTo(3 * u, 2.5 * u);
                        door.lineTo(3 * u, 13.5 * u);
                        door.lineTo(6.5 * u, 13.5 * u);
                        g2.draw(door);
                        g2.draw(new Line2D.Double(7 * u, 8 * u, 13.5 * u, 8 * u));
                        g2.draw(new Line2D.Double(10.8 * u, 5.3 * u, 13.5 * u, 8 * u));
                        g2.draw(new Line2D.Double(10.8 * u, 10.7 * u, 13.5 * u, 8 * u));
                        break;

                    case PLUS:
                        g2.draw(new Line2D.Double(8 * u, 3 * u, 8 * u, 13 * u));
                        g2.draw(new Line2D.Double(3 * u, 8 * u, 13 * u, 8 * u));
                        break;

                    case EDIT:
                        Path2D pen = new Path2D.Double();
                        pen.moveTo(11 * u, 2.5 * u);
                        pen.lineTo(13.5 * u, 5 * u);
                        pen.lineTo(5.5 * u, 13 * u);
                        pen.lineTo(2.5 * u, 13.5 * u);
                        pen.lineTo(3 * u, 10.5 * u);
                        pen.closePath();
                        g2.draw(pen);
                        break;

                    case TRASH:
                        g2.draw(new Line2D.Double(2.5 * u, 4.5 * u, 13.5 * u, 4.5 * u));
                        g2.draw(new Line2D.Double(6 * u, 2.5 * u, 10 * u, 2.5 * u));
                        g2.draw(new RoundRectangle2D.Double(4 * u, 4.5 * u, 8 * u, 9 * u, 1.5 * u, 1.5 * u));
                        g2.draw(new Line2D.Double(6.7 * u, 7.2 * u, 6.7 * u, 11 * u));
                        g2.draw(new Line2D.Double(9.3 * u, 7.2 * u, 9.3 * u, 11 * u));
                        break;

                    case EYE:
                        Path2D eye = new Path2D.Double();
                        eye.moveTo(1.5 * u, 8 * u);
                        eye.curveTo(4 * u, 3.8 * u, 12 * u, 3.8 * u, 14.5 * u, 8 * u);
                        eye.curveTo(12 * u, 12.2 * u, 4 * u, 12.2 * u, 1.5 * u, 8 * u);
                        g2.draw(eye);
                        g2.draw(new Ellipse2D.Double(6.2 * u, 6.2 * u, 3.6 * u, 3.6 * u));
                        break;

                    case CHECK_CIRCLE:
                        g2.draw(new Ellipse2D.Double(2 * u, 2 * u, 12 * u, 12 * u));
                        Path2D check = new Path2D.Double();
                        check.moveTo(5.2 * u, 8.2 * u);
                        check.lineTo(7.2 * u, 10.2 * u);
                        check.lineTo(11 * u, 6 * u);
                        g2.draw(check);
                        break;

                    case CLOCK:
                        g2.draw(new Ellipse2D.Double(2 * u, 2 * u, 12 * u, 12 * u));
                        g2.draw(new Line2D.Double(8 * u, 4.8 * u, 8 * u, 8.2 * u));
                        g2.draw(new Line2D.Double(8 * u, 8.2 * u, 10.6 * u, 9.8 * u));
                        break;

                    case ALERT_TRIANGLE:
                        Path2D tri = new Path2D.Double();
                        tri.moveTo(8 * u, 2.2 * u);
                        tri.lineTo(14 * u, 13.2 * u);
                        tri.lineTo(2 * u, 13.2 * u);
                        tri.closePath();
                        g2.draw(tri);
                        g2.draw(new Line2D.Double(8 * u, 6.3 * u, 8 * u, 9.6 * u));
                        g2.draw(new Line2D.Double(8 * u, 11.4 * u, 8 * u, 11.6 * u));
                        break;

                    case HISTORY:
                        g2.draw(new Ellipse2D.Double(2.5 * u, 2.5 * u, 11 * u, 11 * u));
                        g2.draw(new Line2D.Double(8 * u, 5 * u, 8 * u, 8.2 * u));
                        g2.draw(new Line2D.Double(8 * u, 8.2 * u, 10.5 * u, 9.6 * u));
                        break;

                    case CALENDAR:
                        g2.draw(new RoundRectangle2D.Double(2 * u, 3.5 * u, 12 * u, 10.5 * u, 2 * u, 2 * u));
                        g2.draw(new Line2D.Double(2 * u, 7 * u, 14 * u, 7 * u));
                        g2.draw(new Line2D.Double(5.3 * u, 1.8 * u, 5.3 * u, 4.5 * u));
                        g2.draw(new Line2D.Double(10.7 * u, 1.8 * u, 10.7 * u, 4.5 * u));
                        break;

                    case USER:
                        g2.draw(new Ellipse2D.Double(5.2 * u, 2.4 * u, 5.6 * u, 5.6 * u));
                        Path2D shoulders = new Path2D.Double();
                        shoulders.moveTo(2.5 * u, 13.8 * u);
                        shoulders.curveTo(2.8 * u, 10.4 * u, 5.2 * u, 9.6 * u, 8 * u, 9.6 * u);
                        shoulders.curveTo(10.8 * u, 9.6 * u, 13.2 * u, 10.4 * u, 13.5 * u, 13.8 * u);
                        g2.draw(shoulders);
                        break;

                    case LOCK:
                        g2.draw(new RoundRectangle2D.Double(3.2 * u, 7 * u, 9.6 * u, 6.8 * u, 2 * u, 2 * u));
                        Path2D shackle = new Path2D.Double();
                        shackle.moveTo(5.3 * u, 7 * u);
                        shackle.lineTo(5.3 * u, 5 * u);
                        shackle.curveTo(5.3 * u, 2.5 * u, 10.7 * u, 2.5 * u, 10.7 * u, 5 * u);
                        shackle.lineTo(10.7 * u, 7 * u);
                        g2.draw(shackle);
                        break;

                    case REFRESH:
                        g2.draw(new java.awt.geom.Arc2D.Double(2.5 * u, 2.5 * u, 11 * u, 11 * u, 40, 285,
                                java.awt.geom.Arc2D.OPEN));
                        g2.draw(new Line2D.Double(11.8 * u, 2.6 * u, 12.6 * u, 5.5 * u));
                        g2.draw(new Line2D.Double(9.6 * u, 5.4 * u, 12.6 * u, 5.5 * u));
                        break;
                }

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    // ==================== Dialog Helpers ====================

    public static void showError(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showInfo(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== Custom Lightweight UI Primitives ====================

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fillColor;
        private final Color borderColor;

        RoundedPanel(int radius, Color fillColor, Color borderColor) {
            this.radius = radius;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class FlatButton extends JButton {
        private final Color baseBg;
        private final Color hoverBg;
        private final Color borderColor;
        private boolean hovered = false;

        FlatButton(String text, Icon icon, Color baseBg, Color hoverBg, Color fg, Color borderColor) {
            super(text, icon);
            this.baseBg = baseBg;
            this.hoverBg = hoverBg;
            this.borderColor = borderColor;

            setFont(FONT_BODY_BOLD);
            setForeground(fg);
            setIconTextGap(7);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        hovered = true;
                        repaint();
                    }
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

            Color fill = isEnabled() ? (hovered ? hoverBg : baseBg) : TABLE_HEADER_BG;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);

            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 5, 5);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class StatusPillRenderer extends DefaultTableCellRenderer {
        private String currentStatus = "";
        private Color cellBg = BG_SURFACE;
        private Color pillBg = TABLE_HEADER_BG;
        private Color pillBorder = BORDER;
        private Color pillFg = TEXT_SECONDARY;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            currentStatus = value != null ? value.toString() : "";
            cellBg = isSelected ? PRIMARY_SOFT : (row % 2 == 0 ? BG_SURFACE : TABLE_STRIPE);

            if ("Given".equalsIgnoreCase(currentStatus) || "Available".equalsIgnoreCase(currentStatus)) {
                pillFg = SUCCESS_TEXT;
                pillBg = SUCCESS_BG;
                pillBorder = SUCCESS_BORDER;
            } else if ("Pending".equalsIgnoreCase(currentStatus) || "Low Stock".equalsIgnoreCase(currentStatus)) {
                pillFg = WARNING_TEXT;
                pillBg = WARNING_BG;
                pillBorder = WARNING_BORDER;
            } else if ("Missed".equalsIgnoreCase(currentStatus) || "Out of Stock".equalsIgnoreCase(currentStatus)) {
                pillFg = ERROR_TEXT;
                pillBg = ERROR_BG;
                pillBorder = ERROR_BORDER;
            } else {
                pillFg = TEXT_SECONDARY;
                pillBg = TABLE_HEADER_BG;
                pillBorder = BORDER;
            }

            setText("");
            setOpaque(true);
            setBackground(cellBg);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(cellBg);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (!currentStatus.isEmpty()) {
                g2.setFont(FONT_MICRO);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(currentStatus);
                int pillW = Math.min(getWidth() - 14, textW + 22);
                int pillH = 22;
                int px = (getWidth() - pillW) / 2;
                int py = (getHeight() - pillH) / 2;

                g2.setColor(pillBg);
                g2.fillRoundRect(px, py, pillW, pillH, 11, 11);

                g2.setColor(pillBorder);
                g2.drawRoundRect(px, py, pillW, pillH, 11, 11);

                g2.setColor(pillFg);
                int tx = px + (pillW - textW) / 2;
                int ty = py + ((pillH - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(currentStatus, tx, ty);
            }

            g2.dispose();
        }
    }
}