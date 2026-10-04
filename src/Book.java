import java.time.LocalDate;

public class Book implements Identifiable {

    private int id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private LocalDate publicationDate;
    private int publisherId;          // 0 means the book has no publisher member
    private boolean available = true;

    public Book(int id, String title, String author, String isbn,
                String category, LocalDate publicationDate, int publisherId) {
        this.id = id;
        setTitle(title);
        setAuthor(author);
        setIsbn(isbn);
        setCategory(category);
        setPublicationDate(publicationDate);
        this.publisherId = publisherId;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Helpers.required(title, "Book title cannot be empty.");
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = Helpers.required(author, "Author cannot be empty.");
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = Helpers.orDefault(isbn, "N/A");
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = Helpers.orDefault(category, "General");
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        Helpers.require(publicationDate != null,
                () -> new IllegalArgumentException("Publication date is required."));
        Helpers.require(!publicationDate.isAfter(LocalDate.now()),
                () -> new IllegalArgumentException("Publication date cannot be in the future."));
        this.publicationDate = publicationDate;
    }

    public int getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(int publisherId) {
        this.publisherId = publisherId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        String status = Helpers.either(available, () -> "Available", () -> "Borrowed");
        String publisher = Helpers.either(publisherId == 0, () -> "-", () -> String.valueOf(publisherId));
        return "[" + id + "] " + title + " by " + author
                + " | " + category
                + " | " + publicationDate
                + " | Publisher ID: " + publisher
                + " | " + status;
    }
}