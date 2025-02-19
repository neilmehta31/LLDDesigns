import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Enum for task types
enum TaskType {
    FEATURE, BUG, STORY
}

// Enum for task statuses
enum TaskStatus {
    OPEN, IN_PROGRESS, TESTING, FIXED, DEPLOYED, COMPLETED
}

// Base Task class
abstract class Task {
    private String title;
    private String assignee;
    private TaskType type;
    private LocalDate dueDate;
    private TaskStatus status;
    private Sprint sprint;

    public Task(String title, String assignee, TaskType type, LocalDate dueDate) {
        this.title = title;
        this.assignee = assignee;
        this.type = type;
        this.dueDate = dueDate;
        this.status = TaskStatus.OPEN;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public String getAssignee() { return assignee; }
    public TaskType getType() { return type; }
    public LocalDate getDueDate() { return dueDate; }
    public TaskStatus getStatus() { return status; }
    public Sprint getSprint() { return sprint; }

    public void setAssignee(String assignee) { this.assignee = assignee; }
    public void setSprint(Sprint sprint) { this.sprint = sprint; }

    // Abstract method for status transition validation
    public abstract boolean canTransitionStatus(TaskStatus newStatus);

    public void updateStatus(TaskStatus newStatus) {
        if (canTransitionStatus(newStatus)) {
            this.status = newStatus;
        } else {
            throw new IllegalStateException("Invalid status transition");
        }
    }
}

// Feature Task
class FeatureTask extends Task {
    private String featureSummary;
    private Impact impact;

    public enum Impact { LOW, MODERATE, HIGH }

    public FeatureTask(String title, String assignee, LocalDate dueDate,
                       String featureSummary, Impact impact) {
        super(title, assignee, TaskType.FEATURE, dueDate);
        this.featureSummary = featureSummary;
        this.impact = impact;
    }

    @Override
    public boolean canTransitionStatus(TaskStatus newStatus) {
        TaskStatus currentStatus = getStatus();
        return (currentStatus == TaskStatus.OPEN && newStatus == TaskStatus.IN_PROGRESS) ||
                (currentStatus == TaskStatus.IN_PROGRESS &&
                        (newStatus == TaskStatus.TESTING || newStatus == TaskStatus.DEPLOYED)) ||
                (currentStatus == TaskStatus.TESTING && newStatus == TaskStatus.DEPLOYED);
    }
}

// Bug Task
class BugTask extends Task {
    private Severity severity;

    public enum Severity { P0, P1, P2 }

    public BugTask(String title, String assignee, LocalDate dueDate, Severity severity) {
        super(title, assignee, TaskType.BUG, dueDate);
        this.severity = severity;
    }

    @Override
    public boolean canTransitionStatus(TaskStatus newStatus) {
        TaskStatus currentStatus = getStatus();
        return (currentStatus == TaskStatus.OPEN && newStatus == TaskStatus.IN_PROGRESS) ||
                (currentStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.FIXED);
    }
}

// Story Task with Sub Tracks
class StoryTask extends Task {
    private String storySummary;
    private List<SubTrack> subTracks = new ArrayList<>();

    public StoryTask(String title, String assignee, LocalDate dueDate, String storySummary) {
        super(title, assignee, TaskType.STORY, dueDate);
        this.storySummary = storySummary;
    }

    public void addSubTrack(SubTrack subTrack) {
        if (getStatus() != TaskStatus.COMPLETED) {
            subTracks.add(subTrack);
        } else {
            throw new IllegalStateException("Cannot add sub track to completed story");
        }
    }

    public List<SubTrack> getSubTracks() {
        return new ArrayList<>(subTracks);
    }

    @Override
    public boolean canTransitionStatus(TaskStatus newStatus) {
        TaskStatus currentStatus = getStatus();
        if (newStatus == TaskStatus.COMPLETED) {
            return subTracks.stream().allMatch(st -> st.getStatus() == TaskStatus.COMPLETED);
        }
        return (currentStatus == TaskStatus.OPEN && newStatus == TaskStatus.IN_PROGRESS) ||
                (currentStatus == TaskStatus.IN_PROGRESS && newStatus != TaskStatus.COMPLETED);
    }
}

// Sub Track for Story
class SubTrack {
    private String title;
    private TaskStatus status;
    private StoryTask parentTask;

    public SubTrack(String title, StoryTask parentTask) {
        this.title = title;
        this.status = TaskStatus.OPEN;
        this.parentTask = parentTask;
        parentTask.addSubTrack(this);
    }

