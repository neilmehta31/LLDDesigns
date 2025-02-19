import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class FoodItem {
    private final String foodItemId;
    private final String name;
    private final Integer price;
    private int rating;

    FoodItem(String name, Integer price, String foodItemId) {
        this.name = name;
        this.price = price;
        this.foodItemId = foodItemId;
        this.rating = 0;
    }

    public Integer getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFoodItemId() {
        return foodItemId;
    }
}

class Restaurant{
    private final String restaurantId;
    private Set<FoodItem> foodItems;
    private int rating;
    private List<Order> orders;

    public Restaurant(String restaurantId, Set<FoodItem> foodItems) {
        this.restaurantId = restaurantId;
        this.foodItems = foodItems;
        this.rating = 0;
        this.orders = new ArrayList<>();
    }

    public Set<FoodItem> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(Set<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public boolean orderFood(Order order){
        orders.add(order);
        return true;
    }
}

class Order{
    private final String orderId;
    private final String restaurantId;
    private final String foodItemId;
    private int rating;

    Order(String restaurantId, String foodItemId) {
        this.restaurantId = restaurantId;
        this.foodItemId = foodItemId;
        this.orderId = UUID.randomUUID().toString();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public String getFoodItemId() {
        return foodItemId;
    }

    public void setRating(int rating) {
        this.rating = rating;

    }

    public int getRating() {
        return rating;
    }
}

class RestaurantRepo{

    private HashMap<FoodItem, Set<Restaurant>> foodToRestaurantMapping;

    public RestaurantRepo(){
        this.foodToRestaurantMapping = new HashMap<>();
    };


    public HashMap<FoodItem, Set<Restaurant>> getFoodToRestaurantMapping() {
        return foodToRestaurantMapping;
    }

    public Set<Restaurant> getRestaurantsForFoodItem(FoodItem foodItem){
        return foodToRestaurantMapping.get(foodItem);
    }

    public void addFoodToRestaurant(Set<FoodItem> foodItemSet, Restaurant restaurant){
        restaurant.setFoodItems(foodItemSet);
        foodItemSet.forEach(fi -> {
            if(foodToRestaurantMapping.containsKey(fi)){
                foodToRestaurantMapping.get(fi).add(restaurant);
            }else{
                Set<Restaurant> restaurantSet = new HashSet<>(); // Create a new mutable set
                restaurantSet.add(restaurant); // Add the restaurant
                foodToRestaurantMapping.put(fi, restaurantSet); // Put it in the map
            }
        });
    }
}

class RestaurantManagementSystem{

    private RestaurantRepo restaurantRepo;
    private Map<String, Restaurant> restaurants; // restaurantID -> restaurant mapping
    private Map<String, Order> orders; // orderId -> orders mapping

    public RestaurantManagementSystem(List<Restaurant> restaurants){
        this.restaurants = new HashMap();
        this.restaurantRepo = new RestaurantRepo();
        for (Restaurant restaurant: restaurants){
            this.restaurants.put(restaurant.getRestaurantId(), restaurant);
            this.restaurantRepo.addFoodToRestaurant(restaurant.getFoodItems() , restaurant);
        }
        this.orders = new HashMap<>();
    }

    public String orderFood(String restaurantId, String foodItemId){
        Order order = new Order(restaurantId, foodItemId);
        if(this.restaurants.containsKey(restaurantId)){
            this.restaurants.get(restaurantId).orderFood(order);
            System.out.println("Ordered food:" + foodItemId + " from restaurant : " + restaurantId);
            this.orders.put(order.getOrderId(), order);
            return order.getOrderId();
        }else{
            throw new IllegalStateException("Restaurant does not exist");
        }
    }

    public void rateOrder(String orderId, int rating){
        if(this.orders.containsKey(orderId)){
            Order order = this.orders.get(orderId);
            order.setRating(rating);
            rateRestaurantAndFoodItemAsWell(rating, order);
            System.out.println("Order : " + orderId + "rated !");
        }else{
            throw new IllegalStateException("This order does not exist ;(");
        }
    }

    public List<String> getTopRestaurantsByFood(String foodItemId){
        FoodItem foodItem = getFoodItemObjectFromFoodItemId(foodItemId);
        List<Restaurant> allRestaurantsForFoodItem =
                restaurantRepo.getRestaurantsForFoodItem(foodItem).stream().sorted((r1, r2)-> r2.getRating()- r1.getRating()).toList();
        List<String> topRestaurantIdsForFood = new ArrayList<>();
        allRestaurantsForFoodItem.forEach(r-> topRestaurantIdsForFood.add(r.getRestaurantId()));
        return topRestaurantIdsForFood;
    }

    public List<Restaurant> fetchRestaurantsWithMostRatings(){
        return this.restaurants.values().stream().sorted((r1, r2) -> r2.getRating() - r1.getRating()).toList();
    }

    private void rateRestaurantAndFoodItemAsWell(int rating, Order order) {
        Restaurant restaurant = this.restaurants.get(order.getRestaurantId());
        FoodItem foodItem = getFoodItemObjectFromFoodItemId(order.getFoodItemId());
        restaurant.setRating(rating);
        foodItem.setRating(rating);
    }

    private FoodItem getFoodItemObjectFromFoodItemId(String foodItemId) {
        return this.restaurantRepo.getFoodToRestaurantMapping().keySet()
                .stream()
                .filter(foodItem1 -> foodItem1.getFoodItemId().equals(foodItemId)) // Use equals()
                .findFirst() // Use findFirst() instead of toList().get(0)
                .orElseThrow(() -> new IllegalStateException("Food item with ID " + foodItemId + " not found"));
    }


}

public class Swiggy {
    public static void main(String[] args){
        FoodItem f1 = new FoodItem("f1", 200, "f1");
        FoodItem f2 = new FoodItem("f2", 200, "f2");
        FoodItem f3 = new FoodItem("f3", 200, "f3");
        FoodItem f4 = new FoodItem("f4", 200, "f4");
        FoodItem f5 = new FoodItem("f5", 200, "f5");
        Restaurant r1 = new Restaurant("r1", Set.of(f1, f2, f3));
        Restaurant r2 = new Restaurant("r2", Set.of(f5, f4, f3));
        RestaurantManagementSystem rms = new RestaurantManagementSystem(List.of(r1,r2));
        String orderId = rms.orderFood("r1", "f2");
        rms.rateOrder(orderId, 3);

        String orderId2 = rms.orderFood("r2", "f4");
        rms.rateOrder(orderId2, 1);

        List<String> topRestaurantForFoodf3 = rms.getTopRestaurantsByFood("f3");
        System.out.println(topRestaurantForFoodf3);

        List<String> topRestaurantForFoodf2 = rms.getTopRestaurantsByFood("f2");
        System.out.println(topRestaurantForFoodf2);

        List<Restaurant> topRatedRestaurants = rms.fetchRestaurantsWithMostRatings();
        topRatedRestaurants.forEach(restaurant -> System.out.println(restaurant.getRestaurantId()));

    }

}


