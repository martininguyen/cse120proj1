package nachos.threads;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.SortedMap;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 * 
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		
		
		while(timerQueue.peek() != null){
			long tempTime = timerQueue.peek().getTime();
			if(tempTime <= Machine.timer().getTime()){
				threadTime tempTimeThread = timerQueue.poll();
				KThread tempThread = tempTimeThread.threadToWake;
				tempThread.ready();
				
			}else
				break;
		}
		
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 * 
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 * 
	 * @param x the minimum number of clock ticks to wait.
	 * 
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		long wakeTime = Machine.timer().getTime() + x;
		threadTime temp = new threadTime(wakeTime,KThread.currentThread());
		timerQueue.add(temp);
		Machine.interrupt().disable();
		KThread.sleep();
		Machine.interrupt().enable();
	}
	
	// Place this function inside Alarm. And make sure Alarm.selfTest() is called inside ThreadedKernel.selfTest() method.

	public static void selftest() {
	    KThread t1 = new KThread(new Runnable() {
	        public void run() {
	            long time1 = Machine.timer().getTime();
	            int waitTime = 10000;
	            System.out.println("Thread calling wait at time:" + time1);
	            ThreadedKernel.alarm.waitUntil(waitTime);
	            System.out.println("Thread woken up after:" + (Machine.timer().getTime() - time1));
	            Lib.assertTrue((Machine.timer().getTime() - time1) >= waitTime, " thread woke up too early.");
	            
	        }
	    });
	    t1.setName("T1");
	    t1.fork();
	    t1.join();
	}
    
	
	Comparator<threadTime> comparatorX = new Comparator<threadTime>(){
	public int compare(threadTime o1, threadTime o2) {
			if(o1.timeToWake == o2.timeToWake)
				return 0;
			else if(o1.timeToWake < o2.timeToWake)
				return -1;
			else if(o1.timeToWake > o2.timeToWake)
				return 1;
			return 0;
		}
	}; 
	
	private PriorityQueue<threadTime> timerQueue = new PriorityQueue<threadTime>(500,comparatorX);
	
}
