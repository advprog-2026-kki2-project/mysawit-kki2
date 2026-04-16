package id.ac.ui.cs.advprog.mysawit.modules.transport.exception;

/**
 * Thrown when the total weight of assigned crops exceeds 400kg.
 */
public class CapacityExceededException extends RuntimeException {
    public CapacityExceededException(double weight) {
        super("Truck capacity exceeded! Attempted: " + weight + "kg. Maximum allowed: 400kg.");
    }
}