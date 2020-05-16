import java.util.concurrent.Callable;

/**
 * <p>@program: multithreading </p>
 * <p>@description:  </p>
 * <p>@author: Arise Tang </p>
 * <p>@since: 2020-05-12 00:01 </p>
 **/
public class Thread03 implements Callable {
    public Object call() throws Exception {
        System.out.println("线程3");
        return "线程3";
    }
}
