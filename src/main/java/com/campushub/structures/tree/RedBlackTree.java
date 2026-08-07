package com.campushub.structures.tree;

// Owner: Tree Structures
// A Red-Black Tree built entirely from scratch.
//
// A Red-Black Tree is a self-balancing Binary Search Tree. After every
// insertion, the tree fixes itself using ROTATIONS and RECOLORING so that
// no path from the root to a leaf is more than twice as long as any other.
// This guarantees O(log n) performance for insert and search, even in the
// worst case (which a plain BST cannot promise).
//
// The five Red-Black properties that must ALWAYS hold:
//   1. Every node is either RED or BLACK.
//   2. The root is always BLACK.
//   3. Every leaf (NIL sentinel) is BLACK.
//   4. If a node is RED, both of its children must be BLACK
//      (no two red nodes in a row on any path).
//   5. Every path from a given node down to any NIL leaf contains the
//      same number of BLACK nodes (the "black-height" is uniform).
//
// Stores an integer key (e.g. request ID) and a String value
// (e.g. location name), just like the BST.
public class RedBlackTree {

    // ── Colour constants ────────────────────────────────────────────────
    private static final boolean RED   = true;
    private static final boolean BLACK = false;

    // ── Inner node class ────────────────────────────────────────────────
    // Each node stores a key-value pair, a colour (RED or BLACK), and
    // links to its left child, right child, and parent.
    private static class Node {
        int key;         // unique identifier (e.g. request ID)
        String value;    // associated data   (e.g. location name)
        boolean color;   // RED (true) or BLACK (false)
        Node left;       // left child
        Node right;      // right child
        Node parent;     // parent node (needed for fix-up walk)

        Node(int key, String value, boolean color, Node nil) {
            this.key    = key;
            this.value  = value;
            this.color  = color;
            this.left   = nil;
            this.right  = nil;
            this.parent = nil;
        }
    }

    // ── Sentinel NIL node ───────────────────────────────────────────────
    // Instead of using null for empty children, we use a single shared
    // "NIL" node that is always BLACK. This simplifies the fix-up code
    // because we never have to null-check before reading a node's colour.
    private final Node NIL;

    // The topmost node of the tree.
    private Node root;

    // Constructs an empty Red-Black Tree.
    public RedBlackTree() {
        NIL       = new Node(0, null, BLACK, null);
        NIL.left  = NIL;
        NIL.right = NIL;
        NIL.parent = NIL;
        root      = NIL;
    }

    // ════════════════════════════════════════════════════════════════════
    //  INSERT
    // ════════════════════════════════════════════════════════════════════
    // Adds a new key-value pair to the tree.
    //
    // Step 1 – Standard BST insertion: walk down the tree and attach the
    //          new node as a RED leaf at the correct position.
    // Step 2 – Fix-up: walk back up the tree, applying rotations and
    //          recoloring to restore the five Red-Black properties.
    //
    // If the key already exists its value is updated (no duplicates).
    //
    // @param key   the integer key to insert
    // @param value the string value to associate with the key
    public void insert(int key, String value) {

        // ── Step 1: BST-style walk to find the insertion point ──────────
        Node parent = NIL;
        Node current = root;

        while (current != NIL) {
            parent = current;
            if (key < current.key) {
                current = current.left;
            } else if (key > current.key) {
                current = current.right;
            } else {
                // Key already exists – update value and return.
                // No structural change, so no fix-up is needed.
                current.value = value;
                return;
            }
        }

        // Create the new node. New nodes are always coloured RED because
        // adding a red node does not change the black-height of any path,
        // so property 5 is preserved automatically. We only need to worry
        // about property 4 (no two reds in a row), which the fix-up handles.
        Node newNode = new Node(key, value, RED, NIL);
        newNode.parent = parent;

        if (parent == NIL) {
            // Tree was empty – the new node becomes the root.
            root = newNode;
        } else if (key < parent.key) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // ── Step 2: fix any Red-Black violations ────────────────────────
        insertFixUp(newNode);
    }

