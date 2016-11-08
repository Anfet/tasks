package net.anfet.tasks;

import static net.anfet.tasks.State.CANCELLED;
import static net.anfet.tasks.State.FINISHED;
import static net.anfet.tasks.State.FORFEITED;
import static net.anfet.tasks.State.NEW;
import static net.anfet.tasks.State.RUNNING;

/**
 * Простая задача
 */
public final class Task implements Runnable {

	private final Object owner;
	private State state;
	private Runner runner;

	Task(Object owner, Runner runner) {
		this.owner = owner;
		this.runner = runner;
		state = NEW;
	}

	Object getOwner() {
		return owner;
	}

	/**
	 * более жесткая форма {@link #cancel()}. Указывает на брошенную задачу. Для такой задачи коллбеки не вызываются.
	 * Рекомендуется применять, когда результат уже не важен
	 */
	void forfeit() {
		state = FORFEITED;
	}

	public State getState() {
		return state;
	}

	@Override
	public String toString() {
		return state.toString();
	}

	/**
	 * указывает на то, что задача отменена. По завершению будет вызвана функция {@link Runner#onCancelled()}
	 */
	void cancel() {
		state = CANCELLED;
	}

	public boolean isRuninng() {
		return state == RUNNING;
	}

	public boolean isCancelled() {
		return state == CANCELLED;
	}

	public boolean isForfeited() {
		return state == FORFEITED;
	}

	@Override
	public void run() {
		try {
			try {
				runner.setTask(this);

				if (state != NEW)
					throw new IllegalStateException("Cannot start task in a " + state.toString() + " state");

				state = RUNNING;
				runner.onPreExecute();
				if (state == RUNNING) {
					runner.doInBackground();
				}
			} catch (Exception ex) {
				if (state != FORFEITED) {
					runner.publishError(ex);
				}
			}

			if (state != FORFEITED) {
				if (state == CANCELLED) {
					runner.publishCancelled();
				} else {
					state = FINISHED;
				}

				runner.publishFinished();
			}

		} finally {
			Tasks.remove(Task.this);
		}
	}
}
