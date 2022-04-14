package lockfreelist;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Node<T> {
    T item;
    int key;
    AtomicMarkableReference<T> next;

    public Node(T item, int key){
        this.item = item;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(item, node.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
