package OperationSystem;
/**
 * OS class - Bridge between userland and kerneland
 * Simulates the interrupt mechanism in real CPUs
 */
public class OS {
    private static Kernel kernel = new Kernel();
    
    /**
     * Allows a userland program to voluntarily give up CPU time
     */
    public static void cooperate() {
        kernel.switchTask();
    }
    
    /**
     * Creates a new process with default priority (INTERACTIVE)
     * @param program The program to run
     * @return Process ID of the created process
     */
    public static int createProcess(Runnable program) {
        return kernel.createProcess(program);
    }
    
    /**
     * Creates a new process with specified priority
     * @param program The program to run
     * @param priority The priority level for the process
     * @return Process ID of the created process
     */
    public static int createProcess(Runnable program, Priority priority) {
        return kernel.createProcess(program, priority);
    }
    
    /**
     * Gets the current process's PID
     * @return The current PID
     */
    public static int getPid() {
        return kernel.getPid();
    }
    
    /**
     * Gets a process's PID by name
     * @param name The process name to look for
     * @return The PID, or -1 if not found
     */
    public static int getPidByName(String name) {
        return kernel.getPidByName(name);
    }
    
    /**
     * Sends a message to another process
     * @param message The message to send
     */
    public static void sendMessage(KernelMessage message) {
        kernel.sendMessage(message);
    }
    
    /**
     * Waits for a message to arrive
     * @return The received message, or null if the process is terminated
     */
    public static KernelMessage waitForMessage() {
        return kernel.waitForMessage();
    }
    
    /**
     * Puts the current process to sleep for the specified time
     * @param milliseconds Time to sleep in milliseconds
     */
    public static void sleep(int milliseconds) {
        kernel.sleep(milliseconds);
    }
    
    /**
     * Terminates the current process
     */
    public static void exit() {
        kernel.terminateCurrentProcess();
    }
    
    /**
     * Opens a device or file
     * @param path Path or identifier for the device/file
     * @return Device ID or -1 if failed
     */
    public static int open(String path) {
        return kernel.open(path);
    }
    
    /**
     * Closes a device or file
     * @param id Device ID
     */
    public static void close(int id) {
        kernel.close(id);
    }
    
    /**
     * Reads from a device or file
     * @param id Device ID
     * @param size Number of bytes to read
     * @return Data read from the device/file
     */
    public static byte[] read(int id, int size) {
        return kernel.read(id, size);
    }
    
    /**
     * Writes to a device or file
     * @param id Device ID
     * @param data Data to write
     * @return Number of bytes written
     */
    public static int write(int id, byte[] data) {
        return kernel.write(id, data);
    }
    
    /**
     * Seeks to a position in a device or file
     * @param id Device ID
     * @param position Position to seek to
     */
    public static void seek(int id, int position) {
        kernel.seek(id, position);
    }
    
    /**
     * Gets the physical page mapping for a virtual page
     * @param virtualPageNumber The virtual page number
     * @return The physical page number, or -1 if not mapped
     */
    public static int getMapping(int virtualPageNumber) {
        return kernel.getMapping(virtualPageNumber);
    }
    
    /**
     * Allocates memory for the current process
     * @param size Size in bytes to allocate (must be a multiple of page size)
     * @return Starting virtual address, or -1 if failed
     */
    public static int allocateMemory(int size) {
        // Ensure size is a multiple of page size
        if (size <= 0 || size % Process.PAGE_SIZE != 0) {
            return -1;
        }
        return kernel.allocateMemory(size);
    }
    
    /**
     * Frees memory for the current process
     * @param pointer Starting virtual address (must be page-aligned)
     * @param size Size in bytes to free (must be a multiple of page size)
     * @return true if successful, false otherwise
     */
    public static boolean freeMemory(int pointer, int size) {
        // Ensure pointer and size are valid
        if (pointer < 0 || size <= 0 || 
            pointer % Process.PAGE_SIZE != 0 || 
            size % Process.PAGE_SIZE != 0) {
            return false;
        }
        return kernel.freeMemory(pointer, size);
    }
}