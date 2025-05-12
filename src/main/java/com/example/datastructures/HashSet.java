package com.example.datastructures;

import java.util.LinkedList;

public class HashSet<T> {
    private static final int INITIAL_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private LinkedList<T>[] buckets;
    private int size;

    public HashSet() {
        buckets = new LinkedList[INITIAL_CAPACITY];
        size = 0;
    }

    public boolean add(T value) {
        if (contains(value))
            return false;

        if ((float) size / buckets.length > LOAD_FACTOR) {
            resize();
        }

        int index = getBucketIndex(value);
        if (buckets[index] == null) {
            buckets[index] = new LinkedList<>();
        }

        buckets[index].add(value);
        size++;
        return true;
    }

    public boolean contains(T value) {
        int index = getBucketIndex(value);
        LinkedList<T> bucket = buckets[index];
        return bucket != null && bucket.contains(value);
    }

    public boolean remove(T value) {
        int index = getBucketIndex(value);
        LinkedList<T> bucket = buckets[index];
        if (bucket != null && bucket.remove(value)) {
            size--;
            return true;
        }
        return false;
    }

    public int size() {
        return size;
    }

    private int getBucketIndex(T value) {
        return (value == null) ? 0 : Math.abs(value.hashCode()) % buckets.length;
    }

    private void resize() {
        LinkedList<T>[] oldBuckets = buckets;
        buckets = new LinkedList[oldBuckets.length * 2];
        size = 0;

        for (LinkedList<T> bucket : oldBuckets) {
            if (bucket != null) {
                for (T item : bucket) {
                    add(item);
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (LinkedList<T> bucket : buckets) {
            if (bucket != null) {
                for (T item : bucket) {
                    if (!first)
                        sb.append(", ");
                    sb.append(item);
                    first = false;
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
