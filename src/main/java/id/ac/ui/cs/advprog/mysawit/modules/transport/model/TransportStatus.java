package id.ac.ui.cs.advprog.mysawit.modules.transport.model;

/**
 * Requirement: State machine for delivery lifecycle.
 */
public enum TransportStatus {
    LOADING,      // Default state
    TRANSPORTING, // Mobilizing harvest
    ARRIVED       // Handed to factory
}