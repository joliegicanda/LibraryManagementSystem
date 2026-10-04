import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ReportService {

    private static final TreeMap<Integer, String> RANGES = new TreeMap<>(Map.of(
            Integer.MIN_VALUE, "Before 1990",
            1990, "1990 - 1999",
            2000, "2000 - 2009",
            2010, "2010 - 2019",
            2020, "2020 and later"));

    private final LibraryService service;

    public ReportService(LibraryService service) {
        this.service = service;
    }

    public long totalBorrowedCount() {
        return service.getAllBorrowings().size();
    }

    public long currentlyBorrowedCount() {
        return service.getActiveLoans().size();
    }

    public Map<Character, Long> booksByStartingLetter() {
        return service.getAllBooks().stream()
                .collect(Collectors.groupingBy(
                        book -> Character.toUpperCase(book.getTitle().charAt(0)),
                        TreeMap::new,
                        Collectors.counting()));
    }

    public Map<String, Long> booksByDateRange() {
        Map<String, Long> counted = service.getAllBooks().stream()
                .collect(Collectors.groupingBy(this::dateRangeOf, Collectors.counting()));

        Map<String, Long> ordered = new LinkedHashMap<>();
        RANGES.values().forEach(range -> ordered.put(range, counted.getOrDefault(range, 0L)));
        return ordered;
    }

    private String dateRangeOf(Book book) {
        return RANGES.floorEntry(book.getPublicationDate().getYear()).getValue();
    }

    public Map<Integer, Long> booksPerPublisher() {
        return service.getAllBooks().stream()
                .filter(book -> book.getPublisherId() != 0)
                .collect(Collectors.groupingBy(Book::getPublisherId, TreeMap::new, Collectors.counting()));
    }
}
