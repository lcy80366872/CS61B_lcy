package deque;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T>{
    private int size;
    private int nextFirst;
    private int nextLast;
    private T[] items;

    public ArrayDeque(){
        items= (T [])new Object[8];
        nextFirst=4;
        nextLast=5;
        size=0;
    }
    private void resize(int length){
        T[] a=  (T [])new Object[length];
        if (nextFirst==items.length-1){
            System.arraycopy(items,0,a,0,size);
            items=a;
        }
        else {

            System.arraycopy(items,nextFirst+1,a,nextFirst+1,size-nextFirst-1);
            System.arraycopy(items,0,a,size,nextFirst+1);
            nextLast=size+nextFirst+1;
            items=a;
        }

    }
    @Override
    public void addFirst(T item){
        if (size==items.length){
            resize(2*size);
        }


        items[nextFirst]=item;
        nextFirst -=1;
        if (nextFirst ==-1 ){
            nextFirst= items.length-1;
        }
        size +=1;
    }
    @Override
    public void addLast(T item){
        if (size==items.length){
            resize(2*size);
        }
        items[nextLast]=item;
        nextLast +=1;
        if (nextLast == items.length){
            nextLast= 0;
        }
        size +=1;
    }
    @Override
    public int size(){
        return size;
    }
    @Override
    public void printDeque(){
        for (int i =nextFirst+1;i<nextLast;i++){
            System.out.print(items[i]);
        }
    }
    @Override
    public T removeFirst(){
        if (nextFirst ==items.length-1 ){
            T remove= items[0];
            items[0]=null;
            nextFirst= 0;
            size -=1;
            if( (items.length>=16)&&(size<(items.length/4)) ){
                resize(items.length/4);
            }
            return remove;
        }
        else {
            T remove= items[nextFirst+1];
            items[nextFirst+1]=null;
            nextFirst +=1;
            size -=1;
            if( (items.length>=16)&&(size<(items.length/4)) ){
                resize(items.length/4);
            }
            return remove;
        }

    }
    @Override
    public T removeLast(){
        if (nextLast ==0 ){
            T remove= items[items.length-1];
            items[items.length-1]=null;
            nextLast= items.length-1;
            size -=1;
            if( (items.length>=16)&&(size<(items.length/4)) ){
                resize(items.length/4);
            }
            return remove;
        }
        else {
            T remove= items[nextLast-1];
            items[nextLast-1]=null;
            nextLast -=1;
            size -=1;
            if( (items.length>=16)&&(size<(items.length/4)) ){
                resize(items.length/4);
            }
            return remove;
        }
    }
    @Override
    public T get(int index){
        int indexx=nextFirst+1+index;
        if (indexx>=items.length){
            indexx -=items.length;
        }
        return items[indexx];
    }
    public Iterator <T> iterator(){
        return new ArrayDequeIterator();
    }
    private class ArrayDequeIterator implements Iterator<T>{
        int pos;
        public ArrayDequeIterator(){
            pos=0;
        }
        @Override
        public T next(){
            T array_item= get(pos);
            pos +=1;
            return array_item;
        }
        @Override
        public boolean hasNext(){
            return get(pos+1)!=null;
        }
    }
    @Override
    public boolean equals(Object o){
        if (o instanceof ArrayDeque CastArrays){
            if (CastArrays.size!=this.size){
                return false;
            }
            for (int i=0;i<size;i++){
                if (CastArrays.get(i)!=this.get(i)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public static void main(String[] args) {
        ArrayDeque<String> ad1=new ArrayDeque<>();
        ad1.addLast("1");
        ad1.addLast("2");
        ad1.addLast("3");
        ad1.addLast("4");
        ad1.addLast("5");

        Iterator<String> seer= ad1.iterator();
        while (seer.hasNext()){
            System.out.println(seer.next());
        }
        ArrayDeque<String> ad2=new ArrayDeque<>();
        ad2.addLast("1");
        ad2.addLast("2");
        ad2.addLast("3");
        ad2.addLast("4");
        ad2.addLast("7");

        System.out.println(ad2.equals(ad1));

    }
}
