package deque;


import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.Iterator;
public class LinkedListDeque<T> implements Deque<T>, Iterable<T>{
    private class StuffNode {
        public T item;
        public StuffNode next;
        public StuffNode prev;
        public StuffNode(T item,StuffNode next) {
            this.item=item;
            this.next= next;

        }
    }
    private int size;
    private final StuffNode sentinel;
    public LinkedListDeque(){
        sentinel = new StuffNode(null,null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        int size=0;
    }
    public LinkedListDeque(T x){
        sentinel = new StuffNode(null,null);
        sentinel.next = new StuffNode(x,sentinel);
        sentinel.next.prev= sentinel;
        sentinel.prev = sentinel.next;
        int size=1;
    }
    @Override
    public void addFirst(T item){
        StuffNode FirstNode = sentinel.next;
        sentinel.next = new StuffNode(item,FirstNode);
        sentinel.next.prev= sentinel;
        FirstNode.prev= sentinel.next;
        size=size+1;
    }
    @Override
    public void addLast(T item){
        StuffNode LastNode = sentinel.prev;
        LastNode.next= new StuffNode(item,sentinel);
        LastNode.next.prev=LastNode;
        sentinel.prev=LastNode.next;
        size=size+1;
    }
    @Override
    public int size(){
        return size;
    }
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public void printDeque(){
        for(StuffNode i = sentinel.next;i.next!=null; i = i.next){
            System.out.print(i.item+ "");
        }

    }
    @Override
    public T removeFirst(){
        StuffNode FirstNode = sentinel.next;
        T  FirstNodeItem=FirstNode.item;
        sentinel.next = FirstNode.next;
        FirstNode.next.prev= sentinel;
        size=size-1;
        FirstNode.item=null;
        FirstNode.next=null;
        FirstNode.prev=null;
        return FirstNodeItem;
    }
    @Override
    public T removeLast(){
        StuffNode Last2Node = sentinel.prev.prev;
        StuffNode LastNode = sentinel.prev;
        T LastNodeItem= LastNode.item;
        Last2Node.next=sentinel;
        sentinel.prev=Last2Node;
        size=size-1;
        LastNode.item=null;
        LastNode.next=null;
        LastNode.prev=null;
        return LastNodeItem;
    }
    @Override
    public T get(int index){
        if (index < 0 || isEmpty()) {
            return null;
        }
        StuffNode node=sentinel.next;
        for (int i=0;i<index;i++){
            node=node.next;
        }
        return node.item;
    }
    private class DequeIterator implements Iterator<T> {
        private int wizPos;

        public DequeIterator() {
            wizPos = 0;
        }

        public boolean hasNext() {
            return wizPos < size;
        }

        public T next() {
            T returnItem = get(wizPos);
            wizPos += 1;
            return returnItem;
        }
    }
    @Override
    public Iterator<T> iterator(){
        return new DequeIterator();
    }
    @Override
    public boolean equals(Object o){
        if (o instanceof LinkedListDeque CastList){
            if (CastList.size!=this.size){
                return false;
            }
            for(int i=0;i<this.size;i++){
                if (CastList.get(i)!=this.get(i)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public static void main(String[] args) {

        LinkedListDeque<String> lld = new LinkedListDeque<>();

        lld.addFirst("3");
        lld.addFirst("2");
        lld.addFirst("1");
        lld.addLast("4");
        lld.addLast("5");
        lld.addLast("6");
        lld.addLast("7");

        Iterator<String> seer = lld.iterator();

        while (seer.hasNext()) {
            String s = seer.next();
            System.out.println(s);
        }

        LinkedListDeque<String> lld2 = new LinkedListDeque<>();
        lld2.addFirst("1");
        lld2.addFirst("2");
        lld2.addFirst("3");

        LinkedListDeque<String> lld3 = new LinkedListDeque<>();
        lld3.addFirst("1");
        lld3.addFirst("2");
        lld3.addFirst("3");

        System.out.println(lld2.equals(lld3));
    }

}
