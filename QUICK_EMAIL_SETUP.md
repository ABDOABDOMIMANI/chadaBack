# إعداد Gmail السريع - خطوات مباشرة

## ✅ أنت قريب جداً! لديك التحقق بخطوتين مفعّل بالفعل

من الصورة التي أرسلتها، أرى أن لديك:
- ✅ التحقق بخطوتين مفعّل (Validation en deux étapes: Activé)
- ✅ رقم هاتف: 0656-668002
- ✅ بريد استعادة: abderrahmane.mimani@gmail.com

### الخطوة التالية: إنشاء كلمة مرور التطبيق

1. **من الصفحة الحالية، اضغط على "Validation en deux étapes"** (التحقق بخطوتين)
   - سينقلك إلى صفحة تفاصيل التحقق بخطوتين

2. **في أسفل الصفحة، ابحث عن "Mots de passe des applications"** (كلمات مرور التطبيقات)
   - أو مباشرة: https://myaccount.google.com/apppasswords

3. **أنشئ كلمة مرور جديدة:**
   - اختر التطبيق: **"Courrier"** (Mail)
   - اختر الجهاز: **"Autre (nom personnalisé)"** (Other - Custom name)
   - أدخل الاسم: **"Chada Perfume"**
   - اضغط **"Générer"** (Generate)

4. **انسخ كلمة المرور:**
   - ستظهر كلمة مرور من 16 حرفًا مثل: `abcd efgh ijkl mnop`
   - **انسخها الآن!** لن تتمكن من رؤيتها مرة أخرى

### تحديث application.properties

افتح الملف `chadaBack/src/main/resources/application.properties` وحدّث:

```properties
# استخدم بريدك الإلكتروني (Gmail)
spring.mail.username=abderrahmane.mimani@gmail.com

# استخدم كلمة مرور التطبيق التي أنشأتها (16 حرف)
spring.mail.password=abcd efgh ijkl mnop
```

### مثال كامل:

```properties
# ================= Email Configuration =================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=abderrahmane.mimani@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
admin.email=khalidmimani@gmail.com
```

## رابط مباشر

إذا واجهت صعوبة في العثور على الخيار:
👉 **https://myaccount.google.com/apppasswords**

## ملاحظات مهمة

- ✅ استخدم **كلمة مرور التطبيق** (16 حرف) وليس كلمة مرور Gmail العادية
- ✅ يمكنك استخدام كلمة المرور مع أو بدون مسافات
- ✅ لا تشارك كلمة مرور التطبيق مع أي شخص
- ✅ يمكنك إنشاء عدة كلمات مرور لتطبيقات مختلفة

## اختبار

بعد التحديث:
1. أعد تشغيل التطبيق
2. أنشئ طلبًا تجريبيًا
3. تحقق من بريد khalidmimani@gmail.com

---

**نصيحة:** إذا لم تجد "Mots de passe des applications" في الصفحة، تأكد من:
- أن التحقق بخطوتين مفعّل (وهو مفعّل ✅)
- استخدم الرابط المباشر: https://myaccount.google.com/apppasswords