    // ── INSERT FIX-UP ───────────────────────────────────────────────────
    // After inserting a RED node, the only property that can be violated
    // is property 4: the new node's parent might also be RED (two reds
    // in a row). This method walks up the tree and fixes violations.
    //
    // There are THREE cases (and their mirror images when the parent is
    // a right child instead of a left child):
    //
    //   Case 1 – Uncle is RED
    //            → Recolor parent and uncle to BLACK, grandparent to RED.
    //              Then move up to the grandparent and check again.
    //
    //   Case 2 – Uncle is BLACK and the new node is an "inner child"
    //            (left-right or right-left zig-zag)
    //            → Rotate the parent to convert into Case 3.
    //
    //   Case 3 – Uncle is BLACK and the new node is an "outer child"
    //            (left-left or right-right straight line)
    //            → Rotate the grandparent and recolor.
    //
    private void insertFixUp(Node node) {

        // Keep fixing as long as the node's parent is RED (violation of
        // property 4). If the parent is BLACK there is no violation.
        while (node.parent.color == RED) {

            if (node.parent == node.parent.parent.left) {
                // ── Parent is a LEFT child of grandparent ───────────────
                Node uncle = node.parent.parent.right;

                if (uncle.color == RED) {
                    // ╔══════════════════════════════════════════════════╗
                    // ║  CASE 1: Uncle is RED                           ║
                    // ╠══════════════════════════════════════════════════╣
                    // ║  WHY: Both parent and uncle are red, so we can  ║
                    // ║  push the "redness" up to the grandparent by    ║
                    // ║  recoloring. This fixes the two-reds-in-a-row   ║
                    // ║  violation locally, but the grandparent (now    ║
                    // ║  red) might create a new violation higher up,   ║
                    // ║  so we move up and check again.                 ║
                    // ╚══════════════════════════════════════════════════╝
                    node.parent.color = BLACK;          // parent → BLACK
                    uncle.color       = BLACK;          // uncle  → BLACK
                    node.parent.parent.color = RED;     // grandparent → RED
                    node = node.parent.parent;          // move up to grandparent

                } else {
                    if (node == node.parent.right) {
                        // ╔══════════════════════════════════════════════╗
                        // ║  CASE 2: Uncle is BLACK, node is RIGHT      ║
                        // ║          child (inner / zig-zag shape)      ║
                        // ╠══════════════════════════════════════════════╣
                        // ║  WHY: The node and its parent form a        ║
                        // ║  "bent" shape (left-right). A single        ║
                        // ║  rotation on the parent straightens this    ║
                        // ║  into a left-left line, converting it       ║
                        // ║  into Case 3 which we can then fix with    ║
                        // ║  one more rotation.                         ║
                        // ╚══════════════════════════════════════════════╝
                        node = node.parent;             // move up to parent
                        leftRotate(node);                // straighten the bend
                    }

                    // ╔══════════════════════════════════════════════════╗
                    // ║  CASE 3: Uncle is BLACK, node is LEFT child     ║
                    // ║          (outer / straight line shape)           ║
                    // ╠══════════════════════════════════════════════════╣
                    // ║  WHY: The node, parent, and grandparent form a  ║
                    // ║  straight left-left line. We rotate the         ║
                    // ║  grandparent to the right and swap the colours  ║
                    // ║  of the parent and grandparent. This fixes the  ║
                    // ║  two-reds-in-a-row violation without breaking   ║
                    // ║  the black-height property.                     ║
                    // ╚══════════════════════════════════════════════════╝
                    node.parent.color = BLACK;          // parent → BLACK
                    node.parent.parent.color = RED;     // grandparent → RED
                    rightRotate(node.parent.parent);    // rotate grandparent right
                }

            } else {
                // ── Parent is a RIGHT child of grandparent ──────────────
                // This is the exact MIRROR of the three cases above.
                // Left and right are swapped throughout.
                Node uncle = node.parent.parent.left;

                if (uncle.color == RED) {
                    // CASE 1 (mirror): Uncle is RED → recolor and move up.
                    node.parent.color = BLACK;
                    uncle.color       = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;

                } else {
                    if (node == node.parent.left) {
                        // CASE 2 (mirror): node is LEFT child (inner / zig-zag).
                        // Rotate parent right to straighten into Case 3.
                        node = node.parent;
                        rightRotate(node);
                    }

                    // CASE 3 (mirror): node is RIGHT child (outer / straight line).
                    // Rotate grandparent left and recolor.
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    leftRotate(node.parent.parent);
                }
            }
        }

        // Property 2: the root must always be BLACK.
        // After recoloring the root may have turned red, so we force it black.
        root.color = BLACK;
    }

