package com.chada.service;

import com.chada.entity.Order;
import com.chada.entity.OrderItem;
import com.chada.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.email:abderrahmane.mimani@gmail.com}")
    private String adminEmail;

    public void sendOrderNotification(Order order) {
        // Enhanced validation and logging for production
        if (fromEmail == null || fromEmail.isEmpty()) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR: Email from address is not configured!");
            System.err.println("Please set MAIL_USERNAME environment variable in Railway");
            System.err.println("Current value: " + fromEmail);
            System.err.println("Environment variable MAIL_USERNAME: " + System.getenv("MAIL_USERNAME"));
            System.err.println("========================================");
            return;
        }
        
        if (adminEmail == null || adminEmail.isEmpty()) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR: Admin email address is not configured!");
            System.err.println("Please set ADMIN_EMAIL environment variable in Railway");
            System.err.println("Current value: " + adminEmail);
            System.err.println("Environment variable ADMIN_EMAIL: " + System.getenv("ADMIN_EMAIL"));
            System.err.println("========================================");
            return;
        }
        
        // Check if mail sender is configured
        if (mailSender == null) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR: JavaMailSender is not configured!");
            System.err.println("Please check your email configuration in application.properties");
            System.err.println("========================================");
            return;
        }
        
        // Send email to customer (confirmation)
        sendCustomerConfirmationEmail(order);
        
        // Send email to admin (notification)
        sendAdminNotificationEmail(order);
    }
    
    /**
     * Send confirmation email to customer
     */
    private void sendCustomerConfirmationEmail(Order order) {
        if (order.getCustomerEmail() == null || order.getCustomerEmail().isEmpty()) {
            System.out.println("Skipping customer email - no customer email provided for order #" + order.getId());
            return;
        }
        
        System.out.println("========================================");
        System.out.println("SENDING CUSTOMER CONFIRMATION EMAIL - Order #" + order.getId());
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + order.getCustomerEmail());
        System.out.println("========================================");
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("تأكيد الطلب - Order #" + order.getId());

            // Build email body
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("<!DOCTYPE html>");
            emailBody.append("<html dir='rtl' lang='ar'>");
            emailBody.append("<head><meta charset='UTF-8'></head>");
            emailBody.append("<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>");
            emailBody.append("<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>");
            
            emailBody.append("<h1 style='color: #1a2f4d; text-align: center; margin-bottom: 30px;'>شكراً لك على طلبك!</h1>");
            emailBody.append("<p style='text-align: center; color: #666; font-size: 16px; margin-bottom: 30px;'>تم استلام طلبك بنجاح وسنتواصل معك قريباً</p>");
            
            emailBody.append("<div style='background-color: #f0f9ff; padding: 20px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #1a2f4d;'>");
            emailBody.append("<h2 style='color: #1a2f4d; margin-top: 0;'>معلومات الطلب</h2>");
            emailBody.append("<p><strong>رقم الطلب:</strong> #").append(order.getId()).append("</p>");
            emailBody.append("<p><strong>التاريخ:</strong> ").append(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("</p>");
            emailBody.append("<p><strong>الحالة:</strong> قيد المعالجة</p>");
            emailBody.append("</div>");

            emailBody.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
            emailBody.append("<h2 style='color: #d4af37; margin-top: 0;'>المنتجات</h2>");
            emailBody.append("<table style='width: 100%; border-collapse: collapse;'>");
            emailBody.append("<thead><tr style='background-color: #1a2f4d; color: white;'>");
            emailBody.append("<th style='padding: 10px; text-align: right;'>المنتج</th>");
            emailBody.append("<th style='padding: 10px; text-align: center;'>الكمية</th>");
            emailBody.append("<th style='padding: 10px; text-align: left;'>السعر</th>");
            emailBody.append("<th style='padding: 10px; text-align: left;'>الإجمالي</th>");
            emailBody.append("</tr></thead>");
            emailBody.append("<tbody>");

            DecimalFormat df = new DecimalFormat("#,##0.00");
            for (OrderItem item : order.getItems()) {
                emailBody.append("<tr style='border-bottom: 1px solid #eee;'>");
                emailBody.append("<td style='padding: 10px;'>").append(item.getProduct().getName()).append("</td>");
                emailBody.append("<td style='padding: 10px; text-align: center;'>").append(item.getQuantity()).append("</td>");
                emailBody.append("<td style='padding: 10px;'>").append(df.format(item.getPrice())).append(" د.م</td>");
                emailBody.append("<td style='padding: 10px;'>").append(df.format(item.getSubtotal())).append(" د.م</td>");
                emailBody.append("</tr>");
            }

            emailBody.append("</tbody>");
            emailBody.append("</table>");
            emailBody.append("</div>");

            emailBody.append("<div style='background-color: #d4af37; color: white; padding: 20px; border-radius: 8px; text-align: center;'>");
            emailBody.append("<h2 style='margin: 0; font-size: 24px;'>الإجمالي: ").append(df.format(order.getTotalAmount())).append(" د.م</h2>");
            emailBody.append("</div>");
            
            emailBody.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-top: 20px; text-align: center;'>");
            emailBody.append("<p style='margin: 0; color: #666;'>سنقوم بالاتصال بك قريباً لتأكيد الطلب</p>");
            emailBody.append("<p style='margin: 10px 0 0 0; color: #666;'>شكراً لاختيارك عطور الشدا</p>");
            emailBody.append("</div>");

            emailBody.append("</div>");
            emailBody.append("</body>");
            emailBody.append("</html>");

            helper.setText(emailBody.toString(), true);
            mailSender.send(message);
            System.out.println("========================================");
            System.out.println("SUCCESS: Customer confirmation email sent successfully!");
            System.out.println("Order #" + order.getId() + " to " + order.getCustomerEmail());
            System.out.println("========================================");
        } catch (MessagingException e) {
            System.err.println("========================================");
            System.err.println("ERROR: Failed to send customer confirmation email for order #" + order.getId());
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                System.err.println("Cause Type: " + e.getCause().getClass().getName());
            }
            System.err.println("Stack Trace:");
            e.printStackTrace();
            System.err.println("========================================");
            
            // Fallback to simple email for customer
            try {
                System.out.println("Attempting to send simple customer email as fallback...");
                SimpleMailMessage simpleMessage = new SimpleMailMessage();
                simpleMessage.setFrom(fromEmail);
                simpleMessage.setTo(order.getCustomerEmail());
                simpleMessage.setSubject("تأكيد الطلب - Order #" + order.getId());
                simpleMessage.setText("شكراً لك على طلبك!\n\n" +
                    "رقم الطلب: #" + order.getId() + 
                    "\nالتاريخ: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    "\nالإجمالي: " + order.getTotalAmount() + " د.م\n\n" +
                    "سنقوم بالاتصال بك قريباً لتأكيد الطلب.\nشكراً لاختيارك عطور الشدا");
                mailSender.send(simpleMessage);
                System.out.println("SUCCESS: Simple customer email sent successfully for order #" + order.getId());
            } catch (Exception ex) {
                System.err.println("Failed to send simple customer email fallback: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("CRITICAL: Unexpected error while sending customer confirmation email!");
            System.err.println("Order #" + order.getId());
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            System.err.println("========================================");
            e.printStackTrace();
        }
    }
    
    /**
     * Send notification email to admin
     */
    private void sendAdminNotificationEmail(Order order) {
        System.out.println("========================================");
        System.out.println("SENDING ADMIN NOTIFICATION EMAIL - Order #" + order.getId());
        System.out.println("From: " + fromEmail);
        System.out.println("To: " + adminEmail);
        System.out.println("Mail Host: " + System.getenv().getOrDefault("MAIL_HOST", "smtp.gmail.com"));
        System.out.println("Mail Port: " + System.getenv().getOrDefault("MAIL_PORT", "587"));
        System.out.println("Environment: " + (System.getenv("RAILWAY_ENVIRONMENT") != null ? "Railway (Production)" : "Local"));
        System.out.println("========================================");
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("طلب جديد - Order #" + order.getId());

            // Build admin notification email body
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("<!DOCTYPE html>");
            emailBody.append("<html dir='rtl' lang='ar'>");
            emailBody.append("<head><meta charset='UTF-8'></head>");
            emailBody.append("<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>");
            emailBody.append("<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>");
            
            emailBody.append("<h1 style='color: #1a2f4d; text-align: center; margin-bottom: 30px;'>طلب جديد</h1>");
            
            emailBody.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
            emailBody.append("<h2 style='color: #d4af37; margin-top: 0;'>معلومات الطلب</h2>");
            emailBody.append("<p><strong>رقم الطلب:</strong> #").append(order.getId()).append("</p>");
            emailBody.append("<p><strong>التاريخ:</strong> ").append(order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("</p>");
            emailBody.append("<p><strong>الحالة:</strong> ").append(order.getStatus().name()).append("</p>");
            emailBody.append("</div>");

            emailBody.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
            emailBody.append("<h2 style='color: #d4af37; margin-top: 0;'>معلومات العميل</h2>");
            emailBody.append("<p><strong>الاسم:</strong> ").append(order.getCustomerName()).append("</p>");
            emailBody.append("<p><strong>البريد الإلكتروني:</strong> ").append(order.getCustomerEmail()).append("</p>");
            emailBody.append("<p><strong>رقم الهاتف:</strong> ").append(order.getCustomerPhone()).append("</p>");
            if (order.getCustomerAddress() != null && !order.getCustomerAddress().isEmpty()) {
                emailBody.append("<p><strong>العنوان:</strong> ").append(order.getCustomerAddress()).append("</p>");
            }
            emailBody.append("</div>");

            emailBody.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
            emailBody.append("<h2 style='color: #d4af37; margin-top: 0;'>المنتجات</h2>");
            emailBody.append("<table style='width: 100%; border-collapse: collapse;'>");
            emailBody.append("<thead><tr style='background-color: #1a2f4d; color: white;'>");
            emailBody.append("<th style='padding: 10px; text-align: right;'>المنتج</th>");
            emailBody.append("<th style='padding: 10px; text-align: center;'>الكمية</th>");
            emailBody.append("<th style='padding: 10px; text-align: left;'>السعر</th>");
            emailBody.append("<th style='padding: 10px; text-align: left;'>الإجمالي</th>");
            emailBody.append("</tr></thead>");
            emailBody.append("<tbody>");

            DecimalFormat df = new DecimalFormat("#,##0.00");
            for (OrderItem item : order.getItems()) {
                emailBody.append("<tr style='border-bottom: 1px solid #eee;'>");
                emailBody.append("<td style='padding: 10px;'>").append(item.getProduct().getName()).append("</td>");
                emailBody.append("<td style='padding: 10px; text-align: center;'>").append(item.getQuantity()).append("</td>");
                emailBody.append("<td style='padding: 10px;'>").append(df.format(item.getPrice())).append(" د.م</td>");
                emailBody.append("<td style='padding: 10px;'>").append(df.format(item.getSubtotal())).append(" د.م</td>");
                emailBody.append("</tr>");
            }

            emailBody.append("</tbody>");
            emailBody.append("</table>");
            emailBody.append("</div>");

            emailBody.append("<div style='background-color: #d4af37; color: white; padding: 20px; border-radius: 8px; text-align: center;'>");
            emailBody.append("<h2 style='margin: 0; font-size: 24px;'>الإجمالي: ").append(df.format(order.getTotalAmount())).append(" د.م</h2>");
            emailBody.append("</div>");

            emailBody.append("</div>");
            emailBody.append("</body>");
            emailBody.append("</html>");

            helper.setText(emailBody.toString(), true);
            mailSender.send(message);
            System.out.println("========================================");
            System.out.println("SUCCESS: Admin notification email sent successfully!");
            System.out.println("Order #" + order.getId() + " to " + adminEmail);
            System.out.println("========================================");
        } catch (MessagingException e) {
            System.err.println("========================================");
            System.err.println("ERROR: Failed to send admin notification email for order #" + order.getId());
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
                System.err.println("Cause Type: " + e.getCause().getClass().getName());
            }
            System.err.println("Stack Trace:");
            e.printStackTrace();
            System.err.println("========================================");
            
            // Fallback to simple email for admin
            try {
                System.out.println("Attempting to send simple admin email as fallback...");
                SimpleMailMessage simpleMessage = new SimpleMailMessage();
                simpleMessage.setFrom(fromEmail);
                simpleMessage.setTo(adminEmail);
                simpleMessage.setSubject("طلب جديد - Order #" + order.getId());
                simpleMessage.setText("تم استلام طلب جديد\n\nرقم الطلب: " + order.getId() + 
                    "\nالعميل: " + order.getCustomerName() + 
                    "\nالبريد: " + order.getCustomerEmail() + 
                    "\nالهاتف: " + order.getCustomerPhone() + 
                    "\nالإجمالي: " + order.getTotalAmount() + " د.م");
                mailSender.send(simpleMessage);
                System.out.println("SUCCESS: Simple admin email sent successfully for order #" + order.getId());
            } catch (Exception ex) {
                System.err.println("========================================");
                System.err.println("CRITICAL: Failed to send simple admin email fallback!");
                System.err.println("Order #" + order.getId());
                System.err.println("Error Type: " + ex.getClass().getName());
                System.err.println("Error Message: " + ex.getMessage());
                if (ex.getCause() != null) {
                    System.err.println("Cause: " + ex.getCause().getMessage());
                    System.err.println("Cause Type: " + ex.getCause().getClass().getName());
                }
                System.err.println("Stack Trace:");
                ex.printStackTrace();
                System.err.println("========================================");
                
                // Additional diagnostics for production
                System.err.println("DIAGNOSTICS:");
                System.err.println("- MAIL_HOST: " + System.getenv("MAIL_HOST"));
                System.err.println("- MAIL_PORT: " + System.getenv("MAIL_PORT"));
                System.err.println("- MAIL_USERNAME set: " + (System.getenv("MAIL_USERNAME") != null));
                System.err.println("- MAIL_PASSWORD set: " + (System.getenv("MAIL_PASSWORD") != null));
                System.err.println("- ADMIN_EMAIL set: " + (System.getenv("ADMIN_EMAIL") != null));
                System.err.println("========================================");
                
                // Don't throw - just log the error so order creation doesn't fail
                System.err.println("WARNING: Order was created but admin email notification failed!");
            }
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("CRITICAL: Unexpected error while sending admin notification email!");
            System.err.println("Order #" + order.getId());
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Error Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            System.err.println("========================================");
            e.printStackTrace();
            // Don't throw - just log the error so order creation doesn't fail
            System.err.println("WARNING: Order was created but admin email notification failed!");
        }
    }
    
    /**
     * Send low stock alert email when product stock is less than 3
     */
    public void sendLowStockAlert(Product product) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("تنبيه: نقص في المخزون - " + product.getName());

            // Build email body
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("<!DOCTYPE html>");
            emailBody.append("<html dir='rtl' lang='ar'>");
            emailBody.append("<head><meta charset='UTF-8'></head>");
            emailBody.append("<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;'>");
            emailBody.append("<div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>");
            
            emailBody.append("<h1 style='color: #f97316; text-align: center; margin-bottom: 30px;'>⚠️ تنبيه: نقص في المخزون</h1>");
            
            emailBody.append("<div style='background-color: #fff7ed; padding: 20px; border-radius: 8px; border-left: 5px solid #f97316; margin-bottom: 20px;'>");
            emailBody.append("<h2 style='color: #f97316; margin-top: 0;'>معلومات المنتج</h2>");
            emailBody.append("<p><strong>اسم المنتج:</strong> ").append(product.getName()).append("</p>");
            emailBody.append("<p><strong>رقم المنتج:</strong> #").append(product.getId()).append("</p>");
            if (product.getCategory() != null) {
                emailBody.append("<p><strong>الفئة:</strong> ").append(product.getCategory().getName()).append("</p>");
            }
            emailBody.append("</div>");

            emailBody.append("<div style='background-color: #fef2f2; padding: 20px; border-radius: 8px; border-left: 5px solid #ef4444; margin-bottom: 20px;'>");
            emailBody.append("<h2 style='color: #ef4444; margin-top: 0;'>حالة المخزون</h2>");
            emailBody.append("<p style='font-size: 24px; font-weight: bold; color: #ef4444;'>المخزون المتبقي: ").append(product.getStock()).append(" قطعة</p>");
            emailBody.append("<p style='color: #991b1b;'>⚠️ المخزون منخفض! يرجى تجديد المخزون في أقرب وقت ممكن.</p>");
            emailBody.append("</div>");

            if (product.getPrice() != null) {
                emailBody.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
                emailBody.append("<h2 style='color: #1a2f4d; margin-top: 0;'>معلومات إضافية</h2>");
                DecimalFormat df = new DecimalFormat("#,##0.00");
                emailBody.append("<p><strong>السعر:</strong> ").append(df.format(product.getPrice())).append(" د.م</p>");
                if (product.getFragrance() != null && !product.getFragrance().isEmpty()) {
                    emailBody.append("<p><strong>العطر:</strong> ").append(product.getFragrance()).append("</p>");
                }
                if (product.getVolume() != null) {
                    emailBody.append("<p><strong>الحجم:</strong> ").append(product.getVolume()).append(" ml</p>");
                }
                emailBody.append("</div>");
            }

            emailBody.append("<div style='background-color: #d4af37; color: white; padding: 20px; border-radius: 8px; text-align: center;'>");
            emailBody.append("<p style='margin: 0; font-size: 16px;'>يرجى اتخاذ الإجراءات اللازمة لتجديد المخزون</p>");
            emailBody.append("</div>");

            emailBody.append("</div>");
            emailBody.append("</body>");
            emailBody.append("</html>");

            helper.setText(emailBody.toString(), true);
            mailSender.send(message);
            System.out.println("Low stock alert email sent for product: " + product.getName() + " (Stock: " + product.getStock() + ")");
        } catch (MessagingException e) {
            e.printStackTrace();
            // Fallback to simple email
            try {
                SimpleMailMessage simpleMessage = new SimpleMailMessage();
                simpleMessage.setFrom(fromEmail);
                simpleMessage.setTo(adminEmail);
                simpleMessage.setSubject("تنبيه: نقص في المخزون - " + product.getName());
                simpleMessage.setText("تنبيه: نقص في المخزون\n\n" +
                    "اسم المنتج: " + product.getName() + "\n" +
                    "رقم المنتج: #" + product.getId() + "\n" +
                    "المخزون المتبقي: " + product.getStock() + " قطعة\n\n" +
                    "⚠️ المخزون منخفض! يرجى تجديد المخزون في أقرب وقت ممكن.");
                mailSender.send(simpleMessage);
                System.out.println("Low stock alert email (simple) sent for product: " + product.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Send low stock alert for multiple products
     */
    public void sendLowStockAlerts(List<Product> products) {
        for (Product product : products) {
            sendLowStockAlert(product);
        }
    }
}

