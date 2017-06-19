package com.embedded.socialexercise.movement;

import java.util.ArrayList;

public class LimitedSizeQueue<T> extends ArrayList<T> {
    private  int maxSize;
    public LimitedSizeQueue(int size) {
        maxSize = size;
    }

    public boolean add(T t) {
        boolean r = super.add(t);
        if(size() > maxSize) {
            removeRange(0, size() - maxSize - 1);
        }
        return r;
    }

    public T getFirst() {
        if(size() > 1) {
            return get(size() - 1);
        }
        return null;
    }

    public T getLast() {
        return get(0);
    }
}
