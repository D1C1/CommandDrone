package molotov.drone;

public class Physics {
	// Physics Constants
	static final double c = 0.24;
	static final double g = -9.8;

	public static void main(String[] args) throws InterruptedException {
		// Physics stuff (SI units)
		double dist = 0;
		double m = 0.430;
		double s = 1;
		double a = F(m, s)/m;
		double start = System.currentTimeMillis();

		// Speed part:
		System.out.println("Speedup:\n========");
		s = 1;
		
		// Momentum part:
		System.out.println("Momentum:\n=========");
		while (s > 0) {
			double now = (System.currentTimeMillis() - start) / 1000;
			System.out.print("a = " + a + "m/s^2, F = " + F(m, s));
			System.out.print("N, dist = " + dist + "m, s = " + s + "m/s, t = "
					+ now + "s, p = " + momentum(m, s));
			System.out.println("N, F_air = " + F_air(s) + "N");

			a = -1*F(m, s)/m;
			s += a;
			dist += s;
			Thread.sleep(1000);
		}
	}

	private static double F(double m, double s) {
		return momentum(m, s) + Fg(0) + F_air(s);
	}

	// Force due to gravity
	private static double Fg(double m) {
		return g * m;
	}

	// Momentum
	private static double momentum(double m, double s) {
		return m * s;
	}

	// Air resistance
	public static double F_air(Double s) {
		return -1 * c * Math.pow(s, 2);
	}
}
