package OperationSystem;

/**
 * MemoryTest - Demonstrates memory management functionality
 */
public class MemoryTest implements Runnable {
    @Override
    public void run() {
        System.out.println("MemoryTest: Starting memory test...");
        
        try {
            // Allocate memory
            int address1 = OS.allocateMemory(1024); // 1 page
            if (address1 == -1) {
                System.out.println("MemoryTest: Failed to allocate memory");
                return;
            }
            System.out.println("MemoryTest: Allocated 1024 bytes at virtual address " + address1);
            
            // Write to memory
            Process process = new Process(this);
            for (int i = 0; i < 10; i++) {
                process.write(address1 + i, (byte)i);
                System.out.println("MemoryTest: Wrote value " + i + " to address " + (address1 + i));
            }
            
            // Read from memory
            for (int i = 0; i < 10; i++) {
                byte value = process.read(address1 + i);
                System.out.println("MemoryTest: Read value " + value + " from address " + (address1 + i));
                if (value != (byte)i) {
                    System.out.println("MemoryTest: ERROR - Expected " + i + " but got " + value);
                }
            }
            
            // Allocate more memory
            int address2 = OS.allocateMemory(2048); // 2 pages
            if (address2 == -1) {
                System.out.println("MemoryTest: Failed to allocate more memory");
            } else {
                System.out.println("MemoryTest: Allocated 2048 bytes at virtual address " + address2);
                
                // Write to second allocation
                for (int i = 0; i < 10; i++) {
                    process.write(address2 + i, (byte)(100 + i));
                }
                
                // Read from both allocations to verify they don't interfere
                for (int i = 0; i < 10; i++) {
                    byte value1 = process.read(address1 + i);
                    byte value2 = process.read(address2 + i);
                    System.out.println("MemoryTest: address1[" + i + "] = " + value1 + 
                                      ", address2[" + i + "] = " + value2);
                }
                
                // Free the second allocation
                boolean freed = OS.freeMemory(address2, 2048);
                System.out.println("MemoryTest: Freed second allocation: " + freed);
            }
            
            // Try to access memory outside our allocation (should throw exception)
            try {
                process.read(address1 + 2000);
                System.out.println("MemoryTest: ERROR - Should not be able to read outside allocation");
            } catch (MemoryAccessException e) {
                System.out.println("MemoryTest: Correctly caught exception: " + e.getMessage());
            }
            
            // Free the first allocation
            boolean freed = OS.freeMemory(address1, 1024);
            System.out.println("MemoryTest: Freed first allocation: " + freed);
            
            // Try to access freed memory (should throw exception)
            try {
                process.read(address1);
                System.out.println("MemoryTest: ERROR - Should not be able to read freed memory");
            } catch (MemoryAccessException e) {
                System.out.println("MemoryTest: Correctly caught exception: " + e.getMessage());
            }
            
        } catch (MemoryAccessException e) {
            System.out.println("MemoryTest: Exception: " + e.getMessage());
        }
        
        System.out.println("MemoryTest: Memory test complete");
    }
}