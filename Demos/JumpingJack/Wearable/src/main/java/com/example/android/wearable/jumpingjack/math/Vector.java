package com.example.android.wearable.jumpingjack.math;

public interface Vector<T extends Vector<T>> {
	/** @return a copy of this vector */
	T Cpy ();

	/** @return The euclidean length */
	double len ();

	/** This method is faster than {@link Vector#len()} because it avoids calculating a square root. It is useful for comparisons,
	 * but not for getting exact lengths, as the return value is the square of the actual length.
	 * @return The squared euclidean length */
	double len2 ();

	/** Limits the length of this vector, based on the desired maximum length.
	 * @param limit desired maximum length for this vector
	 * @return this vector for chaining */
	T Limit (double limit);

	/** Limits the length of this vector, based on the desired maximum length squared.
	 * <p />
	 * This method is slightly faster than limit().
	 * @param limit2 squared desired maximum length for this vector
	 * @return this vector for chaining
	 * @see #len2() */
	T Limit2 (double limit2);

	/** Sets the length of this vector. Does nothing is this vector is zero.
	 * @param len desired length for this vector
	 * @return this vector for chaining */
	T SetLength (double len);

	/** Sets the length of this vector, based on the square of the desired length. Does nothing is this vector is zero.
	 * <p />
	 * This method is slightly faster than setLength().
	 * @param len2 desired square of the length for this vector
	 * @return this vector for chaining
	 * @see #len2() */
	T SetLength2 (double len2);

	/** Clamps this vector's length to given min and max values
	 * @param min Min length
	 * @param max Max length
	 * @return This vector for chaining */
	T Clamp (double min, double max);

	/** Sets this vector from the given vector
	 * @param v The vector
	 * @return This vector for chaining */
	T Set (T v);

	/** Subtracts the given vector from this vector.
	 * @param v The vector
	 * @return This vector for chaining */
	T Sub (T v);
	
	/** Subtracts the given vector from this vector.
	 * @param v the vector
	 * @return new vector for assigning
	 */
	T sub (T v);

	/** Normalizes this vector. Does nothing if it is zero.
	 * @return This vector for chaining */
	T Nor ();

	/** Adds the given vector to this vector
	 * @param v The vector
	 * @return This vector for chaining */
	T Add (T v);

	/** Adds the give vector to this vector
	 * @param v the vector
	 * @return new vector for assigning
	 */
	T add(T v);
	
	/** @param v The other vector
	 * @return The dot product between this and the other vector */
	double dot (T v);

	/** Scales this vector by a scalar
	 * @param scalar The scalar
	 * @return This vector for chaining */
	T Scl (double scalar);

	/** Scales this vector by a scalar
	 * @param scalar The scalar
	 * @return New Vector for assigning */
	T scl (double scalar);
	
	/** Scales this vector by another vector
	 * @return This vector for chaining */
	T Scl (T v);

	/** @param v The other vector
	 * @return the distance between this and the other vector */
	double dst (T v);

	/** This method is faster than {@link Vector#dst(Vector)} because it avoids calculating a square root. It is useful for
	 * comparisons, but not for getting accurate distances, as the return value is the square of the actual distance.
	 * @param v The other vector
	 * @return the squared distance between this and the other vector */
	double dst2 (T v);

	/** Linearly interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is stored
	 * in this vector.
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	T Lerp (T target, double alpha);

	/** Interpolates between this vector and the given target vector by alpha (within range [0,1]) using the given Interpolation
	 * method. the result is stored in this vector.
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @param interpolator An Interpolation object describing the used interpolation method
	 * @return This vector for chaining. 
	T interpolate (T target, double alpha, Interpolation interpolator);
	*/
	
	/** @return Whether this vector is a unit length vector */
	boolean isUnit ();

	/** @return Whether this vector is a unit length vector within the given margin. */
	boolean isUnit (final double margin);

	/** @return Whether this vector is a zero vector */
	boolean isZero ();

	/** @return Whether the length of this vector is smaller than the given margin */
	boolean isZero (final double margin);

	/** @return true if this vector is in line with the other vector (either in the same or the opposite direction) */
	boolean isOnLine (T other, double epsilon);

	/** @return true if this vector is in line with the other vector (either in the same or the opposite direction) */
	boolean isOnLine (T other);

	/** @return true if this vector is collinear with the other vector ({@link #isOnLine(Vector, double)} &&
	 *         {@link #hasSameDirection(Vector)}). */
	boolean isCollinear (T other, double epsilon);

	/** @return true if this vector is collinear with the other vector ({@link #isOnLine(Vector)} &&
	 *         {@link #hasSameDirection(Vector)}). */
	boolean isCollinear (T other);

	/** @return true if this vector is opposite collinear with the other vector ({@link #isOnLine(Vector, double)} &&
	 *         {@link #hasOppositeDirection(Vector)}). */
	boolean isCollinearOpposite (T other, double epsilon);

	/** @return true if this vector is opposite collinear with the other vector ({@link #isOnLine(Vector)} &&
	 *         {@link #hasOppositeDirection(Vector)}). */
	boolean isCollinearOpposite (T other);

	/** @return Whether this vector is perpendicular with the other vector. True if the dot product is 0. */
	boolean isPerpendicular (T other);

	/** @return Whether this vector is perpendicular with the other vector. True if the dot product is 0.
	 * @param epsilon a positive small number close to zero */
	boolean isPerpendicular (T other, double epsilon);

	/** @return Whether this vector has similar direction compared to the other vector. True if the normalized dot product is > 0. */
	boolean hasSameDirection (T other);

	/** @return Whether this vector has opposite direction compared to the other vector. True if the normalized dot product is < 0. */
	boolean hasOppositeDirection (T other);

	/** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
	 * @param other
	 * @param epsilon
	 * @return whether the vectors have fuzzy equality. */
	boolean epsilonEquals (T other, double epsilon);

	/** First scale a supplied vector, then add it to this vector.
	 * @param v addition vector
	 * @param scalar for scaling the addition vector */
	T MulAdd (T v, double scalar);

	/** First scale a supplied vector, then add it to this vector.
	 * @param v addition vector
	 * @param mulVec vector by whose values the addition vector will be scaled */
	T MulAdd (T v, T mulVec);

	/** Sets the components of this vector to 0
	 * @return This vector for chaining */
	T SetZero ();
}