import lockfreelist.LockFreeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Servant implements Runnable{
    private Logger log = LogManager.getLogger(Servant.class);
    private final ConcurrentLinkedQueue<Present> bag;
    private final LockFreeList<Present> chain;
    private final Random rng;
    private final AtomicBoolean isDone;
    private final Queue<Present> containsQueue;
    private boolean doneAdding;

    public Servant(ConcurrentLinkedQueue<Present> bag, LockFreeList<Present> chain, Random rng, AtomicBoolean isDone,
                   Queue containsQueue){
        this.bag = bag;
        this.chain = chain;
        this.rng = rng;
        this.isDone = isDone;
        this.containsQueue = containsQueue;
        doneAdding = false;
    }

    private void addToChain(){
        if(!bag.isEmpty()){
            Present present = bag.remove();
            chain.add(present);
            log.info("Added to chain: " + present);
        }
        else{
            doneAdding = true;
            log.info("Bag is empty");
        }
    }

    private void writeLetter(){
        Present head = chain.getHead();
        if(head != null){
            chain.remove(head);
            log.info("Wrote thank you card for " + head);
        }
        else if(doneAdding){
            log.info("Thread DONE");
            isDone.set(true);
        }
    }

    private boolean checkIfPresent(Present present){
        boolean contains = chain.contains(present);
        log.info("Present " + present + " is in chain: " + contains);

        return contains;
    }

    @Override
    public void run() {
        while(!isDone.get()) {
            if (!containsQueue.isEmpty()) {
                checkIfPresent(containsQueue.remove());
            } else if (!doneAdding && rng.nextBoolean()) {
                addToChain();
            } else {
                writeLetter();
            }
        }
    }
}
