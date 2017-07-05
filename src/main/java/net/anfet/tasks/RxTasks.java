package net.anfet.tasks;

import net.anfet.MultiMap;
import net.anfet.support.Nullsafe;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Oleg on 16.06.2017.
 */
public final class RxTasks {
	private static final AtomicInteger id = new AtomicInteger(0);
	private static final MultiMap<WeakReference<Object>, RxRunner> tasks = new MultiMap<>();
	private static final ExecutorService service = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			thread.setName("RxTask" + id.incrementAndGet());
			return thread;
		}
	});

	private synchronized static WeakReference<Object> findRef(Object owner) {
		Iterator<WeakReference<Object>> keys = tasks.keys().iterator();
		while (keys.hasNext()) {
			WeakReference<Object> ref = keys.next();
			if (ref.get() == null) {
				keys.remove();
				continue;
			}

			if (owner.equals(ref.get())) {
				return ref;
			}
		}

		return null;
	}

	static <In, Out, Progress> void enqueue(final RxRunner<In, Out, Progress> runner, Object owner) {
		WeakReference<Object> ref = Nullsafe.get(findRef(owner), new WeakReference<Object>(owner));
		tasks.add(ref, (RxRunner) runner);
		service.execute(runner);
	}

	static void abandon(RxRunner runner) {
		WeakReference<Object> ref = findRef(runner.getOwner());
		if (ref != null) tasks.remove(ref, runner);
	}

	static boolean isAbandoned(RxRunner runner) {
		WeakReference<Object> ref = findRef(runner.getOwner());
		List<RxRunner> list = tasks.get(ref);
		return ref == null || !list.contains(runner);
	}

	public static void abandonAllFor(Object owner) {
		WeakReference<Object> ref = findRef(owner);
		if (ref != null) tasks.remove(ref);
	}
}
