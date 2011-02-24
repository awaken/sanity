package cri.sanity;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;


public abstract class Task implements Runnable
{
	private static final int POOL_SIZE = 16;

	private static final Map<Integer,ScheduledFuture<?>> map = new HashMap<Integer,ScheduledFuture<?>>();
	private static ScheduledThreadPoolExecutor pool;
	private static int idCur = 0;

	//---- instance methods

	/*
	public final void exec() {
		if(pool == null) pool = new ScheduledThreadPoolExecutor(POOL_SIZE);
		pool.execute(this);
	}
	*/
	public final void exec(int delay) {
		if(pool == null) pool = new ScheduledThreadPoolExecutor(POOL_SIZE);
		pool.schedule(this, delay, TimeUnit.MILLISECONDS);
	}
	public final void exec(int id, int delay) {
		//synchronized(map) {
		final ScheduledFuture<?> sf = map.get(id);
		if(sf != null) sf.cancel(false);
		if(pool == null) pool = new ScheduledThreadPoolExecutor(POOL_SIZE);
		map.put(id, pool.schedule(this, delay, TimeUnit.MILLISECONDS));
		//}
	}
	
	//---- static methods

	public static final int idCur() { return   idCur; }
	public static final int idNew() { return ++idCur; }

	public static final void shutdown() {
		if(pool == null) return;
		pool.shutdown();
	}

	public static final void shutdownNow() {
		if(pool == null) return;
		pool.shutdown();
		if(pool.isTerminating()) {
			try {
		    if(!pool.awaitTermination(Conf.TASK_WAIT_SHUTDOWN, TimeUnit.MILLISECONDS)) {
		      pool.shutdownNow();
		      pool.awaitTermination(Conf.TASK_WAIT_SHUTDOWN, TimeUnit.MILLISECONDS);
		    }
		  } catch(InterruptedException e) {
		    pool.shutdownNow();
		  }
		}
	  pool = null;
		map.clear();
	}

	public static final void stop(int id) {
		//synchronized(map) {
		final ScheduledFuture<?> sf = map.remove(id);
		if(sf != null) sf.cancel(false);
		//}
	}
	public static final void stop(int ... ids) {
		if(pool == null) return;
		for(int id : ids) stop(id);
	}

}
