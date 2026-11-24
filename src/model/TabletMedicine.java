package model;

public class TabletMedicine extends MedicineItem {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LOW_THRESHOLD = 10;

    public TabletMedicine(String medicineId, String name, int quantity) {
        super(medicineId, name, quantity, DEFAULT_LOW_THRESHOLD);
    }

    public TabletMedicine(String medicineId, String name, int quantity, int lowStockThreshold) {
        super(medicineId, name, quantity, lowStockThreshold);
    }

    @Override
    public String getItemType() {
        return "Tablet";
    }

    @Override
    public String getUnit() {
        return "tablets";
    }

    @Override
    public String getQuantityDisplay() {
        int qty = getQuantity();
        return qty + (qty == 1 ? " tablet" : " tablets");
    }
}