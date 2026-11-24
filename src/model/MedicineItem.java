package model;

import java.io.Serializable;

public abstract class MedicineItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_LOW_STOCK = "Low Stock";
    public static final String STATUS_OUT_OF_STOCK = "Out of Stock";

    private final String medicineId;
    private String name;
    private int quantity;
    private int lowStockThreshold;

    protected MedicineItem(String medicineId, String name, int quantity, int lowStockThreshold) {
        if (medicineId == null || medicineId.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine ID cannot be empty.");
        }
        if (lowStockThreshold <= 0) {
            throw new IllegalArgumentException("Low stock threshold must be greater than zero.");
        }
        this.medicineId = medicineId.trim();
        setName(name);
        setQuantity(quantity);
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name is required.");
        }
        this.name = name.trim();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    public void adjustQuantity(int delta) {
        int updated = this.quantity + delta;
        if (updated < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock: only " + getQuantityDisplay() + " available.");
        }
        this.quantity = updated;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public String getStockStatus() {
        if (quantity == 0) {
            return STATUS_OUT_OF_STOCK;
        }
        if (quantity <= lowStockThreshold) {
            return STATUS_LOW_STOCK;
        }
        return STATUS_AVAILABLE;
    }

    public abstract String getItemType();

    public abstract String getUnit();

    public abstract String getQuantityDisplay();
}
