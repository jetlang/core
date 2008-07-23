import com.jetlang.core.IDisposable;

public class PerfTimer implements IDisposable
    {
        private final int _count;
        private long _stopWatch;

        public PerfTimer(int count)
        {
            _count = count;
            _stopWatch = System.currentTimeMillis();
        }

        public void Dispose()
        {
            long elapsed = System.currentTimeMillis() - _stopWatch;
            System.out.println("Elapsed: " + elapsed + " Events: " + _count);
            System.out.println("Avg/S: " + (_count/(elapsed/1000.00)));
        }
    }
