# Day 18 – Fixed some issues in Frontend
## ✅ Objectives
- [x] Get rid of Test Connection Button
- [x] Added Eye icon for password
- [x] Learned System Hieararchy

## 📘 What I Learned
- I had implemented a test connection button to see whether the connection was wortking upon screens. Yesterday, did not have the time to delete that button. Therefore, first thing first, I deleted that button from StartScreen.kt.
- I implemented eye icon in order to see the password.
  - I had a review session and on that review I realized that my password does not stay on the password bar in the config screen therefore I need to implement that specialty.
  - Another advice was on putting a password before Database Settings page which is ConfigScreen.kt. The password will be custom 1234 or 2025 smt like that.
  - Another advice was on automatically going back to StartScreen from Config Page at first after that back button will appear.
  - Lastly, the failed message after probelm with the connection could be a dialog instead of a toast.
  - I was also informed more about where to implement Encryption.
- I learned about System Hierarchy and the principles of it.
    What is System?
    - Behaviour of entities or parts defines the system and the system can be used to learn more about patterns, and System Theory tries to answer how does the pattern created by the system
  - Principles of GST (General System Theory): Isomorphism, Dynamics, Holism, Emergence, Boundary, Information and Feedback, Dependence of the parts on the whole,Hierarchy, Organization, Continuity, Adaptation ans evolution. These principles are explained more detailed in Week4 log Day 18.

## ❓ Questions
- When I click on Drinks or any other category on MainScrollablePage; Does it fetch from my DB with the help of my Web Service or Do I initialize as soon as the page is open. In other words, do I load the products one time or everytime I press any category button?


## 💬 Reflection
- Today after I did the implementation of Frontend that I talked about on What I learned part, I analysed the given essay. It consumed most of the time. However, in the review session I had to present what I had learned from the essay. Even though it started of bad' it progressively got better. So today was okay.
