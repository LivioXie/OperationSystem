package OperationSystem;

/**
 * VirtualToPhysicalMapping - Maps virtual pages to physical pages or disk locations
 */
public class VirtualToPhysicalMapping {
    public int physicalPageNumber; // Physical page number, -1 if not in memory
    public int diskPageNumber;     // Page number in swap file, -1 if not on disk
    
    /**
     * Creates a new mapping with no physical or disk page assigned
     */
    public VirtualToPhysicalMapping() {
        this.physicalPageNumber = -1;
        this.diskPageNumber = -1;
    }
    
    /**
     * Checks if this mapping has a physical page assigned
     * @return true if a physical page is assigned
     */
    public boolean isInMemory() {
        return physicalPageNumber != -1;
    }
    
    /**
     * Checks if this mapping has a disk page assigned
     * @return true if a disk page is assigned
     */
    public boolean isOnDisk() {
        return diskPageNumber != -1;
    }
}