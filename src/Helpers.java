import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Helpers {

    public static void require(boolean condition, Supplier<? extends RuntimeException> error) {
        Optional.of(condition)
                .filter(ok -> ok)
                .orElseThrow(error);
    }

    public static <T> T either(boolean condition, Supplier<T> whenTrue, Supplier<T> whenFalse) {
        Map<Boolean, Supplier<T>> choices = Map.of(true, whenTrue, false, whenFalse);
        return choices.get(condition).get();
    }
    public static String required(String text, String message) {
        return Optional.ofNullable(text)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(message));
    }

    public static String orDefault(String text, String fallback) {
        return Optional.ofNullable(text)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(fallback);
    }

    public static boolean isValidEmail(String email) {
        return Optional.ofNullable(email)
                .map(String::trim)
                .filter(Helpers::looksLikeEmail)
                .isPresent();
    }

    private static boolean looksLikeEmail(String text) {
        int at = text.indexOf('@');
        return at > 0 && text.indexOf('.', at) > at + 1 && !text.endsWith(".") && !text.contains(" ");
    }

    public static void printAll(Collection<? extends Book> books) {
        Optional.of(books)
                .filter(list -> !list.isEmpty())
                .ifPresentOrElse(
                        list -> list.forEach(book -> System.out.println("  " + book)),
                        () -> System.out.println("  (nothing to show)"));
    }

    public static void copyBooks(List<? extends Book> from, List<? super Book> to) {
        from.forEach(to::add);
    }

    public static <T> long countMatching(Collection<? extends T> items, Validator<? super T> rule) {
        return items.stream().filter(item -> rule.isValid(item)).count();
    }

    public static List<Integer> idsOf(Collection<? extends Identifiable> items) {
        return items.stream().map(Identifiable::getId).collect(Collectors.toList());
    }

    interface IoAction {
        void run() throws IOException;
    }

    interface IoSupplier<T> {
        T get() throws IOException;
    }

    public static void ioRun(IoAction action, String errorMessage) {
        try {
            action.run();
        } catch (IOException e) {
            throw new LibraryException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    public static <T> T ioCall(IoSupplier<T> supplier, String errorMessage) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new LibraryException(errorMessage + ": " + e.getMessage(), e);
        }
    }
}

class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
