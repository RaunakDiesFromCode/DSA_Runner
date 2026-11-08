package runner;

/**
 * ANSI terminal color formatting with automatic detection and fallback.
 */
public class TerminalColors {
    private static boolean colorEnabled = detectColorSupport();

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";
    private static final String WHITE = "\u001B[37m";

    private static boolean detectColorSupport() {
        // Honor NO_COLOR standard (https://no-color.org)
        String noColor = System.getenv("NO_COLOR");
        if (noColor != null && !noColor.isEmpty()) {
            return false;
        }

        // Dumb terminal does not support colors
        String term = System.getenv("TERM");
        if ("dumb".equalsIgnoreCase(term)) {
            return false;
        }

        // Check if stdout is a console or running in common environments
        if (System.console() != null) {
            return true;
        }

        // Check for VS Code, IntelliJ, or other terminal emulators
        String termProgram = System.getenv("TERM_PROGRAM");
        if (termProgram != null) {
            return true;
        }

        // Fallback check on Windows / Mac / Linux: if TERM is set to standard xterm/screen/etc.
        if (term != null && (term.contains("xterm") || term.contains("color") || term.contains("ansi") || term.contains("screen"))) {
            return true;
        }

        // Default to true on macOS/Linux unless explicitly disabled
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("mac") || os.contains("linux") || os.contains("unix");
    }

    public static void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
    }

    public static boolean isColorEnabled() {
        return colorEnabled;
    }

    private static String colorize(String color, String text) {
        if (!colorEnabled || text == null || text.isEmpty()) {
            return text;
        }
        return color + text + RESET;
    }

    public static String bold(String s) {
        return colorize(BOLD, s);
    }

    public static String dim(String s) {
        return colorize(DIM, s);
    }

    public static String red(String s) {
        return colorize(RED, s);
    }

    public static String green(String s) {
        return colorize(GREEN, s);
    }

    public static String yellow(String s) {
        return colorize(YELLOW, s);
    }

    public static String blue(String s) {
        return colorize(BLUE, s);
    }

    public static String magenta(String s) {
        return colorize(MAGENTA, s);
    }

    public static String cyan(String s) {
        return colorize(CYAN, s);
    }

    public static String gray(String s) {
        return colorize(GRAY, s);
    }

    public static String boldGreen(String s) {
        return colorize(BOLD + GREEN, s);
    }

    public static String boldRed(String s) {
        return colorize(BOLD + RED, s);
    }

    public static String boldYellow(String s) {
        return colorize(BOLD + YELLOW, s);
    }

    public static String boldCyan(String s) {
        return colorize(BOLD + CYAN, s);
    }
}
