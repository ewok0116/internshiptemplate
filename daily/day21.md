# Day 21 – Last Touches

## ✅ Objectives
- [x] Did a working encryption
- [x] Research about APK
- [x] Started editing presentation
- [x] Added vertical slicing to frontend viewmodels

## 📘 What I Learned
- Today, I checked the Level of my project which was 24. It is compatible with 95% of newest android devices. In addition, since the older devices are not supported it's performance is better copmpared to older levels. Currently there are 30 levels, therefore using a level not so new and old seemed logical.
- Finally the encryption worked. I used AES256 again. I used AES256 SIV for encrypting/decrypting keys and used AES256 GCA for values. I used lazy loading which reates the encrypted preferences only when first accessed. The schema can be found in logs.
- In addition, today I used vertical slicing approach on methods in FoodOrderingViewModel. In other words, I seperated the methods inside FoodOrderingViewmodel, which will lead to an easier implementation of updating or adding features.
- I learned how to create an APK and how to rename it.
- I edited the presentation which will be presentend this thursday.
- I realized that I was using API everytime I try to sselect a category it loads again. Moreover, I also have loadalldata in connection manager.
  
## ❓ Questions
- Should I use smart cache when laoding products. Also, my products are laoded with both loadAllData and GetProductsByCategory How to move on from this?

## 💬 Reflection
- Felt like did a lot of things, and more I do stuff more new stuff started to appear. I need to work more though. After, I'am finished at the office, I will continue at home. Two days left. Feeling stressed.
