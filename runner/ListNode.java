package runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LeetCode-compatible definition for singly-linked list.
 */
public class ListNode {
    public int val;
    public ListNode next;

    public ListNode() {}

    public ListNode(int val) {
        this.val = val;
    }

    public ListNode(int val, ListNode next) {
        this.val = val;
        this.next = next;
    }

    public static ListNode fromArray(int[] vals) {
        if (vals == null || vals.length == 0) return null;
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        for (int v : vals) {
            current.next = new ListNode(v);
            current = current.next;
        }
        return dummy.next;
    }

    public static ListNode fromList(List<Integer> vals) {
        if (vals == null || vals.isEmpty()) return null;
        ListNode dummy = new ListNode(0);
        ListNode current = dummy;
        for (Integer v : vals) {
            current.next = new ListNode(v != null ? v : 0);
            current = current.next;
        }
        return dummy.next;
    }

    public static List<Integer> toList(ListNode head) {
        List<Integer> list = new ArrayList<>();
        ListNode current = head;
        // Guard against cycles in user code
        int maxNodes = 10000;
        int count = 0;
        while (current != null && count++ < maxNodes) {
            list.add(current.val);
            current = current.next;
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListNode other = (ListNode) o;
        if (this.val != other.val) return false;
        return Objects.equals(this.next, other.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, next);
    }

    @Override
    public String toString() {
        return toList(this).toString();
    }
}
