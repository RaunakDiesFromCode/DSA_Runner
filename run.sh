#!/usr/bin/env bash
# ========================================================
# LeetCode-Style Java DSA Practice Runner
# ========================================================

set -eo pipefail

# 1. Resolve project root directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"
cd "$SCRIPT_DIR"

# 2. Locate Java and javac
find_jdk() {
    # Test if java and javac already work
    if command -v java >/dev/null 2>&1 && command -v javac >/dev/null 2>&1; then
        if java -version >/dev/null 2>&1 && javac -version >/dev/null 2>&1; then
            return 0
        fi
    fi

    # Check JAVA_HOME
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
        export PATH="$JAVA_HOME/bin:$PATH"
        return 0
    fi

    # Search macOS standard JVM installations
    for jvm in /Library/Java/JavaVirtualMachines/*/Contents/Home; do
        if [ -x "$jvm/bin/java" ] && [ -x "$jvm/bin/javac" ]; then
            export JAVA_HOME="$jvm"
            export PATH="$JAVA_HOME/bin:$PATH"
            return 0
        fi
    done

    # Search Homebrew macOS locations
    for brew_jdk in /opt/homebrew/opt/openjdk*/libexec/openjdk.jdk/Contents/Home /opt/homebrew/opt/openjdk*; do
        if [ -x "$brew_jdk/bin/java" ] && [ -x "$brew_jdk/bin/javac" ]; then
            export JAVA_HOME="$brew_jdk"
            export PATH="$JAVA_HOME/bin:$PATH"
            return 0
        fi
    done

    # Search Linux standard JVM installations
    for linux_jvm in /usr/lib/jvm/*; do
        if [ -x "$linux_jvm/bin/java" ] && [ -x "$linux_jvm/bin/javac" ]; then
            export JAVA_HOME="$linux_jvm"
            export PATH="$JAVA_HOME/bin:$PATH"
            return 0
        fi
    done

    return 1
}

if ! find_jdk; then
    echo "" >&2
    printf "\033[1;31m[Error]\033[0m Java Development Kit (JDK) with 'javac' and 'java' was not found.\n" >&2
    echo "Please install a JDK (version 17 or higher recommended) or set JAVA_HOME." >&2
    echo "" >&2
    exit 1
fi

# 3. Validate required files
if [ ! -f "Solution.java" ]; then
    echo "" >&2
    printf "\033[1;31m[Error]\033[0m Solution.java not found in %s\n" "$SCRIPT_DIR" >&2
    echo "Please create Solution.java with your algorithm implementation." >&2
    echo "" >&2
    exit 1
fi

if [ ! -f "testcases.json" ]; then
    echo "" >&2
    printf "\033[1;31m[Error]\033[0m testcases.json not found in %s\n" "$SCRIPT_DIR" >&2
    echo "Please create testcases.json with your test cases." >&2
    echo "" >&2
    exit 1
fi

# Build classpath from lib/*.jar
CP_LIB=""
for jar in lib/*.jar; do
    if [ -f "$jar" ]; then
        if [ -z "$CP_LIB" ]; then
            CP_LIB="$SCRIPT_DIR/$jar"
        else
            CP_LIB="$CP_LIB:$SCRIPT_DIR/$jar"
        fi
    fi
done

BUILD_DIR="$SCRIPT_DIR/.build"
mkdir -p "$BUILD_DIR"

FULL_CP="$BUILD_DIR"
if [ -n "$CP_LIB" ]; then
    FULL_CP="$FULL_CP:$CP_LIB"
fi

# 4. Compile runner and Solution.java
# Clean old Solution class from build directory to prevent stale runs
rm -f "$BUILD_DIR/Solution.class" "$BUILD_DIR/Solution\$*.class"

set +e
javac -encoding UTF-8 -cp "$FULL_CP" -d "$BUILD_DIR" runner/*.java Solution.java
COMPILE_EXIT=$?
set -e

if [ $COMPILE_EXIT -ne 0 ]; then
    echo "" >&2
    printf "\033[1;31m[Compilation Failed]\033[0m Please fix the errors in Solution.java above.\n" >&2
    exit 1
fi

# 5. Execute runner
set +e
java -cp "$FULL_CP" runner.Main "$@"
RUN_EXIT=$?
set -e

exit $RUN_EXIT
