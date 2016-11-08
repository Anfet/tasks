package net.anfet.tasks;

import static net.anfet.tasks.State.CANCELLED;
import static net.anfet.tasks.State.FINISHED;
import static net.anfet.tasks.State.FORFEITED;
import static net.anfet.tasks.State.NEW;
import static net.anfet.tasks.State.RUNNING;

/**
 * Базовый раннер для выполнения задач
 */
public abstract class Runner implements Runnable {

	private final Object owner;
	private volatile State state;


	public Runner(Object owner) {
		this.owner = owner;
		state = NEW;
	}


	protected abstract void doInBackground() throws Exception;

	@Override
	public String toString() {
		return state.toString();
	}

	Object getOwner() {
		return owner;
	}

	protected void onPublishFinished() {
		try {
			onPostExecute();
		} finally {
			onFinished();
		}
	}

	/**
	 * удобный метод для отмены задачи
	 */
	public void cancel() {
		state = CANCELLED;

		synchronized (this) {
			notifyAll();
		}

		Tasks.remove(this);
	}

	/**
	 * удобный метод для отбрасывания задачи
	 */
	public void forfeit() {
		state = FORFEITED;

		synchronized (this) {
			notifyAll();
		}

		Tasks.remove(this);
	}

	/**
	 * вызывается при появлении ошибки
	 * @param throwable ошибка
	 */
	protected void onError(Throwable throwable) {

	}

	/**
	 * Вызывается если запрос был отменен
	 */
	protected void onCancelled() {

	}

	/**
	 * Вызывается если запрос не был отменен и не был брошен
	 */
	protected void onPostExecute() {

	}

	/**
	 * Вызывается при завершении задачи если она не брошена
	 */
	protected void onFinished() {

	}

	/**
	 * вызывается {@link Tasks} при окончении выполнения.
	 * Не вызывается если состояние задачи {@link State#FORFEITED}
	 */
	protected void publishFinished() {
		onPublishFinished();
	}

	/**
	 * вызывается задачаей если балы словлена какая-то ошибка
	 * @param ex
	 */
	protected void publishError(Throwable ex) {
		onError(ex);
	}

	/**
	 * вызывается задачей после выполнения если она отменена
	 */
	protected void publishCancelled() {
		onCancelled();
	}

	/**
	 * вызывается при старте задачи и ждет окончания
	 */
	protected void publishPreExecute() {
		onPreExecute();
	}

	/**
	 * вызываеся при старте задачи в рабочем потоке. требует окончания для старта задачи. в течении этого метода задачу можно отменить или бросить.
	 */
	protected void onPreExecute() {

	}

	public boolean isRuninng() {
		return state == RUNNING;
	}

	@Override
	public void run() {
		try {
			try {
				if (state != NEW)
					throw new IllegalStateException("Cannot start task in a " + state.toString() + " state");

				state = RUNNING;
				onPreExecute();
				if (state == RUNNING) {
					doInBackground();
				}
			} catch (Exception ex) {
				if (state != FORFEITED) {
					publishError(ex);
				}
			}

			if (state != FORFEITED) {
				if (state == CANCELLED) {
					publishCancelled();
				} else {
					state = FINISHED;
				}

				publishFinished();
			}

		} finally {
			Tasks.remove(this);
		}
	}
}
