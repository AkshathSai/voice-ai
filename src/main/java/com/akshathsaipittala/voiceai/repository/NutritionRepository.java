package com.akshathsaipittala.voiceai.repository;

import com.akshathsaipittala.voiceai.model.NutritionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NutritionRepository extends JpaRepository<NutritionEntry, Long> {

    List<NutritionEntry> findByEntryDate(LocalDate date);

    List<NutritionEntry> findByEntryDateBetween(LocalDate startDate, LocalDate endDate);

    List<NutritionEntry> findByMealType(String mealType);

    @Query("SELECT SUM(n.calories) FROM NutritionEntry n WHERE n.entryDate = :date")
    Integer getTotalCaloriesForDate(LocalDate date);

    @Query("SELECT n FROM NutritionEntry n WHERE n.entryDate = :date ORDER BY n.createdAt DESC")
    List<NutritionEntry> findByEntryDateOrderByCreatedAtDesc(LocalDate date);
}
