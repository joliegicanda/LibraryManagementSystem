@FunctionalInterface
interface Validator<T> {
    boolean isValid(T value);
}
@FunctionalInterface
interface Callback<T> {
    void onDone(T item);
}
