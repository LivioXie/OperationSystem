package OperationSystem;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Process Control Block - Kernel's representation of a process
 */
public class PCB {
    private static int nextPid = 0;
    private int pid;
    private Process process;
    private Priority priority;
    private Instant wakeTime;
    private int consecutiveTimeouts;
    private String name;
    private Queue<KernelMessage> messageQueue;
    
    // Memory management
    public static final int MAX_VIRTUAL_PAGES = 100;
    private VirtualToPhysicalMapping[] pageTable; // Maps virtual page to physical page or disk
    
    public PCB(Process process, Priority priority) {
        this.pid = nextPid++;
        this.process = process;
        this.priority = priority;
        this.wakeTime = null;
        this.consecutiveTimeouts = 0;
        this.name = process.getProgram().getClass().getSimpleName();
        this.messageQueue = new LinkedList<>();
        
        // Initialize page table with no mappings
        this.pageTable = new VirtualToPhysicalMapping[MAX_VIRTUAL_PAGES];
        // Note: We're leaving entries as null to indicate unmapped pages
    }
    
    public int getPid() {
        return pid;
    }
    
    public String getName() {
        return name;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void incrementTimeouts() {
        consecutiveTimeouts++;
        if (consecutiveTimeouts > 5) {
            downgrade();
            consecutiveTimeouts = 0;
        }
    }
    
    private void downgrade() {
        if (priority == Priority.REALTIME) {
            priority = Priority.INTERACTIVE;
        } else if (priority == Priority.INTERACTIVE) {
            priority = Priority.BACKGROUND;
        }
    }
    
    public void sleep(int milliseconds) {
        wakeTime = Clock.systemUTC().instant().plusMillis(milliseconds);
    }
    
    public boolean shouldWake() {
        return wakeTime != null && !Clock.systemUTC().instant().isBefore(wakeTime);
    }
    
    public void clearWakeTime() {
        wakeTime = null;
    }
    
    public void run() {
        process.start();
    }
    
    public void stop() {
        // In a real implementation, we would stop the process here
        // For this simulation, we're relying on Java's thread management
        try {
            while (!process.isStarted()) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public boolean isDone() {
        // In a real implementation, we would check if the process is done
        // For this simulation, we're simplifying
        return false;
    }
    
    public void resume() {
        process.resume();
    }
    
    public boolean isStarted() {
        return process.isStarted();
    }
    
    /**
     * Adds a message to this process's message queue
     * @param message The message to add
     */
    public void addMessage(KernelMessage message) {
        messageQueue.add(message);
    }
    
    /**
     * Checks if this process has any messages
     * @return true if there are messages, false otherwise
     */
    public boolean hasMessages() {
        return !messageQueue.isEmpty();
    }
    
    /**
     * Gets the next message from the queue
     * @return The next message, or null if there are none
     */
    public KernelMessage getNextMessage() {
        return messageQueue.poll();
    }
    
    /**
     * Gets the physical page for a virtual page
     * @param virtualPage The virtual page number
     * @return The physical page number, or -1 if not mapped or not in memory
     */
    public int getPhysicalPage(int virtualPage) {
        if (virtualPage >= 0 && virtualPage < MAX_VIRTUAL_PAGES && pageTable[virtualPage] != null) {
            return pageTable[virtualPage].physicalPageNumber;
        }
        return -1;
    }
    
    /**
     * Gets the disk page for a virtual page
     * @param virtualPage The virtual page number
     * @return The disk page number, or -1 if not mapped or not on disk
     */
    public int getDiskPage(int virtualPage) {
        if (virtualPage >= 0 && virtualPage < MAX_VIRTUAL_PAGES && pageTable[virtualPage] != null) {
            return pageTable[virtualPage].diskPageNumber;
        }
        return -1;
    }
    
    /**
     * Maps a virtual page to a physical page
     * @param virtualPage The virtual page number
     * @param physicalPage The physical page number
     * @return true if successful, false otherwise
     */
    public boolean mapPage(int virtualPage, int physicalPage) {
        if (virtualPage >= 0 && virtualPage < MAX_VIRTUAL_PAGES) {
            if (pageTable[virtualPage] == null) {
                pageTable[virtualPage] = new VirtualToPhysicalMapping();
            }
            pageTable[virtualPage].physicalPageNumber = physicalPage;
            return true;
        }
        return false;
    }
    
    /**
     * Maps a virtual page to a disk page
     * @param virtualPage The virtual page number
     * @param diskPage The disk page number
     * @return true if successful, false otherwise
     */
    public boolean mapToDisk(int virtualPage, int diskPage) {
        if (virtualPage >= 0 && virtualPage < MAX_VIRTUAL_PAGES) {
            if (pageTable[virtualPage] == null) {
                pageTable[virtualPage] = new VirtualToPhysicalMapping();
            }
            pageTable[virtualPage].diskPageNumber = diskPage;
            return true;
        }
        return false;
    }
    
    /**
     * Gets all allocated physical pages
     * @return Array of physical pages that are mapped
     */
    public int[] getAllocatedPhysicalPages() {
        int count = 0;
        for (int i = 0; i < MAX_VIRTUAL_PAGES; i++) {
            if (pageTable[i] != null && pageTable[i].physicalPageNumber != -1) {
                count++;
            }
        }
        
        int[] pages = new int[count];
        int index = 0;
        for (int i = 0; i < MAX_VIRTUAL_PAGES; i++) {
            if (pageTable[i] != null && pageTable[i].physicalPageNumber != -1) {
                pages[index++] = pageTable[i].physicalPageNumber;
            }
        }
        
        return pages;
    }
    
    /**
     * Unmaps a virtual page
     * @param virtualPage The virtual page to unmap
     */
    public void unmapPage(int virtualPage) {
        if (virtualPage >= 0 && virtualPage < MAX_VIRTUAL_PAGES) {
            pageTable[virtualPage] = null;
        }
    }
    
    /**
     * Finds a contiguous block of unmapped virtual pages
     * @param numPages Number of pages needed
     * @return Starting virtual page number, or -1 if not available
     */
    public int findFreeVirtualPages(int numPages) {
        if (numPages <= 0) {
            return -1;
        }
        
        int consecutiveFree = 0;
        int startPage = -1;
        
        for (int i = 0; i < MAX_VIRTUAL_PAGES; i++) {
            if (pageTable[i] == null) {
                if (consecutiveFree == 0) {
                    startPage = i;
                }
                consecutiveFree++;
                
                if (consecutiveFree >= numPages) {
                    return startPage;
                }
            } else {
                consecutiveFree = 0;
                startPage = -1;
            }
        }
        
        return -1; // Not enough contiguous virtual pages
    }
    
    /**
     * Gets the mapping for a virtual page
     * @param virtualPage The virtual page number
     * @return The mapping, or null if not mapped
     */
    public VirtualToPhysicalMapping getMapping(int virtualPage) {
        if (virtualPage >= 0 && virtualPage < MAX_VIRTUAL_PAGES) {
            return pageTable[virtualPage];
        }
        return null;
    }
}