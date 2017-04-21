package com.teng.math;

public class Vector2 implements Vector<Vector2>{
	public final static Vector2 X = new Vector2(1, 0);
	public final static Vector2 Y = new Vector2(0, 1);
	public final static Vector2 Zero = new Vector2(0, 0);

	/** the x-component of this vector **/
	public double x;
	/** the y-component of this vector **/
	public double y;

	/** Constructs a new vector at (0,0) */
	public Vector2 () {
	}

	/** Constructs a vector with the given components
	 * @param x The x-component
	 * @param y The y-component */
	public Vector2 (double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** Constructs a vector from the given vector
	 * @param v The vector */
	public Vector2 (Vector2 v) {
		Set(v);
	}

	@Override
	public Vector2 Cpy () {
		return new Vector2(this);
	}

	public static double len (double x, double y) {
		return (double)Math.sqrt(x * x + y * y);
	}

	@Override
	public double len () {
		return (double)Math.sqrt(x * x + y * y);
	}

	public static double len2 (double x, double y) {
		return x * x + y * y;
	}

	@Override
	public double len2 () {
		return x * x + y * y;
	}

	@Override
	public Vector2 Set (Vector2 v) {
		x = v.x;
		y = v.y;
		return this;
	}

	/** Sets the components of this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining */
	public Vector2 Set (double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	@Override
	public Vector2 Sub (Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}
	
	@Override
	public Vector2 sub (Vector2 v){
		Vector2 temp = new Vector2();
		temp.x = x - v.x;
		temp.y = y - v.y;
		return temp;
	}

	/** Substracts the other vector from this vector.
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return This vector for chaining */
	public Vector2 sub (double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	@Override
	public Vector2 Nor () {
		double len = len();
		if (len != 0) {
			x /= len;
			y /= len;
		}
		return this;
	}

	@Override
	public Vector2 add (Vector2 v)
	{
		Vector2 temp = new Vector2();
		temp.x = x + v.x;
		temp.y = y + v.y;
		return temp;
	}
	
	@Override
	public Vector2 Add (Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/** Adds the given components to this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining */
	public Vector2 Add (double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public static double dot (double x1, double y1, double x2, double y2) {
		return x1 * x2 + y1 * y2;
	}

	@Override
	public double dot (Vector2 v) {
		return x * v.x + y * v.y;
	}

	public double dot (double ox, double oy) {
		return x * ox + y * oy;
	}

	
	@Override
	public Vector2 scl (double scalar) {
		Vector2 temp = new Vector2();
		temp.x = x*scalar;
		temp.y = y*scalar;
		return temp;
	}
	
	@Override
	public Vector2 Scl(double scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}

	/** Multiplies this vector by a scalar
	 * @return This vector for chaining */
	public Vector2 Scl (double x, double y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	@Override
	public Vector2 Scl (Vector2 v) {
		this.x *= v.x;
		this.y *= v.y;
		return this;
	}

	@Override
	public Vector2 MulAdd (Vector2 vec, double scalar) {
		this.x += vec.x * scalar;
		this.y += vec.y * scalar;
		return this;
	}

	@Override
	public Vector2 MulAdd (Vector2 vec, Vector2 mulVec) {
		this.x += vec.x * mulVec.x;
		this.y += vec.y * mulVec.y;
		return this;
	}

	public static double dst (double x1, double y1, double x2, double y2) {
		final double x_d = x2 - x1;
		final double y_d = y2 - y1;
		return (double)Math.sqrt(x_d * x_d + y_d * y_d);
	}

	@Override
	public double dst (Vector2 v) {
		final double x_d = v.x - x;
		final double y_d = v.y - y;
		return (double)Math.sqrt(x_d * x_d + y_d * y_d);
	}

	/** @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return the distance between this and the other vector */
	public double dst (double x, double y) {
		final double x_d = x - this.x;
		final double y_d = y - this.y;
		return (double)Math.sqrt(x_d * x_d + y_d * y_d);
	}

	public static double dst2 (double x1, double y1, double x2, double y2) {
		final double x_d = x2 - x1;
		final double y_d = y2 - y1;
		return x_d * x_d + y_d * y_d;
	}

	@Override
	public double dst2 (Vector2 v) {
		final double x_d = v.x - x;
		final double y_d = v.y - y;
		return x_d * x_d + y_d * y_d;
	}

	/** @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return the squared distance between this and the other vector */
	public double dst2 (double x, double y) {
		final double x_d = x - this.x;
		final double y_d = y - this.y;
		return x_d * x_d + y_d * y_d;
	}

	@Override
	public Vector2 Limit (double limit) {
		return Limit2(limit * limit);
	}

	@Override
	public Vector2 Limit2 (double limit2) {
		double len2 = len2();
		if (len2 > limit2) {
			return Scl((double)Math.sqrt(limit2 / len2));
		}
		return this;
	}

	@Override
	public Vector2 Clamp (double min, double max) {
		final double len2 = len2();
		if (len2 == 0f)
			return this;
		double max2 = max * max;
		if (len2 > max2)
			return Scl((double)Math.sqrt(max2 / len2));
		double min2 = min * min;
		if (len2 < min2)
			return Scl((double)Math.sqrt(min2 / len2));
		return this;
	}

	@Override
	public Vector2 SetLength ( double len ) {
		return SetLength2( len * len );
	}

	@Override
	public Vector2 SetLength2 ( double len2 ) {
		double oldLen2 = len2();
		return ( oldLen2 == 0 || oldLen2 == len2 )
				? this
				: Scl((double) Math.sqrt( len2 / oldLen2 ));
	}

	@Override
	public String toString () {
		return "[" + x + ":" + y + "]";
	}

	/** Left-multiplies this vector by the given matrix
	 * @param mat the matrix
	 * @return this vector */
	public Vector2 Mul (Matrix3 mat) {
		double x = this.x * mat.val[0] + this.y * mat.val[3] + mat.val[6];
		double y = this.x * mat.val[1] + this.y * mat.val[4] + mat.val[7];
		this.x = x;
		this.y = y;
		return this;
	}

	/** Calculates the 2D cross product between this and the given vector.
	 * @param v the other vector
	 * @return the cross product */
	public double crs (Vector2 v) {
		return this.x * v.y - this.y * v.x;
	}

	/** Calculates the 2D cross product between this and the given vector.
	 * @param x the x-coordinate of the other vector
	 * @param y the y-coordinate of the other vector
	 * @return the cross product */
	public double crs (double x, double y) {
		return this.x * y - this.y * x;
	}

	/** @return the angle in degrees of this vector (point) relative to the x-axis. Angles are towards the positive y-axis (typically
	 *         counter-clockwise) and between 0 and 360. */
	public double angle () {
		double angle = (double)Math.atan2(y, x) * MathUtils.radiansToDegrees;
		if (angle < 0) angle += 360;
		return angle;
	}

	/** @return the angle in degrees of this vector (point) relative to the given vector. Angles are towards the positive y-axis
	 *         (typically counter-clockwise.) between -180 and +180 */
	public double angle (Vector2 reference) {
		return (double)Math.atan2(crs(reference), dot(reference)) * MathUtils.radiansToDegrees;
	}

	/** @return the angle in radians of this vector (point) relative to the x-axis. Angles are towards the positive y-axis.
	 *         (typically counter-clockwise) */
	public double angleRad () {
		return (double)Math.atan2(y, x);
	}

	/** @return the angle in radians of this vector (point) relative to the given vector. Angles are towards the positive y-axis.
	 *         (typically counter-clockwise.) */
	public double angleRad (Vector2 reference) {
		return (double)Math.atan2(crs(reference), dot(reference));
	}

	/** Sets the angle of the vector in degrees relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
	 * @param degrees The angle in degrees to Set. */
	public Vector2 SetAngle (double degrees) {
		return SetAngleRad(degrees * MathUtils.degreesToRadians);
	}

	/** Sets the angle of the vector in radians relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
	 * @param radians The angle in radians to Set. */
	public Vector2 SetAngleRad (double radians) {
		this.Set(len(), 0f);
		this.RotateRad(radians);

		return this;
	}

	/** Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
	 * @param degrees the angle in degrees */
	public Vector2 Rotate (double degrees) {
		return RotateRad(degrees * MathUtils.degreesToRadians);
	}

	/** Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
	 * @param radians the angle in radians */
	public Vector2 RotateRad (double radians) {
		double cos = (double)Math.cos(radians);
		double sin = (double)Math.sin(radians);

		double newX = this.x * cos - this.y * sin;
		double newY = this.x * sin + this.y * cos;

		this.x = newX;
		this.y = newY;

		return this;
	}

	/** Rotates the Vector2 by 90 degrees in the specified direction, where >= 0 is counter-clockwise and < 0 is clockwise. */
	public Vector2 rotate90 (int dir) {
		double x = this.x;
		if (dir >= 0) {
			this.x = -y;
			y = x;
		} else {
			this.x = y;
			y = -x;
		}
		return this;
	}

	@Override
	public Vector2 Lerp (Vector2 target, double alpha) {
		final double invAlpha = 1.0f - alpha;
		this.x = (x * invAlpha) + (target.x * alpha);
		this.y = (y * invAlpha) + (target.y * alpha);
		return this;
	}


	@Override
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		//result = prime * result + NumberUtils.doubleToIntBits(x);
		//result = prime * result + NumberUtils.doubleToIntBits(y);
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vector2 other = (Vector2)obj;
		//if (NumberUtils.doubleToIntBits(x) != NumberUtils.doubleToIntBits(other.x)) return false;
		//if (NumberUtils.doubleToIntBits(y) != NumberUtils.doubleToIntBits(other.y)) return false;
		return true;
	}

	@Override
	public boolean epsilonEquals (Vector2 other, double epsilon) {
		if (other == null) return false;
		if (Math.abs(other.x - x) > epsilon) return false;
		if (Math.abs(other.y - y) > epsilon) return false;
		return true;
	}

	/** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
	 * @return whether the vectors are the same. */
	public boolean epsilonEquals (double x, double y, double epsilon) {
		if (Math.abs(x - this.x) > epsilon) return false;
		if (Math.abs(y - this.y) > epsilon) return false;
		return true;
	}

	@Override
	public boolean isUnit () {
		return isUnit(0.000000001f);
	}

	@Override
	public boolean isUnit (final double margin) {
		return Math.abs(len2() - 1f) < margin;
	}

	@Override
	public boolean isZero () {
		return x == 0 && y == 0;
	}

	@Override
	public boolean isZero (final double margin) {
		return len2() < margin;
	}

	@Override
	public boolean isOnLine (Vector2 other) {
		return MathUtils.isZero(x * other.y - y * other.x);
	}

	@Override
	public boolean isOnLine (Vector2 other, double epsilon) {
		return MathUtils.isZero(x * other.y - y * other.x, epsilon);
	}

	@Override
	public boolean isCollinear (Vector2 other, double epsilon) {
		return isOnLine(other, epsilon) && dot(other) > 0f;
	}

	@Override
	public boolean isCollinear (Vector2 other) {
		return isOnLine(other) && dot(other) > 0f;
	}

	@Override
	public boolean isCollinearOpposite (Vector2 other, double epsilon) {
		return isOnLine(other, epsilon) && dot(other) < 0f;
	}

	@Override
	public boolean isCollinearOpposite (Vector2 other) {
		return isOnLine(other) && dot(other) < 0f;
	}

	@Override
	public boolean isPerpendicular (Vector2 vector) {
		return MathUtils.isZero(dot(vector));
	}

	@Override
	public boolean isPerpendicular (Vector2 vector, double epsilon) {
		return MathUtils.isZero(dot(vector), epsilon);
	}

	@Override
	public boolean hasSameDirection (Vector2 vector) {
		return dot(vector) > 0;
	}

	@Override
	public boolean hasOppositeDirection (Vector2 vector) {
		return dot(vector) < 0;
	}

	@Override
	public Vector2 SetZero () {
		this.x = 0;
		this.y = 0;
		return this;
	}

	
}
