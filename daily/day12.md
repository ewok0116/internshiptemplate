# Day 12 – So Close to Finishing Backend

## ✅ Objectives
- [x] Dependency Injection and Inversion for Order part.
- [x] Dependency Injection and Inversion for OrderItem part.
- [x] Learned about HTTP methods.


## 📘 What I Learned
- I learned PUT, HTTP method and its difference from GET and POST I used it in the Backend UpdateStatus Feature. POST changes the data, but creates duplicates; GET does not changes the data and does not creat duplicatess.
  - Whereas PUT changes data and does not creat duplicates. PUT is used mostly on Update operations becausse of that. Like it is used in my project. PUT is used for updateing the status. What I did today can be seen in logs Week3 in a more detailed way.

 - I learned Debugging. I new what debug was, however was not using it because thought it would take a lot of time to understand the problem. Especially, in Android Studio since there are some many files and they are all connected to each other. But, in the end the problem I worked on for the whole day was solved by debugging and it was solved under 1 hour.

  The problem was my responses had a wrapper in android studio, and they were defined as data; but on backend side, I named the responses with their entity names such as user, product etc. I fixed that issue and added UpdateStatus feature which updates the Order status from Pending to Confirm etc. Implemented that feature to PaymentDialog and FoodModel.kt.

## ❓ Questions
- No questions for today.

## 💬 Reflection
- Today, I made a mistake of not asking for help from others. Therefore, wassted huge amount of time. In the end of the day; after I asked for help; my probelem was soleved within an hour.
Tomorrow and the following days, I will ask for help right away.
