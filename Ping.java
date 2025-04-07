package OperationSystem;

/**
 * Ping process - Demonstrates message passing with Pong
 */
public class Ping implements Runnable {
    private static final int MSG_PING = 1;
    private static final int MSG_PONG = 2;
    private static final int MAX_EXCHANGES = 5;
    
    @Override
    public void run() {
        System.out.println("Ping: Starting...");
        
        // Wait for Pong to start
        OS.sleep(1000);
        
        // Find Pong's PID
        int pongPid = OS.getPidByName("Pong");
        if (pongPid == -1) {
            System.out.println("Ping: Could not find Pong process");
            return;
        }
        
        System.out.println("Ping: Found Pong with PID " + pongPid);
        
        int count = 0;
        while (count < MAX_EXCHANGES) {
            // Send PING message
            String message = "PING " + count;
            KernelMessage km = new KernelMessage(pongPid, MSG_PING, message.getBytes());
            OS.sendMessage(km);
            System.out.println("Ping: Sent " + message + " to Pong");
            
            // Wait for PONG response
            KernelMessage response = OS.waitForMessage();
            if (response != null && response.getWhat() == MSG_PONG) {
                String responseText = new String(response.getData());
                System.out.println("Ping: Received " + responseText + " from Pong");
                count++;
            }
            
            // Give other processes a chance to run
            OS.cooperate();
        }
        
        System.out.println("Ping: Finished after " + count + " exchanges");
    }
}