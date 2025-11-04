package infra;

/**
 * ダミーのメール送信クラス。
 * 実際にはメールを送信せず、内容をコンソールに出力します。
 */
public class DummyEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String body) {
        System.out.println("=== DummyEmailService ===");
        System.out.println("宛先: " + to);
        System.out.println("件名: " + subject);
        System.out.println("本文:\n" + body);
        System.out.println("==========================");
    }
}
