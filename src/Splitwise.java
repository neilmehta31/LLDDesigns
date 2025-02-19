import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main Class
public class Splitwise {
    public static void main(String[] args) {
        SplitwiseFacade splitwise = new SplitwiseFacade();

        SplitwiseUser splitwiseUser1 = new SplitwiseUser("1", "Alice");
        SplitwiseUser splitwiseUser2 = new SplitwiseUser("2", "Bob");

        splitwise.addUser(splitwiseUser1);
        splitwise.addUser(splitwiseUser2);

        splitwise.addObserver(new ExpenseObserver());

        splitwise.addEqualExpense(100.0, Arrays.asList(splitwiseUser1, splitwiseUser2));

        // Additional operations...
    }
}

class UserManager {
    private static UserManager instance;
    private Map<String, SplitwiseUser> users;

    private UserManager() {
        users = new HashMap<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void addUser(SplitwiseUser splitwiseUser) {
        users.put(splitwiseUser.getUserId(), splitwiseUser);
    }

    public SplitwiseUser getUser(String userId) {
        return users.get(userId);
    }
}


class SplitwiseUser {
    private String userId;
    private String name;

    public SplitwiseUser(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}

// Factory Pattern
interface ExpenseFactory {
    Expense createExpense(double totalAmount, List<SplitwiseUser> participants);
}

class EqualExpenseFactory implements ExpenseFactory {
    public Expense createExpense(double totalAmount, List<SplitwiseUser> participants) {
        return new EqualExpense(totalAmount, participants);
    }
}

class UnequalExpenseFactory implements ExpenseFactory {
    public Expense createExpense(double totalAmount, List<SplitwiseUser> participants) {
        return new UnequalExpense(totalAmount, participants);
    }
}

// Observer Pattern
interface Observer {
    void update();
}

class ExpenseObserver implements Observer {
    public void update() {
        // Update logic for expenses...
    }
}

// Strategy Pattern
interface SplitStrategy {
    Map<SplitwiseUser, Double> splitExpense(double totalAmount, List<SplitwiseUser> participants);
}

class EqualSplitStrategy implements SplitStrategy {
    public Map<SplitwiseUser, Double> splitExpense(double totalAmount, List<SplitwiseUser> participants) {
        Map<SplitwiseUser, Double> shares = new HashMap<>();
        double share = totalAmount / participants.size();
        for (SplitwiseUser participant : participants) {
            shares.put(participant, share);
        }
        return shares;
    }
}

class UnequalSplitStrategy implements SplitStrategy {
    public Map<SplitwiseUser, Double> splitExpense(double totalAmount, List<SplitwiseUser> participants) {
        // Custom splitting logic based on user preferences, weights, etc.
        // For simplicity, we'll use equal splitting for demonstration purposes.
        return new EqualSplitStrategy().splitExpense(totalAmount, participants);
    }
}


// Expense Classes
abstract class Expense {
    protected double totalAmount;
    protected List<SplitwiseUser> participants;
    protected Map<SplitwiseUser, Double> shares;
    protected SplitStrategy splitStrategy;

    public Expense(double totalAmount, List<SplitwiseUser> participants, SplitStrategy splitStrategy) {
        this.totalAmount = totalAmount;
        this.participants = participants;
        this.splitStrategy = splitStrategy;
    }

    public abstract void calculateShares();
}

// Command Pattern
interface ExpenseCommand {
    void execute();
}

class AddExpenseCommand implements ExpenseCommand {
    private Expense expense;

    public AddExpenseCommand(Expense expense) {
        this.expense = expense;
    }

    public void execute() {
        // Execute add expense logic...
        expense.calculateShares();
    }
}

// Facade Pattern
class SplitwiseFacade {
    private UserManager userManager;
    private List<Expense> expenses;
    private List<Observer> observers;

    public SplitwiseFacade() {
        userManager = UserManager.getInstance();
        expenses = new ArrayList<>();
        observers = new ArrayList<>();
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void addUser(SplitwiseUser splitwiseUser) {
        userManager.addUser(splitwiseUser);
    }

    public void addEqualExpense(double totalAmount, List<SplitwiseUser> participants) {
        ExpenseFactory factory = new EqualExpenseFactory();
        Expense expense = factory.createExpense(totalAmount, participants);
        expenses.add(expense);

        notifyObservers();
    }

    // Other facade methods...

    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}

class EqualExpense extends Expense {
    public EqualExpense(double totalAmount, List<SplitwiseUser> participants) {
        super(totalAmount, participants, new EqualSplitStrategy());
    }

    public void calculateShares() {
        shares = splitStrategy.splitExpense(totalAmount, participants);
    }
}

class UnequalExpense extends Expense {
    public UnequalExpense(double totalAmount, List<SplitwiseUser> participants) {
        super(totalAmount, participants, new UnequalSplitStrategy());
    }

    public void calculateShares() {
        shares = splitStrategy.splitExpense(totalAmount, participants);
    }
}
