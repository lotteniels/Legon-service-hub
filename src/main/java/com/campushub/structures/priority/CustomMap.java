package com.campushub.structures.priority;

// Owner: Priority Structures
// TODO: implement CustomMap
public class CustomMap<K, V> {
    private HashTable<K, V> table;

    public CustomMap(){
        this.table = new HashTable<>();  
    }

    public void put(key, value){
        table.put(key, value); 
    }

    public V get(K key){
        return table.get(key);
    }

    public V remove(K key){
        return table.remove(key);
    }

    public boolean containsKey(K key){
        return table.get(key) != null;
    }

    public int size(){
        return table.size();
    }

    
}
