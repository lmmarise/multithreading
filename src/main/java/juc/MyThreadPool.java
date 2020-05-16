package juc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-16 11:02 </p>
 **/
public class MyThreadPool extends AbstractExecutorService  {
    private volatile int maximumPoolSize;
    private volatile int corePoolSize;
    private volatile long keepAliveTime;
    private volatile boolean allowCoreThreadTimeOut;
    private final BlockingQueue<Runnable> workQueue;
    private final AtomicInteger ctl = new AtomicInteger(0);

    public MyThreadPool(int maximumPoolSize, int corePoolSize,
                        BlockingQueue<Runnable> workQueue) {
        this.maximumPoolSize = maximumPoolSize;
        this.corePoolSize = corePoolSize;
        this.workQueue = workQueue;
    }

    public MyThreadPool(int maximumPoolSize,
                        int corePoolSize,
                        long keepAliveTime,
                        boolean allowCoreThreadTimeOut,
                        BlockingQueue<Runnable> workQueue) {
        this.maximumPoolSize = maximumPoolSize;
        this.corePoolSize = corePoolSize;
        this.keepAliveTime = keepAliveTime;
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        this.workQueue = workQueue;
    }

    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }

        // 当前线程池里面的任务数
        int c = ctl.get();
        if (c < corePoolSize) {
            // 任务加入核心线程
            addWorker(command, true);
        }
        // 任务加入等待队列
        else if (workQueue.offer(command)) {
            addWorker(null, false);
        }
        // 添加到等待队列失败, 使用拒绝策略
        else {
            reject(command);
        }
    }

    void addWorker(Runnable task, boolean core) {
        if (core) {
            ctl.incrementAndGet();
        }
        new Worker(task).thread.start();
    }

    @Override
    public void shutDown() {

    }

    /*
     * 拒绝策略处理器
     * */
    class RejectExecutionHandler {
        public void rejectExecution(Runnable command) {
            throw new RejectedExecutionException("线程池等待队列已满, 抛弃这个task:" + command);
        }
    }

    // 使用拒绝策略处理task
    private void reject(Runnable command) {
        RejectExecutionHandler rejectExecutionHandler = new RejectExecutionHandler();
        rejectExecutionHandler.rejectExecution(command);
    }

    /*
     * 执行任务的
     * */
    class Worker extends ReentrantLock implements Runnable {
        private Runnable firstTask;
        private Thread thread;

        public Worker(Runnable task) {
            this.firstTask = task;
            this.thread = new Thread(this);
        }

        @Override
        public void run() {
            runWorker(this);
        }

        // 拿到task并执行
        private void runWorker(Worker worker) {
            try {
                worker.lock();
                Runnable task = worker.firstTask;

                // 如果获取到的task如果为空, 不会执行run方法
                if (task != null || (task = getTask()) != null) {
                    task.run();
                }

            } finally {
                processWorkerExit(worker);
                worker.unlock();
            }
        }

        // 抛弃task的处理方式 todo
        private void processWorkerExit(Worker worker) {
            addWorker(null, false);
        }

        // 取出等待队列中的第一个, 并从等待队列中删除
        public Runnable getTask() {
            try {
                if (workQueue.isEmpty()) {
                    return null;
                }
                // 等待某个可能发生变化的状态一段时间
                Runnable r = allowCoreThreadTimeOut ?
                        workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                        workQueue.take();
                if (r != null) {
                    return r;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
