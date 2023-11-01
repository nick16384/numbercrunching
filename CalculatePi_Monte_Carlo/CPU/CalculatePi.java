package euler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CalculatePi {
	public static final long MAX_THREAD_POINTS = 500_000_000;
	public static final double APPROX_PIC_PART = 0.7854;
	public static volatile long pointsInsideCircleGlobal = 0;
	
	private static volatile boolean suspendProgressPrinting = false;
	
	public static void main(String[] args) {
		
		// Configurable values
		long totalPoints = 40_000_000_000l;
		long coordinateAccuracy = 200_000;
		System.out.println("Total points: " + totalPoints);
		System.out.println("Coordinate system accuracy: " + coordinateAccuracy + "\n");
		
		// Calculate values
		//double[] results = sequentialRun(totalPoints, coordinateAccuracy);
		//printResults(results);
		
		double[] results = parallelRun(totalPoints, coordinateAccuracy);
		printResults(results);
	}
	
	private static double toDouble(long from) {
		return Double.parseDouble(Long.toString(from));
	}
	
	private static void printResults(double[] results) {
		double pi = results[0];
		
		System.out.println("\n=========================================");
		System.out.println("Percentage of points inside circle: " + (results[1] / results[2]) * 100 + "%");
		System.out.println("Approximate value of Pi: " + pi);
		System.out.println("=========================================");
	}
	
	private static double[] sequentialRun(long totalPoints, long coordinateAccuracy) {
		long x;
		long y;
		long pointsInsideCircle = 0;
		long startTime = System.currentTimeMillis();
		
		System.out.println("Doing sequential run.");
		System.out.print("Running calculation...");
		for (int i = 0; i < totalPoints; i++) {
			x = ThreadLocalRandom.current().nextLong(1, coordinateAccuracy + 1) + 1;
			y = ThreadLocalRandom.current().nextLong(1, coordinateAccuracy + 1) + 1;
			
			double radius = Math.sqrt(x * x + y * y);
			
			if (radius < coordinateAccuracy) {
				pointsInsideCircle++;
			}
		}
		System.out.println(" done.");
		System.out.println("Runtime: " + (System.currentTimeMillis() - startTime) + "ms.");
		
		// (pi * r^2) / (2r^2) = pointsInsideCircle / totalPoints
		// pi*rr / 4*rr = pointsInsideCircle / totalPoints
		// pi / 4 = pointsInsideCircle / totalPoints
		// pi = pointsInsideCircle / totalPoints * 4
		
		double pointsInsideCircle_D = toDouble(pointsInsideCircle);
		double totalPoints_D = toDouble(totalPoints);
		
		return new double[] { (pointsInsideCircle_D / totalPoints_D) * 4, pointsInsideCircle_D, totalPoints_D };
	}
	
	private static double[] parallelRun(long totalPoints, long coordinateAccuracy) {
		ExecutorService parallelExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// totalPoints divided by system threads
		// Minimum start value distance: 500.000.000
		// Maximum start value distance: totalPoints / totalThreads
		long[] threadStartValues = new long[Runtime.getRuntime().availableProcessors()];
		for (int i = 0; i < threadStartValues.length; i++) { threadStartValues[i] = 0; } // Initialize all values with 0
		long remainingTotalPoints = totalPoints;
		
		if (totalPoints <= MAX_THREAD_POINTS) {
			// If small enough, one thread does it all
			threadStartValues[0] = totalPoints;
			System.out.println("Running on single thread only.");
		} else if (totalPoints / threadStartValues.length <= MAX_THREAD_POINTS) {
			// ...otherwise, work is split up on threads (not more than MAX_THREAD_POINTS per thread)
			for (int i = 0; i < threadStartValues.length; i++) {
				threadStartValues[i] = Math.min(remainingTotalPoints, MAX_THREAD_POINTS);
				remainingTotalPoints -= Math.min(remainingTotalPoints, MAX_THREAD_POINTS);
				System.out.println("Remaining points to allocate : " + remainingTotalPoints);
				if (remainingTotalPoints <= 0)
					break;
			}
			System.out.println("Divided work on threads without passing MAX_THREAD_POINTS.");
		} else {
			// Number is very big, so threads need more points than MAX_THREAD_POINTS
			for (int i = 0; i < threadStartValues.length; i++) {
				threadStartValues[i] = totalPoints / threadStartValues.length;
			}
			System.out.println("Warning: Divided work on threads, but threads were given bigger tasks than usual.");
			System.out.println("This may take longer.");
		}
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("Doing parallel run.");
		System.out.print("Running calculation...\n");
		
		for (int i = 0; i < threadStartValues.length; i++) {
			long threadPoints = threadStartValues[i];
			System.out.println("Allocating thread " + i + " with " + threadPoints + " points.");
			parallelExecutor.submit(() -> {
				long x;
				long y;
				long pointsInsideCircle = 0;
				for (long j = 0; j < threadPoints; j++) {
					x = ThreadLocalRandom.current().nextLong(1, coordinateAccuracy + 1) + 1;
					y = ThreadLocalRandom.current().nextLong(1, coordinateAccuracy + 1) + 1;
					
					double radius = Math.sqrt(x * x + y * y);
					
					if (radius < coordinateAccuracy) {
						pointsInsideCircle++;
						
						if (pointsInsideCircle % 1000000 == 0) {
							addToPointsInsideCircle(pointsInsideCircle);
							pointsInsideCircle = 0;
						}
					}
				}
				addToPointsInsideCircle(pointsInsideCircle);
			});
		}
		// Shutdown does not immediately stop all threads, but instead waits for them all to do so.
		parallelExecutor.shutdown();
		
		try {
			// Print progress regularly
			new Thread(() -> {
				suspendProgressPrinting = false;
				while (!suspendProgressPrinting) {
					try { Thread.sleep(1000); }
					catch (InterruptedException ie) { ie.printStackTrace(); }
					printProgress(pointsInsideCircleGlobal, totalPoints);
				}
			}).start();
			System.out.println("Waiting for threads to finish...");
			parallelExecutor.awaitTermination(30, TimeUnit.MINUTES);
			suspendProgressPrinting = true;
		} catch (InterruptedException ie) {
			System.err.println("Calculation was interrupted. Aborting.");
			parallelExecutor.shutdownNow();
			suspendProgressPrinting = true;
		}
		
		System.out.println(" done.");
		System.out.println("Runtime: " + (System.currentTimeMillis() - startTime) + "ms.");
		
		double pointsInsideCircle_D = toDouble(pointsInsideCircleGlobal);
		double totalPoints_D = toDouble(totalPoints);
		
		return new double[] { (pointsInsideCircle_D / totalPoints_D) * 4, pointsInsideCircle_D, totalPoints_D };
	}
	
	private synchronized static void addToPointsInsideCircle(long toAdd) {
		pointsInsideCircleGlobal += toAdd;
	}
	
	private synchronized static void printProgress(long localPointsInsideCircle, long localThreadPoints) {
		System.out.println(
				"Approx. Progress: "
						+ (localPointsInsideCircle / (localThreadPoints * APPROX_PIC_PART) * 100) + "%");
	}
}
