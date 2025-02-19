import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1. SINGLETON PATTERN
class DatabaseConnection {
    private static DatabaseConnection instance;
    private String connectionStatus;

    private DatabaseConnection() {
        connectionStatus = "Connected to database";
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized(DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public void query(String sql) {
        System.out.println(connectionStatus + ": Executing query - " + sql);
    }
}

// Singleton Driver
class SingletonDemo {
    public static void main(String[] args) {
        DatabaseConnection db1 = DatabaseConnection.getInstance();
        DatabaseConnection db2 = DatabaseConnection.getInstance();

        System.out.println("Are both instances same? " + (db1 == db2));
        db1.query("SELECT * FROM users");
        db2.query("SELECT * FROM products");
    }
}

// 2. FACTORY PATTERN
interface Vehicle {
    void drive();
}

class Car implements Vehicle {
    @Override
    public void drive() {
        System.out.println("Driving a car at 60 mph");
    }
}

class Bike implements Vehicle {
    @Override
    public void drive() {
        System.out.println("Riding a bike at 20 mph");
    }
}

class VehicleFactory {
    public Vehicle createVehicle(String type) {
        if (type.equalsIgnoreCase("car")) {
            return new Car();
        } else if (type.equalsIgnoreCase("bike")) {
            return new Bike();
        }
        throw new IllegalArgumentException("Unknown vehicle type");
    }
}

// Factory Driver
class FactoryDemo {
    public static void main(String[] args) {
        VehicleFactory factory = new VehicleFactory();

        // Create and use a car
        Vehicle car = factory.createVehicle("car");
        car.drive();

        // Create and use a bike
        Vehicle bike = factory.createVehicle("bike");
        bike.drive();
    }
}

// 3. OBSERVER PATTERN
interface WeatherObserver {
    void update(double temperature);
}

class WeatherStation {
    private List<WeatherObserver> observers = new ArrayList<>();
    private double temperature;

    public void registerObserver(WeatherObserver observer) {
        observers.add(observer);
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        notifyObservers();
    }

    private void notifyObservers() {
        for (WeatherObserver observer : observers) {
            observer.update(temperature);
        }
    }
}

class TemperatureDisplay implements WeatherObserver {
    private String location;

    public TemperatureDisplay(String location) {
        this.location = location;
    }

    @Override
    public void update(double temperature) {
        System.out.println(location + " Temperature Display: " + temperature + "°C");
    }
}

// Observer Driver
class ObserverDemo {
    public static void main(String[] args) {
        WeatherStation weatherStation = new WeatherStation();

        TemperatureDisplay display1 = new TemperatureDisplay("Office");
        TemperatureDisplay display2 = new TemperatureDisplay("Home");

        weatherStation.registerObserver(display1);
        weatherStation.registerObserver(display2);

        System.out.println("Weather station updating temperature...");
        weatherStation.setTemperature(24.5);
        weatherStation.setTemperature(25.0);
    }
}

// 4. DECORATOR PATTERN
interface Pizza {
    String getDescription();
    double getCost();
}

class BasicPizza implements Pizza {
    @Override
    public String getDescription() {
        return "Basic Pizza";
    }

    @Override
    public double getCost() {
        return 4.00;
    }
}

abstract class ToppingDecorator implements Pizza {
    protected Pizza pizza;

    public ToppingDecorator(Pizza pizza) {
        this.pizza = pizza;
    }
}

class Cheese extends ToppingDecorator {
    public Cheese(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Cheese";
    }

    @Override
    public double getCost() {
        return pizza.getCost() + 1.50;
    }
}

class Pepperoni extends ToppingDecorator {
    public Pepperoni(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Pepperoni";
    }

    @Override
    public double getCost() {
        return pizza.getCost() + 2.00;
    }
}

// Decorator Driver
class DecoratorDemo {
    public static void main(String[] args) {
        // Create a basic pizza
        Pizza pizza = new BasicPizza();
        System.out.println("Basic Pizza - Cost: $" + pizza.getCost() +
                ", Description: " + pizza.getDescription());

        // Add cheese
        pizza = new Cheese(pizza);
        System.out.println("After adding cheese - Cost: $" + pizza.getCost() +
                ", Description: " + pizza.getDescription());

        // Add pepperoni
        pizza = new Pepperoni(pizza);
        System.out.println("After adding pepperoni - Cost: $" + pizza.getCost() +
                ", Description: " + pizza.getDescription());
    }
}

// 5. BUILDER PATTERN
class Sandwich {
    private String bread;
    private String meat;
    private String cheese;
    private List<String> vegetables;

    private Sandwich(Builder builder) {
        this.bread = builder.bread;
        this.meat = builder.meat;
        this.cheese = builder.cheese;
        this.vegetables = builder.vegetables;
    }

    @Override
    public String toString() {
        return "Sandwich with " + bread + " bread, " + meat + ", " +
                cheese + ", and vegetables: " + vegetables;
    }

    public static class Builder {
        private String bread;
        private String meat;
        private String cheese;
        private List<String> vegetables = new ArrayList<>();

        public Builder bread(String bread) {
            this.bread = bread;
            return this;
        }

        public Builder meat(String meat) {
            this.meat = meat;
            return this;
        }

        public Builder cheese(String cheese) {
            this.cheese = cheese;
            return this;
        }

        public Builder addVegetable(String vegetable) {
            this.vegetables.add(vegetable);
            return this;
        }

        public Sandwich build() {
            return new Sandwich(this);
        }
    }
}

// Builder Driver
class BuilderDemo {
    public static void main(String[] args) {
        Sandwich sandwich = new Sandwich.Builder()
                .bread("Wheat")
                .meat("Turkey")
                .cheese("Swiss")
                .addVegetable("Lettuce")
                .addVegetable("Tomato")
                .build();

        System.out.println(sandwich);
    }
}

// FACADE PATTERN
// Complex subsystem classes
class CPU {
    public void freeze() { System.out.println("CPU: Freezing..."); }
    public void jump(long position) { System.out.println("CPU: Jumping to position " + position); }
    public void execute() { System.out.println("CPU: Executing..."); }
}

class Memory {
    public void load(long position, String data) {
        System.out.println("Memory: Loading data '" + data + "' at position " + position);
    }
}

class HardDrive {
    public String read(long lba, int size) {
        System.out.println("HardDrive: Reading " + size + " bytes from position " + lba);
        return "Data from hard drive";
    }
}

// Facade
class ComputerFacade {
    private CPU cpu;
    private Memory memory;
    private HardDrive hardDrive;

    public ComputerFacade() {
        this.cpu = new CPU();
        this.memory = new Memory();
        this.hardDrive = new HardDrive();
    }

    public void startComputer() {
        System.out.println("\nStarting computer using Facade...");
        cpu.freeze();
        memory.load(0, hardDrive.read(0, 1024));
        cpu.jump(0);
        cpu.execute();
    }
}

// Facade Pattern Driver
class FacadeDemo {
    public static void main(String[] args) {
        ComputerFacade computer = new ComputerFacade();
        computer.startComputer();
    }
}

// ADAPTER PATTERN
// Old system - Payment processor that only handles USD
class LegacyPaymentProcessor {
    public void processPayment(double amount) {
        System.out.println("Processing USD payment of $" + amount);
    }
}

// New system interface - Needs to handle multiple currencies
interface MultiCurrencyPaymentProcessor {
    void processPayment(double amount, String currency);
}

// Adapter to make old system work with new interface
class PaymentProcessorAdapter implements MultiCurrencyPaymentProcessor {
    private LegacyPaymentProcessor legacyProcessor;
    private Map<String, Double> exchangeRates;

    public PaymentProcessorAdapter(LegacyPaymentProcessor legacyProcessor) {
        this.legacyProcessor = legacyProcessor;
        // Initialize with some example exchange rates
        this.exchangeRates = new HashMap<>();
        exchangeRates.put("EUR", 1.1);  // 1 EUR = 1.1 USD
        exchangeRates.put("GBP", 1.3);  // 1 GBP = 1.3 USD
        exchangeRates.put("USD", 1.0);  // 1 USD = 1.0 USD
    }

    @Override
    public void processPayment(double amount, String currency) {
        if (!exchangeRates.containsKey(currency)) {
            throw new IllegalArgumentException("Unsupported currency: " + currency);
        }

        // Convert to USD
        double usdAmount = amount * exchangeRates.get(currency);
        System.out.println("Converting " + amount + " " + currency +
                " to USD (Rate: " + exchangeRates.get(currency) + ")");

        // Process using legacy system
        legacyProcessor.processPayment(usdAmount);
    }
}

// Extended Adapter Pattern Demo with real-world usage
class OnlineStore {
    private MultiCurrencyPaymentProcessor paymentProcessor;

    public OnlineStore(MultiCurrencyPaymentProcessor paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    public void checkout(String item, double price, String currency) {
        System.out.println("\nProcessing checkout for: " + item);
        System.out.println("Price: " + price + " " + currency);
        paymentProcessor.processPayment(price, currency);
    }
}

// Adapter Pattern Driver
class AdapterDemo {
    public static void main(String[] args) {
        // Create the legacy system
        LegacyPaymentProcessor legacyProcessor = new LegacyPaymentProcessor();

        // Create the adapter
        MultiCurrencyPaymentProcessor modernProcessor =
                new PaymentProcessorAdapter(legacyProcessor);

        // Create online store with adapted payment processor
        OnlineStore store = new OnlineStore(modernProcessor);

        // Process payments in different currencies
        store.checkout("Laptop", 1000.00, "USD");
        store.checkout("Headphones", 100.00, "EUR");
        store.checkout("Keyboard", 50.00, "GBP");
    }
}

// 1. Strategy Interface
interface SortStrategy {
    void sort(int[] array);
}

// 2. Concrete Strategies
class BubbleSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("Sorting using Bubble Sort");
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    // Swap elements
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
    }
}

class SelectionSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("Sorting using Selection Sort");
        for (int i = 0; i < array.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < array.length; j++) {
                if (array[j] < array[minIndex]) {
                    minIndex = j;
                }
            }
            int temp = array[minIndex];
            array[minIndex] = array[i];
            array[i] = temp;
        }
    }
}

