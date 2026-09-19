package com.smartpantry.service;

import com.smartpantry.entity.PantryItem;
import com.smartpantry.exception.ResourceNotFoundException;
import com.smartpantry.repository.PantryItemRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class PantryService {

    private final PantryItemRepository pantryItemRepository;

    public PantryService(PantryItemRepository pantryItemRepository) {
        this.pantryItemRepository = pantryItemRepository;
    }

    public List<PantryItem> getAllForUser(String userId) {
        return pantryItemRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(PantryItem::getExpiryDate))
                .toList();
    }

    public PantryItem getById(String id, String userId) {
        PantryItem item = pantryItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pantry item not found with id: " + id));
        ensureOwnership(item, userId);
        return item;
    }

    public PantryItem create(PantryItem item, String userId) {
        item.setId(null);
        item.setUserId(userId);
        item.setCreatedAt(Instant.now());
        item.setUpdatedAt(Instant.now());
        return pantryItemRepository.save(item);
    }

    public PantryItem update(String id, PantryItem updated, String userId) {
        PantryItem existing = getById(id, userId);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setQuantity(updated.getQuantity());
        existing.setUnit(updated.getUnit());
        existing.setLowStockThreshold(updated.getLowStockThreshold());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setPrice(updated.getPrice());
        existing.setStorageLocation(updated.getStorageLocation());
        existing.setNotes(updated.getNotes());
        existing.setUpdatedAt(Instant.now());
        return pantryItemRepository.save(existing);
    }

    public void delete(String id, String userId) {
        getById(id, userId);
        pantryItemRepository.deleteByIdAndUserId(id, userId);
    }

    public List<PantryItem> getExpiringWithinDays(String userId, int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return pantryItemRepository.findByUserIdAndExpiryDateLessThanEqual(userId, cutoff).stream()
                .sorted(Comparator.comparing(PantryItem::getExpiryDate))
                .toList();
    }

    public List<PantryItem> getLowStock(String userId) {
        return pantryItemRepository.findByUserId(userId).stream()
                .filter(item -> item.getQuantity() <= item.getLowStockThreshold())
                .sorted(Comparator.comparing(PantryItem::getQuantity))
                .toList();
    }

    private void ensureOwnership(PantryItem item, String userId) {
        if (!item.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Pantry item not found with id: " + item.getId());
        }
    }
}
