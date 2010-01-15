package org.jetlang.fibers;

import org.jetlang.core.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides a deterministic fiber implementation for testing.
 * <p/>
 * FOR TESTING ONLY
 */
public class FiberStub implements Fiber {

    public List<Disposable> Disposables = new ArrayList<Disposable>();
    public List<Runnable> Pending = new ArrayList<Runnable>();
    public List<ScheduledEvent> Scheduled = new ArrayList<ScheduledEvent>();

    public void start() {
    }

    public void add(Disposable disposable) {
        Disposables.add(disposable);
    }

    public boolean remove(Disposable disposable) {
        return Disposables.remove(disposable);
    }

    public int size() {
        return Disposables.size();
    }

    public void execute(Runnable command) {
        Pending.add(command);
    }

    public Disposable schedule(Runnable runnable, long l, TimeUnit timeUnit) {
        final ScheduledEvent event = new ScheduledEvent(runnable, l, timeUnit);
        Scheduled.add(event);
        return new Disposable() {
            public void dispose() {
                Scheduled.remove(event);
            }
        };
    }

    public Disposable scheduleWithFixedDelay(Runnable runnable, long first, long interval, TimeUnit timeUnit) {
        final ScheduledEvent event = new ScheduledEvent(runnable, first, interval, timeUnit);
        Scheduled.add(event);
        return new Disposable() {
            public void dispose() {
                Scheduled.remove(event);
            }
        };
    }

    public void dispose() {
    }

    public void executeAllPending() {
        for (Runnable runnable : new ArrayList<Runnable>(Pending)) {
            runnable.run();
            Pending.remove(runnable);
        }
    }

    public void executeAllScheduled() {
        for (ScheduledEvent event : new ArrayList<ScheduledEvent>(Scheduled)) {
            event.getRunnable().run();
            if (!event.isRecurring()) {
                Scheduled.remove(event);
            }
        }
    }

    public static class ScheduledEvent {

        private Runnable runnable;
        private long first;
        private long interval;
        private TimeUnit timeUnit;
        private boolean isRecurring = false;

        public ScheduledEvent(Runnable runnable, long time, TimeUnit timeUnit) {
            this(runnable, time, -1, timeUnit);
        }

        public ScheduledEvent(Runnable runnable, long first, long interval, TimeUnit timeUnit) {
            this.runnable = runnable;
            this.first = first;
            this.interval = interval;
            this.timeUnit = timeUnit;
            this.isRecurring = true;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public long getFirst() {
            return first;
        }

        public long getInterval() {
            return interval;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScheduledEvent event = (ScheduledEvent) o;

            if (first != event.first) return false;
            if (interval != event.interval) return false;
            if (isRecurring != event.isRecurring) return false;
            if (runnable != null ? !runnable.equals(event.runnable) : event.runnable != null) return false;
            if (timeUnit != event.timeUnit) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = (runnable != null ? runnable.hashCode() : 0);
            result = 31 * result + (int) (first ^ (first >>> 32));
            result = 31 * result + (int) (interval ^ (interval >>> 32));
            result = 31 * result + (timeUnit != null ? timeUnit.hashCode() : 0);
            result = 31 * result + (isRecurring ? 1 : 0);
            return result;
        }

        public boolean isRecurring() {
            return isRecurring;
        }

        public void run() {
            runnable.run();
        }
    }

}
