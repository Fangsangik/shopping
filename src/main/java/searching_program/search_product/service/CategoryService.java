package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Category;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DtoEntityConverter converter;

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = converter.convertToCategoryEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);

        return converter.convertToCategoryDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));

        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return converter.convertToCategoryDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById (Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        return converter.convertToCategoryDto(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(converter::convertToCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto findByName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        return converter.convertToCategoryDto(category);
    }

}


