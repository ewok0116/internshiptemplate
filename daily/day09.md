# Day 9 – Intial Backend Making-Connection Problem

## ✅ Objectives
- [x] Finished the Architecture design
- [x] Added Category, Products entities
- [x] Created new Collection on Postman
- [x] Checked Swagger

## 📘 What I Learned
- I learned that there is anothert tool for testing RESTful APIs called Swagger and unlike Postman other people can see my collections with the URL. Other than that today was more about applying what I know, therefore today I did not learn much.
  
    - I cretaed the clean architecture design in which I added Infrastrcuture, Interface, Filter folders. Inside Infrastrcuture folder I have Repositories folder. There I seperated the DB connection related codes with other codes. In addition with the help of Interfaces I acquired dependency inversion along with injection. Since my other high level features are not bind to a lower level (dependency inversion) and made the project feasable and flexible by seperating the code and making it loose coupling.
     Inside those folders I added category and products entities. User was already added in the 'without DI version'. In the end, only the POST entities are left to add.

- I checked Swagger. I learned that it is different from Postman cause others can see what that URL holds. They are 2 different testing tools. Postman is more detailed though.
- Created GET collections on Postman. right now I have 3 endpoints: products, users, categories.

## ❓ Questions
- Why does the work computer can not connect to my MAC but other windows devices can? What is the problem? I did the same thing.

## 💬 Reflection
Today I felt great. Since my architecture is now set. It was easier to implement on that and I got faster. I kind of finished all features but not sure about POST part, because it
created problems on Android Studio part and then my Android Studio crashed. Therefore that aprt is not commited yet. In addition, I FINALLY HAD NO PROBLEM WITH GIT. Today was great.
