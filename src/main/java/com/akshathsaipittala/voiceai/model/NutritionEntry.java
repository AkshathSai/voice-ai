package com.akshathsaipittala.voiceai.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nutrition_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_description", nullable = false, length = 500)
    private String foodDescription;

    @Column(name = "meal_type", nullable = false)
    private String mealType; // breakfast, lunch, dinner, snack

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "protein_grams")
    private Double proteinGrams;

    @Column(name = "carbs_grams")
    private Double carbsGrams;

    @Column(name = "fat_grams")
    private Double fatGrams;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (entryDate == null) {
            entryDate = LocalDate.now();
        }
    }

    public NutritionEntry(String foodDescription, String mealType, Integer calories) {
        this.foodDescription = foodDescription;
        this.mealType = mealType;
        this.calories = calories;
        this.entryDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }
}
