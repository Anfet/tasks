package net.anfet.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Менеджер управления задачами
 */
public final class Tasks {

	private static final ConcurrentMultiMap<Object, Runner> tasks = new ConcurrentMultiMap<>();
	private static final ExecutorService service = Executors.newCachedThreadPool();

	/**
	 * запускает задачу на выполнение
	 * @param runner задача
	 */
	public static void execute(Runner runner) {
		service.execute(tasks.add(runner.getOwner(), runner));
	}

	public static void shutdown() {
		for (Object key : tasks.keys()) {
			forfeitAllFor(key);
		}

		service.shutdown();
	}

	/**
	 * отбрасывает все задачи для данного владельца
	 * @param owner владелец задач
	 */
	public static void forfeitAllFor(Object owner) {
		for (Runner runner : tasks.get(owner)) {
			forfeit(runner);
		}
	}

	/**
	 * Правильный способ отменить задачу
	 * @param runner задача
	 */
	public static void cancel(Runner runner) {
		runner.cancel();
	}

	/**
	 * внутренняя функция по удалению элемента из списка
	 * @param runner задача
	 */
	static void remove(Runner runner) {
		tasks.remove(runner.getOwner(), runner);
	}

	/**
	 * внутренная функция по отбрасыванию задачи
	 * @param runner
	 */
	static void forfeit(Runner runner) {
		runner.forfeit();
	}
}
