# shopping (아직 진행중)

### 프로젝트 이름

**Shopping 프로젝트 (~ing) (개인 프로젝트)**

---

**프로젝트 소개**:

쇼핑몰 백엔드 시스템을 구현한 개인 프로젝트로, 상품 등록, 검색, 주문, 결제, 배송 관리 등 주요 기능을 포함하고 있습니다. 이 프로젝트는 개인이 전반적인 쇼핑몰 백엔드의 기본적인 기능을 이해하고, 다양한 문제를 해결하는 경험을 쌓기 위해 진행되었습니다.

---

- **구현 기술**:
    - Spring Security, Spring Framework, MySQL, JPA, Java

---

- **프로젝트 성과 및 역할**:
    - **개인 프로젝트**: 기획부터 설계, 구현, 테스트까지 모든 단계를 혼자 수행했습니다. 상품 관리, 주문 처리, 결제 시스템, 장바구니, 쿠폰 발급 기능을 포함한 전체적인 백엔드 구조를 개발했습니다.
    - **성과**:
        - Restful API 설계를 통해 Postman을 사용하여 HTTP 테스트를 완료했습니다.
        - 회원 가입 및 상품 등록, 상품 검색, 결제 기능 구현
        - 장바구니, 주문 상태 관리, 할인 쿠폰 발급 기능 완성
        - JPA를 이용한 연관 관계 매핑 및 데이터베이스 설계
    - **프로젝트 진행 중 겪은 문제 및 해결 과정**:
        1. **Service 설계의 DTO 변환 문제**: DTO를 중심으로 설계했지만 연관관계 설정에 어려움이 있었습니다. 이를 해결하기 위해 Converter 클래스를 만들어 엔티티와 DTO 간의 변환을 수월하게 처리할 수 있었습니다.
        2. **PaymentService 테스트 문제**: 결제 관련 모의 테스트에서 여러 차례 오류가 발생하였으며, 해결하기 위해 현재는 실제 결제 API(예: Toss 또는 카카오페이)를 사용해 모의 테스트를 대체할 계획입니다.
        3. **배송 상태 업데이트 문제**: 배송 상태가 제대로 업데이트되지 않는 문제를 발견했고, OrderService에 대한 로직을 수정하여 주문 상태가 제대로 반영되도록 수정했습니다.
        4. LocalDateTime이 String 타입으로 인식 문제가 발생하여, converter interface를 사용해 String 타입으로 인식 할 수 있도록 변경했습니다. 

---

- **추후 보완할 점 및 느낀 점**:
    - **Spring Security 권한 관리**: 사용자(Admin, Member, Market) 별로 구분된 권한을 설정하여 각기 다른 기능에 접근할 수 있도록 개선할 계획입니다.
    - **실제 결제 API 연동**: 현재는 결제 기능을 모의 테스트로 구현했지만, 추후 실제 결제 API(Toss, 카카오페이 등)를 적용하여 상용화 환경에서의 결제를 구현할 예정입니다.
    - **알림 서비스 추가**: JavaMailSender를 사용하여 주문 완료 및 결제 관련 알림 메일을 발송하는 기능을 추가할 계획입니다.
    - **프로젝트 구조 리팩토링**: Converter 클래스의 기능을 더 세분화하여 가독성을 높이고, 코드 유지보수를 용이하게 할 예정입니다.
