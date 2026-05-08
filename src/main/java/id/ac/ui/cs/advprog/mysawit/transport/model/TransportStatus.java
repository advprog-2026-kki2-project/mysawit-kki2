package id.ac.ui.cs.advprog.mysawit.transport.model;

/**
 * Requirement: State machine for delivery lifecycle.
 * Flow: LOADING -> TRANSPORTING -> ARRIVED -> FOREMAN_APPROVED/FOREMAN_REJECTED
 *       -> ADMIN_APPROVED/ADMIN_REJECTED (after Foreman approval)
 */
public enum TransportStatus {
    LOADING,            // Default state
    TRANSPORTING,       // Mobilizing harvest
    ARRIVED,            // Handed to factory
    FOREMAN_APPROVED,   // Foreman verified delivery results
    FOREMAN_REJECTED,   // Foreman rejected delivery results
    ADMIN_APPROVED,     // Central Admin confirmed factory processing
    ADMIN_REJECTED      // Central Admin rejected delivery results
}