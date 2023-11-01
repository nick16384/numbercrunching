package nc.calculatepi;

import java.text.DecimalFormat;

import com.aparapi.Range;

public class App {
	public static final int RADIUS = 600_000;
	public static final long TOTAL_POINTS = (long)RADIUS * (long)RADIUS;
	
	public static void main(String[] args) {
		System.out.println("Total points: " + new DecimalFormat("#,###").format(TOTAL_POINTS));
		
		PiKernel kernel = new PiKernel(TOTAL_POINTS);
		//CPUReferenceKernel cpuref = new CPUReferenceKernel(TOTAL_POINTS);
		
		Range range = Range.create3D(RADIUS, RADIUS, 1);
		range.setMaxWorkGroupSize(512);
		kernel.runOnGPU(range);
		
		long inCircleCalculated = kernel.getInCircle();
		long coveredPoints = kernel.getTotalCoveredPoints();
		printResults(true, inCircleCalculated, coveredPoints, kernel.getAccumulatedExecutionTime());
		kernel.dispose();
		
		/*kernel.reset();
		
		kernel.runOnCPU(range);
		inCircleCalculated = kernel.getInCircle();
		coveredPoints = kernel.getTotalCoveredPoints();
		printResults(false, inCircleCalculated, coveredPoints, kernel.getAccumulatedExecutionTime());*/
		
		kernel.dispose();
	}
	
	protected static double toDouble(long in) {
		return Double.parseDouble(Long.toString(in));
	}
	
	private static void printResults(boolean isGPU, long inCircle, long coveredPoints, double timeSpentMS) {
		DecimalFormat formatter = new DecimalFormat("#,###");
		DecimalFormat exactFmt = new DecimalFormat("#,###.000");
		System.out.println("Calculating results...");
		System.out.println("Time spent [" + (isGPU ? "GPU" : "CPU") + "]: " + timeSpentMS + "ms");
		
		double result_D = toDouble(inCircle);
		
		System.out.println("\n=========================================================");
		System.out.println("Total points given:....................." + formatter.format(TOTAL_POINTS));
		System.out.println("Total points covered:..................." + formatter.format(coveredPoints));
		System.out.println("Total points in circle:................." + formatter.format(inCircle));
		System.out.println("% of points covered (should be 100%):..~"
				+ exactFmt.format(toDouble(coveredPoints) / TOTAL_POINTS * 100) + "%");
		System.out.println("% of points inside circle:.............~"
				+ exactFmt.format(result_D / coveredPoints * 100) + "%");
		
		System.out.println("----------------------------------------------------------");
		// Absolute accuracy needed, so formatter is not used.
		double pi = (result_D / coveredPoints * 4);
		System.out.println("Pi (calculated):........................" + pi);
		System.out.println("Pi (Java constant):....................." + Math.PI);
		
		// Find number of correct digits
		int correctDigits = 0;
		String javaMathPiStr = Double.toString(Math.PI);
		String calculatedPiStr = Double.toString(pi);
		for (int idx = 0; idx < calculatedPiStr.length(); idx++) {
			if (calculatedPiStr.charAt(idx) == javaMathPiStr.charAt(idx))
				correctDigits++;
			else {
				break;
			}
		}
		correctDigits = correctDigits - 1; // Subtract decimal point, which is not a digit
		int lastDigitDeviation = correctDigits >= 16 ? Integer.MIN_VALUE :
				Integer.parseInt(Character.toString(calculatedPiStr.charAt(correctDigits + 1)))
				- Integer.parseInt(Character.toString(javaMathPiStr.charAt(correctDigits + 1)));
		
		System.out.println("Number of correct digits:..............."
								+ (correctDigits >= 16 ? "16+" : correctDigits));
		System.out.println("Last digit deviation:..................."
								+ (lastDigitDeviation == Integer.MIN_VALUE ? "Unknown" : lastDigitDeviation));
		System.out.println("=========================================================");
	}
}