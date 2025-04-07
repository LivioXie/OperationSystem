package OperationSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.Clock;

/**
 * Kernel class - Core operating system functionality
 * Handles task scheduling and process management
 */
public class Kernel {
    private List<PCB> realtimeProcesses;
    private List<PCB> interactiveProcesses;
    private List<PCB> backgroundProcesses;
    private List<PCB> sleepingProcesses;
    private Map<Integer, PCB> processMap; // PID -> PCB mapping
    private Map<Integer, PCB> waitingForMessageProcesses; // Processes waiting for messages
    private PCB currentProcess;
    private Random random;
    private VFS vfs;
    private MemoryManager memoryManager;
    private int swapFileId;
    private int nextSwapPage;

    public Kernel() {
        realtimeProcesses = new ArrayList<>();
        interactiveProcesses = new ArrayList<>();
        backgroundProcesses = new ArrayList<>();
        sleepingProcesses = new ArrayList<>();
        processMap = new HashMap<>();
        waitingForMessageProcesses = new HashMap<>();
        currentProcess = null;
        random = new Random();
        vfs = new VFS();
        memoryManager = new MemoryManager();
        
        // Open swap file
        swapFileId = vfs.Open("pagefile.sys");
        if (swapFileId == -1) {
            // If the file doesn't exist, create it
            int tempId = vfs.Open("pagefile.sys:create");
            if (tempId != -1) {
                vfs.Close(tempId);
                swapFileId = vfs.Open("pagefile.sys");
            }
        }
        nextSwapPage = 0;
    }
    
    /**
     * Creates a new process with default priority (INTERACTIVE)
     * @param program The program to run
     * @return Process ID of the created process
     */
    public int createProcess(Runnable program) {
        return createProcess(program, Priority.INTERACTIVE);
    }
    
    /**
     * Creates a new process with specified priority
     * @param program The program to run
     * @param priority The priority level for the process
     * @return Process ID of the created process
     */
    public int createProcess(Runnable program, Priority priority) {
        Process process = new Process(program);
        PCB pcb = new PCB(process, priority);
        int pid = pcb.getPid();
        
        // Add to appropriate queue based on priority
        switch (priority) {
            case REALTIME:
                realtimeProcesses.add(pcb);
                break;
            case INTERACTIVE:
                interactiveProcesses.add(pcb);
                break;
            case BACKGROUND:
                backgroundProcesses.add(pcb);
                break;
        }
        
        // Add to process map
        processMap.put(pid, pcb);
        
        // If this is the first process, make it the current one
        if (currentProcess == null) {
            currentProcess = pcb;
            pcb.run();
        }
        
        return pid;
    }
    
    /**
     * Gets the current process's PID
     * @return The current PID
     */
    public int getPid() {
        return currentProcess != null ? currentProcess.getPid() : -1;
    }
    
    /**
     * Gets a process's PID by name
     * @param name The process name to look for
     * @return The PID, or -1 if not found
     */
    public int getPidByName(String name) {
        for (PCB pcb : processMap.values()) {
            if (pcb.getName().equals(name)) {
                return pcb.getPid();
            }
        }
        return -1;
    }
    
    /**
     * Sends a message to another process
     * @param message The message to send
     */
    public void sendMessage(KernelMessage message) {
        // Create a copy of the message
        KernelMessage copy = new KernelMessage(message);
        
        // Set the sender PID
        copy.setSenderPid(getPid());
        
        // Find the target process
        PCB targetPCB = processMap.get(copy.getTargetPid());
        if (targetPCB != null) {
            // Add the message to the target's queue
            targetPCB.addMessage(copy);
            
            // If the target is waiting for a message, move it back to the runnable queue
            if (waitingForMessageProcesses.containsKey(copy.getTargetPid())) {
                PCB waitingPCB = waitingForMessageProcesses.remove(copy.getTargetPid());
                
                // Add back to appropriate queue based on priority
                switch (waitingPCB.getPriority()) {
                    case REALTIME:
                        realtimeProcesses.add(waitingPCB);
                        break;
                    case INTERACTIVE:
                        interactiveProcesses.add(waitingPCB);
                        break;
                    case BACKGROUND:
                        backgroundProcesses.add(waitingPCB);
                        break;
                }
            }
        }
    }
    
