package runner;

import java.util.*;

/**
 * LeetCode-compatible definition for a binary tree node.
 */
public class TreeNode {
    public int val;
    public TreeNode left;
    public TreeNode right;

    public TreeNode() {}

    public TreeNode(int val) {
        this.val = val;
    }

    public TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    public static TreeNode fromLevelOrder(List<Integer> vals) {
        if (vals == null || vals.isEmpty() || vals.get(0) == null) {
            return null;
        }

        TreeNode root = new TreeNode(vals.get(0));
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int i = 1;
        while (!queue.isEmpty() && i < vals.size()) {
            TreeNode current = queue.poll();

            // Left child
            if (i < vals.size()) {
                Integer leftVal = vals.get(i++);
                if (leftVal != null) {
                    current.left = new TreeNode(leftVal);
                    queue.offer(current.left);
                }
            }

            // Right child
            if (i < vals.size()) {
                Integer rightVal = vals.get(i++);
                if (rightVal != null) {
                    current.right = new TreeNode(rightVal);
                    queue.offer(current.right);
                }
            }
        }

        return root;
    }

    public static List<Integer> toLevelOrder(TreeNode root) {
        if (root == null) return Collections.emptyList();

        List<Integer> result = new ArrayList<>();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            TreeNode node = queue.poll();
            if (node == null) {
                result.add(null);
            } else {
                result.add(node.val);
                queue.offer(node.left);
                queue.offer(node.right);
            }
        }

        // Trim trailing nulls
        int lastNonNull = result.size() - 1;
        while (lastNonNull >= 0 && result.get(lastNonNull) == null) {
            lastNonNull--;
        }

        return result.subList(0, lastNonNull + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode other = (TreeNode) o;
        if (this.val != other.val) return false;
        return Objects.equals(this.left, other.left) && Objects.equals(this.right, other.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, left, right);
    }

    @Override
    public String toString() {
        return toLevelOrder(this).toString();
    }
}
