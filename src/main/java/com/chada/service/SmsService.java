package com.chada.service;

import com.chada.entity.Order;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    
    @Value("${sms.api.url:}")
    private String smsApiUrl;
    
    @Value("${sms.api.key:}")
    private String smsApiKey;
    
    @Value("${sms.phone.number:212656668002}")
    private String smsPhoneNumber;

    private final RestTemplate restTemplate;

    public void sendOrderNotification(Order order) {
        try {
            String message = buildOrderMessage(order);
            sendSms(smsPhoneNumber, message);
        } catch (Exception e) {
            logger.error("Failed to send SMS notification for order #{}: {}", order.getId(), e.getMessage(), e);
        }
    }

    private String buildOrderMessage(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("طلب جديد - عطور الشدا\n");
        sb.append("رقم الطلب: #").append(order.getId()).append("\n");
        sb.append("العميل: ").append(order.getCustomerName()).append("\n");
        sb.append("الهاتف: ").append(order.getCustomerPhone()).append("\n");
        sb.append("البريد: ").append(order.getCustomerEmail()).append("\n");
        sb.append("المبلغ الإجمالي: ").append(order.getTotalAmount()).append(" د.م\n");
        sb.append("عدد المنتجات: ").append(order.getItems().size());
        return sb.toString();
    }

    private void sendSms(String phoneNumber, String message) {
        // Using a generic HTTP SMS API
        // You can configure this to use any SMS gateway service
        // Example: Twilio, Nexmo, or a local SMS gateway
        
        if (smsApiUrl == null || smsApiUrl.isEmpty()) {
            // If no SMS API is configured, log the message
            logger.debug("SMS would be sent to {}: {}", phoneNumber, message);
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (smsApiKey != null && !smsApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + smsApiKey);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("to", phoneNumber);
            requestBody.put("message", message);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(smsApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("SMS sent successfully to {}", phoneNumber);
            } else {
                logger.error("Failed to send SMS to {}: {}", phoneNumber, response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
        }
    }
}