    /**
     * Waits for a message to arrive
     * @return The received message, or null if the process is terminated
     */
    public KernelMessage waitForMessage() {
        if (currentProcess == null) {
            return null;
        }
        
        // Check if there's already a message
        if (currentProcess.hasMessages()) {
            return currentProcess.getNextMessage();
        }
        
        // No message yet, so we need to wait
        int pid = currentProcess.getPid();
        
        // Move the current process to the waiting list
        waitingForMessageProcesses.put(pid, currentProcess);
        
        // Clear the current process
        currentProcess = null;
        
        // Switch to another task
        switchTask();
        
        // When we get back here, we should have a message
        PCB process = processMap.get(pid);
        if (process != null && process.hasMessages()) {
            return process.getNextMessage();
        }
        
        return null;
    }
    
    /**
     * Puts the current process to sleep for the specified time
     * @param milliseconds Time to sleep in milliseconds
     */
    public void sleep(int milliseconds) {
        if (currentProcess != null) {
            currentProcess.sleep(milliseconds);
            sleepingProcesses.add(currentProcess);
            currentProcess = null;
            switchTask();
        }
    }
    
    /**
     * Switches to the next task based on priority scheduling
     */
    public void switchTask() {
        // First, check if any sleeping processes should wake up
        checkSleepingProcesses();
        
        // Clear the TLB when switching tasks
        Process.clearTLB();
        
        // If there's no current process, select one
        if (currentProcess == null) {
            selectNextProcess();
        } else {
            // Otherwise, put the current process back in its queue
            switch (currentProcess.getPriority()) {
                case REALTIME:
                    realtimeProcesses.add(currentProcess);
                    break;
                case INTERACTIVE:
                    interactiveProcesses.add(currentProcess);
                    break;
                case BACKGROUND:
                    backgroundProcesses.add(currentProcess);
                    break;
            }
            
            // And select a new one
            currentProcess = null;
            selectNextProcess();
        }
    }
    
    /**
     * Checks if any sleeping processes should wake up
     */
    private void checkSleepingProcesses() {
        List<PCB> awakened = new ArrayList<>();
        
        for (PCB pcb : sleepingProcesses) {
            if (pcb.shouldWake()) {
                pcb.clearWakeTime();
                awakened.add(pcb);
                
                // Add back to appropriate queue
                switch (pcb.getPriority()) {
                    case REALTIME:
                        realtimeProcesses.add(pcb);
                        break;
                    case INTERACTIVE:
                        interactiveProcesses.add(pcb);
                        break;
                    case BACKGROUND:
                        backgroundProcesses.add(pcb);
                        break;
                }
            }
        }
        
        // Remove awakened processes from sleeping list
        sleepingProcesses.removeAll(awakened);
    }
    
    /**
     * Selects the next process to run based on priority
     */
    private void selectNextProcess() {
        // Try to get a realtime process first
        if (!realtimeProcesses.isEmpty()) {
            currentProcess = realtimeProcesses.remove(0);
            currentProcess.resume();
            return;
        }
        
        // Then try interactive
        if (!interactiveProcesses.isEmpty()) {
            // For interactive processes, we'll use a simple round-robin approach
            currentProcess = interactiveProcesses.remove(0);
            currentProcess.resume();
            return;
        }
        
        // Finally, try background
        if (!backgroundProcesses.isEmpty()) {
            currentProcess = backgroundProcesses.remove(0);
            currentProcess.resume();
            return;
        }
        
        // If we get here, there are no runnable processes
        // In a real OS, we would idle or halt
    }
    
    /**
     * Terminates the current process
     */
    public void terminateCurrentProcess() {
        if (currentProcess != null) {
            int pid = currentProcess.getPid();
            
            // Free all memory allocated to this process
            int[] physicalPages = currentProcess.getAllocatedPhysicalPages();
            memoryManager.freePages(physicalPages);
            
            // Remove from process map
            processMap.remove(pid);
            
            // Clear current process
            currentProcess = null;
            
            // Switch to another task
            switchTask();
        }
    }
    
