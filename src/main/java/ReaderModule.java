import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReaderModule {
    private Logger log = LogManager.getLogger(ReaderModule.class);
    private final Set<Thread> threads;
    private final AtomicBoolean end;
    private final AtomicInteger clock;
    private final ConcurrentLinkedQueue<Integer> storage;
    private final Random rng;
    private final int numSensors;
    private final int numHours;
    private final AtomicInteger sensorsActive;
    private final AtomicInteger sensorsInitialized;

    public ReaderModule(int numSensors, int numHours){
        threads = new HashSet<>();
        end = new AtomicBoolean(false);
        clock = new AtomicInteger(0);
        storage = new ConcurrentLinkedQueue<>();
        rng = new Random(5);
        this.numSensors = numSensors;
        this.numHours = numHours;
        this.sensorsActive = new AtomicInteger(numSensors);
        this.sensorsInitialized = new AtomicInteger(numSensors);

        for(int i = 0; i < numSensors; i++){
            threads.add(new Thread(new TempSensor(storage, clock, end, rng, sensorsActive, sensorsInitialized)));
        }
    }

    private int getHighest(LinkedList<Integer> list){
        int val = Integer.MIN_VALUE;
        for(Integer i: list){
            if(i > val){
                val = i;
            }
        }
        return val;
    }

    private int getLowest(LinkedList<Integer> list){
        int val = Integer.MAX_VALUE;
        for(Integer i: list){
            if(i < val){
                val = i;
            }
        }
        return val;
    }

    private long nanoToMs(long nano){
        return TimeUnit.MILLISECONDS.convert(nano, TimeUnit.NANOSECONDS);
    }

    public void run() {
        for(Thread thread: threads){
            thread.start();
        }

        long startTime, endTime, delta = 0;
        for(int i = 0; i < numHours; i++){
            long startHour = System.nanoTime();
            for(int j = 0; j < 60; j++){
                while(sensorsInitialized.get() != 0){}
                sensorsInitialized.set(numSensors);
                clock.incrementAndGet();
                startTime = System.nanoTime();
                while(sensorsActive.get() != 0){}
                endTime = System.nanoTime();
                if(j == 0){
                    delta += nanoToMs(endTime - startTime);
                }
                else{
                    delta = nanoToMs(endTime - startTime);
                }
                sensorsActive.set(numSensors);
            }

            startTime = System.nanoTime();
            TreeSet<Integer> top5Highest = new TreeSet<>();
            TreeSet<Integer> top5Lowest = new TreeSet<>();
            int count = 0;

            LinkedList<Integer> maxInterval = new LinkedList<>();
            int max_smallest = Integer.MAX_VALUE;
            int cur_smallest = Integer.MAX_VALUE;
            int max_largest = Integer.MIN_VALUE;
            int cur_largest = Integer.MIN_VALUE;
            LinkedList<Integer> curInterval = new LinkedList<>();
            for(Integer temp: storage){
                if(count < 10){
                    if(count < 5){
                        top5Highest.add(temp);
                        top5Lowest.add(temp);
                    }
                    maxInterval.push(temp);
                    curInterval.push(temp);
                    if(temp > max_largest){
                        max_largest = temp;
                        cur_largest = temp;
                    }
                    if(temp < max_smallest){
                        max_smallest = temp;
                        cur_smallest = temp;
                    }
                }
                else{
                    int removed = curInterval.pop();
                    curInterval.push(temp);

                    if(removed == cur_largest){
                        cur_largest = getHighest(curInterval);
                    }
                    if(removed == cur_smallest){
                        cur_smallest = getLowest(curInterval);
                    }

                    if(cur_largest - cur_smallest > max_largest - max_smallest){
                        maxInterval.clear();
                        maxInterval.addAll(curInterval);
                        max_largest = cur_largest;
                        max_smallest = cur_smallest;
                    }
                }

                if(count >= 5){
                    if(temp > top5Highest.first() && !top5Highest.contains(temp)){
                        top5Highest.remove(top5Highest.first());
                        top5Highest.add(temp);
                    }
                    if(temp < top5Lowest.last() && !top5Lowest.contains(temp)){
                        top5Lowest.remove(top5Lowest.last());
                        top5Lowest.add(temp);
                    }
                }

                count++;
            }

            System.out.println("Number of Hours: " + numHours + "\n");
            System.out.println("Storage:\n" + storage);
            System.out.println("Storage size: " + storage.size());
            System.out.println("Top 5 highest temperatures: " + top5Highest);
            System.out.println("Top 5 lowest temperatures: " + top5Lowest);
            System.out.println("Largest temperature interval: " + maxInterval + " with delta " +
                    max_largest + " - " + max_smallest + " = " + (max_largest-max_smallest));

            storage.clear();
            endTime = System.nanoTime();
            delta = (endTime - startTime)/1000;

            long endHour = System.nanoTime();
            long hourDelta = nanoToMs(endHour - startHour);
//            log.info("Hour took " + hourDelta + " ms\n");

        }

        end.set(true);
        clock.incrementAndGet();
    }
}
