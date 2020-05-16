import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.*;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-11 23:27 </p>
 **/
public class Main {
    public synchronized static void main1(String[] args) throws InterruptedException {
        Thread01 thread01 = new Thread01();

        Thread02 thread2 = new Thread02();
        Thread thread02 = new Thread(thread2);

        Thread03 thread3 = new Thread03();
        FutureTask<Integer> task = new FutureTask<Integer>(thread3);
        Thread thread03 = new Thread(task, "有返回值线程3");

        thread01.start();
        thread01.join();
        thread02.start();
        thread02.join();
        thread03.start();
        thread03.join();

        /*ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute();
        executor.submit();
        Thread.UncaughtExceptionHandler();
        Executors.newScheduledThreadPool(2);
        "".wait();
        LockSupport;
        Lock;*/
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(100000000);
    }

    static class Condition01 {
        static Lock lock = new ReentrantLock();
        static Condition full = lock.newCondition();
        static Condition empty = lock.newCondition();
        private static int i = 0;

        public static void main(String[] args) {
            new Thread(() -> {
                try {
                    Condition01.producer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                try {
                    Condition01.consumer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private static void producer() throws InterruptedException {
            lock.lock();
            try {
                for (; ; ) {
                    i++;
                    System.out.println("增加, 库存:" + i);
                    if (i >= 3) {
                        System.out.println("请消费。。。");
                        // Thread.sleep(1000);
                        full.await();
                        empty.signal();
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        private static void consumer() throws InterruptedException {
            lock.lock();
            try {
                for (; ; ) {
                    i--;
                    System.out.println("消费, 库存:" + i);
                    if (i <= 0) {
                        System.out.println("请生产。。。");
                        // Thread.sleep(1000);
                        full.signal();
                        empty.await();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    static class AtomicAdd implements Runnable {
        static int i = 0;
        static AtomicInteger k = new AtomicInteger(0);

        @Override
        public void run() {
            for (int j = 0; j < 10000; j++) {
                ++i;
                k.incrementAndGet();
            }
        }

        public static void main(String[] args) throws InterruptedException {
            AtomicAdd add = new AtomicAdd();
            Thread thread1 = new Thread(add);
            Thread thread2 = new Thread(add);
            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();
            System.out.println(i);
            System.out.println(k);
        }
    }

    static class ABA {
        private static AtomicInteger atomicInt = new AtomicInteger(100);
        private static AtomicStampedReference<Integer> atomicStampedRef = new AtomicStampedReference<Integer>(100, 0);

        public static void main(String[] args) throws InterruptedException {
            Thread intT1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    atomicInt.compareAndSet(100, 101);
                    atomicInt.compareAndSet(101, 100);
                }
            });

            Thread intT2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    boolean c3 = atomicInt.compareAndSet(100, 101);
                    System.out.println(c3);        //true
                }
            });

            intT1.start();
            intT2.start();
            intT1.join();
            intT2.join();

            Thread refT1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    atomicStampedRef.compareAndSet(100, 101,
                            atomicStampedRef.getStamp(), atomicStampedRef.getStamp() + 1);
                    atomicStampedRef.compareAndSet(101, 100,
                            atomicStampedRef.getStamp(), atomicStampedRef.getStamp() + 1);
                }
            });

            Thread refT2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    int stamp = atomicStampedRef.getStamp();
                    System.out.println("before sleep : stamp = " + stamp);    // stamp = 0
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("after sleep : stamp = " + atomicStampedRef.getStamp());//stamp = 1
                    boolean c3 = atomicStampedRef.compareAndSet(100, 101, stamp, stamp + 1);
                    System.out.println(c3);        //false
                }
            });

            refT1.start();
            refT2.start();
        }
    }

    static class Scheduled {
        static int i = 0;

        public static void main(String[] args) {
            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
            // 延时启动
            executorService.schedule(() -> {
                System.out.println(++i);
            }, 1, TimeUnit.SECONDS);
            // 周期性固定延时启动
            // period时间过去后，检测上一个任务是否执行完毕，如果上一个任务执行完毕，
            // 则当前任务立即执行，如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行。
            // |任务耗时| - delay + |下一个任务耗时|
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    System.out.println("cxk1");
                }
            }, 0, 1, TimeUnit.SECONDS);
            // 周期性非固定延时启动
            // 以上一个任务结束时开始计时，delay时间过去后，立即执行。
            // |任务耗时| + delay + |下一个任务耗时|
            executorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.print("准备睡眠两秒==》");
                        Thread.sleep(2000);
                        System.out.println("睡眠完成");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("cxk2");
                }
            }, 0, 1, TimeUnit.SECONDS);
        }
    }


    static class Singleton01 {
        private static Singleton01 singleton01 = new Singleton01();

        private Singleton01() {
        }

        public static Singleton01 getSingleton01() {
            return singleton01;
        }

        public static void main(String[] args) {
            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    System.out.println(getSingleton01());
                }).start();
            }
        }
    }

    static class Singleton02 {
        private static Singleton02 singleton02 = null;

        private Singleton02() {
        }

        public static Singleton02 getSingleton02() {
            if (singleton02 == null) {
                synchronized (Singleton02.class) {
                    singleton02 = new Singleton02();
                }
            }
            return singleton02;
        }

        public static void main(String[] args) {
            for (int i = 0; i < 10; i++) {
                new Thread(() -> {
                    System.out.println(getSingleton02());
                }).start();
            }
        }
    }

    static class DoubleCheckLock {
        private static volatile DoubleCheckLock doubleCheckLock = null;

        public static DoubleCheckLock getDoubleCheckLock() {
            if (doubleCheckLock == null) {
                synchronized (DoubleCheckLock.class) {
                    if (doubleCheckLock == null) {
                        doubleCheckLock = new DoubleCheckLock();
                    }
                }
            }
            return doubleCheckLock;
        }
    }

    static class InnerClassSingleton {

        private InnerClassSingleton() {
        }

        // 内部类是延时加载的，只会在第一次使用时加载，不使用就不加载
        // 该实现方式时线程安全的
        private static class SingletonInner {
            private static final InnerClassSingleton instance = new InnerClassSingleton();
        }

        public InnerClassSingleton getInstance() {
            return SingletonInner.instance;
        }
    }


}

class Resource {
}

// 反射在通过newInstance创建对象时，会检查该类是否ENUM修饰，如果是则抛出异常，反射失败
// 定义的一个枚举，在第一次被真正用到的时候，会被虚拟机加载并初始化，而这个初始化过程是线程安全的
enum ResourceSingleton {
    INSTANCE;
    private Resource instance;

    ResourceSingleton() {
        instance = new Resource();
    }

    public Resource getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        ResourceSingleton.INSTANCE.getInstance();
    }
}