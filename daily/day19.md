# Day 19 – Encryption Implementation and some UI additions

## ✅ Objectives
- [x] Add password dialog before ConfigScrenn.kt
- [x] Create encryption system with the help of ConfigHelper
- [x] Add AES-256 encryption dependency

## 📘 What I Learned
- Today I added the adviced dialogs, features etc to the project.
  - I changed the flow of the program. Now, the first time the program opens ConfigScreen.kt will not have a back button and after the connection it goes directly to the MainScrollablePage
  - I added a defaul tpassword dialog to reach ConfigScreen.kt which has the password of '1234'
  - After the first login the back button is visible again. In addition chanegd the failed connection errors from toast to cards.
- Added encryption specialty.
  - Used AES-256 encryption because it generates unique encrypted outputs for each operation, even with identical input data. This randomization prevents attackers from identifying patterns or repeated information, as each encryption operation includes random initialization vectors and authentication tags that also detect any tampering attempts with the stored data.
  - Used dual storage architecture separating non-sensitive data from encrypted sensitive information by creating two distinct SharedPreferences files: "food_app_settings.xml" for regular data (server URLs, connection flags, timestamps) that can remain readable for debugging purposes, and "food_app_encrypted_settings.xml" for sensitive data (passwords, tokens, credentials) that gets encrypted.
  - Learned that singleton design pattern for secure data management ensures thread-safe access to encrypted preferences across the entire application

## ❓ Questions
How to verify that the encryption is working correctly in production environments and what additional security measures should be considered for enterprise deployment?

## 💬 Reflection
Today, implementation was not that problematic unlike other days. I tried to understand how does encryption works and checked some encryption types. Had problkem with the internet which made my progress slower. At some point I lost some of my progress consumed some time try to get back where I was. Other than that today was great. I only need to fully understand how the whole program words.
