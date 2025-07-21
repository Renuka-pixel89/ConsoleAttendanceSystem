package util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    public static String sendOTPAndQR(String toEmail) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        String qrLink = "https://qrcode.fake/" + toEmail;

        final String from = "vemurirenuka4@gmail.com"; // Replace with your Gmail
        final String appPassword = "aquiqjlntunpyzle"; // 16-char app password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, appPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🔐 OTP for Attendance & QR Code");
            message.setText("Your OTP is: " + otp + "\nQR Link: " + qrLink);
            Transport.send(message);
            System.out.println("✅ Email sent to: " + toEmail);
        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email: " + e.getMessage());
        }

        return otp; // Sent back to verify input (never shown on console)
    }
}
