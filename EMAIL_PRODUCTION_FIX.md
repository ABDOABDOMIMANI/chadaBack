# Email Production Fix Guide

## Problem
Email works locally but not in production (Railway).

## Common Issues in Production

### 1. Environment Variables Not Set
**Solution:** Make sure all environment variables are set in Railway:
- `MAIL_HOST=smtp.gmail.com`
- `MAIL_PORT=587` (or `465` for SSL)
- `MAIL_USERNAME=your-email@gmail.com`
- `MAIL_PASSWORD=your-app-password` (16 characters, no spaces)
- `ADMIN_EMAIL=your-email@gmail.com`

### 2. Port 587 Blocked
**Solution:** Railway might block port 587. Try port 465 with SSL:
- Set `MAIL_PORT=465`
- Set `MAIL_USE_SSL=true`

### 3. Connection Timeouts
**Solution:** Increased timeouts in `application.properties`:
- Connection timeout: 30 seconds (was 10)
- Read timeout: 30 seconds (was 10)
- Write timeout: 30 seconds (was 10)

### 4. Gmail App Password Issues
**Solution:** 
1. Make sure 2-Step Verification is enabled
2. Generate a new App Password: https://myaccount.google.com/apppasswords
3. Remove spaces from the password when setting in Railway
4. Make sure the password is exactly 16 characters

### 5. SSL/TLS Configuration
**Solution:** The configuration now supports both:
- **Port 587 with STARTTLS** (default)
- **Port 465 with SSL** (set `MAIL_USE_SSL=true`)

## Step-by-Step Fix

### Step 1: Check Railway Environment Variables

1. Go to Railway dashboard
2. Select your `chadaBack` service
3. Go to **Variables** tab
4. Verify these variables are set:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=khalidmimani@gmail.com
MAIL_PASSWORD=gwkdneycvkwomrqv
ADMIN_EMAIL=khalidmimani@gmail.com
```

### Step 2: Try Port 465 (SSL) if Port 587 Fails

If port 587 doesn't work, try port 465:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=465
MAIL_USE_SSL=true
MAIL_USERNAME=khalidmimani@gmail.com
MAIL_PASSWORD=gwkdneycvkwomrqv
ADMIN_EMAIL=khalidmimani@gmail.com
```

### Step 3: Check Railway Logs

After deploying, check Railway logs for:

**Success:**
```
EMAIL SEND ATTEMPT - Order #X
SUCCESS: Email notification sent successfully!
```

**Error:**
```
ERROR: Failed to send HTML email for order #X
Error Message: [specific error]
```

### Step 4: Verify Gmail App Password

1. Go to: https://myaccount.google.com/apppasswords
2. Make sure you have an App Password for "Mail"
3. If not, create a new one
4. Copy the 16-character password (remove spaces)
5. Update `MAIL_PASSWORD` in Railway

### Step 5: Test Email Sending

1. Create a test order
2. Check Railway logs for email attempt
3. Check your email inbox (and spam folder)
4. Look for detailed error messages in logs

## Enhanced Error Logging

The updated `EmailService` now logs:
- Environment variable values (without exposing passwords)
- Connection details (host, port)
- Detailed error messages with stack traces
- Cause information for nested errors
- Diagnostics for troubleshooting

## Alternative Solutions

If Gmail continues to have issues, consider:

### 1. SendGrid (Recommended)
- Free tier: 100 emails/day
- Better deliverability
- Easy setup
- SMTP: `smtp.sendgrid.net:587`

### 2. Mailgun
- Free tier: 5,000 emails/month
- Good for production
- SMTP: `smtp.mailgun.org:587`

### 3. AWS SES
- Very cheap ($0.10 per 1,000 emails)
- High deliverability
- Requires AWS account

## Quick Fix Checklist

- [ ] All environment variables set in Railway
- [ ] Gmail App Password is valid (16 characters)
- [ ] 2-Step Verification enabled on Gmail
- [ ] Checked Railway logs for errors
- [ ] Tried port 465 if port 587 fails
- [ ] Verified email addresses are correct
- [ ] Checked spam folder
- [ ] Redeployed after changing variables

## Debug Mode

To enable detailed email logging, set in Railway:
```
MAIL_DEBUG=true
```

This will show all SMTP communication in logs (useful for debugging).

## Support

If email still doesn't work after trying all steps:
1. Check Railway logs for specific error messages
2. Verify Gmail App Password is not expired
3. Try generating a new App Password
4. Consider using an alternative email service (SendGrid, Mailgun)

---

**Note:** Email sending failures will NOT prevent order creation. Orders will still be saved even if email fails.


