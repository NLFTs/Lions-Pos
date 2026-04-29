package com.dak.spravel.seeder;

import com.dak.spravel.model.Category;
import com.dak.spravel.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Seeds default categories on application startup.
 */
@Component
@RequiredArgsConstructor
public class CategorySeeder {
    private final CategoryRepository categoryRepository;

    @Value("${app.enable.seeder:false}")
    private boolean enableSeeder;

    private final String[][] categories = {
        {"Technology", "Articles related to software, hardware, and tech trends"},
        {"Tutorial", "Step-by-step guides and how-to articles"},
        {"News", "Latest news and updates"},
        {"Opinion", "Editorial and opinion pieces"},
        {"Uncategorized", "General articles without a specific category"},
    };

    public void run() {
        if (!enableSeeder) return;
        for (String[] data : categories) {
            if (!categoryRepository.existsByName(data[0])) {
                Category category = new Category();
                category.setName(data[0]);
                category.setDescription(data[1]);
                categoryRepository.save(category);
            }
        }
    }
}
