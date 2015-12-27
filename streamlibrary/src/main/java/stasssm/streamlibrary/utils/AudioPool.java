package stasssm.streamlibrary.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Стас on 18.08.2015.
 */
public class AudioPool {

    private static AudioPool sharedInstance = null;
    private ExecutorService pool;

    public static AudioPool getSharedInstance() {
        if(sharedInstance == null)
            sharedInstance = new AudioPool();

        return sharedInstance;
    }

    private AudioPool() {
        pool = Executors.newFixedThreadPool(4, new HighPriorityThreadFactory());
    }

    public void submit(Runnable audioRunnable) {
        pool.submit(audioRunnable);
    }
}

