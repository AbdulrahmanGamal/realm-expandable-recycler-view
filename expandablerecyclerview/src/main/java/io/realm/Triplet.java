package io.realm;

public class Triplet<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triplet(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Triplet)) {
            return false;
        }
        Triplet<?, ?, ?> t = (Triplet<?, ?, ?>) obj;
        return objectsEqual(t.first, first) &&
                objectsEqual(t.second, second) &&
                objectsEqual(t.third, third);
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^
                (second == null ? 0 : second.hashCode()) ^
                (third == null ? 0 : third.hashCode());
    }

    @Override
    public String toString() {
        return "Triplet{" + String.valueOf(first) + " " +
                String.valueOf(second) + " " +
                String.valueOf(third) + "}";
    }

    public static <A, B, C> Triplet <A, B, C> create(A a, B b, C c) {
        return new Triplet<>(a, b, c);
    }
}
