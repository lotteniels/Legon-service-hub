package com.campushub.structures.tree;

// Owner: Tree Structures
// A Binary Search Tree (BST) built entirely from scratch.
// Each node stores an integer key (e.g. a request ID) and a String value
// (e.g. a location name). Keys are kept in sorted order: for every node,
// all keys in its left subtree are smaller and all keys in its right
// subtree are larger. Duplicate keys are handled by updating the value.
public class BST {

    // ── Inner node class ────────────────────────────────────────────────
    // Each Node holds a key-value pair and references to its left and
    // right children. Leaf nodes have both children set to null.
    private static class Node {
        int key; // unique identifier (e.g. request ID)
        String value; // associated data (e.g. location name)
        Node left; // left child – holds keys smaller than this node
        Node right; // right child – holds keys larger than this node

        // Creates a new leaf node with the given key and value.
        Node(int key, String value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    // The topmost node of the tree. It is null when the tree is empty.
    private Node root;

    // Constructs an empty BST.
    public BST() {
        this.root = null;
    }

    // ── INSERT ──────────────────────────────────────────────────────────
    // Adds a new key-value pair to the tree. The node is placed so that
    // the BST ordering property is maintained (left < parent < right).
    // If the key already exists, its value is updated instead of creating
    // a duplicate node.
    //
    // @param key the integer key to insert (e.g. request ID)
    // @param value the string value to associate with the key
    public void insert(int key, String value) {
        root = insertRecursive(root, key, value);
    }

    // Recursive helper for insert.
    // Walks down the tree comparing the new key with each node's key:
    // • key < node.key → go left
    // • key > node.key → go right
    // • key == node.key → update the existing node's value (duplicate)
    // When a null spot is reached, a new node is created there.
    //
    // Returns the (possibly new) subtree root so the parent link is updated.
    private Node insertRecursive(Node node, int key, String value) {
        // Base case: we've reached an empty spot – create a new node here.
        if (node == null) {
            return new Node(key, value);
        }

        if (key < node.key) {
            // The new key is smaller, so it belongs in the left subtree.
            node.left = insertRecursive(node.left, key, value);
        } else if (key > node.key) {
            // The new key is larger, so it belongs in the right subtree.
            node.right = insertRecursive(node.right, key, value);
        } else {
            // The key already exists – update the value rather than
            // inserting a duplicate, keeping the tree free of duplicates.
            node.value = value;
        }

        return node;
    }

    // ── SEARCH ──────────────────────────────────────────────────────────
    // Looks up a key in the tree and returns its associated value.
    // Returns null if the key is not found.
    //
    // @param key the integer key to search for
    // @return the value mapped to the key, or null if absent
    public String search(int key) {
        return searchRecursive(root, key);
    }

    // Recursive helper for search.
    // At each node the key is compared:
    // • key < node.key → the target must be in the left subtree
    // • key > node.key → the target must be in the right subtree
    // • key == node.key → found it – return the value
    // If we reach null the key does not exist in the tree.
    private String searchRecursive(Node node, int key) {
        // Base case: reached a null child – the key is not in the tree.
        if (node == null) {
            return null;
        }

        if (key < node.key) {
            // Key is smaller – search the left subtree.
            return searchRecursive(node.left, key);
        } else if (key > node.key) {
            // Key is larger – search the right subtree.
            return searchRecursive(node.right, key);
        } else {
            // Keys match – return the stored value.
            return node.value;
        }
    }

    // ── IN-ORDER TRAVERSAL ──────────────────────────────────────────────
    // Prints every key-value pair in ascending key order.
    // The traversal visits: left subtree → current node → right subtree.
    // Because of the BST ordering property this always produces a sorted
    // sequence of keys.
    public void inorderTraversal() {
        inorderRecursive(root);
    }

    // Recursive helper for in-order traversal.
    // 1. Recursively visit the left subtree (smaller keys first).
    // 2. Print the current node's key and value.
    // 3. Recursively visit the right subtree (larger keys next).
    private void inorderRecursive(Node node) {
        if (node == null) {
            return;
        }

        // Step 1 – visit left subtree (all keys smaller than this node).
        inorderRecursive(node.left);

        // Step 2 – process the current node.
        System.out.println("Key: " + node.key + ", Value: " + node.value);

        // Step 3 – visit right subtree (all keys larger than this node).
        inorderRecursive(node.right);
    }
}
