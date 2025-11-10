# Railway Email Configuration Checklist

## Quick Fix Steps

### Step 1: Verify Environment Variables in Railway

Go to Railway → Your Service → Variables tab and ensure these are set:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=khalidmimani@gmail.com
MAIL_PASSWORD=gwkdneycvkwomrqv
ADMIN_EMAIL=khalidmimani@gmail.com
```

**Important:**
- Remove spaces from `MAIL_PASSWORD` (16 characters only)
- Make sure `MAIL_USERNAME` matches your Gmail address
- `ADMIN_EMAIL` is where notifications will be sent

### Step 2: Try Port 465 if Port 587 Doesn't Work

If port 587 is blocked, try port 465 with SSL:

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=465
MAIL_USE_SSL=true
MAIL_USERNAME=khalidmimani@gmail.com
MAIL_PASSWORD=gwkdneycvkwomrqv
ADMIN_EMAIL=khalidmimani@gmail.com
```

### Step 3: Verify Gmail App Password

1. Go to: https://myaccount.google.com/apppasswords
2. Make sure you have an App Password for "Mail"
3. If expired or missing, create a new one
4. Copy the 16-character password (no spaces)
5. Update `MAIL_PASSWORD` in Railway

### Step 4: Enable Debug Mode (Temporary)

To see detailed email logs, add this variable:

```
MAIL_DEBUG=true
```

This will show all SMTP communication in Railway logs.

### Step 5: Check Railway Logs

After creating an order, check Railway logs for:

**Success:**
```
EMAIL SEND ATTEMPT - Order #X
SUCCESS: Email notification sent successfully!
```

**Error:**
```
ERROR: Failed to send HTML email for order #X
Error Message: [specific error]
DIAGNOSTICS:
- MAIL_HOST: smtp.gmail.com
- MAIL_PORT: 587
- MAIL_USERNAME set: true
- MAIL_PASSWORD set: true
- ADMIN_EMAIL set: true
```

### Step 6: Redeploy After Changes

After updating environment variables:
1. Save changes in Railway
2. Service will automatically redeploy
3. Wait for deployment to complete
4. Test by creating a new order

## Common Issues & Solutions

### Issue: "Authentication failed"
**Solution:**
- Verify Gmail App Password is correct
- Make sure 2-Step Verification is enabled
- Remove spaces from password
- Generate a new App Password

### Issue: "Connection timeout"
**Solution:**
- Try port 465 with SSL (`MAIL_PORT=465`, `MAIL_USE_SSL=true`)
- Timeouts are already increased to 30 seconds
- Check Railway network restrictions

### Issue: "Email from address not configured"
**Solution:**
- Verify `MAIL_USERNAME` is set in Railway
- Check variable name spelling (case-sensitive)
- Redeploy after adding variable

### Issue: "SSL handshake failed"
**Solution:**
- Try port 587 with STARTTLS (default)
- Or use port 465 with SSL
- Configuration now trusts all SSL certificates

## Testing

1. Create a test order through the website
2. Check Railway logs immediately
3. Look for email send attempt messages
4. Check your email inbox (and spam folder)
5. Verify email was received

## Enhanced Logging

The updated EmailService now logs:
- Environment variable status (without exposing passwords)
- Connection details (host, port)
- Detailed error messages with stack traces
- Diagnostics for troubleshooting
- Success/failure status

## Next Steps

If email still doesn't work after trying all steps:

1. **Check Railway logs** for specific error messages
2. **Verify Gmail App Password** is not expired
3. **Try port 465** if port 587 fails
4. **Enable debug mode** (`MAIL_DEBUG=true`) to see detailed logs
5. **Consider alternative email service** (SendGrid, Mailgun) if Gmail continues to fail

## Alternative Email Services

If Gmail doesn't work, consider:

### SendGrid (Recommended)
- Free: 100 emails/day
- Setup: https://sendgrid.com
- SMTP: `smtp.sendgrid.net:587`

### Mailgun
- Free: 5,000 emails/month
- Setup: https://mailgun.com
- SMTP: `smtp.mailgun.org:587`

---

**Remember:** Email failures will NOT prevent order creation. Orders are always saved, even if email fails.


