package lockfreelist;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeList<T> {
    Node head;
    Node tail;

    public LockFreeList(){
        head = new Node(null, 0);
        tail = new Node(null, Integer.MAX_VALUE);
        head.next = new AtomicMarkableReference(tail, false);
        tail.next = new AtomicMarkableReference(null, false);
    }

    public boolean add(T item){
        int key = item.hashCode();
        while(true){
            Window window = Window.find(head, key);
            Node pred = window.pred;
            Node curr = window.curr;

            if(curr.key == key){
                return false;
            }
            else{
                Node node = new Node(item, key);
                node.next = new AtomicMarkableReference(curr, false);

                if(pred.next.compareAndSet(curr, node, false, false)){
                    return true;
                }
            }
        }
    }

    public boolean remove(T item){
        int key = item.hashCode();
        boolean snip;

        while(true){
            Window window = Window.find(head, key);

            Node pred = window.pred, curr = window.curr;
            if(curr.key != key){
                return false;
            }
            else{
                Node succ = (Node) curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if(!snip){
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public T getHead(){
        return (T) ((Node) head.next.getReference()).item;
    }

    public boolean contains(T item){
        int key = item.hashCode();
        Node curr = head;
        while(curr.key < key){
            curr = (Node) curr.next.getReference();
        }
        return (curr.key == key && !curr.next.isMarked());
    }

    public void print(){
        Node curr = head;
        while(curr != null && !curr.next.equals(tail)){
            if(curr.item != null){
                System.out.println(curr.item + " | " + curr.item.hashCode());
            }
            curr = (Node) curr.next.getReference();
        }
    }

}
