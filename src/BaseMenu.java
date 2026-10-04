import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseMenu {

    protected static final Predicate<Member> EVERYONE = member -> true;
    protected static final Predicate<Member> PUBLISHER_OR_LIBRARIAN =
            member -> member instanceof PublisherMember || member instanceof LibrarianMember;
    protected static final Predicate<Member> LIBRARIAN_ONLY = member -> member instanceof LibrarianMember;

    protected final LibraryService service;
    protected final ConsoleHelper input;
    protected final Member user;     // the person who is logged in

    public BaseMenu(LibraryService service, ConsoleHelper input, Member user) {
        this.service = service;
        this.input = input;
        this.user = user;
    }

    protected boolean isLibrarian() {
        return user instanceof LibrarianMember;
    }

    protected boolean isPublisher() {
        return user instanceof PublisherMember;
    }

    protected MenuOption option(int number, String label, Predicate<Member> allowedFor, Runnable action) {
        return new MenuOption(number, label, allowedFor, action);
    }

    protected MenuOption back() {
        return option(0, "Back", EVERYONE, () -> { });
    }


    protected void runMenu(String title, List<MenuOption> options) {
        List<MenuOption> allowed = options.stream()
                .filter(option -> option.allowedFor().test(user))
                .collect(Collectors.toList());

        MenuOption chosen;
        do {
            chosen = chooseOption(title, allowed);
            runSafely(chosen.action());
        } while (chosen.number() != 0);
    }

    private MenuOption chooseOption(String title, List<MenuOption> allowed) {
        System.out.println("\n" + title);
        allowed.forEach(option -> System.out.println(option.number() + ". " + option.label()));

        return Stream.generate(() -> input.readInt("Choose: ", 0, 99))
                .map(number -> allowed.stream().filter(option -> option.number() == number).findFirst())
                .filter(found -> input.report(found.isPresent(), "That option is not available."))
                .map(Optional::get)
                .findFirst()
                .orElseThrow();
    }


    protected <T> void printList(List<T> items, String emptyText, Function<T, String> toText) {
        Optional.of(items)
                .filter(list -> !list.isEmpty())
                .ifPresentOrElse(
                        list -> list.forEach(item -> System.out.println("  " + toText.apply(item))),
                        () -> System.out.println("  " + emptyText));
    }


    protected <K, V> void printMap(Map<K, V> map, String emptyText, BiConsumer<K, V> printLine) {
        Optional.of(map)
                .filter(entries -> !entries.isEmpty())
                .ifPresentOrElse(
                        entries -> entries.forEach(printLine),
                        () -> System.out.println("  " + emptyText));
    }


    protected void runSafely(Runnable action) {
        try {
            action.run();
        } catch (InputClosedException e) {
            throw e;                          // let Main close the program
        } catch (LibraryException | IllegalArgumentException e) {
            System.out.println("\n  Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("\n  Something unexpected went wrong: " + e.getMessage());
        }
    }
}


record MenuOption(int number, String label, Predicate<Member> allowedFor, Runnable action) {
}
