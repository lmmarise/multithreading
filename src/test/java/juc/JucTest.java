package juc;

import org.junit.Test;

import java.util.concurrent.*;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-16 11:25 </p>
 **/
public class JucTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        MyThreadPool pool = new MyThreadPool(3, 10,
                new LinkedBlockingQueue<>(5));
        for (int i = 0; i < 100; i++) {
            pool.execute(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("cxk-nhm");
            });

            /*Future<Object> future = pool.submit(() -> {
                return "蔡徐坤";
            });
            System.out.println(future.get());*/

            Future<Object> future = pool.submit(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("cxk");
            });
        }
        Thread.sleep(10000);
    }
}
