package searching_program.search_product.service.notification;

import searching_program.search_product.domain.Item;

public interface NotificationService {

    //재고 부족한 항목시 알람 전송
    void sendLowStockAlert(Item item);

    //to(수신자 이메일 주소), subject(이메일 제목), body(이메일 본문)
    void sendEmailNotification (String to, String subject, String body);

    void sendSmsNotification(String phoneNumber, String message);
}
