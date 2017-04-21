package com.example.android.wearable.jumpingjack.math;

import java.util.Random;

public class MathUtils {
	static public final double nanoToSec = 1 / 1000000000f;

	// ---
	static public final double double_ROUNDING_ERROR = 0.000001f; // 32 bits
	static public final double PI = 3.1415927f;
	static public final double PI2 = PI * 2;

	static public final double E = 2.7182818f;

	static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
	static private final int SIN_MASK = ~(-1 << SIN_BITS);
	static private final int SIN_COUNT = SIN_MASK + 1;

	static private final double radFull = PI * 2;
	static private final double degFull = 360;
	static private final double radToIndex = SIN_COUNT / radFull;
	static private final double degToIndex = SIN_COUNT / degFull;

	/** multiply by this to convert from radians to degrees */
	static public final double radiansToDegrees = 180f / PI;
	static public final double radDeg = radiansToDegrees;
	/** multiply by this to convert from degrees to radians */
	static public final double degreesToRadians = PI / 180;
	static public final double degRad = degreesToRadians;

	static private class Sin {
		static final double[] table = new double[SIN_COUNT];
		static {
			for (int i = 0; i < SIN_COUNT; i++)
				table[i] = (double)Math.sin((i + 0.5f) / SIN_COUNT * radFull);
			for (int i = 0; i < 360; i += 90)
				table[(int)(i * degToIndex) & SIN_MASK] = (double)Math.sin(i * degreesToRadians);
		}
	}

	/** Returns the sine in radians from a lookup table. */
	static public double sin (double radians) {
		return Sin.table[(int)(radians * radToIndex) & SIN_MASK];
	}

	/** Returns the cosine in radians from a lookup table. */
	static public double cos (double radians) {
		return Sin.table[(int)((radians + PI / 2) * radToIndex) & SIN_MASK];
	}

	/** Returns the sine in radians from a lookup table. */
	static public double sinDeg (double degrees) {
		return Sin.table[(int)(degrees * degToIndex) & SIN_MASK];
	}

	/** Returns the cosine in radians from a lookup table. */
	static public double cosDeg (double degrees) {
		return Sin.table[(int)((degrees + 90) * degToIndex) & SIN_MASK];
	}

	// ---

	static private final int ATAN2_BITS = 7; // Adjust for accuracy.
	static private final int ATAN2_BITS2 = ATAN2_BITS << 1;
	static private final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	static private final int ATAN2_COUNT = ATAN2_MASK + 1;
	static final int ATAN2_DIM = (int)Math.sqrt(ATAN2_COUNT);
	static private final double INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

	static private class Atan2 {
		static final double[] table = new double[ATAN2_COUNT];
		static {
			for (int i = 0; i < ATAN2_DIM; i++) {
				for (int j = 0; j < ATAN2_DIM; j++) {
					double x0 = (double)i / ATAN2_DIM;
					double y0 = (double)j / ATAN2_DIM;
					table[j * ATAN2_DIM + i] = (double)Math.atan2(y0, x0);
				}
			}
		}
	}

	/** Returns atan2 in radians from a lookup table. */
	static public double atan2 (double y, double x) {
		double add, mul;
		if (x < 0) {
			if (y < 0) {
				y = -y;
				mul = 1;
			} else
				mul = -1;
			x = -x;
			add = -PI;
		} else {
			if (y < 0) {
				y = -y;
				mul = -1;
			} else
				mul = 1;
			add = 0;
		}
		double invDiv = 1 / ((x < y ? y : x) * INV_ATAN2_DIM_MINUS_1);

		if (invDiv == Double.POSITIVE_INFINITY) return ((double)Math.atan2(y, x) + add) * mul;

		int xi = (int)(x * invDiv);
		int yi = (int)(y * invDiv);
		return (Atan2.table[yi * ATAN2_DIM + xi] + add) * mul;
	}

	// ---

