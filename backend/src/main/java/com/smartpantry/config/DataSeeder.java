package com.smartpantry.config;

import com.smartpantry.entity.PantryItem;
import com.smartpantry.entity.Recipe;
import com.smartpantry.entity.ShoppingItem;
import com.smartpantry.entity.User;
import com.smartpantry.repository.PantryItemRepository;
import com.smartpantry.repository.RecipeRepository;
import com.smartpantry.repository.ShoppingItemRepository;
import com.smartpantry.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a ready-to-demo dataset on first boot: a demo login, a stocked
 * pantry (with items already expired, expiring soon, and low on stock so
 * every dashboard widget has something to show), a shopping list with a
 * mix of pending/purchased items, and a starter recipe catalog.
 *
 * Runs only once — each block is skipped independently if that collection
 * already has data, so it is safe to restart the app without duplicating
 * anything or overwriting real data you've since added.
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    public static final String DEMO_EMAIL = "demo@smartpantry.com";
    public static final String DEMO_PASSWORD = "Demo@1234";

    private final UserRepository userRepository;
    private final PantryItemRepository pantryItemRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final RecipeRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                       PantryItemRepository pantryItemRepository,
                       ShoppingItemRepository shoppingItemRepository,
                       RecipeRepository recipeRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.pantryItemRepository = pantryItemRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.recipeRepository = recipeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRecipes();
        String demoUserId = seedDemoUser();
        if (demoUserId != null) {
            seedPantryItems(demoUserId);
            seedShoppingList(demoUserId);
        }
    }

    /**
     * Creates the demo account only if it does not already exist.
     * Returns the demo user's id when the account was just created
     * (so pantry/shopping data is seeded alongside it), or null if the
     * account already existed — leaving any real demo data untouched.
     */
    private String seedDemoUser() {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return null;
        }
        User demo = new User("Demo User", DEMO_EMAIL, passwordEncoder.encode(DEMO_PASSWORD));
        demo.setRole("USER");
        demo.setCreatedAt(Instant.now());
        return userRepository.save(demo).getId();
    }

    private void seedPantryItems(String userId) {
        LocalDate today = LocalDate.now();

        List<PantryItem> items = List.of(
                pantry(userId, "Whole Milk", "Dairy", 1.0, "l", 1.0, today.minusDays(1), 3.20, "Fridge", "Already past its date — good demo of expiry alerts"),
                pantry(userId, "Greek Yogurt", "Dairy", 2.0, "pcs", 2.0, today.plusDays(1), 4.25, "Fridge", null),
                pantry(userId, "Cheddar Cheese", "Dairy", 0.3, "kg", 0.2, today.plusDays(21), 6.50, "Fridge", null),
                pantry(userId, "Spinach", "Produce", 1.0, "pack", 1.0, today.plusDays(2), 2.10, "Fridge", null),
                pantry(userId, "Tomato", "Produce", 6.0, "pcs", 3.0, today.plusDays(5), 0.40, "Pantry", null),
                pantry(userId, "Banana", "Produce", 4.0, "pcs", 2.0, today.plusDays(3), 0.25, "Pantry", null),
                pantry(userId, "Onion", "Produce", 5.0, "pcs", 2.0, today.plusDays(18), 0.30, "Pantry", null),
                pantry(userId, "Garlic", "Produce", 1.0, "pack", 1.0, today.plusDays(25), 1.10, "Pantry", null),
                pantry(userId, "Chicken Breast", "Meat & Seafood", 0.5, "kg", 0.5, today.plusDays(2), 5.80, "Freezer", null),
                pantry(userId, "Shrimp", "Meat & Seafood", 0.25, "kg", 0.3, today.plusDays(6), 7.20, "Freezer", "Below threshold — low stock demo"),
                pantry(userId, "Eggs", "Dairy", 8.0, "pcs", 4.0, today.plusDays(14), 0.22, "Fridge", null),
                pantry(userId, "Pasta", "Grains", 1.0, "kg", 0.5, today.plusDays(180), 1.80, "Pantry", null),
                pantry(userId, "Brown Rice", "Grains", 2.0, "kg", 1.0, today.plusDays(200), 3.40, "Pantry", null),
                pantry(userId, "Olive Oil", "Condiments", 0.15, "l", 0.3, today.plusDays(90), 8.90, "Pantry", "Below threshold — low stock demo"),
                pantry(userId, "Soy Sauce", "Condiments", 0.4, "l", 0.2, today.plusDays(150), 2.60, "Pantry", null),
                pantry(userId, "Butter", "Dairy", 0.2, "kg", 0.1, today.plusDays(30), 3.10, "Fridge", null),
                pantry(userId, "Orange Juice", "Beverages", 1.0, "l", 1.0, today.minusDays(3), 2.75, "Fridge", "Already expired — good demo of expiry alerts"),
                pantry(userId, "Broccoli", "Produce", 1.0, "pcs", 1.0, today.plusDays(4), 1.50, "Fridge", null),
                pantry(userId, "Bell Pepper", "Produce", 3.0, "pcs", 2.0, today.plusDays(6), 0.60, "Fridge", null),
                pantry(userId, "Granola", "Snacks", 0.5, "kg", 0.2, today.plusDays(60), 4.00, "Pantry", null)
        );

        pantryItemRepository.saveAll(items);
    }

    private void seedShoppingList(String userId) {
        List<ShoppingItem> items = List.of(
                shopping(userId, "Avocado", "Produce", 3.0, "pcs", 1.20, "HIGH", false),
                shopping(userId, "Whole Wheat Bread", "Bakery", 1.0, "pack", 3.00, "MEDIUM", false),
                shopping(userId, "Almond Milk", "Beverages", 2.0, "l", 3.50, "MEDIUM", false),
                shopping(userId, "Parmesan Cheese", "Dairy", 1.0, "pcs", 5.90, "LOW", false),
                shopping(userId, "Blueberries", "Produce", 2.0, "pack", 4.10, "HIGH", false),
                shopping(userId, "Paper Towels", "Other", 1.0, "pack", 6.00, "LOW", true),
                shopping(userId, "Coffee Beans", "Beverages", 1.0, "pack", 9.50, "MEDIUM", true)
        );

        shoppingItemRepository.saveAll(items);
    }

    private void seedRecipes() {
        if (recipeRepository.count() > 0) {
            return;
        }

        recipeRepository.saveAll(List.of(
                recipe("Classic Tomato Pasta",
                        "A quick weeknight pasta with a rich tomato sauce.",
                        List.of("Pasta", "Tomato", "Garlic", "Olive Oil", "Onion", "Basil"),
                        List.of("Boil pasta until al dente", "Saute garlic and onion in olive oil",
                                "Add chopped tomatoes and simmer 10 minutes", "Toss with pasta and fresh basil"),
                        20, "EASY", "Italian", "🍝"),
                recipe("Vegetable Stir Fry",
                        "A fast, colorful stir fry using whatever vegetables are on hand.",
                        List.of("Broccoli", "Carrot", "Bell Pepper", "Soy Sauce", "Garlic", "Ginger", "Rice"),
                        List.of("Cook rice separately", "Stir fry garlic and ginger in a hot pan",
                                "Add chopped vegetables and cook 5-7 minutes", "Season with soy sauce and serve over rice"),
                        25, "EASY", "Asian", "🥦"),
                recipe("Cheesy Scrambled Eggs",
                        "A protein-rich breakfast ready in minutes.",
                        List.of("Eggs", "Milk", "Cheese", "Butter", "Salt", "Pepper"),
                        List.of("Whisk eggs with milk, salt and pepper", "Melt butter in a nonstick pan",
                                "Cook eggs on low heat, stirring gently", "Fold in cheese just before serving"),
                        10, "EASY", "American", "🍳"),
                recipe("Chicken & Rice Bowl",
                        "A balanced, filling bowl built around pantry staples.",
                        List.of("Chicken Breast", "Rice", "Broccoli", "Soy Sauce", "Garlic", "Olive Oil"),
                        List.of("Cook rice", "Season and pan-sear chicken breast until cooked through",
                                "Steam broccoli", "Slice chicken and assemble bowl with sauce"),
                        30, "MEDIUM", "Fusion", "🍗"),
                recipe("Creamy Potato Soup",
                        "A cozy soup that uses up potatoes before they sprout.",
                        List.of("Potato", "Onion", "Garlic", "Milk", "Butter", "Vegetable Stock", "Salt"),
                        List.of("Saute onion and garlic in butter", "Add cubed potatoes and stock, simmer until soft",
                                "Blend until smooth", "Stir in milk and season to taste"),
                        35, "MEDIUM", "American", "🥣"),
                recipe("Fruit & Yogurt Parfait",
                        "A no-cook, healthy breakfast or snack.",
                        List.of("Yogurt", "Banana", "Honey", "Granola", "Berries"),
                        List.of("Layer yogurt in a glass", "Add sliced banana and berries",
                                "Top with granola and a drizzle of honey"),
                        5, "EASY", "General", "🍓"),
                recipe("Bean & Cheese Quesadilla",
                        "A crispy, cheesy quesadilla ready in one pan.",
                        List.of("Tortilla", "Cheese", "Beans", "Onion", "Salsa"),
                        List.of("Spread beans and cheese on a tortilla", "Add chopped onion and fold in half",
                                "Cook in a dry pan until golden on both sides", "Serve with salsa"),
                        15, "EASY", "Mexican", "🌮"),
                recipe("Garlic Butter Shrimp Pasta",
                        "A restaurant-style pasta that comes together in under 30 minutes.",
                        List.of("Pasta", "Shrimp", "Garlic", "Butter", "Lemon", "Parsley"),
                        List.of("Boil pasta", "Saute garlic in butter until fragrant",
                                "Add shrimp and cook until pink", "Toss with pasta, lemon juice and parsley"),
                        25, "MEDIUM", "Italian", "🍤"),
                recipe("Loaded Veggie Omelette",
                        "A protein-packed breakfast that clears out fridge produce.",
                        List.of("Eggs", "Bell Pepper", "Onion", "Cheddar Cheese", "Butter", "Salt"),
                        List.of("Whisk eggs with salt", "Saute diced pepper and onion in butter",
                                "Pour in eggs and cook on low heat", "Top with cheese, fold and serve"),
                        12, "EASY", "General", "🍳"),
                recipe("Orange-Glazed Chicken",
                        "Sweet and savory chicken using up citrus before it turns.",
                        List.of("Chicken Breast", "Orange Juice", "Soy Sauce", "Garlic", "Rice"),
                        List.of("Sear chicken breast until golden", "Add orange juice, soy sauce and garlic",
                                "Simmer until sauce thickens and chicken is cooked through", "Serve over rice"),
                        28, "MEDIUM", "Fusion", "🍊")
        ));
    }

    private PantryItem pantry(String userId, String name, String category, double qty, String unit,
                               double threshold, LocalDate expiry, double price, String location, String notes) {
        PantryItem item = new PantryItem();
        item.setUserId(userId);
        item.setName(name);
        item.setCategory(category);
        item.setQuantity(qty);
        item.setUnit(unit);
        item.setLowStockThreshold(threshold);
        item.setExpiryDate(expiry);
        item.setPrice(price);
        item.setStorageLocation(location);
        item.setNotes(notes);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return item;
    }

    private ShoppingItem shopping(String userId, String name, String category, double qty, String unit,
                                   double estimatedPrice, String priority, boolean purchased) {
        ShoppingItem item = new ShoppingItem();
        item.setUserId(userId);
        item.setName(name);
        item.setCategory(category);
        item.setQuantity(qty);
        item.setUnit(unit);
        item.setEstimatedPrice(estimatedPrice);
        item.setPriority(priority);
        item.setPurchased(purchased);
        item.setCreatedAt(Instant.now());
        if (purchased) {
            item.setPurchasedAt(Instant.now());
        }
        return item;
    }

    private Recipe recipe(String title, String description, List<String> ingredients, List<String> steps,
                           int prepTime, String difficulty, String cuisine, String emoji) {
        Recipe recipe = new Recipe();
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setIngredients(ingredients);
        recipe.setSteps(steps);
        recipe.setPrepTimeMinutes(prepTime);
        recipe.setDifficulty(difficulty);
        recipe.setCuisine(cuisine);
        recipe.setImageEmoji(emoji);
        return recipe;
    }
}
