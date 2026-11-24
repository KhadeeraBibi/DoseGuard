import service.AuthService;
import service.DoseGuardService;
import ui.MainFrame;
import ui.UITheme;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back cleanly to default Java Look and Feel
        }

        SwingUtilities.invokeLater(() -> {
            DoseGuardService service = new DoseGuardService();
            AuthService authService = new AuthService();

            try {
                service.loadData();
            } catch (Exception ex) {
                UITheme.showWarning(
                        null,
                        "Data File Could Not Be Loaded",
                        "DoseGuard could not read the existing care data file (" + service.getDataFilePath() + ").\n"
                                + "Reason: " + ex.getMessage() + "\n\n"
                                + "The application will open in read-only storage mode so your existing file is not overwritten.\n"
                                + "Move or repair that file and restart DoseGuard to re-enable saving."
                );
            }

            try {
                authService.loadData();
            } catch (Exception ex) {
                UITheme.showWarning(
                        null,
                        "Accounts File Could Not Be Loaded",
                        "DoseGuard could not read the caretaker accounts file (" + authService.getAccountsFilePath() + ").\n"
                                + "Reason: " + ex.getMessage() + "\n\n"
                                + "Account saving is paused so your existing accounts file is not overwritten.\n"
                                + "Move or repair that file and restart DoseGuard to re-enable registration."
                );
            }

            MainFrame frame = new MainFrame(service, authService);
            frame.setVisible(true);
        });
    }
}