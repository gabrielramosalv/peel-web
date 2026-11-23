package net.peelweb.server;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** Runs tasks on the delegate while at most {@code permits} are running; the others wait on their own virtual thread. */
final class BoundedExecutor extends AbstractExecutorService {

    private final ExecutorService delegate;
    private final Semaphore permits;

    BoundedExecutor(ExecutorService delegate, int permits) {
        this.delegate = delegate;
        this.permits = new Semaphore(permits);
    }

    @Override
    public void execute(Runnable task) {
        delegate.execute(() -> {
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                task.run();
            } finally {
                permits.release();
            }
        });
    }

    @Override public void shutdown() { delegate.shutdown(); }
    @Override public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
    @Override public boolean isShutdown() { return delegate.isShutdown(); }
    @Override public boolean isTerminated() { return delegate.isTerminated(); }
    @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }
}
