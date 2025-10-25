package lterator_v3;

class LinkedList_ implements Collection_ {

    LinkedList_.Node head = null;
    LinkedList_.Node tail = null;
    private int size = 0;

    public void add(Object o) {
        LinkedList_.Node n = new LinkedList_.Node(o);
        n.next = null;
        if (head == null) {
            head = n;
            tail = n;
        }
        tail.next = n;
        tail = n;
        size++;
    }

    public int size() {
        return size;
    }

    private class Node {
        private Object o;
        LinkedList_.Node next;

        public Node(Object o) {
            this.o = o;
        }
    }
}
