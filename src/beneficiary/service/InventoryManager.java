package beneficiary.service;

import beneficiary.model.Resource;
import beneficiary.repository.BeneficiaryRepository.Data;

/** Singleton: one stateless application inventory coordinator. Durable quantities live in Data. */
public final class InventoryManager {
    private static final InventoryManager INSTANCE = new InventoryManager();
    private InventoryManager() { }
    public static InventoryManager getInstance() { return INSTANCE; }

    public boolean isAvailable(Data data, int resourceId, int quantity) {
        Resource r=data.resources.get(resourceId);
        return quantity>0 && r!=null && r.getStock()>=quantity;
    }
    public void deduct(Data data, int resourceId, int quantity) {
        Resource r=get(data,resourceId);
        if (quantity<=0) throw new IllegalArgumentException("Quantity must be positive");
        if (r.getStock()<quantity) throw new IllegalArgumentException("Insufficient stock: only " + r.getStock() + " available");
        data.resources.put(resourceId,new Resource(r.getId(),r.getName(),r.getCategory(),r.getStock()-quantity));
    }
    public void replenish(Data data,int resourceId,int quantity) {
        Resource r=get(data,resourceId);
        if (quantity<=0) throw new IllegalArgumentException("Quantity must be positive");
        int total=Math.addExact(r.getStock(),quantity);
        data.resources.put(resourceId,new Resource(r.getId(),r.getName(),r.getCategory(),total));
    }
    private Resource get(Data data,int id) {
        Resource r=data.resources.get(id);
        if (r==null) throw new IllegalArgumentException("Unknown resource #" + id);
        return r;
    }
}
