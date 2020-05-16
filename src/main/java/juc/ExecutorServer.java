package juc;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-16 19:44 </p>
 **/
public interface ExecutorServer extends Executor {
    void shutDown();

    <T> Future<T> submit(Runnable runnable);

    <T> Future<T> submit(Callable callable);
}
