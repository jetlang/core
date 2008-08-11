package org.jetlang;

import java.util.concurrent.Executor;

public class PerfCommandExecutor implements Executor {

    public void execute(Runnable command) {
        command.run();
    }

}
