package searching_program.search_product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.repository.CategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DtoEntityConverter converter;

    private CategoryDto categoryDto;

    CategoryServiceTest() {
    }

    @BeforeEach
    void setUp() {
        categoryDto = CategoryDto.builder()
                .name("Test Category")
                .build();
    }

    @Transactional
    @Test
    void createCategory() {
        CategoryDto savedCategoryDto = categoryService.createCategory(categoryDto);

        assertNotNull(savedCategoryDto.getId(), "카테고리 생성 시 ID는 null이 아니어야 합니다.");
        assertEquals(categoryDto.getName(), savedCategoryDto.getName(), "카테고리 이름이 예상과 일치해야 합니다.");
    }

    @Transactional
    @Test
    void updateCategory() {
        CategoryDto savedCategoryDto = categoryService.createCategory(categoryDto);

        CategoryDto updateDto = CategoryDto.builder()
                .name("Updated Category")
                .build();

        CategoryDto updatedCategoryDto = categoryService.updateCategory(savedCategoryDto.getId(), updateDto);

        assertEquals(savedCategoryDto.getId(), updatedCategoryDto.getId(), "카테고리 ID는 동일해야 합니다.");
        assertEquals("Updated Category", updatedCategoryDto.getName(), "카테고리 이름이 업데이트된 값과 일치해야 합니다.");
    }

    @Transactional
    @Test
    void deleteCategory() {
        CategoryDto savedCategoryDto = categoryService.createCategory(categoryDto);

        categoryService.deleteCategory(savedCategoryDto.getId());

        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.getCategoryById(savedCategoryDto.getId());
        }, "카테고리 삭제 후 조회 시 예외가 발생해야 합니다.");
    }

    @Transactional(readOnly = true)
    @Test
    void getCategoryById() {
        CategoryDto savedCategoryDto = categoryService.createCategory(categoryDto);

        CategoryDto foundCategoryDto = categoryService.getCategoryById(savedCategoryDto.getId());

        assertEquals(savedCategoryDto.getId(), foundCategoryDto.getId(), "조회된 카테고리의 ID가 예상과 일치해야 합니다.");
        assertEquals(savedCategoryDto.getName(), foundCategoryDto.getName(), "조회된 카테고리의 이름이 예상과 일치해야 합니다.");
    }

    @Transactional(readOnly = true)
    @Test
    void getAllCategories() {
        categoryService.createCategory(categoryDto);

        List<CategoryDto> categories = categoryService.getAllCategories();

        assertFalse(categories.isEmpty(), "카테고리 목록은 비어있지 않아야 합니다.");
        assertEquals(1, categories.size(), "카테고리 목록의 크기가 예상과 일치해야 합니다.");
    }

    @Test
    void findByName() {
        categoryService.createCategory(categoryDto);

        CategoryDto foundCategoryDto = categoryService.findByName("Test Category");

        assertNotNull(foundCategoryDto, "이름으로 조회한 카테고리는 null이 아니어야 합니다.");
        assertEquals(categoryDto.getName(), foundCategoryDto.getName(), "이름으로 조회한 카테고리의 이름이 예상과 일치해야 합니다.");
    }
}
