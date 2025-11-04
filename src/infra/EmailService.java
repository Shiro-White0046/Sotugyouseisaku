package infra;

/**
 * メール送信処理の共通インターフェース。
 * 実運用では SmtpEmailService に置き換え可能。
 */
public interface EmailService {
    /**
     * メールを送信します。
     *
     * @param to      宛先メールアドレス
     * @param subject 件名
     * @param body    本文
     */
    void send(String to, String subject, String body);
}
