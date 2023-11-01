package nc.calculatepi;

public class CPUReferenceKernel {
	private long inCircle;
	private long totalPointsDone;
	private long executionTime;
	
	public CPUReferenceKernel(long totalPoints) {
		inCircle = 0;
		this.totalPointsDone = totalPoints;
		executionTime = 0;
	}
	
	public void execute() {
		System.out.println("Running on CPU (single-threaded)...");
		long timeStart = System.currentTimeMillis();
		
		for (int i = 0; i < App.TOTAL_POINTS; i++) {
			int x = i % App.RADIUS + 1;
			int y = i / App.RADIUS + 1;
			double radius = Math.sqrt(x*x + y*y);
			if (radius < App.RADIUS)
				inCircle++;
			totalPointsDone++;
		}
		
		executionTime = System.currentTimeMillis() - timeStart;
	}
	
	public long getInCircle() {
		return inCircle;
	}
	
	public long getTotalCoveredPoints() {
		return totalPointsDone;
	}
	
	public long getAccumulatedExecutionTime() {
		return executionTime;
	}
}
