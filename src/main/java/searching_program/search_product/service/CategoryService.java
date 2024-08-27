package searching_program.search_product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.domain.Category;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.repository.CategoryRepository;
import searching_program.search_product.type.ErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final DtoEntityConverter converter;

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryDto.getName());
        if (existingCategory.isPresent()) {
            log.info("이미 존재하는 카테고리입니다: 이름 = {}", categoryDto.getName());
            return converter.convertToCategoryDto(existingCategory.get());
        }

        Category category = converter.convertToCategoryEntity(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 생성 성공: 이름 = {}", savedCategory.getName());

        return converter.convertToCategoryDto(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomError(ErrorCode.CATEGORY_NOT_FOUND));

        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return converter.convertToCategoryDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomError(ErrorCode.CATEGORY_NOT_FOUND));
        log.info("카테고리 조회 성공: ID = {}", id);
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
                .orElseThrow(() -> new CustomError(ErrorCode.CATEGORY_NOT_FOUND));

        return converter.convertToCategoryDto(category);
    }

}


