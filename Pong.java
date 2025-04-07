package OperationSystem;

/**
 * Pong process - Demonstrates message passing with Ping
 */
public class Pong implements Runnable {
    private static final int MSG_PING = 1;
    private static final int MSG_PONG = 2;
    
    @Override
    public void run() {
        System.out.println("Pong: Starting...");
        
        while (true) {
            // Wait for a message
            KernelMessage message = OS.waitForMessage();
            
            // If we got a message and it's a PING
            if (message != null && message.getWhat() == MSG_PING) {
                String pingText = new String(message.getData());
                System.out.println("Pong: Received " + pingText + " from Ping");
                
                // Send PONG response
                String response = "PONG " + pingText.split(" ")[1];
                KernelMessage responseMsg = new KernelMessage(
                    message.getSenderPid(), MSG_PONG, response.getBytes());
                OS.sendMessage(responseMsg);
                System.out.println("Pong: Sent " + response + " to Ping");
            }
            
            // Give other processes a chance to run
            OS.cooperate();
        }
    }
}