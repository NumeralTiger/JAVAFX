package com.nbody.simulator;

/**
 * A utility class for 2D vector operations.
 */
public class Vector2D {
    public double x;
    public double y;

    public Vector2D(double x, double doubleY) {
        this.x = x;
        this.y = doubleY;
    }

    //region Basic Vector Operations

    /**
     * Adds another vector to this vector, returning a new vector.
     * @param other The vector to add.
     * @return A new Vector2D representing the sum.
     */
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    /**
     * Subtracts another vector from this vector, returning a new vector.
     * @param other The vector to subtract.
     * @return A new Vector2D representing the difference.
     */
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    /**
     * Scales this vector by a scalar value, returning a new vector.
     * @param scalar The scalar value.
     * @return A new Vector2D representing the scaled vector.
     */
    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /**
     * Calculates the dot product of this vector with another vector.
     * @param other The other vector.
     * @return The dot product.
     */
    public double dotProduct(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Calculates the magnitude (length) of this vector.
     * @return The magnitude of the vector.
     */
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Calculates the squared magnitude of this vector (useful for distance comparisons
     * as it avoids a sqrt operation).
     * @return The squared magnitude.
     */
    public double magnitudeSq() {
        return x * x + y * y;
    }

    /**
     * Normalizes this vector (makes its magnitude 1), returning a new vector.
     * If the magnitude is zero, returns a zero vector.
     * @return A new Vector2D representing the normalized vector.
     */
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            return new Vector2D(0, 0); // Avoid division by zero
        }
        return new Vector2D(x / mag, y / mag);
    }

    /**
     * Calculates the distance between this vector and another vector.
     * @param other The other vector.
     * @return The distance between the two vectors.
     */
    public double distance(Vector2D other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Calculates the squared distance between this vector and another vector (useful for comparisons).
     * @param other The other vector.
     * @return The squared distance.
     */
    public double distanceSq(Vector2D other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    //endregion

    @Override
    public String toString() {
        return String.format("Vector2D(%.2f, %.2f)", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vector2D = (Vector2D) o;
        return Double.compare(vector2D.x, x) == 0 && Double.compare(vector2D.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}