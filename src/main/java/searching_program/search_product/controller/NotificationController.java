package searching_program.search_product.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searching_program.search_product.domain.Item;
import searching_program.search_product.dto.DtoEntityConverter;
import searching_program.search_product.dto.ItemDto;
import searching_program.search_product.error.CustomError;
import searching_program.search_product.service.ItemService;
import searching_program.search_product.service.notification.NotificationServiceImpl;

@Slf4j
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final ItemService itemService;
    private final NotificationServiceImpl notificationService;  // 인터페이스 타입으로 변경
    private final DtoEntityConverter converter;

    @PostMapping("/sendLowStockAlert")
    public ResponseEntity<String> sendLowStockAlert(@RequestParam Long itemId) {
        try {
            // ItemService를 사용하여 아이템을 조회
            ItemDto itemDto = itemService.findById(itemId);
            Item item = converter.convertToItemEntity(itemDto); // ItemDto를 Item으로 변환
            notificationService.sendLowStockAlert(item);
            log.info("저재고 알림 이메일 전송 성공: 아이템 ID = {}", itemId);
            return ResponseEntity.ok("저재고 알림 이메일이 성공적으로 전송되었습니다.");
        } catch (CustomError e) {
            log.error("저재고 알림 이메일 전송 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("저재고 알림 이메일 전송 중 예기치 않은 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("저재고 알림 이메일 전송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 일반 이메일 알림을 보내는 엔드포인트
     *
     * @param to 이메일 수신자 주소
     * @param subject 이메일 제목
     * @param body 이메일 본문
     * @return ResponseEntity<String>
     */
    @PostMapping("/sendEmail")
    public ResponseEntity<String> sendEmailNotification(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {
        try {
            notificationService.sendEmailNotification(to, subject, body);
            log.info("이메일 전송 성공: 수신자 = {}, 제목 = {}", to, subject);
            return ResponseEntity.ok("이메일이 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("이메일 전송 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 전송 중 오류가 발생했습니다.");
        }
    }

    /**
     * SMS 알림을 보내는 엔드포인트
     *
     * @param phoneNumber 수신자 전화번호
     * @param message SMS 메시지
     * @return ResponseEntity<String>
     */
    @PostMapping("/sendSms")
    public ResponseEntity<String> sendSmsNotification(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        try {
            notificationService.sendSmsNotification(phoneNumber, message);
            log.info("SMS 전송 성공: 수신자 번호 = {}, 메시지 = {}", phoneNumber, message);
            return ResponseEntity.ok("SMS가 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("SMS 전송 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("SMS 전송 중 오류가 발생했습니다.");
        }
    }
}

