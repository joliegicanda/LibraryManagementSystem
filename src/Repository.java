import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Repository<T extends Identifiable> {

    void add(T item);

    void addAll(Collection<? extends T> items);

    Optional<T> findById(int id);

    List<T> findAll();

    boolean remove(int id);

    int nextId();

    int count();
}

class InMemoryRepository<T extends Identifiable> implements Repository<T> {

    private final Map<Integer, T> items = new LinkedHashMap<>();

    @Override
    public void add(T item) {
        Helpers.require(!items.containsKey(item.getId()),
                () -> new LibraryException("An item with ID " + item.getId() + " already exists."));
        items.put(item.getId(), item);
    }

    @Override
    public void addAll(Collection<? extends T> newItems) {
        newItems.forEach(this::add);
    }

    @Override
    public Optional<T> findById(int id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public boolean remove(int id) {
        return items.remove(id) != null;
    }

    @Override
    public int nextId() {
        return items.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public int count() {
        return items.size();
    }
}
interface Identifiable {
    int getId();
}