    // ════════════════════════════════════════════════════════════════════
    //  LEFT ROTATE
    // ════════════════════════════════════════════════════════════════════
    // Rotates the subtree rooted at node 'x' to the LEFT.
    //
    // Before:          After:
    //     x               y
    //    / \             / \
    //   a   y    →     x   c
    //      / \        / \
    //     b   c      a   b
    //
    // WHY we rotate: rotations restructure the tree to fix Red-Black
    // violations while preserving the BST ordering property (in-order
    // sequence stays the same).
    private void leftRotate(Node x) {
        Node y = x.right;       // y is x's right child (will become new root of subtree)
        x.right = y.left;       // x adopts y's left subtree (b) as its right child

        if (y.left != NIL) {
            y.left.parent = x;  // update b's parent pointer
        }

        y.parent = x.parent;   // y takes x's old position in the tree

        if (x.parent == NIL) {
            root = y;           // x was root → y is now root
        } else if (x == x.parent.left) {
            x.parent.left = y;  // x was a left child → y replaces it
        } else {
            x.parent.right = y; // x was a right child → y replaces it
        }

        y.left = x;            // x becomes y's left child
        x.parent = y;          // update x's parent pointer
    }

    // ════════════════════════════════════════════════════════════════════
    //  RIGHT ROTATE
    // ════════════════════════════════════════════════════════════════════
    // Rotates the subtree rooted at node 'y' to the RIGHT.
    // This is the exact mirror of leftRotate.
    //
    // Before:          After:
    //       y             x
    //      / \           / \
    //     x   c   →    a   y
    //    / \               / \
    //   a   b             b   c
    //
    private void rightRotate(Node y) {
        Node x = y.left;       // x is y's left child (will become new root of subtree)
        y.left = x.right;      // y adopts x's right subtree (b) as its left child

        if (x.right != NIL) {
            x.right.parent = y; // update b's parent pointer
        }

        x.parent = y.parent;   // x takes y's old position in the tree

        if (y.parent == NIL) {
            root = x;           // y was root → x is now root
        } else if (y == y.parent.left) {
            y.parent.left = x;  // y was a left child → x replaces it
        } else {
            y.parent.right = x; // y was a right child → x replaces it
        }

        x.right = y;           // y becomes x's right child
        y.parent = x;          // update y's parent pointer
    }

    // ════════════════════════════════════════════════════════════════════
    //  SEARCH
    // ════════════════════════════════════════════════════════════════════
    // Looks up a key in the tree and returns its associated value.
    // Works exactly like BST search – the balancing does not change
    // the search logic because the BST ordering property is always
    // preserved by rotations.
    //
    // @param key the integer key to search for
    // @return    the value mapped to the key, or null if absent
    public String search(int key) {
        Node current = root;

        while (current != NIL) {
            if (key < current.key) {
                // Key is smaller – go left.
                current = current.left;
            } else if (key > current.key) {
                // Key is larger – go right.
                current = current.right;
            } else {
                // Keys match – return the stored value.
                return current.value;
            }
        }

        // Reached a NIL leaf – the key does not exist in the tree.
        return null;
    }

    // ════════════════════════════════════════════════════════════════════
    //  PRINT TREE
    // ════════════════════════════════════════════════════════════════════
    // Prints the tree sideways so the structure, colours, and balance
    // are clearly visible. The root appears on the left, right children
    // go up, and left children go down.
    //
    // Example output for keys 1–5:
    //          ── [5 BLACK] "Location E"
    //     ── [4 BLACK] "Location D"
    // ── [3 BLACK] "Location C"
    //          ── [2 BLACK] "Location B"
    //     ── [1 BLACK] "Location A"
    //
    public void printTree() {
        if (root == NIL) {
            System.out.println("(empty tree)");
            return;
        }
        printRecursive(root, "", false);
    }

    // Recursive helper that prints each node with indentation showing
    // its depth and a label showing its key, colour, and value.
    // It prints the RIGHT subtree first (appears on top), then the
    // current node, then the LEFT subtree (appears on the bottom).
    private void printRecursive(Node node, String indent, boolean isLeft) {
        if (node == NIL) {
            return;
        }

        // Print right subtree first (it will appear above this node).
        printRecursive(node.right, indent + (isLeft ? "│    " : "     "), false);

        // Print the current node with its colour and value.
        String colorLabel = (node.color == RED) ? "RED" : "BLACK";
        System.out.println(indent + (isLeft ? "└── " : "┌── ")
                + "[" + node.key + " " + colorLabel + "] \"" + node.value + "\"");

        // Print left subtree (it will appear below this node).
        printRecursive(node.left, indent + (isLeft ? "     " : "│    "), true);
    }
}
