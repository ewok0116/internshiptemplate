
# Day 13 – So close, yet so far... Could not start frontend.

## ✅ Objectives
- [ ] Start editing the architecture of Frontend
- [x] Convert DB to ANSI SQL
- [x] Edit Backend so that it works well with ANSI SQL
- [x] Make some adjustments on Project tab, daily and weekly log


## 📘 What I Learned

- I laerned that my DB was partial ANSI SQL. what was wanted from me was fully integrated ANSI. Therefore, new DB named FoodOrderingANSI was created. In addition learned what is ANSI SQL.

    - There is ANSI SQL and non-ANSI SQL. ANSI SQL can be used with multiple DBs. In other words, it is compatible with most of the known DBs such as MySQL, SQL Server etc. If someone wants to run their code on multiple DBs; Then using ANSI SQL would be a better approach. Since, non-ANSI is not compatibel with other DBs and specifically used with that specific DB. However, the pros of it is sometimes DBs have their native funstions etc. In that case, if someone needs the whole specific functions of a DB, and will use only that DB; Then, using non-ANSI would be a better solution.

  - In my sscnerio, I want my application to be compatible with other DBs therefore will usse ANSI SQL.

- I updated Backend aswell in order for it to work well with ANSI. The changes were applied easily because I had a clean structure. I only changed the Repositories inside Infrastructure folder. I realized the benefits of implementing architectures and approachess to my program.

- Realized that my reports lack details, therefore decided to edit daily, project tab and weekly logs.
  
## ❓ Questions
- Had a question about implementing UpdateStatus which is a feature that updates status from pending to confirmed etc., however that problem was solved because I realized that I do not have a restaurant side that can confirm or prepare my order. I will delete it from the code.

## 💬 Reflection
- I was quite happy that the backend part was finished. In addition, quite excited to start to learn Frontend architectures etc. However. In the end of the day I couldn't start because of my own mistakes. I did not detaily write these dailies, and deep down I guess I also knew that. I focused more on writing and learning about coding. Today's suffering of editing my writings thought me that this is important as much as coding. There is no value in doing anything, if in the end no one can understand it. 
