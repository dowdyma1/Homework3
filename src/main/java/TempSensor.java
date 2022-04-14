import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TempSensor implements Runnable{
    private final ConcurrentLinkedQueue<Integer> storage;
    private final AtomicInteger clock;
    private final AtomicBoolean end;
    private final Random rng;
    private final AtomicInteger sensorsActive;
    private final AtomicInteger sensorsInitialized;

    public TempSensor(ConcurrentLinkedQueue<Integer> storage, AtomicInteger clock, AtomicBoolean end, Random rng,
                      AtomicInteger sensorsActive, AtomicInteger sensorsInitialized){
        this.storage = storage;
        this.clock = clock;
        this.end = end;
        this.rng = rng;
        this.sensorsActive = sensorsActive;
        this.sensorsInitialized = sensorsInitialized;
    }

    @Override
    public void run() {
        int previousTime;

        while(!end.get()){
            previousTime = clock.get();
            sensorsInitialized.decrementAndGet();
            while(previousTime == clock.get()){}
            storage.add(-100 +rng.nextInt(171));
            sensorsActive.decrementAndGet();
        }
    }
}
