# Railway Production Email Setup Guide

This guide will help you configure email notifications for your Chada Perfume application deployed on Railway.

## Step 1: Get Gmail App Password

1. Go to: https://myaccount.google.com/security
2. Enable **2-Step Verification** if not already enabled
3. Go to: https://myaccount.google.com/apppasswords
4. Select:
   - App: **Mail**
   - Device: **Other (Custom name)**
   - Name: **Chada Perfume Railway**
5. Click **Generate**
6. **Copy the 16-character password** (remove spaces if any)
   - Example: `abcd efgh ijkl mnop` → use as `abcdefghijklmnop` or keep spaces

## Step 2: Set Environment Variables in Railway

1. Go to your Railway project dashboard
2. Select your **chadaBack** service
3. Go to **Variables** tab
4. Add the following environment variables:

### Required Variables:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-16-char-app-password
ADMIN_EMAIL=your-email@gmail.com
```

### Example:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=khalidmimani@gmail.com
MAIL_PASSWORD=gwkdneycvkwomrqv
ADMIN_EMAIL=khalidmimani@gmail.com
```

## Step 3: Important Notes

### ⚠️ Password Format
- **Remove spaces** from the app password when setting in Railway
- Example: `gwkd neyc vkwo mrqv` → `gwkdneycvkwomrqv`
- Or you can keep spaces, both work

### ✅ Verification
- After setting variables, **redeploy** your service
- Check Railway logs for email configuration messages
- Look for: `EMAIL SEND ATTEMPT` in logs

### 🔍 Troubleshooting

#### Email not sending?
1. Check Railway logs for error messages
2. Verify all environment variables are set correctly
3. Ensure Gmail app password is valid (not expired)
4. Check that 2-Step Verification is enabled on Gmail

#### Common Errors:

**"Authentication failed"**
- Verify `MAIL_PASSWORD` is the app password (16 chars), not your Gmail password
- Check for extra spaces or special characters

**"Connection timeout"**
- Railway might be blocking SMTP port 587
- Try port 465 with SSL instead (update `MAIL_PORT=465` and add SSL config)

**"Email from address not configured"**
- Verify `MAIL_USERNAME` environment variable is set in Railway
- Check variable name spelling (case-sensitive)

## Step 4: Test Email Configuration

1. Create a test order through your website
2. Check Railway logs for:
   ```
   EMAIL SEND ATTEMPT - Order #X
   SUCCESS: Email notification sent successfully!
   ```
3. Check your email inbox (and spam folder)

## Step 5: Monitor Email Sending

Check Railway logs regularly for:
- `SUCCESS: Email notification sent successfully!` ✅
- `ERROR: Failed to send...` ❌ (check error details)

## Alternative: Use Railway's Email Service

If Gmail continues to have issues, consider using:
- **SendGrid** (free tier available)
- **Mailgun** (free tier available)
- **AWS SES** (very cheap)

These services are more reliable for production and have better deliverability.

---

**Need Help?** Check Railway logs first - they contain detailed error messages about email configuration issues.

