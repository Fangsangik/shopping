package searching_program.search_product.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.domain.Item;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.FavoriteService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteItemController {

    private final FavoriteService favoriteService;

    /**
     *http://localhost:8080/favorite/add?userId=ik0605&itemId=1
     */
    @PostMapping("/add")
    public ResponseEntity<String> addItem
            (@RequestParam String userId,
             @RequestParam Long itemId) {
        try {
            favoriteService.addFavorites(userId, itemId);
            return ResponseEntity.ok("아이탬이 즐겨찾기에 추가되었습니다.");
        } catch (CustomError e) {
            log.error("즐겨찾기 추가 실패 : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("즐겨찾ㄱ기 추가 중 오류 발생" + e.getMessage());
        } catch (Exception e) {
            log.error("서버오류 : {}" , e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeItem(@RequestParam String userId, @RequestParam Long itemId) {
        try {
            favoriteService.removeFavorites(userId, itemId);
            return ResponseEntity.ok("아이템이 즐겨찾기에서 제거되었습니다.");
        } catch (IllegalArgumentException e) {
            log.error("즐겨찾기 제거 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("즐겨찾기 제거 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            log.error("서버 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findFavorite(@RequestParam(required = false) String userId) {
        // userId 파라미터가 제공되지 않은 경우
        if (userId == null || userId.isEmpty()) {
            String errorMessage = "userId가 제공되지 않았습니다.";
            log.error(errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }

        try {
            // 서비스 호출을 통해 즐겨찾기 아이템 조회
            List<ItemDto> favoriteItemsByUserId = favoriteService.findFavoriteItemsByUserId(userId);
            return ResponseEntity.ok(favoriteItemsByUserId);
        } catch (CustomError e) {
            // 사용자 정의 예외 처리
            log.error("즐겨찾기 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("즐겨찾기 조회 중 오류 발생: " + e.getMessage());
        } catch (Exception e) {
            // 일반 예외 처리
            log.error("서버 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
