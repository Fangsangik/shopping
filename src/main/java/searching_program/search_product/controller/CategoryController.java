package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.CategoryDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.CategoryService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categoryName")
    public ResponseEntity<CategoryDto> findByName
            (@RequestBody String categoryName) {
        CategoryDto byName = categoryService.findByName(categoryName);
        return ResponseEntity.ok(byName);
    }

    @GetMapping("/categoryAllName")
    public ResponseEntity<List<CategoryDto>> allCategories() {
        try {
            List<CategoryDto> allCategories = categoryService.getAllCategories();
            return ResponseEntity.ok(allCategories);
        } catch (CustomError e) {
            log.error("Error fetching categories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        try {
            CategoryDto categoryById = categoryService.getCategoryById(id);
            return ResponseEntity.ok(categoryById);
        } catch (IllegalArgumentException e) {
            log.error("카테고리를 찾을 수 없습니다: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto category = categoryService.createCategory(categoryDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (CustomError e) {
            log.error("카테고리 생성 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/updateCategory")
    public ResponseEntity<CategoryDto> updateCategory(
            @RequestParam Long id, @RequestBody CategoryDto categoryDto) {
        try {
            CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            log.error("카테고리 업데이트 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (CustomError e) {
            log.error("카테고리 업데이트 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteCategory(@RequestParam Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("카테고리가 삭제되었습니다.");
        } catch (CustomError e) {
            log.error("카테고리 삭제 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("카테고리를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("카테고리 삭제 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("카테고리가 삭제되지 않았습니다.");
        }
    }
}
