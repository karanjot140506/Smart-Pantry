package com.smartpantry.service;

import com.smartpantry.entity.PantryItem;
import com.smartpantry.entity.ShoppingItem;
import com.smartpantry.exception.ResourceNotFoundException;
import com.smartpantry.repository.PantryItemRepository;
import com.smartpantry.repository.ShoppingItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class ShoppingListService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final PantryItemRepository pantryItemRepository;

    public ShoppingListService(ShoppingItemRepository shoppingItemRepository,
                                PantryItemRepository pantryItemRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.pantryItemRepository = pantryItemRepository;
    }

    public List<ShoppingItem> getAllForUser(String userId) {
        return shoppingItemRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(ShoppingItem::isPurchased)
                        .thenComparing(item -> priorityWeight(item.getPriority()), Comparator.reverseOrder())
                        .thenComparing(ShoppingItem::getCreatedAt, Comparator.reverseOrder()))
                .toList();
    }

    public ShoppingItem getById(String id, String userId) {
        ShoppingItem item = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shopping item not found with id: " + id));
        ensureOwnership(item, userId);
        return item;
    }

    public ShoppingItem create(ShoppingItem item, String userId) {
        item.setId(null);
        item.setUserId(userId);
        item.setPurchased(false);
        item.setCreatedAt(Instant.now());
        return shoppingItemRepository.save(item);
    }

    public ShoppingItem update(String id, ShoppingItem updated, String userId) {
        ShoppingItem existing = getById(id, userId);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setQuantity(updated.getQuantity());
        existing.setUnit(updated.getUnit());
        existing.setEstimatedPrice(updated.getEstimatedPrice());
        existing.setPriority(updated.getPriority());
        return shoppingItemRepository.save(existing);
    }

    public ShoppingItem togglePurchased(String id, String userId) {
        ShoppingItem existing = getById(id, userId);
        existing.setPurchased(!existing.isPurchased());
        existing.setPurchasedAt(existing.isPurchased() ? Instant.now() : null);

        ShoppingItem saved = shoppingItemRepository.save(existing);

        // Purchasing an item automatically restocks it into the pantry
        // with a sensible default expiry window, closing the loop between
        // shopping and pantry tracking.
        if (saved.isPurchased()) {
            PantryItem pantryItem = new PantryItem();
            pantryItem.setUserId(userId);
            pantryItem.setName(saved.getName());
            pantryItem.setCategory(saved.getCategory());
            pantryItem.setQuantity(saved.getQuantity());
            pantryItem.setUnit(saved.getUnit());
            pantryItem.setLowStockThreshold(1.0);
            pantryItem.setExpiryDate(java.time.LocalDate.now().plusDays(14));
            pantryItem.setPrice(saved.getEstimatedPrice());
            pantryItem.setStorageLocation("Pantry");
            pantryItem.setCreatedAt(Instant.now());
            pantryItem.setUpdatedAt(Instant.now());
            pantryItemRepository.save(pantryItem);
        }

        return saved;
    }

    public void delete(String id, String userId) {
        getById(id, userId);
        shoppingItemRepository.deleteById(id);
    }

    private int priorityWeight(String priority) {
        return switch (priority) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    private void ensureOwnership(ShoppingItem item, String userId) {
        if (!item.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Shopping item not found with id: " + item.getId());
        }
    }
}
