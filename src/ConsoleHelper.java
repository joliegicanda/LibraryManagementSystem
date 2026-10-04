import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

public class ConsoleHelper {

    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        Helpers.require(scanner.hasNextLine(), InputClosedException::new);
        return scanner.nextLine().trim();
    }

    public boolean report(boolean passed, String message) {
        Optional.of(passed)
                .filter(ok -> !ok)
                .ifPresent(ok -> System.out.println("  " + message));
        return passed;
    }
    public String readValid(String prompt, Validator<String> validator, String errorMessage) {
        return Stream.generate(() -> readLine(prompt))
                .filter(text -> report(validator.isValid(text), errorMessage))
                .findFirst()
                .orElseThrow();
    }
    public String readRequired(String prompt) {
        return readValid(prompt, text -> !text.isEmpty(), "This cannot be empty, please try again.");
    }

    public int readInt(String prompt, int min, int max) {
        return Stream.generate(() -> readLine(prompt))
                .map(this::parseNumber)
                .flatMap(Optional::stream)
                .filter(number -> report(number >= min && number <= max,
                        "Please enter a number between " + min + " and " + max + "."))
                .findFirst()
                .orElseThrow();
    }
    public LocalDate readPastDate(String prompt) {
        return Stream.generate(() -> readLine(prompt))
                .map(this::parseDate)
                .flatMap(Optional::stream)
                .filter(date -> report(!date.isAfter(LocalDate.now()), "The date cannot be in the future."))
                .findFirst()
                .orElseThrow();
    }

    private Optional<Integer> parseNumber(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            System.out.println("  That is not a valid number.");
            return Optional.empty();
        }
    }

    private Optional<LocalDate> parseDate(String text) {
        try {
            return Optional.of(LocalDate.parse(text));
        } catch (DateTimeException e) {
            System.out.println("  Please use the format yyyy-MM-dd, for example 2015-06-30.");
            return Optional.empty();
        }
    }
}

class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Input was closed.");
    }
}
