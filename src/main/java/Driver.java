import lockfreelist.LockFreeList;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

public class Driver {
    static int NUM_PRESENTS = 500000;
    static int NUM_SERVANTS = 4;
    static int NUM_SENSORS = 8;
    static int NUM_HOURS = 5;


    public static void main(String[] args) throws InterruptedException {
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.OFF);

        LockFreeList<Present> list = new LockFreeList<>();

        BirthdayPresents birthdayPresents = new BirthdayPresents(list, NUM_PRESENTS, NUM_SERVANTS);
        birthdayPresents.run();


        ReaderModule readerModule = new ReaderModule(NUM_SENSORS, NUM_HOURS);
        readerModule.run();
    }
}
