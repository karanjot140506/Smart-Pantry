package com.smartpantry.service;

import com.smartpantry.entity.PantryItem;
import com.smartpantry.entity.ShoppingItem;
import com.smartpantry.repository.PantryItemRepository;
import com.smartpantry.repository.ShoppingItemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final PantryItemRepository pantryItemRepository;
    private final ShoppingItemRepository shoppingItemRepository;

    public AnalyticsService(PantryItemRepository pantryItemRepository,
                             ShoppingItemRepository shoppingItemRepository) {
        this.pantryItemRepository = pantryItemRepository;
        this.shoppingItemRepository = shoppingItemRepository;
    }

    public Map<String, Object> getDashboardSummary(String userId) {
        List<PantryItem> pantryItems = pantryItemRepository.findByUserId(userId);
        List<ShoppingItem> shoppingItems = shoppingItemRepository.findByUserId(userId);

        LocalDate today = LocalDate.now();
        LocalDate weekOut = today.plusDays(7);

        long expiringSoon = pantryItems.stream()
                .filter(i -> !i.getExpiryDate().isBefore(today) && !i.getExpiryDate().isAfter(weekOut))
                .count();
        long expired = pantryItems.stream().filter(i -> i.getExpiryDate().isBefore(today)).count();
        long lowStock = pantryItems.stream().filter(i -> i.getQuantity() <= i.getLowStockThreshold()).count();

        double totalInventoryValue = pantryItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        double pendingShoppingCost = shoppingItems.stream()
                .filter(s -> !s.isPurchased())
                .mapToDouble(s -> s.getEstimatedPrice() * s.getQuantity())
                .sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalPantryItems", pantryItems.size());
        summary.put("expiringSoonCount", expiringSoon);
        summary.put("expiredCount", expired);
        summary.put("lowStockCount", lowStock);
        summary.put("shoppingListCount", shoppingItems.stream().filter(s -> !s.isPurchased()).count());
        summary.put("totalInventoryValue", round(totalInventoryValue));
        summary.put("pendingShoppingCost", round(pendingShoppingCost));
        summary.put("categoryBreakdown", categoryBreakdown(pantryItems));
        summary.put("monthlySpend", monthlySpend(pantryItems));
        summary.put("expiryTimeline", expiryTimeline(pantryItems));
        return summary;
    }

    private List<Map<String, Object>> categoryBreakdown(List<PantryItem> items) {
        Map<String, Double> byCategory = items.stream()
                .collect(Collectors.groupingBy(PantryItem::getCategory,
                        LinkedHashMap::new,
                        Collectors.summingDouble(i -> i.getPrice() * i.getQuantity())));

        return byCategory.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("category", e.getKey());
                    m.put("value", round(e.getValue()));
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> monthlySpend(List<PantryItem> items) {
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Double> byMonth = items.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().format(monthFormatter),
                        LinkedHashMap::new,
                        Collectors.summingDouble(i -> i.getPrice() * i.getQuantity())));

        return byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    LocalDate parsed = LocalDate.parse(e.getKey() + "-01");
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("month", parsed.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + parsed.getYear());
                    m.put("amount", round(e.getValue()));
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> expiryTimeline(List<PantryItem> items) {
        LocalDate today = LocalDate.now();
        Map<String, Long> buckets = new LinkedHashMap<>();
        buckets.put("Expired", items.stream().filter(i -> i.getExpiryDate().isBefore(today)).count());
        buckets.put("0-3 days", items.stream().filter(i -> daysBetween(today, i.getExpiryDate()) >= 0 && daysBetween(today, i.getExpiryDate()) <= 3).count());
        buckets.put("4-7 days", items.stream().filter(i -> daysBetween(today, i.getExpiryDate()) >= 4 && daysBetween(today, i.getExpiryDate()) <= 7).count());
        buckets.put("8-30 days", items.stream().filter(i -> daysBetween(today, i.getExpiryDate()) >= 8 && daysBetween(today, i.getExpiryDate()) <= 30).count());
        buckets.put("30+ days", items.stream().filter(i -> daysBetween(today, i.getExpiryDate()) > 30).count());

        return buckets.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("range", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    private long daysBetween(LocalDate from, LocalDate to) {
        return java.time.temporal.ChronoUnit.DAYS.between(from, to);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