// 3. Context
class Sorter {
    private SortStrategy strategy;

    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void sort(int[] array) {
        if (strategy == null) {
            throw new IllegalStateException("Sort strategy not set");
        }
        strategy.sort(array);
    }
}

// 4. Driver Class
class SimpleStrategyDemo {
    public static void main(String[] args) {
        // Create array to sort
        int[] numbers = {64, 34, 25, 12, 22, 11, 90};

        // Create context
        Sorter sorter = new Sorter();

        // Print original array
        System.out.println("Original array: " + Arrays.toString(numbers));

        // Use Bubble Sort
        sorter.setStrategy(new BubbleSort());
        int[] bubbleArray = numbers.clone();
        sorter.sort(bubbleArray);
        System.out.println("After Bubble Sort: " + Arrays.toString(bubbleArray));

        // Use Selection Sort
        sorter.setStrategy(new SelectionSort());
        int[] selectionArray = numbers.clone();
        sorter.sort(selectionArray);
        System.out.println("After Selection Sort: " + Arrays.toString(selectionArray));
    }
}

// Main class to run all demos
public class DesignPatternsWithDrivers {
    public static void main(String[] args) {
        System.out.println("\n=== Singleton Pattern Demo ===");
        SingletonDemo.main(args);

        System.out.println("\n=== Factory Pattern Demo ===");
        FactoryDemo.main(args);

        System.out.println("\n=== Observer Pattern Demo ===");
        ObserverDemo.main(args);

        System.out.println("\n=== Decorator Pattern Demo ===");
        DecoratorDemo.main(args);

        System.out.println("\n=== Builder Pattern Demo ===");
        BuilderDemo.main(args);

        System.out.println("=== Facade Pattern Demo ===");
        FacadeDemo.main(args);

        System.out.println("\n=== Adapter Pattern Demo ===");
        AdapterDemo.main(args);

        System.out.println("\n=== Adapter Pattern Demo ===");
        SimpleStrategyDemo.main(args);
    }
}