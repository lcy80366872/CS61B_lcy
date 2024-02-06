package bstmap;
import java.lang.UnsupportedOperationException;
import java.lang.Comparable;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K , V> {
    private BSTNode root;
    public BSTMap() {
    }
    private class BSTNode{
        private K key;
        private V value;
        private BSTNode left;
        private BSTNode right;
        private int size;  // number of nodes in subtree

        public BSTNode(K key,V value,int size){
            this.key=key;
            this.value=value;
            this.size=size;
        }
    }
    /** Removes all of the mappings from this map. */
    @Override
    public void clear(){
        root=clear(root);
    }
    private BSTNode clear(BSTNode x){
        if(x==null) return null;
        if (x.size==1){
            x=null;
            return x;
        }
        else {
            x = clear(x.left);
        }
        return x;
    }


    @Override/* Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key){
        return get(key)!=null;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key){
        return get(root,key);
    }
    private V get(BSTNode x,K key){
        if (x==null) return null;
        int cmp =key.compareTo(x.key);
        if (cmp>0){
            return get(x.right,key);
        }
        else if (cmp<0){
            return get(x.left,key);
        }
        else return x.value;
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size(){
        return size(root);
    }
    private int size(BSTNode x){
        if (x==null) return 0;
        return x.size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value){
        if (key==null) throw new IllegalArgumentException("calls put() with a null key");;
        if (value==null){
            remove(key);
            return;
        }
        root=put(root,key,value);

    }
    private BSTNode put(BSTNode x,K key, V value){
        if (x==null) return new BSTNode(key,value,1);
        int cmp = key.compareTo(x.key);
        if (cmp>0){
            x.right= put(x.right,key,value);
        }
        else if (cmp<0){
            x.left= put(x.left,key,value);
        }
        else x.value=value;
        x.size= 1 + size(x.left) + size(x.right);

        return x;
    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet(){
        throw new UnsupportedOperationException();
    }


    private BSTNode min(BSTNode x){
        if (x.left==null)return x;
        else return min(x.left);
    }
    public void Deletemin(BSTNode x){
        root= deletemin(root);
    }
    private BSTNode deletemin(BSTNode x){
        if (x.left==null){return x.right;}
        x.left=deletemin(x.left);
        x.size =1+size(x.left)+size(x.right);
        return x;
    }
    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key){
        V value= get(key);
        root = remove(root,key);
        return value;
    }
    private BSTNode remove(BSTNode x,K key){

        if (key==null||x==null) return null;
        int cmp =key.compareTo(x.key);
        if (cmp>0) x.right= remove(x.right,key);
        else if (cmp<0) x.left= remove(x.left,key);
        else {
            if (x.left==null) return x.right;
            if (x.right==null) return x.left;
            BSTNode t=x;
            x=min(t.right);
            x.right = deletemin(t.right);
            x.left=t.left;
        }
        x.size=1+size(x.left)+size(x.right);
        return x;
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value){
        if(value== get(key)){
            root = remove(root,key);
        }
        return value;
    }
    @Override
    public Iterator<K> iterator(){
        throw new UnsupportedOperationException();
    }
}
