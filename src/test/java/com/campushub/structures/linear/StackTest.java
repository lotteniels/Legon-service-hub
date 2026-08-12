package com.campushub.structures.linear;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StackTest {

    @Test
    public void testPushAndPop() {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    public void testPeek() {
        Stack<String> stack = new Stack<>();
        stack.push("Hello");
        assertEquals("Hello", stack.peek());
        assertEquals(1, stack.size());
    }

    @Test
    public void testEmptyStackExceptions() {
        Stack<Double> stack = new Stack<>();
        assertThrows(IllegalStateException.class, () -> stack.pop());
        assertThrows(IllegalStateException.class, () -> stack.peek());
    }
}
