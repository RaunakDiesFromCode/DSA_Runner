# Reusable LeetCode-Style Java DSA Practice Runner

A lightweight, zero-configuration local Java DSA practice environment designed to feel just like LeetCode's online judge.

> **Core Philosophy**: During everyday practice, you **ONLY** modify `Solution.java` and `testcases.json`. Everything else behaves as a black box.

---

## 1. Quick Start Workflow

1. **Write or paste your algorithm** into [`Solution.java`](file:///Users/raunakmanna/Documents/Programming/Java/A2Z/DSA_Runner/Solution.java).
2. **Add test cases** to [`testcases.json`](file:///Users/raunakmanna/Documents/Programming/Java/A2Z/DSA_Runner/testcases.json).
3. **Execute** in the terminal:

```bash
./run.sh
```

*(On Windows, run `run.bat`)*

You immediately see a detailed, color-coded terminal report with per-test results, actual execution times, and a summary.

---

## 2. Project Structure

```text
DSA_Runner/
│
├── Solution.java          # ✍️ ONLY EDIT THIS: Your solution class & methods
├── testcases.json         # ✍️ ONLY EDIT THIS: Input arguments & expected outputs
├── run.sh                 # 🚀 Launcher for macOS and Linux
├── run.bat                # 🚀 Launcher for Windows
├── README.md              # 📖 Documentation & reference guide
├── .gitignore             # 🙈 Ignores build artifacts
│
├── runner/                # ⚙️ Internal Runner Infrastructure (Black Box)
│   ├── Main.java              # CLI entry point and orchestration
│   ├── SolutionLoader.java    # Dynamic reflection loader & instantiator
│   ├── MethodResolver.java    # Intelligent automatic method discovery
│   ├── JsonParser.java        # Robust test case parser
│   ├── ArgumentConverter.java # JSON-to-Java type converter
│   ├── ResultComparator.java  # Deep recursive result comparator & formatter
│   ├── TestExecutor.java      # Isolated per-test execution & timing
│   ├── ReportPrinter.java     # Color-coded ANSI terminal reporter
│   ├── TerminalColors.java    # ANSI color handling with NO_COLOR support
│   ├── TestCase.java          # Test case data model
│   ├── TestSuite.java         # Test suite metadata container
│   ├── TestResult.java        # Execution outcome container
│   ├── ListNode.java          # LeetCode-compatible singly linked list node
│   └── TreeNode.java          # LeetCode-compatible binary tree node
│
└── lib/
    └── gson-2.10.1.jar        # Fast, lightweight JSON parsing library
```

---

## 3. How to Practice a New Problem

### Example 1: Math / Primitives (LCM)

**`Solution.java`**:
```java
class Solution {
    public int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    public int lcm(int a, int b) {
        return (a / gcd(a, b)) * b;
    }
}
```

**`testcases.json`**:
```json
{
  "method": "lcm",
  "testCases": [
    {
      "input": [12, 18],
      "expected": 36
    },
    {
      "input": [8, 12],
      "expected": 24
    }
  ]
}
```

---

### Example 2: Booleans & Single Arguments (isPrime)

When `Solution` contains a single public solution method, the `"method"` field is **optional** and automatically discovered:

**`Solution.java`**:
```java
class Solution {
    public boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
```

**`testcases.json`**:
```json
{
  "testCases": [
    {
      "input": [13],
      "expected": true
    },
    {
      "input": [12],
      "expected": false
    }
  ]
}
```

---

### Example 3: Arrays (Two Sum)

Multi-argument methods pass arguments inside the `"input"` array in positional order:

**`Solution.java`**:
```java
import java.util.HashMap;
import java.util.Map;

class Solution {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int diff = target - nums[i];
            if (map.containsKey(diff)) {
                return new int[] { map.get(diff), i };
            }
            map.put(nums[i], i);
        }
        return new int[0];
    }
}
```

**`testcases.json`**:
```json
{
  "method": "twoSum",
  "testCases": [
    {
      "input": [[2, 7, 11, 15], 9],
      "expected": [0, 1]
    },
    {
      "input": [[3, 2, 4], 6],
      "expected": [1, 2]
    }
  ]
}
```

---

### Example 4: In-Place Modification (`void` Return)

For methods that modify an argument in-place (e.g., `void reverseString(char[] s)`, `void sortColors(int[] nums)`), the runner tests the first modified input array against `"expected"`:

**`Solution.java`**:
```java
class Solution {
    public void reverseString(char[] s) {
        int l = 0, r = s.length - 1;
        while (l < r) {
            char tmp = s[l];
            s[l++] = s[r];
            s[r--] = tmp;
        }
    }
}
```

**`testcases.json`**:
```json
{
  "testCases": [
    {
      "input": [["h","e","l","l","o"]],
      "expected": ["o","l","l","e","h"]
    }
  ]
}
```

---

### Example 5: Linked Lists (`ListNode`)

Standard LeetCode linked list node is built in under `runner.ListNode`:

**`Solution.java`**:
```java
import runner.ListNode;

class Solution {
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode next = curr.next;
            curr.next = prev;
            prev = curr;
            curr = next;
        }
        return prev;
    }
}
```

**`testcases.json`**:
```json
{
  "testCases": [
    {
      "input": [[1, 2, 3, 4, 5]],
      "expected": [5, 4, 3, 2, 1]
    }
  ]
}
```

---

### Example 6: Binary Trees (`TreeNode`)

Standard LeetCode binary tree node is built in under `runner.TreeNode` and deserializes from level-order traversal with `null`:

**`Solution.java`**:
```java
import runner.TreeNode;

class Solution {
    public int maxDepth(TreeNode root) {
        if (root == null) return 0;
        return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
    }
}
```

**`testcases.json`**:
```json
{
  "testCases": [
    {
      "input": [[3, 9, 20, null, null, 15, 7]],
      "expected": 3
    }
  ]
}
```

---

## 4. Supported Types

| Category | Supported Types |
| :--- | :--- |
| **Primitives & Wrappers** | `int`, `long`, `double`, `float`, `boolean`, `char`, `byte`, `short`, `String` |
| **1D Arrays** | `int[]`, `long[]`, `double[]`, `float[]`, `boolean[]`, `char[]`, `String[]`, etc. |
| **2D & 3D Arrays** | `int[][]`, `char[][]` (grid problems), `String[][]`, `int[][][]`, etc. |
| **Collections** | `List<Integer>`, `List<String>`, `List<List<Integer>>`, `Set<T>`, etc. |
| **LeetCode Data Structures** | `ListNode` (singly-linked list), `TreeNode` (binary tree) |
| **Return Types** | Any of the above, plus `void` (for in-place array/collection modification) |
| **Special Values** | `null`, floating-point tolerances ($10^{-6}$ default) |

---

## 5. Automatic Method Discovery

The runner discovers your target method automatically:
1. Inspects public methods declared in `Solution.java`.
2. Automatically filters out `Object` methods (`equals`, `hashCode`, `toString`, etc.) and compiler-generated bridges.
3. If `"method"` is specified in `testcases.json`, invokes that method.
4. If `"method"` is omitted and there is a unique matching public method, automatically selects it.
5. If multiple methods match the parameter count and name is omitted, produces a clear error showing all candidate signatures and instructions to resolve the ambiguity.

---

## 6. Command Line Options

```bash
# Normal run (only shows failed test cases; clean summary when all pass)
./run.sh

# Show all test cases including passing ones
./run.sh --all

# Run with full exception stack traces for errors
./run.sh --debug

# Force disable ANSI colors (also honors NO_COLOR=1)
./run.sh --no-color

# Force enable ANSI colors
./run.sh --color

# Use a custom test case file
./run.sh --testcases my_tests.json
```

---

## 7. Troubleshooting

- **Java Not Found**: Ensure you have JDK 17+ installed. On macOS Apple Silicon, the runner searches `/Library/Java/JavaVirtualMachines/*/Contents/Home` and Homebrew locations automatically.
- **Ambiguous Method Error**: Add `"method": "<yourMethodName>"` to the top of `testcases.json`.
- **In-Place Modification**: If your method returns `void` (e.g. `void sortColors(int[] nums)`), the runner checks the modified first parameter against `"expected"`.
