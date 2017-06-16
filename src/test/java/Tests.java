import net.anfet.tasks.RxRunner;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Oleg on 16.06.2017.
 */
public class Tests {

	Object ownerA;
	Object ownerB;

	@org.junit.Before
	public void setUp() throws Exception {
		ownerA = new Object();
		ownerB = new Object();
	}

	@Test
	public void simple() {
		RxRunner<Integer, Integer, Integer> runner = new RxRunner<Integer, Integer, Integer>() {
			@Override
			public Integer onExecute(Integer i) throws Exception {
				return i * 20;
			}

			@Override
			public void onPostExecute(Integer integer) {
				super.onPostExecute(integer);
				System.out.println(integer);
			}
		};

		runner.enqueue(ownerA, 10);
	}

	@Test
	public void abandon() {
		new RxRunner<Void, Void, Void>() {
			@Override
			public Void onExecute(Void aVoid) throws Exception {
				await(5000, TimeUnit.MILLISECONDS);
				return super.onExecute(aVoid);
			}

			@Override
			public void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				Assert.fail();
			}

			@Override
			public void onError(Throwable error) {
				super.onError(error);
				Assert.fail();
			}

			@Override
			public void onDone() {
				super.onDone();
				Assert.fail();
			}
		}.enqueue(ownerA).abandon();
	}

	@Test
	public void cancel() {
		final CountDownLatch latch = new CountDownLatch(1);
		RxRunner x = new RxRunner<Void, Void, Void>() {
			@Override
			public Void onExecute(Void aVoid) throws Exception {
				await(5000, TimeUnit.MILLISECONDS);
				return super.onExecute(aVoid);
			}

			@Override
			public void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				Assert.fail();
			}

			@Override
			public void onError(Throwable error) {
				super.onError(error);
				error.printStackTrace();
			}

			@Override
			public void onDone() {
				super.onDone();
				latch.countDown();
			}
		}.enqueue(ownerA);

		try {
			Thread.sleep(100);
			x.cancel();
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
