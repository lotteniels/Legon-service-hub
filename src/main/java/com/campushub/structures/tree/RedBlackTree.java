package com.campushub.structures.tree;

// Owner: Tree Structures
// A Red-Black Tree built entirely from scratch.
//
// A Red-Black Tree is a self-balancing Binary Search Tree. After every
// insertion, the tree fixes itself using rotations and recoloring so that
// it stays roughly balanced. This guarantees O(log n) performance for
// insert and search, even in the worst case.
public class RedBlackTree {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    // Node class representing a single key-value pair in the tree.
    private static class Node {
        int key;
        String value;
        boolean color;
        Node left, right, parent;

        Node(int key, String value) {
            this.key = key;
            this.value = value;
            this.color = RED; // new nodes are always initially RED
        }
    }

    // A sentinel node used to represent all leaf nodes (NULLs).
    // Using a single shared NIL node makes rotation logic much simpler
    // because we never have to check for null pointers.
    private final Node NIL;
    private Node root;

    public RedBlackTree() {
        NIL = new Node(-1, null);
        NIL.color = BLACK;
        root = NIL;
    }

    // Insert method
    // Inserts a new integer key and string value into the tree.
    public void insert(int key, String value) {
        Node newNode = new Node(key, value);
        newNode.left = NIL;
        newNode.right = NIL;

        Node parent = NIL;
        Node current = root;

        // Step 1: Standard BST insertion (find the right leaf spot)
        while (current != NIL) {
            parent = current;
            if (key < current.key) {
                current = current.left;
            } else if (key > current.key) {
                current = current.right;
            } else {
                // If the key already exists, just update its value and return early.
                current.value = value;
                return;
            }
        }

        newNode.parent = parent;

        if (parent == NIL) {
            // Tree was empty, this is the first node
            root = newNode;
        } else if (key < parent.key) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // Step 2: Fix any Red-Black properties that might have been violated.
        if (newNode.parent == NIL) {
            newNode.color = BLACK; // Root must always be black
            return;
        }

        if (newNode.parent.parent == NIL) {
            return; // Grandparent doesn't exist yet, no complex violations possible
        }

        insertFixUp(newNode);
    }

    // Insert FixUp method
    // Restores the red-black properties after an insertion.
    private void insertFixUp(Node node) {
        // While there is a "two reds in a row" violation
        while (node.parent.color == RED) {
            
            if (node.parent == node.parent.parent.left) {
                // Parent is a left child of grandparent
                Node uncle = node.parent.parent.right;

                if (uncle.color == RED) {
                    // Case 1: Uncle is RED
                    // Both parent and uncle are red, so we can push the redness
                    // up to the grandparent by recoloring. 
                    node.parent.color = BLACK;          // parent → BLACK
                    uncle.color       = BLACK;          // uncle  → BLACK
                    node.parent.parent.color = RED;     // grandparent → RED
                    node = node.parent.parent;          // move up to grandparent

                } else {
                    if (node == node.parent.right) {
                        // Case 2: Uncle is BLACK, node is RIGHT child (inner / zig-zag shape)
                        // The node and its parent form a bent shape. A single rotation 
                        // on the parent straightens this into a left-left line (Case 3).
                        node = node.parent;
                        leftRotate(node);
                    }

                    // Case 3: Uncle is BLACK, node is LEFT child (outer / straight line shape)
                    // The node, parent, and grandparent form a straight line. We rotate the 
                    // grandparent right and swap colours of the parent and grandparent.
                    node.parent.color = BLACK;          // parent → BLACK
                    node.parent.parent.color = RED;     // grandparent → RED
                    rightRotate(node.parent.parent);    // rotate grandparent right
                }

            } else {
                // Mirror Cases: Parent is a right child of grandparent
                // This is the exact mirror of the three cases above (left/right swapped).
                Node uncle = node.parent.parent.left;

                if (uncle.color == RED) {
                    // Case 1 (mirror): Uncle is RED
                    node.parent.color = BLACK;
                    uncle.color       = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;

                } else {
                    if (node == node.parent.left) {
                        // Case 2 (mirror): node is LEFT child (inner / zig-zag shape)
                        node = node.parent;
                        rightRotate(node);
                    }

                    // Case 3 (mirror): node is RIGHT child (outer / straight line shape)
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    leftRotate(node.parent.parent);
                }
            }
        }

        // The root must always be BLACK.
        root.color = BLACK;
    }

    // Left Rotate method
    // Rotates the subtree rooted at node 'x' to the LEFT.
    private void leftRotate(Node x) {
        Node y = x.right;       // y is x's right child
        x.right = y.left;       // x adopts y's left subtree

        if (y.left != NIL) {
            y.left.parent = x;
        }

        y.parent = x.parent;

        if (x.parent == NIL) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    // Right Rotate method
    // Rotates the subtree rooted at node 'y' to the RIGHT.
    // This is the exact mirror of leftRotate.
    private void rightRotate(Node y) {
        Node x = y.left;       // x is y's left child
        y.left = x.right;      // y adopts x's right subtree

        if (x.right != NIL) {
            x.right.parent = y;
        }

        x.parent = y.parent;

        if (y.parent == NIL) {
            root = x;
        } else if (y == y.parent.left) {
            y.parent.left = x;
        } else {
            y.parent.right = x;
        }

        x.right = y;
        y.parent = x;
    }

    // Search method
    // Looks up a key in the tree and returns its associated value.
    public String search(int key) {
        Node current = root;

        while (current != NIL) {
            if (key < current.key) {
                current = current.left;
            } else if (key > current.key) {
                current = current.right;
            } else {
                return current.value; // found it
            }
        }

        return null; // not found
    }

    // Print Tree method
    // Prints the tree sideways so the structure, colours, and balance are visible.
    public void printTree() {
        if (root == NIL) {
            System.out.println("(empty tree)");
            return;
        }
        printRecursive(root, "", false);
    }

    // Recursive helper that prints each node with indentation.
    // It prints right subtree first, then current node, then left subtree.
    private void printRecursive(Node node, String indent, boolean isLeft) {
        if (node == NIL) {
            return;
        }

        // Print right subtree first
        printRecursive(node.right, indent + (isLeft ? "│    " : "     "), false);

        // Print current node
        String colorLabel = (node.color == RED) ? "RED" : "BLACK";
        System.out.println(indent + (isLeft ? "└── " : "┌── ")
                + "[" + node.key + " " + colorLabel + "] \"" + node.value + "\"");

        // Print left subtree
        printRecursive(node.left, indent + (isLeft ? "     " : "│    "), true);
    }
}
