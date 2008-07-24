import com.jetlang.core.CommandQueue;
import com.jetlang.core.ProcessFiber;
import com.jetlang.core.ThreadFiber;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:40:58 PM
 */
public class ThreadFiberTests extends FiberBaseTest {

    public ProcessFiber CreateBus() {
        return new ThreadFiber(new CommandQueue(), System.currentTimeMillis() + "", true);
    }

    public void DoSetup() {
    }

    public void DoTearDown() {
    }


}
