package stasssm.streamlibrary.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class HighPriorityThreadFactory implements ThreadFactory {

    ThreadFactory internalFactory = Executors.defaultThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = internalFactory.newThread(r);
        thread.setPriority(Thread.MAX_PRIORITY);
        return thread;
    }

}
