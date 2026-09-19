package com.smartpantry.repository;

import com.smartpantry.entity.PantryItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PantryItemRepository extends MongoRepository<PantryItem, String> {
    List<PantryItem> findByUserId(String userId);
    List<PantryItem> findByUserIdAndExpiryDateLessThanEqual(String userId, LocalDate date);
    void deleteByIdAndUserId(String id, String userId);
}
