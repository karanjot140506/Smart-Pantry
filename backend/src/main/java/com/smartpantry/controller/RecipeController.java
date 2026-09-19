package com.smartpantry.controller;

import com.smartpantry.entity.Recipe;
import com.smartpantry.security.CurrentUser;
import com.smartpantry.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recipes", description = "Browse recipes and get suggestions based on pantry stock")
public class RecipeController {

    private final RecipeService recipeService;
    private final CurrentUser currentUser;

    public RecipeController(RecipeService recipeService, CurrentUser currentUser) {
        this.recipeService = recipeService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "List all recipes")
    public ResponseEntity<List<Recipe>> getAll() {
        return ResponseEntity.ok(recipeService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single recipe by id")
    public ResponseEntity<Recipe> getOne(@PathVariable String id) {
        return ResponseEntity.ok(recipeService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Add a custom recipe")
    public ResponseEntity<Recipe> create(@Valid @RequestBody Recipe recipe) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(recipe));
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Suggest recipes that can be made from the current pantry stock, ranked by match %")
    public ResponseEntity<List<Map<String, Object>>> suggestions() {
        return ResponseEntity.ok(recipeService.suggestFromPantry(currentUser.id()));
    }
}
