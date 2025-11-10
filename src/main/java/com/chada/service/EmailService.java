package com.chada.service;

import com.chada.dto.ProductImageDTO;
import com.chada.entity.Order;
import com.chada.entity.OrderItem;
import com.chada.entity.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.email:abderrahmane.mimani@gmail.com}")
    private String adminEmail;

    public void sendOrderNotification(Order order) {
        // Enhanced validation and logging for production
        if (fromEmail == null || fromEmail.isEmpty()) {
            logger.error("CRITICAL ERROR: Email from address is not configured! Please set MAIL_USERNAME environment variable in Railway");
            return;
        }
        
        if (adminEmail == null || adminEmail.isEmpty()) {
            logger.error("CRITICAL ERROR: Admin email address is not configured! Please set ADMIN_EMAIL environment variable in Railway");
            return;
        }
        
        // Check if mail sender is configured
        if (mailSender == null) {
            logger.error("CRITICAL ERROR: JavaMailSender is not configured! Please check your email configuration in application.properties");
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
            logger.debug("Skipping customer email - no customer email provided for order #{}", order.getId());
            return;
        }
        
        logger.info("Sending customer confirmation email for order #{} to {}", order.getId(), order.getCustomerEmail());
        
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
            logger.info("Customer confirmation email sent successfully for order #{} to {}", order.getId(), order.getCustomerEmail());
        } catch (MessagingException e) {
            logger.error("Failed to send customer confirmation email for order #{}: {}", order.getId(), e.getMessage(), e);
            
            // Fallback to simple email for customer
            try {
                logger.debug("Attempting to send simple customer email as fallback for order #{}", order.getId());
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
                logger.info("Simple customer email sent successfully for order #{}", order.getId());
            } catch (Exception ex) {
                logger.error("Failed to send simple customer email fallback for order #{}: {}", order.getId(), ex.getMessage(), ex);
            }
        } catch (Exception e) {
            logger.error("Unexpected error while sending customer confirmation email for order #{}: {}", order.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Send notification email to admin
     */
    private void sendAdminNotificationEmail(Order order) {
        logger.info("Sending admin notification email for order #{} to {}", order.getId(), adminEmail);
        
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
            logger.info("Admin notification email sent successfully for order #{} to {}", order.getId(), adminEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send admin notification email for order #{}: {}", order.getId(), e.getMessage(), e);
            
            // Fallback to simple email for admin
            try {
                logger.debug("Attempting to send simple admin email as fallback for order #{}", order.getId());
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
                logger.info("Simple admin email sent successfully for order #{}", order.getId());
            } catch (Exception ex) {
                logger.error("Failed to send simple admin email fallback for order #{}: {}", order.getId(), ex.getMessage(), ex);
                logger.warn("Order #{} was created but admin email notification failed", order.getId());
            }
        } catch (Exception e) {
            logger.error("Unexpected error while sending admin notification email for order #{}: {}", order.getId(), e.getMessage(), e);
            logger.warn("Order #{} was created but admin email notification failed", order.getId());
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
            
            // Parse imageDetails to get stock information
            List<ProductImageDTO> imageDetails = new ArrayList<>();
            int totalStock = 0;
            int minStock = Integer.MAX_VALUE;
            List<String> lowStockImages = new ArrayList<>();
            
            if (product.getImageDetails() != null && !product.getImageDetails().isEmpty()) {
                try {
                    imageDetails = objectMapper.readValue(
                            product.getImageDetails(),
                            new TypeReference<List<ProductImageDTO>>() {}
                    );
                    
                    for (int i = 0; i < imageDetails.size(); i++) {
                        ProductImageDTO img = imageDetails.get(i);
                        Integer quantity = img.getQuantity();
                        if (quantity != null && quantity > 0) {
                            totalStock += quantity;
                            if (quantity < minStock) {
                                minStock = quantity;
                            }
                            if (quantity < 3) {
                                lowStockImages.add("صورة " + (i + 1) + ": " + quantity + " قطعة");
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse imageDetails for low stock alert: {}", e.getMessage());
                }
            }
            
            if (totalStock > 0) {
                emailBody.append("<p style='font-size: 24px; font-weight: bold; color: #ef4444;'>إجمالي المخزون: ").append(totalStock).append(" قطعة</p>");
                if (minStock < Integer.MAX_VALUE && minStock < 3) {
                    emailBody.append("<p style='font-size: 18px; font-weight: bold; color: #dc2626;'>أقل كمية متوفرة: ").append(minStock).append(" قطعة</p>");
                }
            } else {
                emailBody.append("<p style='font-size: 24px; font-weight: bold; color: #ef4444;'>المخزون: 0 قطعة (نفد المخزون)</p>");
            }
            
            if (!lowStockImages.isEmpty()) {
                emailBody.append("<p style='color: #991b1b; margin-top: 10px;'><strong>الصور ذات المخزون المنخفض:</strong></p>");
                emailBody.append("<ul style='color: #991b1b; margin-left: 20px;'>");
                for (String imgInfo : lowStockImages) {
                    emailBody.append("<li>").append(imgInfo).append("</li>");
                }
                emailBody.append("</ul>");
            }
            
            emailBody.append("<p style='color: #991b1b; margin-top: 10px;'>⚠️ المخزون منخفض! يرجى تجديد المخزون في أقرب وقت ممكن.</p>");
            emailBody.append("</div>");

            // Additional information section
            DecimalFormat df = new DecimalFormat("#,##0.00");
            boolean hasAdditionalInfo = false;
            StringBuilder additionalInfo = new StringBuilder();
            additionalInfo.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>");
            additionalInfo.append("<h2 style='color: #1a2f4d; margin-top: 0;'>معلومات إضافية</h2>");
            
            // Show price range from images if available
            if (!imageDetails.isEmpty()) {
                BigDecimal minPrice = null;
                BigDecimal maxPrice = null;
                for (ProductImageDTO img : imageDetails) {
                    if (img.getPrice() != null) {
                        if (minPrice == null || img.getPrice().compareTo(minPrice) < 0) {
                            minPrice = img.getPrice();
                        }
                        if (maxPrice == null || img.getPrice().compareTo(maxPrice) > 0) {
                            maxPrice = img.getPrice();
                        }
                    }
                }
                if (minPrice != null && maxPrice != null) {
                    if (minPrice.compareTo(maxPrice) == 0) {
                        additionalInfo.append("<p><strong>السعر:</strong> ").append(df.format(minPrice)).append(" د.م</p>");
                    } else {
                        additionalInfo.append("<p><strong>نطاق الأسعار:</strong> ").append(df.format(minPrice))
                                .append(" - ").append(df.format(maxPrice)).append(" د.م</p>");
                    }
                    hasAdditionalInfo = true;
                }
            }
            
            if (product.getFragrance() != null && !product.getFragrance().isEmpty()) {
                additionalInfo.append("<p><strong>العطر:</strong> ").append(product.getFragrance()).append("</p>");
                hasAdditionalInfo = true;
            }
            if (product.getVolume() != null) {
                additionalInfo.append("<p><strong>الحجم:</strong> ").append(product.getVolume()).append(" ml</p>");
                hasAdditionalInfo = true;
            }
            
            additionalInfo.append("</div>");
            
            if (hasAdditionalInfo) {
                emailBody.append(additionalInfo);
            }

            emailBody.append("<div style='background-color: #d4af37; color: white; padding: 20px; border-radius: 8px; text-align: center;'>");
            emailBody.append("<p style='margin: 0; font-size: 16px;'>يرجى اتخاذ الإجراءات اللازمة لتجديد المخزون</p>");
            emailBody.append("</div>");

            emailBody.append("</div>");
            emailBody.append("</body>");
            emailBody.append("</html>");

            helper.setText(emailBody.toString(), true);
            mailSender.send(message);
            logger.info("Low stock alert email sent for product: {} (Total Stock: {})", product.getName(), totalStock);
        } catch (MessagingException e) {
            logger.error("Failed to send low stock alert email for product {}: {}", product.getName(), e.getMessage(), e);
            // Fallback to simple email
            try {
                SimpleMailMessage simpleMessage = new SimpleMailMessage();
                simpleMessage.setFrom(fromEmail);
                simpleMessage.setTo(adminEmail);
                simpleMessage.setSubject("تنبيه: نقص في المخزون - " + product.getName());
                // Parse imageDetails for simple email
                int simpleTotalStock = 0;
                int simpleMinStock = Integer.MAX_VALUE;
                if (product.getImageDetails() != null && !product.getImageDetails().isEmpty()) {
                    try {
                        List<ProductImageDTO> imageDetails = objectMapper.readValue(
                                product.getImageDetails(),
                                new TypeReference<List<ProductImageDTO>>() {}
                        );
                        for (ProductImageDTO img : imageDetails) {
                            Integer quantity = img.getQuantity();
                            if (quantity != null && quantity > 0) {
                                simpleTotalStock += quantity;
                                if (quantity < simpleMinStock) {
                                    simpleMinStock = quantity;
                                }
                            }
                        }
                    } catch (Exception parseEx) {
                        logger.warn("Failed to parse imageDetails for simple email: {}", parseEx.getMessage());
                    }
                }
                
                String stockInfo = simpleTotalStock > 0 
                    ? (simpleMinStock < Integer.MAX_VALUE && simpleMinStock < 3 
                        ? "إجمالي: " + simpleTotalStock + " قطعة (أقل كمية: " + simpleMinStock + ")" 
                        : "إجمالي: " + simpleTotalStock + " قطعة")
                    : "0 قطعة (نفد المخزون)";
                
                simpleMessage.setText("تنبيه: نقص في المخزون\n\n" +
                    "اسم المنتج: " + product.getName() + "\n" +
                    "رقم المنتج: #" + product.getId() + "\n" +
                    "المخزون: " + stockInfo + "\n\n" +
                    "⚠️ المخزون منخفض! يرجى تجديد المخزون في أقرب وقت ممكن.");
                mailSender.send(simpleMessage);
                logger.info("Low stock alert email (simple) sent for product: {}", product.getName());
            } catch (Exception ex) {
                logger.error("Failed to send simple low stock alert email for product {}: {}", product.getName(), ex.getMessage(), ex);
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

