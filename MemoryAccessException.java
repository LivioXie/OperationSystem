package OperationSystem;

/**
 * Exception thrown when a memory access violation occurs
 */
public class MemoryAccessException extends Exception {
    public MemoryAccessException(String message) {
        super(message);
    }
}