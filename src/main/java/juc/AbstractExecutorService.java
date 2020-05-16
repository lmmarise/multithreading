package juc;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-16 19:55 </p>
 **/
public abstract class AbstractExecutorService implements ExecutorServer {

    @Override
    public <T> Future<T> submit(Runnable runnable) {
        FutureTask<T> futureTask = new FutureTask<>(runnable, null);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public <T> Future<T> submit(Callable callable) {
        FutureTask<T> futureTask = new FutureTask<T>(callable);
        execute(futureTask);
        return futureTask;
    }
}
