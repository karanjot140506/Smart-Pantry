package com.smartpantry.controller;

import com.smartpantry.entity.ShoppingItem;
import com.smartpantry.security.CurrentUser;
import com.smartpantry.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shopping-list")
@Tag(name = "Shopping List", description = "Manage the grocery shopping list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final CurrentUser currentUser;

    public ShoppingListController(ShoppingListService shoppingListService, CurrentUser currentUser) {
        this.shoppingListService = shoppingListService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "List all shopping list items for the current user")
    public ResponseEntity<List<ShoppingItem>> getAll() {
        return ResponseEntity.ok(shoppingListService.getAllForUser(currentUser.id()));
    }

    @PostMapping
    @Operation(summary = "Add a new item to the shopping list")
    public ResponseEntity<ShoppingItem> create(@Valid @RequestBody ShoppingItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.create(item, currentUser.id()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a shopping list item")
    public ResponseEntity<ShoppingItem> update(@PathVariable String id, @Valid @RequestBody ShoppingItem item) {
        return ResponseEntity.ok(shoppingListService.update(id, item, currentUser.id()));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle purchased status; purchasing an item also restocks the pantry")
    public ResponseEntity<ShoppingItem> togglePurchased(@PathVariable String id) {
        return ResponseEntity.ok(shoppingListService.togglePurchased(id, currentUser.id()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove an item from the shopping list")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        shoppingListService.delete(id, currentUser.id());
        return ResponseEntity.ok(Map.of("message", "Shopping list item removed successfully"));
    }
}
