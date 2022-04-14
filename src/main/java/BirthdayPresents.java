import lockfreelist.LockFreeList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BirthdayPresents {
    private Logger log = LogManager.getLogger(BirthdayPresents.class);
    final int numPresents;
    final int numServants;
    final ConcurrentLinkedQueue<Present> bag;
    final LockFreeList<Present> chain;
    final Set<Thread> threads;
    final Map<Thread, AtomicBoolean> endMap;
    final Map<Thread, Queue<Present>> containsQueueMap;
    final Random rng;

    public BirthdayPresents(LockFreeList<Present> chain, int numPresents, int numServants){
        this.numPresents = numPresents;
        this.chain = chain;
        this.numServants = numServants;
        this.endMap = new ConcurrentHashMap<>();
        this.containsQueueMap = new ConcurrentHashMap<>();
        bag = new ConcurrentLinkedQueue<>();
        fillBag();
        log.info("Bag size: " + bag.size());

        rng = new Random(5);

        threads = new HashSet<>();

        for(int i = 0; i < numServants; i++){
            AtomicBoolean isDone = new AtomicBoolean(false);
            Queue queue = new LinkedList();
            Thread curThread = new Thread(new Servant(bag, chain, rng, isDone, queue));
            threads.add(curThread);
            endMap.put(curThread, isDone);
            containsQueueMap.put(curThread, queue);
        }
    }

    public void fillBag(){
        ArrayList<Integer> tags = new ArrayList<>();
        for(int i = 0; i < numPresents; i++){
            tags.add(i);
        }
        Collections.shuffle(tags);

        for(Integer tag: tags){
            bag.add(new Present(tag));
        }
    }

    private boolean hasRunningThreads(){
        for(AtomicBoolean isDone: endMap.values()){
            boolean threadIsDone = isDone.get();
//            log.info("Thread is running: " + threadIsRunning);
            if(!threadIsDone){
                return true;
            }
        }
        return false;
    }

    public void run() throws InterruptedException {
        for(Thread thread: threads){
            thread.start();
        }
        while(hasRunningThreads()){
            Thread.sleep(5);
            for(Queue queue: containsQueueMap.values()){
                Present present = new Present(rng.nextInt(numPresents));
                queue.add(present);
            }

        }
        System.out.println("ALL LETTERS WRITTEN\n");
    }
}
