package OperationSystem;

/**
 * Main class - Entry point for the OS simulation
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting OS simulation...");
        
        // Create and run the virtual memory test
        OS.createProcess(new VirtualMemoryTest());
        
        // Let it run for a while
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("OS simulation complete.");
    }
}