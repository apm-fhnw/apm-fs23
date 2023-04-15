import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/*
synthetic memory allocation
---------------------------

This application's memory allocation scheme is parameterized as follows:
- delay     = period of time between successive memory allocation (in msec)
- size      = size of allocated memory chunks (in byte)
- number    = number of memory chunks to be allocated
              (i.e. size x number = amount of memory to be allocated)
- hold rate = number of delays after which the allocated memory is free
              (i.e. hold rate x delay = period of time for which the memory is alive)
           
The application starts several "memory allocators" each of which can have a different
configuration (see -> allocStarter).
              
The application terminated after a specified period of time (see -> stopAfter).
*/



public class Test {
	
	private static class MemoryAllocator extends TimerTask {
		
		private int cycle;
		private final int size;
		private final int number;
		private final int holdRate;
		private final Map<Integer, byte[][]> chunks = new HashMap<Integer, byte[][]>();

		public MemoryAllocator(int size, int number, int holdRate) {
			this.size = size;
			this.number = number;
			this.holdRate = holdRate;
		}

		@Override
		public void run() {
			cycle++;
			
			byte [][] holder = new byte[number][];
			for (int i=0; i<number; i++)
				holder[i] = new byte[size];
			
			chunks.put(cycle+holdRate, holder);
			for (int i=0; i<holder.length; i++)
				holder[i] = null;
			
			holder = chunks.remove(cycle);
			if (holder != null) {
				for (int i=0; i<holder.length; i++)
					holder[i] = null;
			}				
		}
		
	}
	
	final static Timer timer = new Timer();
	
	private static void stopAfter(long runTime) {
		timer.schedule(new TimerTask() {
			public void run() {
				timer.cancel();
			}
		}, runTime);
	}
	
	private static void allocStarter(int delay, int size, int number, int holdRate) {
		timer.scheduleAtFixedRate(new MemoryAllocator(size, number, holdRate), delay, delay);
	}
	
	public static void main(String[] argv) {
		         //  delay,      size, number,     hold rate (= multiple of delay)
		allocStarter(   50,   32*1024,    256,     10);
		allocStarter(  100,   32*1024,    256,     10);
		allocStarter(  250,    8*1024,    128,     60);
		allocStarter(  500,    4*1024,    128,     60*2);
		allocStarter(  750,    4*1024,    128,     60*2);
		allocStarter( 1000,      1024,    128,     60*5);
		
		stopAfter(1000*60*2);
	}
}
