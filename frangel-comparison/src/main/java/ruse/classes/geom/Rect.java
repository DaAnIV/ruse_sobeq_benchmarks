package ruse.classes.geom;

/** Axis-aligned rectangle (sides parallel to x, y axes). */
public class Rect {
    public Vec2 min;
    public Vec2 max;

    /** Create a new axis-aligned rectangle with opposite corners (min, max). */
    public Rect(Vec2 min, Vec2 max) {
        this.min = min;
        this.max = max;
    }

    public Rect(double x0, double y0, double x1, double y1) {
        this.min = new Vec2(x0, y0);
        this.max = new Vec2(x1, y1);
    }

    /** Returns the center of this rect. */
    public Vec2 center() {
        return this.min.add(this.max).times(0.5);
    }

    /** Returns the diagonal of this rect (vector from min to max). */
    public Vec2 diagonal() {
        return this.max.sub(this.min);
    }

    /** Returns true if point is contained in this rect (including boundary). */
    public boolean test(Vec2 point) {
        return this.min.x() <= point.x() && this.max.x() >= point.x() && 
               this.min.y() <= point.y() && this.max.y() >= point.y();
    }

    /** Returns an expanded copy of this rect with size multiplied by 'factor' (same center). */
    public Rect expand(double factor) {
        Vec2 center = this.center();
        Vec2 span = this.diagonal().times(factor / 2);
        return new Rect(center.sub(span), center.add(span));
    }

    /** Returns an expanded copy of this rect with added padding given by 'amount'. */
    public Rect grow(Vec2 amount) {
        return new Rect(this.min.sub(amount), this.max.add(amount));
    }

    /** Returns a translated copy of this rect with the same size. */
    public Rect translate(Vec2 displacement) {
        return new Rect(
            this.min.add(displacement),
            this.max.add(displacement)
        );
    }

    /**
     * Returns a Rect with the given center, whose distance to its (min, max) corners is span.
     *
     * Example: fromCenterSpan(center: {x: 1, y: 2}, span: {x: 3, y: 4}) returns {min: {x: -2, y: -2}, max: {x: 4, y: 6}}.
     */
    public static Rect fromCenterSpan(Vec2 center, Vec2 span) {
        return new Rect(center.sub(span), center.add(span));
    }

    /** Returns a square Rect with the given center and inner radius. */
    public static Rect fromCenterRadius(Vec2 center, double radius) {
        Vec2 span = new Vec2(radius, radius);
        return fromCenterSpan(center, span);
    }

    /** Given a collection of rects, return the smallest rect that contains them all. */
    public static Rect commonBounds(Rect... rects) {
        double x0 = Double.POSITIVE_INFINITY;
        double y0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY;
        double y1 = Double.NEGATIVE_INFINITY;
        
        for (Rect rect : rects) {
            x0 = Math.min(x0, rect.min.x());
            y0 = Math.min(y0, rect.min.y());
            x1 = Math.max(x1, rect.max.x());
            y1 = Math.max(y1, rect.max.y());
        }
        return new Rect(x0, y0, x1, y1);
    }

    /** Given a collection of points, return the smallest rect that contains them all. */
    public static Rect boundingBox(Vec2... points) {
        double x0 = Double.POSITIVE_INFINITY;
        double y0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY;
        double y1 = Double.NEGATIVE_INFINITY;
        
        for (Vec2 p : points) {
            x0 = Math.min(x0, p.x());
            y0 = Math.min(y0, p.y());
            x1 = Math.max(x1, p.x());
            y1 = Math.max(y1, p.y());
        }
        return new Rect(x0, y0, x1, y1);
    }
}