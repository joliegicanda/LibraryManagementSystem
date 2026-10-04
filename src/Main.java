import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

  public static void main(String[] args) {
    System.out.println("==============================================");
    System.out.println("        LIBRARY MANAGEMENT SYSTEM");
    System.out.println("==============================================");

    ConsoleHelper input = new ConsoleHelper();

    try {
      FileStorage storage = new FileStorage();
      storage.init();
      LibraryService service = new LibraryService(storage);
      service.loadAll();
      ReportService reports = new ReportService(service);

      Member user = chooseProfile(service, input);

      new MainMenu(service, reports, input, user).run();

    } catch (InputClosedException e) {
      System.out.println("\n\nInput closed. Goodbye!");
    } catch (LibraryException e) {
      System.out.println("\nThe program could not start: " + e.getMessage());
    } catch (RuntimeException e) {
      System.out.println("\nUnexpected error: " + e.getMessage());
    }
  }

  private static Member chooseProfile(LibraryService service, ConsoleHelper input) {
    return Stream.generate(() -> tryChooseProfile(service, input))
            .flatMap(Optional::stream)
            .findFirst()
            .orElseThrow();
  }

  private static Optional<Member> tryChooseProfile(LibraryService service, ConsoleHelper input) {
    try {
      return Optional.of(Helpers.either(service.hasMembers(),
              () -> newOrExisting(service, input),
              () -> firstProfile(service, input)));
    } catch (InputClosedException e) {
      throw e;
    } catch (RuntimeException e) {

      System.out.println("\n  Error: " + e.getMessage());
      return Optional.empty();
    }
  }

  private static Member firstProfile(LibraryService service, ConsoleHelper input) {
    System.out.println("\nNo profiles yet. Let's create yours.");
    return createProfile(service, input);
  }

  private static Member newOrExisting(LibraryService service, ConsoleHelper input) {
    System.out.println("\n1. Create a new profile");
    System.out.println("2. Continue with an existing profile");
    int choice = input.readInt("Choose: ", 1, 2);
    return Helpers.either(choice == 2,
            () -> existingProfile(service, input),
            () -> createProfile(service, input));
  }

  private static Member existingProfile(LibraryService service, ConsoleHelper input) {
    int id = input.readInt("Enter your member ID: ", 1, Integer.MAX_VALUE);
    Member existing = service.getMember(id);
    System.out.println("\nWelcome back, " + existing.getName() + "!");
    return existing;
  }

  private static Member createProfile(LibraryService service, ConsoleHelper input) {
    Member member = readAndRegister(service, input);
    System.out.println("\nProfile created! Your member ID is " + member.getId()
            + " - remember it to log in next time.");
    return member;
  }

  private static final List<String> ROLES = List.of("Student", "Publisher", "Librarian");

  private static final Map<String, String> EXTRA_PROMPTS = Map.of(
          "Student", "School (optional): ",
          "Publisher", "Company name (optional): ",
          "Librarian", "Staff number (optional): ");

  public static Member readAndRegister(LibraryService service, ConsoleHelper input) {
    System.out.println("\nSelect a role:");
    IntStream.range(0, ROLES.size())
            .forEach(i -> System.out.println((i + 1) + ". " + ROLES.get(i)));
    String role = ROLES.get(input.readInt("Role: ", 1, ROLES.size()) - 1);

    String name = input.readRequired("Full name: ");
    String email = input.readValid("Email: ", Helpers::isValidEmail,
            "That does not look like a valid email (example: name@mail.com).");
    String phone = input.readLine("Phone (optional): ");
    String extra = input.readLine(EXTRA_PROMPTS.get(role));

    return service.registerMember(role, name, email, phone, extra);
  }
}
