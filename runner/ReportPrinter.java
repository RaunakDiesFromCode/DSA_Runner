package runner;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

/**
 * Prints a color-coded, professional terminal test report.
 */
public class ReportPrinter {

    private static final String DIVIDER_DOUBLE = "========================================================";
    private static final String DIVIDER_SINGLE = "--------------------------------------------------------";

    public static void printReport(
        TestSuite testSuite,
        MethodResolver.ResolvedMethod resolvedMethod,
        List<TestResult> results,
        boolean debugMode
    ) {
        printReport(testSuite, resolvedMethod, results, debugMode, false);
    }

    public static void printReport(
        TestSuite testSuite,
        MethodResolver.ResolvedMethod resolvedMethod,
        List<TestResult> results,
        boolean debugMode,
        boolean showAll
    ) {
        PrintStream out = System.out;

        String problem = testSuite.getProblem();
        if (problem == null || problem.isEmpty()) {
            problem = inferProblemName(resolvedMethod.getMethod().getName());
        }

        int passed = 0;
        int failed = 0;
        int errors = 0;
        double totalDurationMs = 0.0;

        for (TestResult r : results) {
            totalDurationMs += r.getDurationMs();
            if (r.getStatus() == TestResult.Status.PASS) passed++;
            else if (r.getStatus() == TestResult.Status.FAIL) failed++;
            else if (r.getStatus() == TestResult.Status.ERROR) errors++;
        }

        // Header
        out.println();
        out.println(TerminalColors.bold(DIVIDER_DOUBLE));
        out.println(TerminalColors.bold("               JAVA DSA PRACTICE RUNNER"));
        out.println(TerminalColors.bold(DIVIDER_DOUBLE));
        out.println();
        out.println(TerminalColors.bold("Problem : ") + TerminalColors.boldCyan(problem));
        out.println(TerminalColors.bold("Method  : ") + TerminalColors.cyan(resolvedMethod.getSignatureString()));
        out.println(TerminalColors.bold("Cases   : ") + results.size());
        out.println();

        // Failed / Error tests section (only show failures by default, or all if showAll is true)
        int issuesCount = failed + errors;
        if (issuesCount > 0 || showAll) {
            out.println(TerminalColors.gray(DIVIDER_SINGLE));
            if (!showAll) {
                out.println(TerminalColors.boldRed("FAILED TESTS (" + issuesCount + "/" + results.size() + ")"));
            } else {
                out.println(TerminalColors.bold("TEST RESULTS"));
            }
            out.println(TerminalColors.gray(DIVIDER_SINGLE));
            out.println();

            for (TestResult r : results) {
                // If not showing all, skip passing tests
                if (!showAll && r.getStatus() == TestResult.Status.PASS) {
                    continue;
                }

                String statusTag;
                switch (r.getStatus()) {
                    case PASS:
                        statusTag = TerminalColors.boldGreen("[PASS]");
                        break;
                    case FAIL:
                        statusTag = TerminalColors.boldRed("[FAIL]");
                        break;
                    case ERROR:
                        statusTag = TerminalColors.boldRed("[ERROR]");
                        break;
                    case SKIPPED:
                    default:
                        statusTag = TerminalColors.boldYellow("[SKIPPED]");
                        break;
                }

                String testLabel = "Test " + r.getTestCase().getIndex();
                if (r.getTestCase().getName() != null && !r.getTestCase().getName().isEmpty()) {
                    testLabel += ": " + r.getTestCase().getName();
                }

                out.println(statusTag + " " + TerminalColors.bold(testLabel));
                out.println("       " + TerminalColors.gray("Input    : ") + TerminalColors.blue(r.getFormattedInput()));
                out.println("       " + TerminalColors.gray("Expected : ") + TerminalColors.magenta(r.getFormattedExpected()));

                if (r.getStatus() == TestResult.Status.ERROR) {
                    out.println("       " + TerminalColors.boldRed("Error    : ") + TerminalColors.red(r.getFormattedActual()));
                    if (r.getErrorLocation() != null) {
                        out.println("       " + TerminalColors.gray("Location : ") + TerminalColors.yellow(r.getErrorLocation()));
                    }
                    if (debugMode && r.getError() != null) {
                        out.println("       " + TerminalColors.gray("Stack Trace:"));
                        r.getError().printStackTrace(out);
                    }
                } else {
                    String actualColor = (r.getStatus() == TestResult.Status.PASS)
                        ? TerminalColors.magenta(r.getFormattedActual())
                        : TerminalColors.red(r.getFormattedActual());
                    out.println("       " + TerminalColors.gray("Actual   : ") + actualColor);
                }

                out.println("       " + TerminalColors.gray("Time     : ") + String.format(Locale.US, "%.2f ms", r.getDurationMs()));
                out.println();
            }
        }

        // Summary Section
        out.println(TerminalColors.gray(DIVIDER_SINGLE));
        out.println(TerminalColors.bold("SUMMARY"));
        out.println(TerminalColors.gray(DIVIDER_SINGLE));
        out.println();

        int total = results.size();
        double passRate = total > 0 ? (passed * 100.0 / total) : 0.0;

        out.printf(Locale.US, "%-15s: %d%n", TerminalColors.bold("Total Tests"), total);
        out.printf(Locale.US, "%-15s: %s%n", TerminalColors.bold("Passed"), TerminalColors.green(String.valueOf(passed)));
        if (failed > 0) {
            out.printf(Locale.US, "%-15s: %s%n", TerminalColors.bold("Failed"), TerminalColors.red(String.valueOf(failed)));
        } else {
            out.printf(Locale.US, "%-15s: %d%n", TerminalColors.bold("Failed"), 0);
        }
        if (errors > 0) {
            out.printf(Locale.US, "%-15s: %s%n", TerminalColors.bold("Errors"), TerminalColors.red(String.valueOf(errors)));
        }
        out.printf(Locale.US, "%-15s: %.2f%%%n", TerminalColors.bold("Pass Rate"), passRate);
        out.printf(Locale.US, "%-15s: %.2f ms%n", TerminalColors.bold("Total Time"), totalDurationMs);
        out.println();

        // Status banner
        if (passed == total) {
            out.println(TerminalColors.boldGreen("STATUS: ALL TESTS PASSED"));
        } else if (errors > 0 && failed == 0) {
            out.println(TerminalColors.boldRed("STATUS: EXECUTION ENCOUNTERED ERRORS"));
        } else {
            out.println(TerminalColors.boldRed("STATUS: SOME TESTS FAILED"));
        }

        out.println();
        out.println(TerminalColors.bold(DIVIDER_DOUBLE));
        out.println();
    }

    private static String inferProblemName(String methodName) {
        if (methodName == null || methodName.isEmpty()) return "Unknown Problem";
        if (methodName.length() <= 3) {
            return methodName.toUpperCase(Locale.US);
        }
        // Convert camelCase to Title Case (e.g. twoSum -> Two Sum, isPrime -> Is Prime)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < methodName.length(); i++) {
            char c = methodName.charAt(i);
            if (i == 0) {
                sb.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                sb.append(" ").append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
