package cri.sanity.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import cri.sanity.Conf;


public abstract class Task implements Runnable
{
	public static class Pool extends ScheduledThreadPoolExecutor {
		public Pool() { super(3); }		// FIX: leave 3???
	}
	
	private static final Map<Integer,ScheduledFuture<?>> map = new HashMap<Integer,ScheduledFuture<?>>(16);
	private static Pool pool;
	private static int idCur = 0;

	//---- instance methods

	/*
	public final void exec() {
		if(pool == null) pool = new Pool();
		pool.execute(this);
	}
	*/
	public final void exec(int delay) {
		if(pool == null) pool = new Pool();
		pool.schedule(this, delay, TimeUnit.MILLISECONDS);
	}
	public final void exec(int id, int delay) {
		//synchronized(map) {
		final ScheduledFuture<?> sf = map.get(id);
		if(sf != null) sf.cancel(false);
		if(pool == null) pool = new Pool();
		map.put(id, pool.schedule(this, delay, TimeUnit.MILLISECONDS));
		//}
	}

	//---- static methods

	public static final int idCur() { return   idCur; }
	public static final int idNew() { return ++idCur; }

	public static final boolean has(int id) {
		final ScheduledFuture<?> sf = map.get(id);
		return sf!=null && !sf.isDone();
	}

	public static final Pool shutdown() {
		if(pool == null) return null;
		Pool p = pool;
		p.shutdown();
	  pool = null;
		map.clear();
		return p;
	}

	public static final void shutdownWait() { shutdownWait(Task.pool); }
	public static final void shutdownWait(Pool pool) {
		if(pool == null) return;
		if(pool == Task.pool) shutdown();
		else             pool.shutdown();
		if(!pool.isTerminating()) return;
		try {
	    if(!pool.awaitTermination(Conf.TASK_WAIT_SHUTDOWN, TimeUnit.MILLISECONDS)) {
	      pool.shutdownNow();
	      pool.awaitTermination(Conf.TASK_WAIT_SHUTDOWN, TimeUnit.MILLISECONDS);
	    }
	  } catch(InterruptedException e) {
	    pool.shutdownNow();
	  }
	}

	public static final void stopAll() {
		//synchronized(map) {
		for(ScheduledFuture<?> sf : map.values())
			sf.cancel(false);
		map.clear();
		//}
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