    /**
     * Opens a device
     * @param path Path or identifier for the device
     * @return Device ID or -1 if failed
     */
    public int open(String path) {
        return vfs.Open(path);
    }
    
    /**
     * Closes a device
     * @param id Device ID
     */
    public void close(int id) {
        vfs.Close(id);
    }
    
    /**
     * Reads from a device
     * @param id Device ID
     * @param size Number of bytes to read
     * @return Data read from the device
     */
    public byte[] read(int id, int size) {
        return vfs.Read(id, size);
    }
    
    /**
     * Writes to a device
     * @param id Device ID
     * @param data Data to write
     * @return Number of bytes written
     */
    public int write(int id, byte[] data) {
        return vfs.Write(id, data);
    }
    
    /**
     * Seeks to a position in a device
     * @param id Device ID
     * @param position Position to seek to
     */
    public void seek(int id, int position) {
        vfs.Seek(id, position);
    }
    
    /**
     * Gets the mapping for a virtual page
     * @param virtualPageNumber The virtual page number
     * @return The physical page number
     */
    public int getMapping(int virtualPageNumber) {
        if (currentProcess == null) {
            return -1;
        }
        
        // Get the mapping for this virtual page
        VirtualToPhysicalMapping mapping = currentProcess.getMapping(virtualPageNumber);
        
        // If no mapping exists, this is an invalid access
        if (mapping == null) {
            return -1;
        }
        
        // If the page is already in memory, just return it
        if (mapping.isInMemory()) {
            // Update the TLB with this mapping
            Process.updateTLB(virtualPageNumber, mapping.physicalPageNumber);
            return mapping.physicalPageNumber;
        }
        
        // Page is not in memory, need to load it
        
        // First, try to allocate a new physical page
        int physicalPage = memoryManager.getRandomFreePage();
        
        // If no free pages, need to swap out a page
        if (physicalPage == -1) {
            // Find a page to swap out (simple random selection for now)
            physicalPage = swapOutPage();
            
            // If still no page available, we're out of memory
            if (physicalPage == -1) {
                return -1;
            }
        }
        
        // Now we have a physical page, check if we need to load from disk
        if (mapping.isOnDisk()) {
            // Load the page from disk
            loadPageFromDisk(mapping.diskPageNumber, physicalPage);
        } else {
            // This is a newly allocated page, initialize it to zeros
            clearPage(physicalPage);
        }
        
        // Update the mapping
        mapping.physicalPageNumber = physicalPage;
        
        // Update the TLB
        Process.updateTLB(virtualPageNumber, physicalPage);
        
        return physicalPage;
    }
    
    /**
     * Swaps out a page to disk to free up physical memory
     * @return The freed physical page number, or -1 if failed
     */
    private int swapOutPage() {
        // For simplicity, we'll just pick a random process and a random page
        // In a real OS, this would use a more sophisticated algorithm (LRU, etc.)
        
        // Get a list of all processes
        List<PCB> allProcesses = new ArrayList<>();
        allProcesses.addAll(realtimeProcesses);
        allProcesses.addAll(interactiveProcesses);
        allProcesses.addAll(backgroundProcesses);
        allProcesses.addAll(sleepingProcesses);
        
        if (allProcesses.isEmpty()) {
            return -1;
        }
        
        // Try each process until we find a page to swap out
        for (int attempt = 0; attempt < allProcesses.size(); attempt++) {
            // Pick a random process
            PCB process = allProcesses.get(random.nextInt(allProcesses.size()));
            
            // Find a page that's in memory
            for (int virtualPage = 0; virtualPage < PCB.MAX_VIRTUAL_PAGES; virtualPage++) {
                VirtualToPhysicalMapping mapping = process.getMapping(virtualPage);
                
                if (mapping != null && mapping.isInMemory()) {
                    // Found a page to swap out
                    int physicalPage = mapping.physicalPageNumber;
                    
                    // Write the page to disk
                    int diskPage = nextSwapPage++;
                    savePageToDisk(physicalPage, diskPage);
                    
                    // Update the mapping
                    mapping.physicalPageNumber = -1;
                    mapping.diskPageNumber = diskPage;
                    
                    // Return the freed physical page
                    return physicalPage;
                }
            }
        }
        
        return -1; // Couldn't find any page to swap out
    }
    
