package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.domain.Item;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> findByItemName
            (@RequestParam String itemName) {
        List<ItemDto> byItemName = itemService.findByItemName(itemName);
        return ResponseEntity.ok(byItemName);
    }

    /**
     http://localhost:8080/items/search/multipleName?itemName1=airpods&itemName2=mac
     */
    @GetMapping("/search/multipleName")
    public ResponseEntity<List<ItemDto>> findByItemNames
            (@RequestParam String itemName1, @RequestParam String itemName2) {
        log.debug("Received itemName1: {}", itemName1);
        log.debug("Received itemName2: {}", itemName2);

        List<ItemDto> byItemNames = itemService.findByItemNames(itemName1, itemName2);
        return ResponseEntity.ok(byItemNames);
    }

    /**
     *http://localhost:8080/items/search/multiple?itemName=airpods&minPrice=100&maxPrice=1000&categoryName=전자기기&pageNumber=0&pageSize=10
     */
    @GetMapping("/search/multiple")
    public ResponseEntity<Page<Item>> multipleSearch
            (@RequestParam(required = false) String itemName,
             @RequestParam(required = false, defaultValue = "0") int minPrice,
             @RequestParam(required = false, defaultValue = "1000000") int maxPrice,
             @RequestParam(required = false) String categoryName,
             @RequestParam(defaultValue = "0") int pageNumber,
             @RequestParam(defaultValue = "10") int pageSize) {
        Page<Item> items = itemService.searchItems(itemName, minPrice, maxPrice, categoryName, pageNumber, pageSize);
        return ResponseEntity.ok(items);
    }

    /**
     *category 만들고 만들어야 합니다
     */
    @PostMapping("/create")
    public ResponseEntity<ItemDto> create
            (@RequestBody ItemDto itemDto) {
        ItemDto item = itemService.createItem(itemDto);
        return ResponseEntity.ok(item);
    }

    /**
     *http://localhost:8080/items/search/itemPrice?price=10000
     */
    @GetMapping("/search/itemPrice")
    public ResponseEntity<Page<ItemDto>> findByPrice
            (@RequestParam(required = false) Integer price,
             @RequestParam(defaultValue = "0") int pageNumber,
             @RequestParam(defaultValue = "5") int pageSize){

        Page<ItemDto> byItemPrice = itemService.findByItemPrice(price, pageNumber, pageSize);
        return ResponseEntity.ok(byItemPrice);
    }

    @GetMapping("/search/itemPriceRange")
    public ResponseEntity<Page<ItemDto>> findPriceRange(
            @RequestParam(required = false, defaultValue = "0") int minPrice,
            @RequestParam(required = false, defaultValue = "1000000") int maxPrice,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {
        Page<ItemDto> byItemPriceRange = itemService.findByItemPriceRange(minPrice, maxPrice, pageNumber, pageSize);
        return ResponseEntity.ok(byItemPriceRange);
    }

    @PostMapping("/notify")
    public ResponseEntity<?> notification
            (@RequestParam(defaultValue = "10") int stock) {
        try {
            itemService.checkAndNotification(stock);
            return ResponseEntity.ok("재고 알람이 성공적으로 처리 되었습니다");
        } catch (Exception e) {
            log.error("재고 알람 처리중 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알림 처리 중 오류가 발생했습니다.");
        }
    }
}
