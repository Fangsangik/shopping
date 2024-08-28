package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.dto.BucketDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.BucketService;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bucket")
@RequiredArgsConstructor
public class BucketController {

    private final BucketService bucketService;

    @GetMapping("/search/AllBucket")
    public ResponseEntity<List<BucketDto>> findAllBuckets() {
        try {
            List<BucketDto> allBuckets = bucketService.getAllBuckets();
            if (allBuckets.isEmpty()) {
                return ResponseEntity.noContent().build(); // HTTP 204
            } else {
                return ResponseEntity.ok(allBuckets);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    /**
     * http://localhost:8080/bucket/search/ItemName?itemName=mac
     */
    @GetMapping("/search/ItemName")
    public ResponseEntity<BucketDto> findBYItemName
            (@RequestParam String itemName) {
        BucketDto byItemName = bucketService.findByItemName(itemName);
        return ResponseEntity.ok(byItemName);
    }

    /**
     *http://localhost:8080/bucket/addItem/1/mac
     */
    @PostMapping("/addItem/{memberId}/{itemName}")
    public ResponseEntity<BucketDto> addItem
            (@PathVariable Long memberId,
             @PathVariable String itemName,
             @RequestBody int quantity) {
        BucketDto bucketDto = bucketService.addItemToBucket(memberId, itemName, quantity);
        return ResponseEntity.ok(bucketDto);
    }


    @PostMapping("/validateBucketItem")
    public ResponseEntity<String> validate
            (@RequestParam Long memberId) {
        try {
            bucketService.validateBucketItems(memberId);
            return ResponseEntity.ok("모든 장바구니 항목이 유효");
        } catch (CustomError e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * http://localhost:8080/bucket/remove/1
     */
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> removeItemFromBucket(@RequestParam Long bucketId) {
        try {
            bucketService.removeItemFromBucket(bucketId);
            return ResponseEntity.ok("장바구니 항목이 삭제되었습니다.");
        } catch (CustomError e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 적절한 에러 메시지 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/removeAll")
    public ResponseEntity<String> removeAllItemsFromBucket(@RequestParam Long memberId) {
        try {
            bucketService.clearBucket(memberId);
            return ResponseEntity.ok("장바구니의 모든 항목이 삭제되었습니다.");
        } catch (CustomError e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 적절한 에러 메시지 반환
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
