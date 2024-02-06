package deque;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {


    @Test
    public void addIsEmptySizeTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();

        assertTrue("ad1 should be empty", ad1.isEmpty());

        ad1.addFirst("front");

        assertEquals(1, ad1.size());
        assertFalse("ad1 should now contain 1 item", ad1.isEmpty());

        ad1.addLast("middle");
        assertEquals(2, ad1.size());

        ad1.addLast("back");
        assertEquals(3, ad1.size());

        System.out.println("addIsEmptySizeTest Done");
    }

    @Test
    public void addRemoveTest() {

        ArrayDeque<Integer> ad1 = new ArrayDeque<>();

        ad1.addLast(10);
        assertFalse("adl should now contain 1 item", ad1.isEmpty());

        ad1.removeFirst();
        assertTrue("ad1 should be empty after removal", ad1.isEmpty());

        System.out.println("addRemoveTest Done");
    }

    @Test
    public void removeLastTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();

        ad1.addLast("1");
        ad1.addLast("2");
        ad1.addLast("3");
        ad1.addLast("4");
        ad1.addLast("5");

        assertEquals("5", ad1.removeLast());
        assertEquals(4, ad1.size());

        assertEquals("4", ad1.removeLast());
        assertEquals(3, ad1.size());

        assertEquals("3", ad1.removeLast());
        assertEquals(2, ad1.size());

        assertEquals("2", ad1.removeLast());
        assertEquals(1, ad1.size());

        System.out.println("removeLastTest Done");
    }

    @Test
    public void removeFirstTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();

        ad1.addFirst("1");
        ad1.addFirst("2");
        ad1.addFirst("3");
        ad1.addFirst("4");
        ad1.addFirst("5");
        ad1.addFirst("6");

        assertEquals("6", ad1.removeFirst());
        assertEquals("5", ad1.removeFirst());
        assertEquals("4", ad1.removeFirst());
        assertEquals("3", ad1.removeFirst());
        assertEquals("2", ad1.removeFirst());
        assertEquals("1", ad1.removeFirst());

        System.out.println("removeFirst Done");
    }

    @Test
    public void getArrayDequeTest() {

        ArrayDeque<String> ad1 = new ArrayDeque<>();

        ad1.addLast("1");
        ad1.addLast("2");
        ad1.addLast("3");
        ad1.addLast("4");
        ad1.addLast("5");

        assertEquals("5", ad1.get(4));

        System.out.println("getArrayDequeTest Done");
    }
}
