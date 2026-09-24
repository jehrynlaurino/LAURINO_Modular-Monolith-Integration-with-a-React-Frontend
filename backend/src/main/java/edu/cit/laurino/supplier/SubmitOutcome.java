package edu.cit.laurino.supplier;

/**
 * Internal result of one submit attempt to LegacySupply. "retryable" is what
 * tells the caller whether to leave the order PENDING for the scheduler to
 * pick up later, versus marking it permanently FAILED.
 */
record SubmitOutcome(boolean success, boolean retryable, String poNumber, Integer statusCode, String failureReason) {
    static SubmitOutcome success(String poNumber, int statusCode) {
        return new SubmitOutcome(true, false, poNumber, statusCode, null);
    }

    static SubmitOutcome retryableFailure(String reason) {
        return new SubmitOutcome(false, true, null, null, reason);
    }

    static SubmitOutcome terminalFailure(String reason) {
        return new SubmitOutcome(false, false, null, null, reason);
    }
}
