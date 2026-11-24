package model;

public class InjectionMedicine extends MedicineItem {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LOW_THRESHOLD = 5;

    public InjectionMedicine(String medicineId, String name, int quantity) {
        super(medicineId, name, quantity, DEFAULT_LOW_THRESHOLD);
    }

    public InjectionMedicine(String medicineId, String name, int quantity, int lowStockThreshold) {
        super(medicineId, name, quantity, lowStockThreshold);
    }

    @Override
    public String getItemType() {
        return "Injection";
    }

    @Override
    public String getUnit() {
        return "vials";
    }

    @Override
    public String getQuantityDisplay() {
        int qty = getQuantity();
        return qty + (qty == 1 ? " vial" : " vials");
    }
}