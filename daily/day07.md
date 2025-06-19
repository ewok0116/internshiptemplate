# Day 7 – Deciding on My Architecture and Pattern

## ✅ Objectives
- [x] Learn more about Clean Architecture and Artchitectures in general
- [x] Learn more about approaches especially Vector Slicing Architecture approach
- [x] Start Backend from Scratch by deciding the folders etc.

## 📘 What I Learned
- Clean Architecture: learned that it encapsulates all the aspects of a feature and each feature is like a slice through layers,
and my backend's structure is created upon that.

- Vertical Slicing Approach: I decided to use Clean Architecture because it has extra MediarT and Handler layer instead of Servicce layer
that way unlike in traditional architecture DB can be changed in an eassy way and the newcomers can understand the code in an easier way.

- More about DI: I learned more about dependency injection. I thought the idea of DI was to get rid of unnecessary code and make the program flexible by
just changing the dependencies and not touch the code. What I thought was true. However, the thing I missed was the usage of if else. By using DI I do
not need those hundreds and hundreds of if else blocks. It is important because those blocks cover huge amount size if the project is big.
In addition, it is hard for newcomers to understand the code. Moreover, if they change the code; there will be chaos. 

## ❓ Questions
- Is Vertical Slicing an approach or architecture in internet it says 'Vertical Slicing Architecture' but explain it as an approach. WHY?
  
## 💬 Reflection
- I need to do my best and do not demotivate myself when I do not have a solid result. Learning and making mistakes are also part of the process.
- There is no bad or good approach, architecture. Understanding what the project needs and take action upon that is what's important.
- Code Review is important. Run away if a company is not familiar with this concept.
- When writing code. Thing about other people. You should design it in a way that others can understand. Easier to understand, better the code.
