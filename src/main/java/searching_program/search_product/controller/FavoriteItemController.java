package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.FavoriteService;

@Slf4j
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
public class FavoriteItemController {

    private final FavoriteService favoriteService;

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
}
