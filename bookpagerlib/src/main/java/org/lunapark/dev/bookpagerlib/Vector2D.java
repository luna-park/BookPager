package org.lunapark.dev.bookpagerlib;


/**
 * Inner class used to represent a 2D point.
 */
class Vector2D {
    float x, y;

    Vector2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float lengthSquared() {
        return (x * x) + (y * y);
    }

    public boolean equals(Object o) {
        if (o instanceof Vector2D) {
            Vector2D p = (Vector2D) o;
            return p.x == x && p.y == y;
        }
        return false;
    }

    public Vector2D reverse() {
        return new Vector2D(-x, -y);
    }

    public Vector2D sum(Vector2D b) {
        return new Vector2D(x + b.x, y + b.y);
    }

    public Vector2D sub(Vector2D b) {
        return new Vector2D(x - b.x, y - b.y);
    }

    public float dot(Vector2D vec) {
        return (x * vec.x) + (y * vec.y);
    }

    public float cross(Vector2D a, Vector2D b) {
        return a.cross(b);
    }

    public float cross(Vector2D vec) {
        return x * vec.y - y * vec.x;
    }

    public float distanceSquared(Vector2D other) {
        float dx = other.x - x;
        float dy = other.y - y;

        return (dx * dx) + (dy * dy);
    }

    public float distance(Vector2D other) {
        return (float) Math.sqrt(distanceSquared(other));
    }

    public float dotProduct(Vector2D other) {
        return other.x * x + other.y * y;
    }

    public Vector2D normalize() {
        float magnitude = (float) Math.sqrt(dotProduct(this));
        return new Vector2D(x / magnitude, y / magnitude);
    }

    public Vector2D mult(float scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }
}
