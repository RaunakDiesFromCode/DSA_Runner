package runner;

/**
 * Encapsulates the execution result of a single test case.
 */
public class TestResult {
    public enum Status {
        PASS,
        FAIL,
        ERROR,
        SKIPPED
    }

    private final TestCase testCase;
    private final Status status;
    private final double durationMs;
    private final String formattedInput;
    private final String formattedExpected;
    private final String formattedActual;
    private final Throwable error;
    private final String errorLocation;

    public TestResult(
        TestCase testCase,
        Status status,
        double durationMs,
        String formattedInput,
        String formattedExpected,
        String formattedActual,
        Throwable error,
        String errorLocation
    ) {
        this.testCase = testCase;
        this.status = status;
        this.durationMs = durationMs;
        this.formattedInput = formattedInput;
        this.formattedExpected = formattedExpected;
        this.formattedActual = formattedActual;
        this.error = error;
        this.errorLocation = errorLocation;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public Status getStatus() {
        return status;
    }

    public double getDurationMs() {
        return durationMs;
    }

    public String getFormattedInput() {
        return formattedInput;
    }

    public String getFormattedExpected() {
        return formattedExpected;
    }

    public String getFormattedActual() {
        return formattedActual;
    }

    public Throwable getError() {
        return error;
    }

    public String getErrorLocation() {
        return errorLocation;
    }
}
