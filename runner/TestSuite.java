package runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the complete set of test cases and metadata loaded from testcases.json.
 */
public class TestSuite {
    private final String method;
    private final String problem;
    private final List<TestCase> testCases;

    public TestSuite(String method, String problem, List<TestCase> testCases) {
        this.method = method;
        this.problem = problem;
        this.testCases = testCases != null ? testCases : new ArrayList<>();
    }

    public String getMethod() {
        return method;
    }

    public String getProblem() {
        return problem;
    }

    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }
}
