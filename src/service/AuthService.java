package service;

import model.Caretaker;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthService implements DataManager {
    private static final String DEFAULT_ACCOUNTS_PATH = "data" + File.separator + "caretakers.dat";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int MIN_PASSWORD_LENGTH = 4;

    private final String accountsFilePath;
    private final List<Caretaker> caretakers = new ArrayList<>();
    private final SecureRandom secureRandom = new SecureRandom();

    private boolean dataLoadFailed = false;
    private String lastLoadError = null;

    public AuthService() {
        this(DEFAULT_ACCOUNTS_PATH);
    }

    public AuthService(String accountsFilePath) {
        this.accountsFilePath = accountsFilePath;
    }

    public String getAccountsFilePath() {
        return accountsFilePath;
    }

    public boolean hasRegisteredCaretakers() {
        return !caretakers.isEmpty();
    }

    public boolean isUsernameTaken(String username) {
        if (username == null) {
            return false;
        }
        String target = username.trim();
        for (Caretaker existing : caretakers) {
            if (existing.getUsername().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    public Caretaker registerCaretaker(String fullName, String username,
                                       char[] password, char[] confirmPassword) throws IOException {
        try {
            String cleanName = fullName != null ? fullName.trim() : "";
            String cleanUser = username != null ? username.trim() : "";

            if (cleanName.isEmpty() || cleanUser.isEmpty()
                    || password == null || password.length == 0
                    || confirmPassword == null || confirmPassword.length == 0) {
                throw new IllegalArgumentException("All fields are required.");
            }
            if (cleanUser.length() < 3) {
                throw new IllegalArgumentException("Username must be at least 3 characters long.");
            }
            if (containsWhitespace(cleanUser)) {
                throw new IllegalArgumentException("Username cannot contain spaces.");
            }
            if (isUsernameTaken(cleanUser)) {
                throw new IllegalArgumentException(
                        "Username \"" + cleanUser + "\" is already registered. Please choose another.");
            }
            if (password.length < MIN_PASSWORD_LENGTH) {
                throw new IllegalArgumentException(
                        "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            }
            if (!Arrays.equals(password, confirmPassword)) {
                throw new IllegalArgumentException("Password and Confirm Password do not match.");
            }

            byte[] salt = generateSalt();
            byte[] hash = hashPassword(password, salt);

            Caretaker caretaker = new Caretaker(cleanName, cleanUser, salt, hash);
            caretakers.add(caretaker);
            try {
                saveData();
            } catch (IOException ex) {
                caretakers.remove(caretaker);
                throw ex;
            }
            return caretaker;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
            if (confirmPassword != null) {
                Arrays.fill(confirmPassword, '\0');
            }
        }
    }

    public Caretaker authenticate(String username, char[] password) {
        try {
            if (username == null || username.trim().isEmpty()
                    || password == null || password.length == 0) {
                return null;
            }
            String cleanUser = username.trim();
            for (Caretaker caretaker : caretakers) {
                if (caretaker.getUsername().equalsIgnoreCase(cleanUser)) {
                    byte[] candidateHash = hashPassword(password, caretaker.getSalt());
                    if (MessageDigest.isEqual(candidateHash, caretaker.getPasswordHash())) {
                        return caretaker;
                    }
                    return null;
                }
            }
            return null;
        } finally {
            if (password != null) {
                Arrays.fill(password, '\0');
            }
        }
    }

    private boolean containsWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }

    private byte[] hashPassword(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing algorithm unavailable: " + ex.getMessage(), ex);
        } finally {
            spec.clearPassword();
        }
    }

    // ==================== Persistence (DataManager) ====================

    @Override
    public boolean loadData() throws IOException, ClassNotFoundException {
        File file = new File(accountsFilePath);
        if (!file.exists()) {
            dataLoadFailed = false;
            lastLoadError = null;
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object raw = ois.readObject();
            if (!(raw instanceof CaretakerSnapshot)) {
                throw new IOException("Unrecognized account file format in " + accountsFilePath);
            }
            CaretakerSnapshot snapshot = (CaretakerSnapshot) raw;
            caretakers.clear();
            if (snapshot.caretakers != null) {
                caretakers.addAll(snapshot.caretakers);
            }
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
            throw new IOException("Corrupted account file: " + lastLoadError, ex);
        }
    }

    @Override
    public void saveData() throws IOException {
        if (dataLoadFailed) {
            throw new IOException(
                    "Account saving is paused to avoid overwriting the unreadable accounts file ("
                            + accountsFilePath + "). Please repair or move that file and restart.");
        }

        File targetFile = new File(accountsFilePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Could not create data directory: " + parentDir.getAbsolutePath());
        }

        CaretakerSnapshot snapshot = new CaretakerSnapshot(new ArrayList<>(caretakers));

        File tempFile = new File(accountsFilePath + ".tmp");
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

    private static class CaretakerSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        final List<Caretaker> caretakers;

        CaretakerSnapshot(List<Caretaker> caretakers) {
            this.caretakers = caretakers;
        }
    }
}