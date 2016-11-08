package net.anfet.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Менеджер управления задачами
 */
public final class Tasks {

	private static final ConcurrentMultiMap<Object, Task> tasks = new ConcurrentMultiMap<>();
	private static final ExecutorService service = Executors.newCachedThreadPool();

	/**
	 * запускает задачу на выполнение
	 * @param runner задача
	 * @param owner  владелец
	 */
	public static void execute(Runner runner, Object owner) {
		service.execute(tasks.add(owner, new Task(owner, runner)));
	}

	public static void shutdown() {
		for (Object key : tasks.keys()) {
			forfeitAll(key);
		}

		service.shutdown();
	}

	/**
	 * отбрасывает все задачи для данного владельца
	 * @param owner владелец задач
	 */
	public static void forfeitAll(Object owner) {
		for (Task task : tasks.get(owner)) {
			forfeit(task);
		}
	}

	/**
	 * Правильный способ отменить задачу
	 * @param task задача
	 */
	public static void cancel(Task task) {
		task.cancel();
		remove(task);
	}

	/**
	 * внутренняя функция по удалению элемента из списка
	 * @param task задача
	 */
	static void remove(Task task) {
		tasks.remove(task.getOwner(), task);
	}

	/**
	 * внутренная функция по отбрасыванию задачи
	 * @param task
	 */
	static void forfeit(Task task) {
		task.forfeit();
		remove(task);
	}
}
