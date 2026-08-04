package com.campushub.structures.priority;

// Owner: Priority Structures
// TODO: implement HashTable
public class HashTable<K, V> {
    private static class HashNode<K,V>{
        K key;
        V value;
        HashNode<K, V> next;

        public HashNode(K key, V value){
            this.key = key;
            this.value = value; 
        }
    }

    private HashNode<K, V>[] buckets;
    private int size;
    private int capacity;
    private static final double DEFAULT_LOAD_FACTOR = 0.75;
    private int totalCollisions;

    @SuppressWarnings("unchecked") 
    public HashTable(int capacity){
        this.capacity = capacity;
        this.buckets = new HashNode[capacity];
        this.size = 0;
        this.totalCollisions = 0;
    }  

    public HashTable(){
        this(16);
    }

    private int getBucketIndex(K key){
        int hashCode = key.hashCode(); 
        return Math.abs(hashCode) % capacity; 
    }


    public void put(K key, V value){
        if (key == null) return; 

        int bucketIndex = getBucketIndex(key); 
        HashNode<K,V> head = buckets[bucketIndex]; 

        HashNode<K, V> current = head;
        while (current != null){
            if (current.key.equals(key)){
                current.value = value;
                return; 
            }
            current = current.next;
        }
        size ++;
        HashNode<K, V> newNode = new HashNode<>(key, value); 


        if (head != null){
            totalCollisions++;
        } 

        newNode.next = head;
        buckets[bucketIndex] = newNode; 

        if ((1.0 *size) / capacity >= DEFAULT_LOAD_FACTOR){
            resize();
        }
    }

    @SuppressWarnings("unchecked")
    private void resize(){
        HashNode<K, V>[] oldBuckets = buckets;
        capacity = capacity *2;
        buckets = new HashNode[capacity];
        size = 0;
        totalCollisions = 0;


        for (HashNode<K, V> headNode : oldBuckets){
            HashNode<K, V> current = headNode;

            while (current != null){
                put(current.key, current.value); 
                current = current.next;
            }
        }
    }

    public V get(K key) {
        if (key == null) return null;
        int bucketIndex = getBucketIndex(key);
        HashNode<K, V> current = buckets[bucketIndex];

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value; 
            }
            current = current.next;
        }
        return null; 
    }

    public V remove(K key) {
        if (key == null) return null;

        int bucketIndex = getBucketIndex(key);
        HashNode<K, V> current = buckets[bucketIndex];
        HashNode<K, V> previous = null;


        while (current != null) {
            if (current.key.equals(key)) {
                if (previous == null) { 
                    buckets[bucketIndex] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return current.value;
            }
            previous = current;
            current = current.next;
        }
        return null; 
    }   



    public int getCollisionStats() {
        return totalCollisions;
    }
    

    
    public int size() {
        return size;
    }
}
