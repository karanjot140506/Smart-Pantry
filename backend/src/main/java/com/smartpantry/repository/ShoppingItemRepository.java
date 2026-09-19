package com.smartpantry.repository;

import com.smartpantry.entity.ShoppingItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ShoppingItemRepository extends MongoRepository<ShoppingItem, String> {
    List<ShoppingItem> findByUserId(String userId);
    List<ShoppingItem> findByUserIdAndPurchased(String userId, boolean purchased);
}
