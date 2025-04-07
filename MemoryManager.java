package OperationSystem;

import java.util.Random;

/**
 * MemoryManager - Handles physical memory allocation
 */
public class MemoryManager {
    private static final int PAGE_SIZE = 1024; // 1KB pages
    private static final int TOTAL_PAGES = 1024; // 1MB total memory
    
    private boolean[] pageInUse; // Tracks which physical pages are in use
    private Random random;
    
    public MemoryManager() {
        pageInUse = new boolean[TOTAL_PAGES];
        random = new Random();
    }
    
    /**
     * Allocates the specified number of physical pages
     * @param numPages Number of pages to allocate
     * @return Array of allocated physical page numbers, or null if not enough memory
     */
    public int[] allocatePages(int numPages) {
        if (numPages <= 0) {
            return new int[0];
        }
        
        // Count available pages
        int availablePages = 0;
        for (int i = 0; i < TOTAL_PAGES; i++) {
            if (!pageInUse[i]) {
                availablePages++;
            }
        }
        
        // Check if we have enough memory
        if (availablePages < numPages) {
            return null; // Not enough memory
        }
        
        // Allocate pages
        int[] allocatedPages = new int[numPages];
        int count = 0;
        
        for (int i = 0; i < TOTAL_PAGES && count < numPages; i++) {
            if (!pageInUse[i]) {
                pageInUse[i] = true;
                allocatedPages[count++] = i;
            }
        }
        
        return allocatedPages;
    }
    
    /**
     * Frees the specified physical pages
     * @param pages Array of physical page numbers to free
     */
    public void freePages(int[] pages) {
        if (pages == null) {
            return;
        }
        
        for (int page : pages) {
            if (page >= 0 && page < TOTAL_PAGES) {
                pageInUse[page] = false;
            }
        }
    }
    
    /**
     * Gets a random free physical page
     * @return A free physical page number, or -1 if none available
     */
    public int getRandomFreePage() {
        // Count free pages
        int freePages = 0;
        for (int i = 0; i < TOTAL_PAGES; i++) {
            if (!pageInUse[i]) {
                freePages++;
            }
        }
        
        if (freePages == 0) {
            return -1;
        }
        
        // Select a random free page
        int target = random.nextInt(freePages);
        int count = 0;
        
        for (int i = 0; i < TOTAL_PAGES; i++) {
            if (!pageInUse[i]) {
                if (count == target) {
                    pageInUse[i] = true;
                    return i;
                }
                count++;
            }
        }
        
        return -1; // Should never reach here
    }
}