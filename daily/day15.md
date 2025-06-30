# Day 15 – Updated Frontend Progress and Changes in DB
## ✅ Objectives
- [x] Remove status column from DB
- [x] Remove status and status related features

## 📘 What I Learned
- I had decided to remove UpdateStatus feature in Backend. Furthermore, I had decided to remove status entirely.
  - First I removed it from DB.
  - Then I removed it from Backend
  -  Frontend side was decided to be feature focused meaning each features' related files should be in one folder. In other words, vertical slicing should be used. Moreover it was decided that the clean architecture should be used.
    
    - I had tried to do both at once (both vertical slicing and clean architecture). However, in the end I could not have a working code and it became complicated later on (The non-finished part is on git)
    - This time I decided to start with just the clean architecture. therefore I created a folder structure that can be found in logs. Aside from that, I also tried to upgrade the already existing project. However, it was hard to implement on that. Moreover, I created a new project named FooDOrdering_ver2.
    -  I had probelem with dependencies on Frontend side specifically one dependency which is called AppDependencies.kt. The imports had a problem which ı couldnt' resolve just yet. In addition, FoodOrderingModel.kt also had problems. That is the reason why new clean arch. program was not working

## ❓ Questions
- Do I really need clean architecture. Isn't my code understandable by everyone already.

## 💬 Reflection
- Today I tried to stay focus but the editing part was quite boring. In addition to editing, I realized I had a problem with git in which I forgot to upload newest version of daily to git. Instead I uploaded an older version.
