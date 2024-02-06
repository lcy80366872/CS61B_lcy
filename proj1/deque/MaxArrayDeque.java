package deque;
import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.Comparator;
public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cmp;
    public MaxArrayDeque(Comparator<T> c){
        super();
        cmp=c;;
    }

    public T max(){
        if (isEmpty()) {
            return null;
        }
        T maxitem=this.get(0);
        for (T i:this){
            if (cmp.compare(i,maxitem)>0){
                maxitem=i;
            }
        }
        if (cmp.compare(this.get(super.size()-1),maxitem)>0){
            maxitem=this.get(super.size()-1);
        }
        return maxitem;
    }
    public T max(Comparator<T> c){
        if (isEmpty()) {
            return null;
        }
        T maxitem=this.get(0);
        for (T i:this){
            if (c.compare(i,maxitem)>0){
                maxitem=i;
            }
        }
        if (c.compare(this.get(super.size()-1),maxitem)>0){
            maxitem=this.get(super.size()-1);
        }
        return maxitem;

    }
    public static void main(String[] args) {
        Comparator<Integer> cmp=new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
        MaxArrayDeque mad1 = new MaxArrayDeque(cmp);

        int n = 99;

        for (int i = n; i >= 0; i--) {
            mad1.addFirst(i);
        }

        System.out.println(mad1.max());
    }
}
