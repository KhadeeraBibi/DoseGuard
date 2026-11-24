package model;

import java.io.Serializable;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String patientId;
    private String name;
    private int age;
    private String roomNumber;

    public Patient(String patientId, String name, int age, String roomNumber) {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be empty.");
        }
        this.patientId = patientId.trim();
        setName(name);
        setAge(age);
        setRoomNumber(roomNumber);
    }

    public String getPatientId() {
        return patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name is required.");
        }
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 1 || age > 120) {
            throw new IllegalArgumentException("Patient age must be between 1 and 120.");
        }
        this.age = age;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        this.roomNumber = roomNumber.trim();
    }

    @Override
    public String toString() {
        return name + " (" + patientId + " - Room " + roomNumber + ")";
    }
}