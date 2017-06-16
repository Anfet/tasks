package net.anfet.tasks;


import java.util.concurrent.TimeUnit;

/**
 * Created by Oleg on 16.06.2017.
 */
public class RxRunner<In, Out, Progress> implements Runnable {

	protected In in;
	protected Object owner;
	protected boolean cancelled = false;

	public boolean onPreExecute() throws Exception {
		return !cancelled;
	}

	protected boolean publishPreExecute() throws Exception {
		return onPreExecute();
	}

	public Out onExecute(In in) throws Exception {
		return null;
	}

	protected Out publishExecute(In in) throws Exception {
		return onExecute(in);
	}

	public void onPostExecute(Out out) {

	}

	protected void publishPostExecute(Out out) {
		onPostExecute(out);
	}

	public void onError(Throwable error) {

	}

	protected void publishError(Throwable error) {
		onError(error);
	}

	public void onDone() {

	}

	protected void publishDone() {
		onDone();
	}

	public void onProgress(Progress progress) {

	}

	protected void publishProgress(Progress progress) {
		onProgress(progress);
	}


	public RxRunner<In, Out, Progress> enqueue(Object owner, In... in) {
		this.in = (in == null || in.length == 0) ? null : in[0];
		this.owner = owner;
		RxTasks.enqueue(this, owner);
		return this;
	}

	public Object getOwner() {
		return owner;
	}

	public void abandon() {
		RxTasks.abandon(this);
	}

	public boolean isInterrupted() {
		return cancelled || RxTasks.isAbandoned(this);
	}

	public boolean cancel() {
		return cancelled = true;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void await(long timeout, TimeUnit unit) throws InterruptedException {
		long ms = unit.toMillis(timeout);
		synchronized (this) {
			wait(ms);
		}
	}

	@Override
	public void run() {
		try {
			try {
				if (!publishPreExecute()) {
					throw new ENotStarted();
				}

				Out out = publishExecute(in);

				if (isCancelled()) {
					throw new ECancelled();
				}

				if (RxTasks.isAbandoned(this)) {
					publishPostExecute(out);
				}
			} catch (Exception e) {
				publishError(e);
			}

			if (!RxTasks.isAbandoned(this)) {
				publishDone();
			}
		} finally {
			RxTasks.abandon(this);
		}
	}
}
