
import java.io.Serializable;
import java.time.LocalDate;

public class Task implements Serializable, Comparable<Task> {
    private String title, description, priority;
    private LocalDate dueDate;
    private boolean completed;

    public Task(String title, String description, LocalDate dueDate, String priority) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }

    public void toggleStatus() { this.completed = !this.completed; }

    @Override
    public int compareTo(Task other) {
        return this.dueDate.compareTo(other.dueDate);
    }
}
