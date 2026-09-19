package com.smartpantry.controller;

import com.smartpantry.entity.PantryItem;
import com.smartpantry.security.CurrentUser;
import com.smartpantry.service.PantryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pantry")
@Tag(name = "Pantry", description = "Manage pantry items, expiry dates and stock levels")
public class PantryController {

    private final PantryService pantryService;
    private final CurrentUser currentUser;

    public PantryController(PantryService pantryService, CurrentUser currentUser) {
        this.pantryService = pantryService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @Operation(summary = "List all pantry items for the current user")
    public ResponseEntity<List<PantryItem>> getAll() {
        return ResponseEntity.ok(pantryService.getAllForUser(currentUser.id()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single pantry item by id")
    public ResponseEntity<PantryItem> getOne(@PathVariable String id) {
        return ResponseEntity.ok(pantryService.getById(id, currentUser.id()));
    }

    @PostMapping
    @Operation(summary = "Add a new pantry item")
    public ResponseEntity<PantryItem> create(@Valid @RequestBody PantryItem item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pantryService.create(item, currentUser.id()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing pantry item")
    public ResponseEntity<PantryItem> update(@PathVariable String id, @Valid @RequestBody PantryItem item) {
        return ResponseEntity.ok(pantryService.update(id, item, currentUser.id()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pantry item")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        pantryService.delete(id, currentUser.id());
        return ResponseEntity.ok(Map.of("message", "Pantry item deleted successfully"));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get items expiring within the given number of days (default 7)")
    public ResponseEntity<List<PantryItem>> getExpiring(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(pantryService.getExpiringWithinDays(currentUser.id(), days));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get items at or below their low-stock threshold")
    public ResponseEntity<List<PantryItem>> getLowStock() {
        return ResponseEntity.ok(pantryService.getLowStock(currentUser.id()));
    }
}
