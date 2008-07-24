import com.jetlang.core.CommandExecutor;
import com.jetlang.core.IProcessQueue;
import com.jetlang.core.PoolQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: mrettig
 * Date: Jul 23, 2008
 * Time: 8:04:04 PM
 */
public class PoolQueueTests extends SubstitutabilityBaseTest {

    private ExecutorService _executor;

    public IProcessQueue CreateBus() {
        return new PoolQueue(_executor, new CommandExecutor());
    }

    public void DoSetup() {
        _executor = Executors.newCachedThreadPool();
    }

    public void DoTearDown() {
        if (_executor != null)
            _executor.shutdown();
    }
}
