package juc;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-16 11:15 </p>
 **/
public interface Executor {
    void execute(Runnable task);
}
