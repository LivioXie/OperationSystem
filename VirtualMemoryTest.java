package OperationSystem;

/**
 * VirtualMemoryTest - Demonstrates virtual memory functionality
 */
public class VirtualMemoryTest implements Runnable {
    @Override
    public void run() {
        System.out.println("VirtualMemoryTest: Starting virtual memory test...");
        
        try {
            // Allocate a lot of memory to force swapping
            int totalPages = 50; // 50KB total
            int[] addresses = new int[totalPages];
            
            System.out.println("VirtualMemoryTest: Allocating " + totalPages + " pages of memory...");
            
            // Allocate memory in 1KB chunks
            for (int i = 0; i < totalPages; i++) {
                addresses[i] = OS.allocateMemory(1024);
                if (addresses[i] == -1) {
                    System.out.println("VirtualMemoryTest: Failed to allocate page " + i);
                    break;
                }
                System.out.println("VirtualMemoryTest: Allocated page " + i + " at address " + addresses[i]);
            }
            
            // Write to each page
            Process process = new Process(this);
            for (int i = 0; i < totalPages; i++) {
                if (addresses[i] != -1) {
                    // Write a unique value to each page
                    for (int j = 0; j < 10; j++) {
                        process.write(addresses[i] + j, (byte)(i * 10 + j));
                    }
                    System.out.println("VirtualMemoryTest: Wrote data to page " + i);
                }
            }
            
            // Read from each page to verify
            for (int i = 0; i < totalPages; i++) {
                if (addresses[i] != -1) {
                    System.out.println("VirtualMemoryTest: Reading from page " + i);
                    for (int j = 0; j < 10; j++) {
                        byte value = process.read(addresses[i] + j);
                        byte expected = (byte)(i * 10 + j);
                        if (value != expected) {
                            System.out.println("VirtualMemoryTest: ERROR - Page " + i + 
                                              " offset " + j + " expected " + expected + 
                                              " but got " + value);
                        }
                    }
                }
            }
            
            // Free half the pages
            System.out.println("VirtualMemoryTest: Freeing half the pages...");
            for (int i = 0; i < totalPages / 2; i++) {
                if (addresses[i] != -1) {
                    boolean freed = OS.freeMemory(addresses[i], 1024);
                    System.out.println("VirtualMemoryTest: Freed page " + i + ": " + freed);
                }
            }
            
            // Allocate some more pages
            System.out.println("VirtualMemoryTest: Allocating more pages...");
            int[] newAddresses = new int[totalPages / 2];
            for (int i = 0; i < totalPages / 2; i++) {
                newAddresses[i] = OS.allocateMemory(1024);
                if (newAddresses[i] == -1) {
                    System.out.println("VirtualMemoryTest: Failed to allocate new page " + i);
                    break;
                }
                System.out.println("VirtualMemoryTest: Allocated new page " + i + " at address " + newAddresses[i]);
                
                // Write to the new page
                for (int j = 0; j < 10; j++) {
                    process.write(newAddresses[i] + j, (byte)(100 + i * 10 + j));
                }
            }
            
            // Read from remaining original pages
            System.out.println("VirtualMemoryTest: Reading from remaining original pages...");
            for (int i = totalPages / 2; i < totalPages; i++) {
                if (addresses[i] != -1) {
                    for (int j = 0; j < 10; j++) {
                        byte value = process.read(addresses[i] + j);
                        byte expected = (byte)(i * 10 + j);
                        if (value != expected) {
                            System.out.println("VirtualMemoryTest: ERROR - Page " + i + 
                                              " offset " + j + " expected " + expected + 
                                              " but got " + value);
                        }
                    }
                }
            }
            
            // Read from new pages
            System.out.println("VirtualMemoryTest: Reading from new pages...");
            for (int i = 0; i < totalPages / 2; i++) {
                if (newAddresses[i] != -1) {
                    for (int j = 0; j < 10; j++) {
                        byte value = process.read(newAddresses[i] + j);
                        byte expected = (byte)(100 + i * 10 + j);
                        if (value != expected) {
                            System.out.println("VirtualMemoryTest: ERROR - New page " + i + 
                                              " offset " + j + " expected " + expected + 
                                              " but got " + value);
                        }
                    }
                }
            }
            
            // Free all memory
            System.out.println("VirtualMemoryTest: Freeing all memory...");
            for (int i = totalPages / 2; i < totalPages; i++) {
                if (addresses[i] != -1) {
                    OS.freeMemory(addresses[i], 1024);
                }
            }
            for (int i = 0; i < totalPages / 2; i++) {
                if (newAddresses[i] != -1) {
                    OS.freeMemory(newAddresses[i], 1024);
                }
            }
            
        } catch (MemoryAccessException e) {
            System.out.println("VirtualMemoryTest: Exception: " + e.getMessage());
        }
        
        System.out.println("VirtualMemoryTest: Virtual memory test complete");
    }
}