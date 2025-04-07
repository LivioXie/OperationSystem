package OperationSystem;
/**
 * Process class - Represents a running program
 */
public class Process {
    private Runnable program;
    private Thread thread;
    private boolean started;
    
    // Memory management
    public static final int PAGE_SIZE = 1024; // 1KB pages
    private static final int MEMORY_SIZE = 1024 * 1024; // 1MB total memory
    private static byte[] memory = new byte[MEMORY_SIZE];
    
    // Translation Lookaside Buffer (TLB)
    private static final int TLB_SIZE = 2;
    private static int[][] tlb = new int[TLB_SIZE][2]; // [entry][0=virtual, 1=physical]
    
    static {
        // Initialize TLB with invalid entries
        clearTLB();
    }
    
    public Process(Runnable program) {
        this.program = program;
        this.started = false;
    }
    
    public boolean isStarted() {
        return started;
    }
    
    /**
     * Gets the program associated with this process
     * @return The program
     */
    public Runnable getProgram() {
        return program;
    }
    
    /**
     * Starts the process
     */
    public void start() {
        if (!started) {
            thread = new Thread(program);
            thread.start();
            started = true;
        }
    }
    
    /**
     * Resumes the process
     * Note: In a real cooperative multitasking system, this would be more complex
     * For this simulation, we're simplifying by using Java threads
     */
    public void resume() {
        // In a real cooperative system, we would need to save and restore state
        // For this simulation, Java's thread scheduler handles this for us
    }
    
    /**
     * Clears the TLB (called during context switch)
     */
    public static void clearTLB() {
        for (int i = 0; i < TLB_SIZE; i++) {
            tlb[i][0] = -1; // Invalid virtual page
            tlb[i][1] = -1; // Invalid physical page
        }
    }
    
    /**
     * Updates the TLB with a new mapping
     * @param virtualPage Virtual page number
     * @param physicalPage Physical page number
     */
    public static void updateTLB(int virtualPage, int physicalPage) {
        // Simple random replacement policy
        int index = (int)(Math.random() * TLB_SIZE);
        tlb[index][0] = virtualPage;
        tlb[index][1] = physicalPage;
    }
    
    /**
     * Checks if a virtual page is in the TLB
     * @param virtualPage Virtual page number to check
     * @return Physical page number, or -1 if not in TLB
     */
    private static int checkTLB(int virtualPage) {
        for (int i = 0; i < TLB_SIZE; i++) {
            if (tlb[i][0] == virtualPage) {
                return tlb[i][1];
            }
        }
        return -1; // Not found in TLB
    }
    
    /**
     * Reads a byte from memory
     * @param virtualAddress Virtual address to read from
     * @return The byte at that address
     * @throws MemoryAccessException If the memory access is invalid
     */
    public byte read(int virtualAddress) throws MemoryAccessException {
        if (virtualAddress < 0) {
            throw new MemoryAccessException("Invalid virtual address: " + virtualAddress);
        }
        
        // Calculate virtual page and offset
        int virtualPage = virtualAddress / PAGE_SIZE;
        int offset = virtualAddress % PAGE_SIZE;
        
        // Check TLB for the mapping
        int physicalPage = checkTLB(virtualPage);
        
        // If not in TLB, get mapping from kernel
        if (physicalPage == -1) {
            physicalPage = OS.getMapping(virtualPage);
            if (physicalPage == -1) {
                throw new MemoryAccessException("Memory access violation at address: " + virtualAddress);
            }
        }
        
        // Calculate physical address
        int physicalAddress = physicalPage * PAGE_SIZE + offset;
        
        // Read from memory
        return memory[physicalAddress];
    }
    
    /**
     * Writes a byte to memory
     * @param virtualAddress Virtual address to write to
     * @param value The byte to write
     * @throws MemoryAccessException If the memory access is invalid
     */
    public void write(int virtualAddress, byte value) throws MemoryAccessException {
        if (virtualAddress < 0) {
            throw new MemoryAccessException("Invalid virtual address: " + virtualAddress);
        }
        
        // Calculate virtual page and offset
        int virtualPage = virtualAddress / PAGE_SIZE;
        int offset = virtualAddress % PAGE_SIZE;
        
        // Check TLB for the mapping
        int physicalPage = checkTLB(virtualPage);
        
        // If not in TLB, get mapping from kernel
        if (physicalPage == -1) {
            physicalPage = OS.getMapping(virtualPage);
            if (physicalPage == -1) {
                throw new MemoryAccessException("Memory access violation at address: " + virtualAddress);
            }
        }
        
        // Calculate physical address
        int physicalAddress = physicalPage * PAGE_SIZE + offset;
        
        // Write to memory
        memory[physicalAddress] = value;
    }
    
    /**
     * Gets the shared memory array (for testing purposes)
     * @return The memory array
     */
    public static byte[] getMemory() {
        return memory;
    }
}