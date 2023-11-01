package nc.calculatepi;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.Kernel.EXECUTION_MODE;

public class PiKernel extends Kernel {
	public static final int DIMENSION_X = 2;
	public static final int DIMENSION_Y = 1;
	
	// TODO Make this work with AtomicIntegers
	//(currently, opencl compilation error occurs)
	private volatile int[] inCircle;
	private volatile int[] totalPointsCovered;
	
	public PiKernel(long totalPoints) {
		inCircle = new int[App.RADIUS];
		totalPointsCovered = new int[App.RADIUS];
	}
	
	@Override
	public void run() {
		/**
		 * Time results for rectangle per kernel sizes:
		 * RADIUS = 1,000,000
		 * TOTAL_POINTS = 1,000,000,000,000
		 * 
		 * 2*1 (slightly higher accuracy):
		 * 21537.51217ms
		 * 21183.807575ms
		 * 21235.060923ms
		 * 
		 * 2*2:
		 * 38902.078287ms
		 * 40097.443391ms
		 * 39998.550059ms
		 * 
		 * 1*1:
		 * 21519.859036ms
		 * 21132.36578ms
		 * 21167.146552ms
		 */
		
		int gid = getGlobalId();
		
		long x = getGlobalId(0);
		long y = getGlobalId(1);
		
		if (sqrt(x * x + y * y) < App.RADIUS) {
			atomicAdd(inCircle, gid, 1);
		}
		
		atomicAdd(totalPointsCovered, gid, 1);
	}
	
	@Override
	public Kernel execute(Range range) {
		Kernel superKernel = super.execute(range);
		return superKernel;
	}
	
	@SuppressWarnings("deprecation")
	public void runOnGPU(Range range) {
		System.out.println("Running new kernels on GPU...");
		System.out.println("Estimated runtime (64 Bit Ubuntu 22.04 + Nvidia RTX 3060): "
		+ App.toDouble(App.TOTAL_POINTS) / 1_000_000_000_000l * 23 + " seconds.");
		this.setExecutionModeWithoutFallback(EXECUTION_MODE.GPU);
		execute(range);
	}
	
	@SuppressWarnings("deprecation")
	public void runOnCPU(Range range) {
		System.out.println("Running new kernels as Java Thread Pool...");
		this.setExecutionModeWithoutFallback(EXECUTION_MODE.JTP);
		execute(range);
	}
	
	public long getInCircle() {
		System.out.println("Adding kernel inCircle points together...");
		long resultInCircle = 0;
		for (long value : inCircle) {
			if (value <= 0)
				continue;
			resultInCircle += value;
		}
		return resultInCircle;
	}
	
	public long getTotalCoveredPoints() {
		System.out.println("Adding kernel coveredPoints together...");
		long resultCoveredPoints = 0;
		for (long value : totalPointsCovered) {
			if (value <= 0)
				continue;
			resultCoveredPoints += value;
		}
		return resultCoveredPoints;
	}
	
	public void reset() {
		inCircle = new int[inCircle.length];
		totalPointsCovered = new int[totalPointsCovered.length];
	}
}
