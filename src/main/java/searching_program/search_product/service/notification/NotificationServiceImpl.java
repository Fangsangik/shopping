package searching_program.search_product.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import searching_program.search_product.domain.Item;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final JavaMailSender mailSender;

    @Override
    public void sendLowStockAlert(Item item) {
        String to = "hwangsangik@gmail.com";
        String subject = "Low Stock Alert: " + item.getItemName();
        String body = "The stock for Item" + item.getItemName() + "is Low. Current Stock: " + item.getStock();

        sendEmailNotification(to, subject, body);
    }

    @Override
    public void sendEmailNotification(String to, String subject, String body) {

        //이메일 메시지 내용을 구성, 전송
        MimeMessage message = mailSender.createMimeMessage();

        //이메일을 생성하고 관리하는데 사용되는 MimeMessage를 쉽게 다룰 수 있도록 도와줌
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void sendSmsNotification(String phoneNumber, String message) {
        /**
         * ToDo : SMS API 가져온뒤 사용
         */
    }
}