	static public Random random = new Random();

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public int random (int range) {
		return random.nextInt(range + 1);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public int random (int start, int end) {
		return start + random.nextInt(end - start + 1);
	}

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public long random (long range) {
		return (long)(random.nextDouble() * range);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public long random (long start, long end) {
		return start + (long)(random.nextDouble() * (end - start));
	}

	/** Returns a random boolean value. */
	static public boolean randomBoolean () {
		return random.nextBoolean();
	}

	/** Returns true if a random value between 0 and 1 is less than the specified value. */
	static public boolean randomBoolean (double chance) {
		return MathUtils.random() < chance;
	}

	/** Returns random number between 0.0 (inclusive) and 1.0 (exclusive). */
	static public double random () {
		return random.nextDouble();
	}

	/** Returns a random number between 0 (inclusive) and the specified value (exclusive). */
	static public double random (double range) {
		return random.nextDouble() * range;
	}

	/** Returns a random number between start (inclusive) and end (exclusive). */
	static public double random (double start, double end) {
		return start + random.nextDouble() * (end - start);
	}

	/** Returns -1 or 1, randomly. */
	static public int randomSign () {
		return 1 | (random.nextInt() >> 31);
	}

	/** Returns a triangularly distributed random number between -1.0 (exclusive) and 1.0 (exclusive), where values around zero are
	 * more likely.
	 * <p>
	 * This is an optimized version of {@link #randomTriangular(double, double, double) randomTriangular(-1, 1, 0)} */
	public static double randomTriangular () {
		return random.nextDouble() - random.nextDouble();
	}

	/** Returns a triangularly distributed random number between {@code -max} (exclusive) and {@code max} (exclusive), where values
	 * around zero are more likely.
	 * <p>
	 * This is an optimized version of {@link #randomTriangular(double, double, double) randomTriangular(-max, max, 0)}
	 * @param max the upper limit */
	public static double randomTriangular (double max) {
		return (random.nextDouble() - random.nextDouble()) * max;
	}

	/** Returns a triangularly distributed random number between {@code min} (inclusive) and {@code max} (exclusive), where the
	 * {@code mode} argument defaults to the midpoint between the bounds, giving a symmetric distribution.
	 * <p>
	 * This method is equivalent of {@link #randomTriangular(double, double, double) randomTriangular(min, max, (max - min) * .5f)}
	 * @param min the lower limit
	 * @param max the upper limit */
	public static double randomTriangular (double min, double max) {
		return randomTriangular(min, max, min + (max - min) * 0.5f);
	}

	/** Returns a triangularly distributed random number between {@code min} (inclusive) and {@code max} (exclusive), where values
	 * around {@code mode} are more likely.
	 * @param min the lower limit
	 * @param max the upper limit
	 * @param mode the point around which the values are more likely */
	public static double randomTriangular (double min, double max, double mode) {
		double u = random.nextDouble();
		double d = max - min;
		if (u <= (mode - min) / d) return min + (double)Math.sqrt(u * d * (mode - min));
		return max - (double)Math.sqrt((1 - u) * d * (max - mode));
	}

	// ---

	/** Returns the next power of two. Returns the specified value if the value is already a power of two. */
	static public int nextPowerOfTwo (int value) {
		if (value == 0) return 1;
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return value + 1;
	}

	static public boolean isPowerOfTwo (int value) {
		return value != 0 && (value & value - 1) == 0;
	}

	// ---

	static public short clamp (short value, short min, short max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public int clamp (int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public long clamp (long value, long min, long max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public double clamp (double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	//static public double clamp (double value, double min, double max) {
		//if (value < min) return min;
		//if (value > max) return max;
		//return value;
	//}

	// ---

	/** Linearly interpolates between fromValue to toValue on progress position. */
	static public double lerp (double fromValue, double toValue, double progress) {
		return fromValue + (toValue - fromValue) * progress;
	}

	// ---

	static private final int BIG_ENOUGH_INT = 16 * 1024;
	static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	static private final double CEIL = 0.9999999;
	static private final double BIG_ENOUGH_CEIL = 16384.999999999996;
	static private final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

	/** Returns the largest integer less than or equal to the specified double. This method will only properly floor doubles from
	 * -(2^14) to (double.MAX_VALUE - 2^14). */
	static public int floor (double value) {
		return (int)(value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}

	/** Returns the largest integer less than or equal to the specified double. This method will only properly floor doubles that are
	 * positive. Note this method simply casts the double to int. */
	static public int floorPositive (double value) {
		return (int)value;
	}

	/** Returns the smallest integer greater than or equal to the specified double. This method will only properly ceil doubles from
	 * -(2^14) to (double.MAX_VALUE - 2^14). */
	static public int ceil (double value) {
		return (int)(value + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
	}

	/** Returns the smallest integer greater than or equal to the specified double. This method will only properly ceil doubles that
	 * are positive. */
	static public int ceilPositive (double value) {
		return (int)(value + CEIL);
	}

	/** Returns the closest integer to the specified double. This method will only properly round doubles from -(2^14) to
	 * (double.MAX_VALUE - 2^14). */
	static public int round (double value) {
		return (int)(value + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
	}

	/** Returns the closest integer to the specified double. This method will only properly round doubles that are positive. */
	static public int roundPositive (double value) {
		return (int)(value + 0.5f);
	}

	/** Returns true if the value is zero (using the default tolerance as upper bound) */
	static public boolean isZero (double value) {
		return Math.abs(value) <= double_ROUNDING_ERROR;
	}

	/** Returns true if the value is zero.
	 * @param tolerance represent an upper bound below which the value is considered zero. */
	static public boolean isZero (double value, double tolerance) {
		return Math.abs(value) <= tolerance;
	}

	/** Returns true if a is nearly equal to b. The function uses the default doubleing error tolerance.
	 * @param a the first value.
	 * @param b the second value. */
	static public boolean isEqual (double a, double b) {
		return Math.abs(a - b) <= double_ROUNDING_ERROR;
	}

	/** Returns true if a is nearly equal to b.
	 * @param a the first value.
	 * @param b the second value.
	 * @param tolerance represent an upper bound below which the two values are considered equal. */
	static public boolean isEqual (double a, double b, double tolerance) {
		return Math.abs(a - b) <= tolerance;
	}

	/** @return the logarithm of value with base a */
	static public double log (double a, double value) {
		return (double)(Math.log(value) / Math.log(a));
	}

	/** @return the logarithm of value with base 2 */
	static public double log2 (double value) {
		return log(2, value);
	}
}
