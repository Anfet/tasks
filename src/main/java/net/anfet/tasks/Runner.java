package net.anfet.tasks;

/**
 * Базовый раннер для выполнения задач
 */
public abstract class Runner implements Runnable {


	protected final Object owner;
	protected RunnerState state;


	public Runner(Object owner) {
		this.owner = owner;
		state = RunnerState.NEW;
	}

	protected void await(long timeout) throws InterruptedException {
		synchronized (this) {
			wait(timeout);
		}
	}


	protected abstract void doInBackground() throws Exception;

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public Object getOwner() {
		return owner;
	}

	/**
	 * executes {@link #onPostExecute()}
	 */
	protected void publishPostExecute() {
		onPostExecute();
	}


	/**
	 * удобный метод для отмены задачи
	 */
	public void cancel() {
		state = RunnerState.CANCELLED;
	}

	/**
	 * удобный метод для отбрасывания задачи
	 */
	void forfeit() {
		state = RunnerState.FORFEITED;
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
	 * Не вызывается если состояние задачи {@link RunnerState#FORFEITED}
	 */
	protected void publishFinished() {
		onFinished();
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
	protected void publishPreExecute() throws Exception {
		onPreExecute();
	}

	/**
	 * вызываеся при старте задачи в рабочем потоке. требует окончания для старта задачи. в течении этого метода задачу можно отменить или бросить.
	 * Если выбрасывается какая-либо ошибка - то запрос не будет выполнен, а ошибка попадает в {@link #onError(Throwable)}
	 */
	protected void onPreExecute() throws Exception {

	}

	public boolean cancelled() {
		return state == RunnerState.CANCELLED;
	}

	public boolean forfeited() {
		return state == RunnerState.FORFEITED;
	}

	public boolean alive() {
		return state == RunnerState.RUNNING;
	}

	@Override
	public void run() {

		Throwable error = null;
		try {
			try {
				state = RunnerState.RUNNING;
				publishPreExecute();
				doInBackground();
			} catch (Exception ex) {
				error = ex;
				state = RunnerState.ERROR;
			}


			switch (state) {
				case RUNNING:
					publishPostExecute();
					break;
				case CANCELLED:
					publishCancelled();
					break;
				case FORFEITED:
					return;
				case ERROR:
					publishError(error);
					break;
				default:
					throw new IllegalStateException("Runner in wrong state after execution " + state);
			}

			switch (state) {
				case FORFEITED:
					return;
				default:
					publishFinished();
					state = RunnerState.FINISHED;
					break;
			}

		} finally {
			Tasks.remove(this);
		}
	}

	public RunnerState getState() {
		return state;
	}
}
