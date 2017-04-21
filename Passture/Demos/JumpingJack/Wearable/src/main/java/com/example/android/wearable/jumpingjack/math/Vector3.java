package com.example.android.wearable.jumpingjack.math;

public class Vector3 implements Vector<Vector3>{
	/** the x-component of this vector **/
	public double x;
	/** the y-component of this vector **/
	public double y;
	/** the z-component of this vector **/
	public double z;

	public final static Vector3 X = new Vector3(1, 0, 0);
	public final static Vector3 Y = new Vector3(0, 1, 0);
	public final static Vector3 Z = new Vector3(0, 0, 1);
	public final static Vector3 Zero = new Vector3(0, 0, 0);

	private final static Matrix4 tmpMat = new Matrix4();

	/** Constructs a vector at (0,0,0) */
	public Vector3 () {
	}

	/** Creates a vector with the given components
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component */
	public Vector3 (double x, double y, double z) {
		this.Set(x, y, z);
	}

	/** Creates a vector from the given vector
	 * @param vector The vector */
	public Vector3 (final Vector3 vector) {
		this.Set(vector);
	}

	/** Creates a vector from the given array. The array must have at least 3 elements.
	 *
	 * @param values The array */
	public Vector3 (final double[] values) {
		this.Set(values[0], values[1], values[2]);
	}

	/** Creates a vector from the given vector and z-component
	 *
	 * @param vector The vector
	 * @param z The z-component */
	public Vector3 (final Vector2 vector, double z) {
		this.Set(vector.x, vector.y, z);
	}

