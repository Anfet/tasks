package net.anfet.tasks;

/**
 * Базовый раннер для выполнения задач
 */
public abstract class Runner {

	private Task task;

	protected Task getTask() {
		return task;
	}

	void setTask(Task task) {
		this.task = task;
	}

	/**
	 * абстрактный метод выполняющий работу в треде
	 */
	protected abstract void doInBackground();

	protected void onPublishFinished() {
		try {
			onPostExecute();
		} finally {
			onFinished();
		}
	}

	/**
	 * удобный метод для отмены задачи. вызывает {@link Tasks#cancel(Task)}
	 */
	public void cancel() {
		Tasks.cancel(task);
	}

	/**
	 * удобный метод для отбрасывания задачи. вызывает {@link Tasks#forfeit(Task)}
	 */
	public void forfeit() {
		Tasks.forfeit(task);
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
	 * вызывается {@link Task} при окончении выполнения.
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
	 * вызываеся при старте задачи. требует окончания для старта задачи. в течении этого метода задачу можно отменить или бросить.
	 */
	protected void onPreExecute() {

	}
}
