package OperationSystem;

/**
 * Priority levels for processes
 */
public enum Priority {
    REALTIME,    // Highest priority - for time-critical tasks
    INTERACTIVE, // Medium priority - for user-facing applications
    BACKGROUND   // Lowest priority - for background tasks
}