package OperationSystem;

import java.util.Arrays;

/**
 * KernelMessage - Represents a message passed between processes
 */
public class KernelMessage {
    private int senderPid;
    private int targetPid;
    private int what;
    private byte[] data;
    
    /**
     * Creates a new message
     * @param targetPid The process ID of the target
     * @param what An integer indicating the message type
     * @param data The message data
     */
    public KernelMessage(int targetPid, int what, byte[] data) {
        this.senderPid = -1; // Will be set by the kernel
        this.targetPid = targetPid;
        this.what = what;
        this.data = data != null ? Arrays.copyOf(data, data.length) : new byte[0];
    }
    
    /**
     * Copy constructor - creates a deep copy of another message
     * @param other The message to copy
     */
    public KernelMessage(KernelMessage other) {
        this.senderPid = other.senderPid;
        this.targetPid = other.targetPid;
        this.what = other.what;
        this.data = Arrays.copyOf(other.data, other.data.length);
    }
    
    /**
     * Gets the sender's process ID
     * @return The sender's PID
     */
    public int getSenderPid() {
        return senderPid;
    }
    
    /**
     * Sets the sender's process ID (should only be called by the kernel)
     * @param senderPid The sender's PID
     */
    public void setSenderPid(int senderPid) {
        this.senderPid = senderPid;
    }
    
    /**
     * Gets the target's process ID
     * @return The target's PID
     */
    public int getTargetPid() {
        return targetPid;
    }
    
    /**
     * Gets the message type
     * @return The message type
     */
    public int getWhat() {
        return what;
    }
    
    /**
     * Gets the message data
     * @return The message data
     */
    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
    
    @Override
    public String toString() {
        return "KernelMessage{" +
                "senderPid=" + senderPid +
                ", targetPid=" + targetPid +
                ", what=" + what +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}