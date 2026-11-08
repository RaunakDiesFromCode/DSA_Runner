package runner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main entry point for the LeetCode-style Java DSA Practice Runner.
 */
public class Main {

    public static void main(String[] args) {
        boolean debugMode = false;
        boolean showAll = false;
        Path testCasesPath = Paths.get("testcases.json");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--debug".equals(arg) || "-d".equals(arg)) {
                debugMode = true;
            } else if ("--all".equals(arg) || "-a".equals(arg)) {
                showAll = true;
            } else if ("--no-color".equals(arg)) {
                TerminalColors.setColorEnabled(false);
            } else if ("--color".equals(arg)) {
                TerminalColors.setColorEnabled(true);
            } else if ("--testcases".equals(arg) && i + 1 < args.length) {
                testCasesPath = Paths.get(args[++i]);
            }
        }

        try {
            // 1. Parse testcases.json
            TestSuite testSuite = JsonParser.parse(testCasesPath);

            // 2. Load Solution class
            Class<?> solutionClass;
            try {
                solutionClass = SolutionLoader.loadSolutionClass();
            } catch (ClassNotFoundException e) {
                System.err.println(TerminalColors.boldRed("Error: ") + "Could not find compiled class 'Solution'.");
                System.err.println(TerminalColors.gray("Make sure Solution.java exists and compiled successfully."));
                System.exit(1);
                return;
            }

            // 3. Resolve the target method
            int argumentCount = testSuite.getTestCases().get(0).getRawInput().size();
            MethodResolver.ResolvedMethod resolvedMethod = MethodResolver.resolve(
                solutionClass,
                testSuite.getMethod(),
                argumentCount
            );

            // 4. Execute test cases
            List<TestResult> results = TestExecutor.execute(solutionClass, resolvedMethod, testSuite);

            // 5. Print formatted terminal report
            ReportPrinter.printReport(testSuite, resolvedMethod, results, debugMode, showAll);

            // 6. Exit code
            boolean allPassed = results.stream().allMatch(r -> r.getStatus() == TestResult.Status.PASS);
            System.exit(allPassed ? 0 : 1);

        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println();
            System.err.println(TerminalColors.boldRed("Configuration Error:"));
            System.err.println(TerminalColors.yellow(e.getMessage()));
            System.err.println();
            if (debugMode) {
                e.printStackTrace(System.err);
            }
            System.exit(1);
        } catch (Throwable t) {
            System.err.println();
            System.err.println(TerminalColors.boldRed("Unexpected Runner Error:"));
            System.err.println(TerminalColors.red(t.getClass().getSimpleName() + ": " + t.getMessage()));
            System.err.println();
            if (debugMode) {
                t.printStackTrace(System.err);
            } else {
                System.err.println(TerminalColors.gray("Run with --debug for a detailed stack trace."));
            }
            System.exit(1);
        }
    }
}
