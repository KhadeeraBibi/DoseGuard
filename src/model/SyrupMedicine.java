package model;

public class SyrupMedicine extends MedicineItem {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LOW_THRESHOLD = 50;

    public SyrupMedicine(String medicineId, String name, int quantity) {
        super(medicineId, name, quantity, DEFAULT_LOW_THRESHOLD);
    }

    public SyrupMedicine(String medicineId, String name, int quantity, int lowStockThreshold) {
        super(medicineId, name, quantity, lowStockThreshold);
    }

    @Override
    public String getItemType() {
        return "Syrup";
    }

    @Override
    public String getUnit() {
        return "ml";
    }

    @Override
    public String getQuantityDisplay() {
        return getQuantity() + " ml";
    }
}