	/** Sets the vector to the given components
	 *
	 * @param x The x-component
	 * @param y The y-component
	 * @param z The z-component
	 * @return this vector for chaining */
	public Vector3 Set (double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	@Override
	public Vector3 Set (final Vector3 vector) {
		return this.Set(vector.x, vector.y, vector.z);
	}

	/** Sets the components from the array. The array must have at least 3 elements
	 *
	 * @param values The array
	 * @return this vector for chaining */
	public Vector3 Set (final double[] values) {
		return this.Set(values[0], values[1], values[2]);
	}

	/** Sets the components of the given vector and z-component
	 *
	 * @param vector The vector
	 * @param z The z-component
	 * @return This vector for chaining */
	public Vector3 Set (final Vector2 vector, double z) {
		return this.Set(vector.x, vector.y, z);
	}

	@Override
	public Vector3 Cpy() {
		return new Vector3(this);
	}

	@Override
	public Vector3 add (final Vector3 vector){
		Vector3 temp = new Vector3();
		temp.x = x + vector.x;
		temp.y = y + vector.y;
		temp.z = z + vector.z;
		return temp;
	}
	
	@Override
	public Vector3 Add (final Vector3 vector) {
		return this.Add(vector.x, vector.y, vector.z);
	}

	/** Adds the given vector to this component
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @param z The z-component of the other vector
	 * @return This vector for chaining. */
	public Vector3 Add (double x, double y, double z) {
		return this.Set(this.x + x, this.y + y, this.z + z);
	}

	/** Adds the given value to all three components of the vector.
	 *
	 * @param values The value
	 * @return This vector for chaining */
	public Vector3 Add (double values) {
		return this.Set(this.x + values, this.y + values, this.z + values);
	}

	@Override
	public Vector3 Sub (final Vector3 a_vec) {
		return this.Sub(a_vec.x, a_vec.y, a_vec.z);
	}
	
	@Override
	public Vector3 sub (final Vector3 a_vec){
		Vector3 temp = new Vector3();
		temp.x = x - a_vec.x;
		temp.y = y - a_vec.y;
		temp.z = z - a_vec.z;
		return temp;
	}

	/** Subtracts the other vector from this vector.
	 *
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @param z The z-component of the other vector
	 * @return This vector for chaining */
	public Vector3 Sub (double x, double y, double z) {
		return this.Set(this.x - x, this.y - y, this.z - z);
	}

	/** Subtracts the given value from all components of this vector
	 *
	 * @param value The value
	 * @return This vector for chaining */
	public Vector3 Sub (double value) {
		return this.Set(this.x - value, this.y - value, this.z - value);
	}

	@Override
	public Vector3 Scl(double scalar) {
		// TODO Auto-generated method stub
		return this.Set(this.x * scalar, this.y * scalar, this.z * scalar);
	}
	
	
	@Override
	public Vector3 scl (double scalar) {
		Vector3 temp = new Vector3();
		return temp.Set(this.x * scalar, this.y * scalar, this.z * scalar);
	}

	@Override
	public Vector3 Scl (final Vector3 other) {
		return this.Set(x * other.x, y * other.y, z * other.z);
	}

	/** Scales this vector by the given values
	 * @param vx X value
	 * @param vy Y value
	 * @param vz Z value
	 * @return This vector for chaining */
	public Vector3 Scl (double vx, double vy, double vz) {
		return this.Set(this.x * vx, this.y * vy, this.z * vz);
	}

	@Override
	public Vector3 MulAdd (Vector3 vec, double scalar) {
		this.x += vec.x * scalar;
		this.y += vec.y * scalar;
		this.z += vec.z * scalar;
		return this;
	}

	@Override
	public Vector3 MulAdd (Vector3 vec, Vector3 mulVec) {
		this.x += vec.x * mulVec.x;
		this.y += vec.y * mulVec.y;
		this.z += vec.z * mulVec.z;
		return this;
	}

	/** @return The euclidian length */
	public static double len (final double x, final double y, final double z) {
		return (double)Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public double len () {
		return (double)Math.sqrt(x * x + y * y + z * z);
	}

	/** @return The squared euclidian length */
	public static double len2 (final double x, final double y, final double z) {
		return x * x + y * y + z * z;
	}

	@Override
	public double len2 () {
		return x * x + y * y + z * z;
	}

	/** @param vector The other vector
	 * @return Wether this and the other vector are equal */
	public boolean idt (final Vector3 vector) {
		return x == vector.x && y == vector.y && z == vector.z;
	}

	/** @return The euclidian distance between the two specified vectors */
	public static double dst (final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		final double a = x2 - x1;
		final double b = y2 - y1;
		final double c = z2 - z1;
		return (double)Math.sqrt(a * a + b * b + c * c);
	}

	@Override
	public double dst (final Vector3 vector) {
		final double a = vector.x - x;
		final double b = vector.y - y;
		final double c = vector.z - z;
		return (double)Math.sqrt(a * a + b * b + c * c);
	}

	/** @return the distance between this point and the given point */
	public double dst (double x, double y, double z) {
		final double a = x - this.x;
		final double b = y - this.y;
		final double c = z - this.z;
		return (double)Math.sqrt(a * a + b * b + c * c);
	}

	/** @return the squared distance between the given points */
	public static double dst2 (final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
		final double a = x2 - x1;
		final double b = y2 - y1;
		final double c = z2 - z1;
		return a * a + b * b + c * c;
	}

	@Override
	public double dst2 (Vector3 point) {
		final double a = point.x - x;
		final double b = point.y - y;
		final double c = point.z - z;
		return a * a + b * b + c * c;
	}

	/** Returns the squared distance between this point and the given point
	 * @param x The x-component of the other point
	 * @param y The y-component of the other point
	 * @param z The z-component of the other point
	 * @return The squared distance */
	public double dst2 (double x, double y, double z) {
		final double a = x - this.x;
		final double b = y - this.y;
		final double c = z - this.z;
		return a * a + b * b + c * c;
	}

	@Override
	public Vector3 Nor () {
		final double len2 = this.len2();
		if (len2 == 0f || len2 == 1f) return this;
		return this.Scl(1f / (double)Math.sqrt(len2));
	}

	/** @return The dot product between the two vectors */
	public static double dot (double x1, double y1, double z1, double x2, double y2, double z2) {
		return x1 * x2 + y1 * y2 + z1 * z2;
	}

	@Override
	public double dot (final Vector3 vector) {
		return x * vector.x + y * vector.y + z * vector.z;
	}

	/** Returns the dot product between this and the given vector.
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @param z The z-component of the other vector
	 * @return The dot product */
	public double dot (double x, double y, double z) {
		return this.x * x + this.y * y + this.z * z;
	}

	/** Sets this vector to the cross product between it and the other vector.
	 * @param vector The other vector
	 * @return This vector for chaining */
	public Vector3 Crs (final Vector3 vector) {
		return this.Set(y * vector.z - z * vector.y, z * vector.x - x * vector.z, x * vector.y - y * vector.x);
	}

	/** Sets this vector to the cross product between it and the other vector.
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @param z The z-component of the other vector
	 * @return This vector for chaining */
	public Vector3 Crs (double x, double y, double z) {
		return this.Set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
	}

	/** Left-multiplies the vector by the given 4x3 column major matrix. The matrix should be composed by a 3x3 matrix representing
	 * rotation and scale plus a 1x3 matrix representing the translation.
	 * @param matrix The matrix
	 * @return This vector for chaining */
	public Vector3 Mul4x3 (double[] matrix) {
		return Set(x * matrix[0] + y * matrix[3] + z * matrix[6] + matrix[9], x * matrix[1] + y * matrix[4] + z * matrix[7]
			+ matrix[10], x * matrix[2] + y * matrix[5] + z * matrix[8] + matrix[11]);
	}

	/** Left-multiplies the vector by the given matrix, assuming the fourth (w) component of the vector is 1.
	 * @param matrix The matrix
	 * @return This vector for chaining */
	public Vector3 Mul (final Matrix4 matrix) {
		final double l_mat[] = matrix.val;
		return this.Set(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03], x
			* l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13], x * l_mat[Matrix4.M20] + y
			* l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]);
	}

	/** Multiplies the vector by the transpose of the given matrix, assuming the fourth (w) component of the vector is 1.
	 * @param matrix The matrix
	 * @return This vector for chaining */
	public Vector3 TraMul (final Matrix4 matrix) {
		final double l_mat[] = matrix.val;
		return this.Set(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M10] + z * l_mat[Matrix4.M20] + l_mat[Matrix4.M30], x
			* l_mat[Matrix4.M01] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M21] + l_mat[Matrix4.M31], x * l_mat[Matrix4.M02] + y
			* l_mat[Matrix4.M12] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M32]);
	}

	/** Left-multiplies the vector by the given matrix.
	 * @param matrix The matrix
	 * @return This vector for chaining */
	public Vector3 Mul (Matrix3 matrix) {
		final double l_mat[] = matrix.val;
		return Set(x * l_mat[Matrix3.M00] + y * l_mat[Matrix3.M01] + z * l_mat[Matrix3.M02], x * l_mat[Matrix3.M10] + y
			* l_mat[Matrix3.M11] + z * l_mat[Matrix3.M12], x * l_mat[Matrix3.M20] + y * l_mat[Matrix3.M21] + z * l_mat[Matrix3.M22]);
	}

	/** Multiplies the vector by the transpose of the given matrix.
	 * @param matrix The matrix
	 * @return This vector for chaining */
	public Vector3 TraMul (Matrix3 matrix) {
		final double l_mat[] = matrix.val;
		return Set(x * l_mat[Matrix3.M00] + y * l_mat[Matrix3.M10] + z * l_mat[Matrix3.M20], x * l_mat[Matrix3.M01] + y
			* l_mat[Matrix3.M11] + z * l_mat[Matrix3.M21], x * l_mat[Matrix3.M02] + y * l_mat[Matrix3.M12] + z * l_mat[Matrix3.M22]);
	}

	/** Multiplies the vector by the given {@link Quaternion}.
	 * @return This vector for chaining */
	public Vector3 Mul (final Quaternion quat) {
		return quat.transform(this);
	}

	/** Multiplies this vector by the given matrix dividing by w, assuming the fourth (w) component of the vector is 1. This is
	 * mostly used to project/unproject vectors via a perspective projection matrix.
	 *
	 * @param matrix The matrix.
	 * @return This vector for chaining */
	public Vector3 Prj (final Matrix4 matrix) {
		final double l_mat[] = matrix.val;
		final double l_w = 1f / (x * l_mat[Matrix4.M30] + y * l_mat[Matrix4.M31] + z * l_mat[Matrix4.M32] + l_mat[Matrix4.M33]);
		return this.Set((x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03]) * l_w, (x
			* l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13])
			* l_w, (x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]) * l_w);
	}

	/** Multiplies this vector by the first three columns of the matrix, essentially only applying rotation and scaling.
	 *
	 * @param matrix The matrix
	 * @return This vector for chaining */
	public Vector3 Rot (final Matrix4 matrix) {
		final double l_mat[] = matrix.val;
		return this.Set(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02], x * l_mat[Matrix4.M10] + y
			* l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12], x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22]);
	}

	/** Multiplies this vector by the transpose of the first three columns of the matrix. Note: only works for translation and
	 * rotation, does not work for scaling. For those, use {@link #rot(Matrix4)} with {@link Matrix4#inv()}.
	 * @param matrix The transformation matrix
	 * @return The vector for chaining */
	public Vector3 Unrotate (final Matrix4 matrix) {
		final double l_mat[] = matrix.val;
		return this.Set(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M10] + z * l_mat[Matrix4.M20], x * l_mat[Matrix4.M01] + y
			* l_mat[Matrix4.M11] + z * l_mat[Matrix4.M21], x * l_mat[Matrix4.M02] + y * l_mat[Matrix4.M12] + z * l_mat[Matrix4.M22]);
	}

	/** Translates this vector in the direction opposite to the translation of the matrix and the multiplies this vector by the
	 * transpose of the first three columns of the matrix. Note: only works for translation and rotation, does not work for
	 * scaling. For those, use {@link #mul(Matrix4)} with {@link Matrix4#inv()}.
	 * @param matrix The transformation matrix
	 * @return The vector for chaining */
	public Vector3 Untransform (final Matrix4 matrix) {
		final double l_mat[] = matrix.val;
		x -= l_mat[Matrix4.M03];
		y -= l_mat[Matrix4.M03];
		z -= l_mat[Matrix4.M03];
		return this.Set(x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M10] + z * l_mat[Matrix4.M20], x * l_mat[Matrix4.M01] + y
			* l_mat[Matrix4.M11] + z * l_mat[Matrix4.M21], x * l_mat[Matrix4.M02] + y * l_mat[Matrix4.M12] + z * l_mat[Matrix4.M22]);
	}

	/** Rotates this vector by the given angle in degrees around the given axis.
	 *
	 * @param degrees the angle in degrees
	 * @param axisX the x-component of the axis
	 * @param axisY the y-component of the axis
	 * @param axisZ the z-component of the axis
	 * @return This vector for chaining */
	public Vector3 Rotate (double degrees, double axisX, double axisY, double axisZ) {
		return this.Mul(tmpMat.SetToRotation(axisX, axisY, axisZ, degrees));
	}

	/** Rotates this vector by the given angle in radians around the given axis.
	 *
	 * @param radians the angle in radians
	 * @param axisX the x-component of the axis
	 * @param axisY the y-component of the axis
	 * @param axisZ the z-component of the axis
	 * @return This vector for chaining */
	public Vector3 RotateRad (double radians, double axisX, double axisY, double axisZ) {
		return this.Mul(tmpMat.SetToRotationRad(axisX, axisY, axisZ, radians));
	}

	/** Rotates this vector by the given angle in degrees around the given axis.
	 *
	 * @param axis the axis
	 * @param degrees the angle in degrees
	 * @return This vector for chaining */
	public Vector3 Rotate (final Vector3 axis, double degrees) {
		tmpMat.SetToRotation(axis, degrees);
		return this.Mul(tmpMat);
	}

	/** Rotates this vector by the given angle in radians around the given axis.
	 *
	 * @param axis the axis
	 * @param radians the angle in radians
	 * @return This vector for chaining */
	public Vector3 RotateRad (final Vector3 axis, double radians) {
		tmpMat.SetToRotationRad(axis, radians);
		return this.Mul(tmpMat);
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
		return x == 0 && y == 0 && z == 0;
	}

	@Override
	public boolean isZero (final double margin) {
		return len2() < margin;
	}

	@Override
	public boolean isOnLine (Vector3 other, double epsilon) {
		return len2(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x) <= epsilon;
	}

	@Override
	public boolean isOnLine (Vector3 other) {
		return len2(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x) <= MathUtils.double_ROUNDING_ERROR;
	}

	@Override
	public boolean isCollinear (Vector3 other, double epsilon) {
		return isOnLine(other, epsilon) && hasSameDirection(other);
	}

	@Override
	public boolean isCollinear (Vector3 other) {
		return isOnLine(other) && hasSameDirection(other);
	}

	@Override
	public boolean isCollinearOpposite (Vector3 other, double epsilon) {
		return isOnLine(other, epsilon) && hasOppositeDirection(other);
	}

	@Override
	public boolean isCollinearOpposite (Vector3 other) {
		return isOnLine(other) && hasOppositeDirection(other);
	}

	@Override
	public boolean isPerpendicular (Vector3 vector) {
		return MathUtils.isZero(dot(vector));
	}

	@Override
	public boolean isPerpendicular (Vector3 vector, double epsilon) {
		return MathUtils.isZero(dot(vector), epsilon);
	}

	@Override
	public boolean hasSameDirection (Vector3 vector) {
		return dot(vector) > 0;
	}

	@Override
	public boolean hasOppositeDirection (Vector3 vector) {
		return dot(vector) < 0;
	}

	@Override
	public Vector3 Lerp (final Vector3 target, double alpha) {
		x += alpha * (target.x - x);
		y += alpha * (target.y - y);
		z += alpha * (target.z - z);
		return this;
	}
	
	//@Override
	//public Vector3 interpolate (Vector3 target, double alpha, Interpolation interpolator) {
		//return lerp(target, interpolator.apply(0f, 1f, alpha));
	//}

	/** Spherically interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is
	 * stored in this vector.
	 *
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	public Vector3 Slerp (final Vector3 target, double alpha) {
		final double dot = dot(target);
		// If the inputs are too close for comfort, simply linearly interpolate.
		if (dot > 0.9995 || dot < -0.9995) return Lerp(target, alpha);

		// theta0 = angle between input vectors
		final double theta0 = (double)Math.acos(dot);
		// theta = angle between this vector and result
		final double theta = theta0 * alpha;

		final double st = (double)Math.sin(theta);
		final double tx = target.x - x * dot;
		final double ty = target.y - y * dot;
		final double tz = target.z - z * dot;
		final double l2 = tx * tx + ty * ty + tz * tz;
		final double dl = st * ((l2 < 0.0001f) ? 1f : 1f / (double)Math.sqrt(l2));

		return Scl((double)Math.cos(theta)).Add(tx * dl, ty * dl, tz * dl).Nor();
	}

    @Override
	public String toString () {
		return "[" + x + ", " + y + ", " + z + "]";
	}


	@Override
	public Vector3 Limit (double limit) {
		return Limit2(limit * limit);
	}

	@Override
	public Vector3 Limit2 (double limit2) {
		double len2 = len2();
		if (len2 > limit2) {
			Scl((double) Math.sqrt(limit2 / len2));
		}
		return this;
	}

	@Override
	public Vector3 SetLength ( double len ) {
		return SetLength2( len * len );
	}

	@Override
	public Vector3 SetLength2 ( double len2 ) {
		double oldLen2 = len2();
		return ( oldLen2 == 0 || oldLen2 == len2 )
				? this
				: Scl((double) Math.sqrt( len2 / oldLen2 ));
	}

	@Override
	public Vector3 Clamp (double min, double max) {
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
	public int hashCode () {
		final int prime = 31;
		int result = 1;
		//result = prime * result + NumberUtils.doubleToIntBits(x);
		//result = prime * result + NumberUtils.doubleToIntBits(y);
		//result = prime * result + NumberUtils.doubleToIntBits(z);
		return result;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vector3 other = (Vector3)obj;
		//if (NumberUtils.doubleToIntBits(x) != NumberUtils.doubleToIntBits(other.x)) return false;
		//if (NumberUtils.doubleToIntBits(y) != NumberUtils.doubleToIntBits(other.y)) return false;
		//if (NumberUtils.doubleToIntBits(z) != NumberUtils.doubleToIntBits(other.z)) return false;
		return true;
	}

	@Override
	public boolean epsilonEquals (final Vector3 other, double epsilon) {
		if (other == null) return false;
		if (Math.abs(other.x - x) > epsilon) return false;
		if (Math.abs(other.y - y) > epsilon) return false;
		if (Math.abs(other.z - z) > epsilon) return false;
		return true;
	}

	/** Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
	 * @return whether the vectors are the same. */
	public boolean epsilonEquals (double x, double y, double z, double epsilon) {
		if (Math.abs(x - this.x) > epsilon) return false;
		if (Math.abs(y - this.y) > epsilon) return false;
		if (Math.abs(z - this.z) > epsilon) return false;
		return true;
	}

	@Override
	public Vector3 SetZero () {
		this.x = 0;
		this.y = 0;
		this.z = 0;
		return this;
	}

	public Vector3 Abs()
	{
		this.x = Math.abs(this.x);
		this.y = Math.abs(this.y);
		this.z = Math.abs(this.z);
		
		return this;
	}
}
