package com.campushub.structures.priority;

// Owner: Priority Structures
// TODO: implement CustomSet

public class CustomSet<E> {
    
    private CustomMap<E, Object> map;
    
    private static final Object PRESENT = new Object();
  
    public CustomSet(){
        this.map = new CustomMap<>();
    }   

    public void add(E element) {
        map.put(element, PRESENT);
    }

    public void remove(E element) {
        map.remove(element);
    }

    public boolean contains(E element) {
        return map.containsKey(element);
    }

    public int size() {
        return map.size();
    }
}
 