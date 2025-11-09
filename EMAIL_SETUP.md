# Email Setup Guide

This guide will help you configure email notifications for the Chada Perfume application.

## Gmail Setup - Quick Guide

### Step 1: Enable 2-Factor Authentication
1. Go to: https://myaccount.google.com/security
2. Click on "2-Step Verification" under "How you sign in to Google"
3. Click "Get Started" and follow the steps
4. You'll need to verify with your phone number

### Step 2: Generate App Password
1. Go to: https://myaccount.google.com/apppasswords
   - Or: Security → 2-Step Verification → App passwords
2. Select:
   - App: "Mail"
   - Device: "Other (Custom name)"
   - Name: "Chada Perfume" (or any name you like)
3. Click "Generate"
4. **Copy the 16-character password immediately!** (You won't be able to see it again)
   - Example: `abcd efgh ijkl mnop` or `abcdefghijklmnop`
   - You can use it with or without spaces

### Step 3: Update application.properties
Open `src/main/resources/application.properties` and update:

```properties
# Replace with your Gmail address
spring.mail.username=your-email@gmail.com

# Replace with the 16-character app password from Step 2
spring.mail.password=abcdefghijklmnop
```

**Complete Example:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=khalidmimani@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
admin.email=khalidmimani@gmail.com
```

**Important Notes:**
- ✅ Use your **actual Gmail address** (the one you use to sign in)
- ✅ Use the **App Password** (16 characters), NOT your regular Gmail password
- ✅ The `admin.email` is where order notifications will be sent
- ✅ Keep your App Password secret and don't share it

### Step 4: Test Email Configuration
1. Start the Spring Boot application
2. Create a test order through the API or frontend
3. Check the admin email inbox for the order notification

## Troubleshooting

### Email Not Sending
- Verify that 2-Factor Authentication is enabled
- Ensure you're using an App Password, not your regular Gmail password
- Check that the email and password in `application.properties` are correct
- Verify that port 587 is not blocked by your firewall

### Gmail Security Warnings
- If you see security warnings, make sure you're using an App Password
- Check that "Less secure app access" is not required (App Passwords are more secure)

## Alternative Email Providers

If you prefer to use a different email provider, update the SMTP settings accordingly:

### Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
```

### Yahoo
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
```

### Custom SMTP Server
Update the `spring.mail.host` and `spring.mail.port` to match your SMTP server configuration.

