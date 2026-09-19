package com.smartpantry.service;

import com.smartpantry.entity.PantryItem;
import com.smartpantry.entity.Recipe;
import com.smartpantry.repository.PantryItemRepository;
import com.smartpantry.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final PantryItemRepository pantryItemRepository;

    public RecipeService(RecipeRepository recipeRepository, PantryItemRepository pantryItemRepository) {
        this.recipeRepository = recipeRepository;
        this.pantryItemRepository = pantryItemRepository;
    }

    public List<Recipe> getAll() {
        return recipeRepository.findAll();
    }

    public Recipe getById(String id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new com.smartpantry.exception.ResourceNotFoundException("Recipe not found with id: " + id));
    }

    public Recipe create(Recipe recipe) {
        recipe.setId(null);
        return recipeRepository.save(recipe);
    }

    /**
     * Suggests recipes the user can make (fully or mostly) using ingredients
     * currently in their pantry. Each result is annotated with a match score,
     * the ingredients on hand, and the ones still missing.
     */
    public List<Map<String, Object>> suggestFromPantry(String userId) {
        Set<String> pantryIngredients = pantryItemRepository.findByUserId(userId).stream()
                .map(PantryItem::getName)
                .map(String::toLowerCase)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<Recipe> recipes = recipeRepository.findAll();

        return recipes.stream()
                .map(recipe -> buildSuggestion(recipe, pantryIngredients))
                .sorted(Comparator.comparingDouble((Map<String, Object> m) -> (double) m.get("matchPercentage")).reversed())
                .toList();
    }

    private Map<String, Object> buildSuggestion(Recipe recipe, Set<String> pantryIngredients) {
        List<String> have = recipe.getIngredients().stream()
                .filter(ingredient -> containsIngredient(pantryIngredients, ingredient))
                .toList();
        List<String> missing = recipe.getIngredients().stream()
                .filter(ingredient -> !containsIngredient(pantryIngredients, ingredient))
                .toList();

        double matchPercentage = recipe.getIngredients().isEmpty() ? 0
                : Math.round((have.size() * 1000.0 / recipe.getIngredients().size())) / 10.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recipe", recipe);
        result.put("haveIngredients", have);
        result.put("missingIngredients", missing);
        result.put("matchPercentage", matchPercentage);
        result.put("canMakeNow", missing.isEmpty());
        return result;
    }

    private boolean containsIngredient(Set<String> pantryIngredients, String ingredient) {
        String needle = ingredient.toLowerCase().trim();
        return pantryIngredients.stream().anyMatch(p -> p.contains(needle) || needle.contains(p));
    }
}
