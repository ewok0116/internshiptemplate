# Day 14 – Started Editing the Folder structure of Frontend
## ✅ Objectives
- [x] Add detail to project tab
- [x] Edit the Folder Structure
- [x] Decide on which architecture to use

## 📘 What I Learned
- Got some advice on project tab. Until now, I was not giving much detail which was wrong since I need to write my progress in a way that a random person could understand.
  Therefore, today I started editing project tab in the wanted way.
  
- I needed to decide on which architecture and approach to use and in the end I was told that the important was to show the features the project has. Because it would be easier to
implement or make changes on the projects later on. Therefore, In the end it was decided to use MVVM like architecture with vertical slicing approach like I did with Backend.

- MVVM seperates the code in a three layers.
View: My Composes screens such as CartScreen, HomeScreen, and it only displays UI and handles user interactions. For example when you tap "Add to Cart", the View tells the ViewModel
ViewModel: In the project it's DemoFoodOrderingViewModel, and it processes business logic, manages data, and prepares it for the View For example: Calculates cart totals, handles payment processing, and updates product lists
Model inside Data: In my project it's Product/CartItem classes and API network calls, and stores raw data and handles data operations such as product details, API responses from my backend

I pushed the final folder structure to the git, however got many errors while editing code. So, it is not completed just yet.

  
## ❓ Questions
- Is MVVM really the best approach and was I able to implement a fully ANSI SQL. Are my features same with my backend?

## 💬 Reflection
- Today I tried to edit folder like I did in backend. I did the same mistake. I tried to change all of them at once. In addition, thought this time it would be easier to implement from creating the folder. Even though part of me want to do the same approach with backend which was go through each feature one by one, another part of me wants to try this creat folder first then implement approach. I will work on that approach on the weekends this week. 