    public boolean canTransitionStatus(TaskStatus newStatus) {
        return (status == TaskStatus.OPEN && newStatus == TaskStatus.IN_PROGRESS) ||
                (status == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.COMPLETED);
    }

    public void updateStatus(TaskStatus newStatus) {
        if (canTransitionStatus(newStatus)) {
            this.status = newStatus;
        } else {
            throw new IllegalStateException("Invalid status transition");
        }
    }

    public TaskStatus getStatus() { return status; }

    public String getTitle() {
        return title;
    }

    public StoryTask getParentTask() {
        return parentTask;
    }
}

// Sprint class
class Sprint {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private TaskStatus status;
    private List<Task> tasks = new ArrayList<>();

    public Sprint(String name, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = TaskStatus.OPEN;
    }

    public void addTask(Task task) {
        if (status != TaskStatus.COMPLETED) {
            if (task.getSprint() == null) {
                tasks.add(task);
                task.setSprint(this);
            } else {
                throw new IllegalStateException("Task already assigned to a sprint");
            }
        } else {
            throw new IllegalStateException("Cannot add tasks to a completed sprint");
        }
    }

    public void removeTask(Task task) {
        if (status != TaskStatus.COMPLETED) {
            tasks.remove(task);
            task.setSprint(null);
        } else {
            throw new IllegalStateException("Cannot remove tasks from a completed sprint");
        }
    }

    public void start() {
        if (status == TaskStatus.OPEN) {
            status = TaskStatus.IN_PROGRESS;
        }
    }

    public void complete() {
        if (status == TaskStatus.IN_PROGRESS) {
            status = TaskStatus.COMPLETED;
        }
    }

    public List<Task> getSprintSnapshot(LocalDate currentDate) {
        return tasks.stream().filter(task -> task.getDueDate().isBefore(currentDate)).toList();
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}

class TaskManagementSystem{
    private List<Task> tasks = new ArrayList<>();
    private List<Sprint> sprints = new ArrayList<>();

    public void createTask(Task task){
        tasks.add(task);
    }

    public void createSprint(Sprint sprint){
        sprints.add(sprint);
    }

    public List<Task> getTasksByAssignee(String asignee){
        return tasks.stream().filter(task -> task.getAssignee().equals(asignee)).sorted(
                Comparator.comparing((Task t) -> getPriority(t.getType())).thenComparing(t ->t.getSprint() != null ?
                        t.getSprint().getStartDate() : LocalDate.MAX).thenComparing(Task::getDueDate)).collect(Collectors.toList());
    }

    private int getPriority(TaskType taskType){
        return switch (taskType){
            case STORY -> 1;
            case FEATURE -> 2;
            case BUG -> 3;
            default -> 4;
        };
    }
}

public class JiraMain{
    public static void main(String[] args){
        TaskManagementSystem planner = new TaskManagementSystem();


        // Create tasks
        FeatureTask dashboardTask = new FeatureTask(
                "Create dashboard", "Peter", LocalDate.of(2021, 7, 24),
                "Create console for debugging", FeatureTask.Impact.LOW
        );

        BugTask mysqlTask = new BugTask(
                "Fix mysql issue", "Ryan", LocalDate.of(2021, 7, 26),
                BugTask.Severity.P0
        );

        StoryTask microserviceTask = new StoryTask(
                "Create a microservice", "Ryan", LocalDate.of(2021, 7, 22),
                "Create new service"
        );

        // Add sub tracks to the story
        SubTrack developmentTrack = new SubTrack("Development", microserviceTask);
        SubTrack testingTrack = new SubTrack("Testing", microserviceTask);
        SubTrack deploymentTrack = new SubTrack("Deployment", microserviceTask);

        // Add tasks to the planner
        planner.createTask(dashboardTask);
        planner.createTask(mysqlTask);
        planner.createTask(microserviceTask);

        // Create and manage sprint
        Sprint sprint = new Sprint("Sprint-1", LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 15));
        sprint.addTask(mysqlTask);
        sprint.addTask(microserviceTask);
        planner.createSprint(sprint);
        List<Task> ryanTasks = planner.getTasksByAssignee("Ryan");

        ryanTasks.forEach(task -> {
            System.out.println("Task Type: " + task.getType());
            System.out.println("Title: " + task.getTitle());

            // Special handling for Story tasks to show sub-tracks
            if (task instanceof StoryTask storyTask) {
                System.out.println("Sub Tracks:");
                storyTask.getSubTracks().forEach(subTrack ->
                        System.out.println("   - " + subTrack.getTitle())
                );
            }

            System.out.println("Sprint: " + (task.getSprint() != null ? task.getSprint().getName() : "None"));
            System.out.println("---");
        });
    }
}