package com.campushub.structures.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StackTest {

    private Stack<String> stack;

    @BeforeEach
    public void setUp() {
        stack = new Stack<>(4);
    }

    @Test
    public void testInitialization() {
        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
        assertEquals(4, stack.capacity());
    }

    @Test
    public void testPushAndPop() {
        stack.push("JQB");
        stack.push("UGBS");
        stack.push("Pentagon");

        assertEquals(3, stack.size());
        assertEquals("Pentagon", stack.pop());
        assertEquals("UGBS", stack.pop());
        assertEquals("JQB", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testPeek() {
        stack.push("Alpha");
        stack.push("Beta");

        assertEquals("Beta", stack.peek());
        assertEquals(2, stack.size()); // Size should remain unchanged after peek
    }

    @Test
    public void testAutoDoublingResize() {
        stack.push("1");
        stack.push("2");
        stack.push("3");
        stack.push("4");
        assertEquals(4, stack.capacity());

        stack.push("5"); // Auto-doubles capacity to 8
        assertEquals(5, stack.size());
        assertEquals(8, stack.capacity());
        assertTrue(stack.getResizeCount() > 0);
    }

    @Test
    public void testClear() {
        stack.push("A");
        stack.push("B");
        stack.clear();

        assertTrue(stack.isEmpty());
        assertEquals(0, stack.size());
    }

    @Test
    public void testEmptyStackExceptions() {
        assertThrows(IllegalStateException.class, () -> stack.pop());
        assertThrows(IllegalStateException.class, () -> stack.peek());
    }

    @Test
    public void testToArrayAndMetrics() {
        stack.push("First");
        stack.push("Second");

        Object[] arr = stack.toArray();
        assertEquals(2, arr.length);
        assertEquals("First", arr[0]);
        assertEquals("Second", arr[1]);

        assertEquals(2, stack.getPushCount());
        assertEquals(0, stack.getPopCount());

        stack.pop();
        assertEquals(1, stack.getPopCount());

        stack.resetOpCounters();
        assertEquals(0, stack.getPushCount());
        assertEquals(0, stack.getPopCount());
    }
}
