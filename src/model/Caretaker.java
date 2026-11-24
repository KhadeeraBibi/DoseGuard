package model;

import java.io.Serializable;
import java.util.Arrays;

public class Caretaker implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String fullName;
    private final String username;
    private final byte[] salt;
    private final byte[] passwordHash;

    public Caretaker(String fullName, String username, byte[] salt, byte[] passwordHash) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (salt == null || salt.length == 0 || passwordHash == null || passwordHash.length == 0) {
            throw new IllegalArgumentException("Valid password credentials are required.");
        }
        this.fullName = fullName.trim();
        this.username = username.trim();
        this.salt = Arrays.copyOf(salt, salt.length);
        this.passwordHash = Arrays.copyOf(passwordHash, passwordHash.length);
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return Arrays.copyOf(salt, salt.length);
    }

    public byte[] getPasswordHash() {
        return Arrays.copyOf(passwordHash, passwordHash.length);
    }
}