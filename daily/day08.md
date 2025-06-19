# Day 8 – Intial Backend Making-Connection Problem

## ✅ Objectives
- [x] Finish User part
- [x] Establish connection
- [x] Learn Dependency Inversion

## 📘 What I Learned

Dependency Inversion: High-level modules such as files in domain level which contains business logic etc. should not rely on low level modules such as UserRepo in infrastructure layer, 
therefore both should depend on abstraction. Moreover, abstractions should not rely on details; details should rely on abstraction. Since modules are connected to abstract, 
it is low coupling meaning if in a project a low coupling design is wanted dependency inversion can be used. We should ask whether the dependency will be changed or not 
and if the answer is yes then it is logical to use Dependency Inversion since abstract module is used.

When to use Dependency Injection? When to use Dependency Inversion? They are both used in cases when we want our code to be flexible. meaning we might change the dependencies later on. 
These to principle helps us with that. However, if we want to use ASP .NET there are frameworks that depends on dependency injection. In addition, unlike dependency inversion, dependency injection base upon a class, 
therefore if we want our modules to be based upon a class we might choose injection instead of inversion since it uses abstract. Agasin, in inversion it is mostly used for decoupling

## ❓ Questions
-Which one should I use in my project? How to implement?

## 💬 Reflection
It was frustrating trying to solve the same problem which I was able to solve last time but not this time. Other than that it was great to see that I was able to undertsan what I was doing.
Unlike, Android studio frontend phase, Backend is more clear and understandable which motivated me to learn more about architectures and approaches.