    /**
     * Saves a physical page to the swap file
     * @param physicalPage The physical page to save
     * @param diskPage The disk page number to save to
     */
    private void savePageToDisk(int physicalPage, int diskPage) {
        int pageSize = Process.PAGE_SIZE;
        int offset = diskPage * pageSize;
        
        // Seek to the right position in the swap file
        vfs.Seek(swapFileId, offset);
        
        // Get the page data
        byte[] pageData = new byte[pageSize];
        System.arraycopy(Process.getMemory(), physicalPage * pageSize, pageData, 0, pageSize);
        
        // Write to the swap file
        vfs.Write(swapFileId, pageData);
    }
    
    /**
     * Loads a page from the swap file into physical memory
     * @param diskPage The disk page to load
     * @param physicalPage The physical page to load into
     */
    private void loadPageFromDisk(int diskPage, int physicalPage) {
        int pageSize = Process.PAGE_SIZE;
        int offset = diskPage * pageSize;
        
        // Seek to the right position in the swap file
        vfs.Seek(swapFileId, offset);
        
        // Read from the swap file
        byte[] pageData = vfs.Read(swapFileId, pageSize);
        
        // Copy to physical memory
        System.arraycopy(pageData, 0, Process.getMemory(), physicalPage * pageSize, pageSize);
    }
    
    /**
     * Clears a physical page (fills with zeros)
     * @param physicalPage The physical page to clear
     */
    private void clearPage(int physicalPage) {
        int pageSize = Process.PAGE_SIZE;
        int startAddr = physicalPage * pageSize;
        
        // Fill with zeros
        for (int i = 0; i < pageSize; i++) {
            Process.getMemory()[startAddr + i] = 0;
        }
    }
    
    /**
     * Allocates memory for the current process
     * @param size Size in bytes to allocate
     * @return Starting virtual address, or -1 if failed
     */
    public int allocateMemory(int size) {
        if (currentProcess == null || size <= 0) {
            return -1;
        }
        
        // Round up to the nearest page size
        int pageSize = Process.PAGE_SIZE;
        int numPages = (size + pageSize - 1) / pageSize;
        
        // Find contiguous virtual pages
        int startVirtualPage = currentProcess.findFreeVirtualPages(numPages);
        if (startVirtualPage == -1) {
            return -1; // Not enough virtual address space
        }
        
        // Create mappings for each page (lazy allocation - no physical pages yet)
        for (int i = 0; i < numPages; i++) {
            VirtualToPhysicalMapping mapping = new VirtualToPhysicalMapping();
            // Both physical and disk page numbers are -1 (not allocated yet)
            currentProcess.mapPage(startVirtualPage + i, -1);
        }
        
        // Return the starting virtual address
        return startVirtualPage * pageSize;
    }
    
    /**
     * Frees memory for the current process
     * @param pointer Starting virtual address
     * @param size Size in bytes to free
     * @return true if successful, false otherwise
     */
    public boolean freeMemory(int pointer, int size) {
        if (currentProcess == null || pointer < 0 || size <= 0) {
            return false;
        }
        
        int pageSize = Process.PAGE_SIZE;
        
        // Check if pointer is page-aligned
        if (pointer % pageSize != 0) {
            return false;
        }
        
        // Calculate pages to free
        int startVirtualPage = pointer / pageSize;
        int numPages = (size + pageSize - 1) / pageSize;
        
        // Collect physical pages to free
        int[] physicalPages = new int[numPages];
        int count = 0;
        
        for (int i = 0; i < numPages; i++) {
            int virtualPage = startVirtualPage + i;
            VirtualToPhysicalMapping mapping = currentProcess.getMapping(virtualPage);
            
            if (mapping != null) {
                if (mapping.isInMemory()) {
                    physicalPages[count++] = mapping.physicalPageNumber;
                }
                currentProcess.unmapPage(virtualPage);
            }
        }
        
        // Free the physical pages
        if (count > 0) {
            int[] pagesToFree = new int[count];
            System.arraycopy(physicalPages, 0, pagesToFree, 0, count);
            memoryManager.freePages(pagesToFree);
        }
        
        return true;
    }
}