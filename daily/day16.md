# Day 16 – Edited both architecture of Frontend and Backend
## ✅ Objectives
- [x] Added webAPI folder to backend
- [x] Created a half-working Frontend with clean architecture.
- [x] Learned about: CQRS, CQS, Service lifetimes
      
## 📘 What I Learned
- Today I learned that I need a webAPI folder that contains the controllers, with that implementation; now the code can be modified by only changing webAPI, and wanted platform can be chosen easily. For example if I want windows platform for my project I can change it by implement on webAPI folder. Even though the platform was mobile first
- Today I learned that the reason why my Frontedn had a problem working was because of not adding the proper dependencies. I added it for build variant and compose, Then the problem was solved.
- I also learned more about backend implementation terms such as Service lifetime, CQRS and CQS.
    - AddScoped: After a HTTP request new object is created. The same instance is then reused till the request process ends. Different request gets different instances. It is used when we dont want to mix the data between requests. Stateful services that should not be shared across requests.
    
    - AddTransient: A new instance is created everytime service requests. However, unlike AddScoped; In AddTransient no reusage of instance. Each dependency has its own copy. Safest one since new instance everytime service is requested.

    - AddSingleton: Only one instance is created for the entire application lifetime, amnd it is reused for all requests and dependencies. Best for configuration login.

- Also Terms CRQS and CQS were mentioned a quick search was done for these terms also

  - CQS: Commands and queries have seperation in which read and write operations are in the same model.
  Commands-->Write, Queries-->Read.
  - CQRS: Upgraded CQS. Now the read write operations are seperated (Different models or different DBs)

 - In addition, My FoodOrderingApp_ver2 is crashing after I say Connect to DB on StartingPage. Tomorrow, I will debug it and plan to finish the structure of Frontend.

## ❓ Questions
- Is the problem of not connecting properly and crashing happens because of backend, or do I have a problem with frontend?
- Which service lifetime should be integrated?

## 💬 Reflection
- I was quite stressed about dailies and logs. Finally, they are in a format that is accepted by my supervisor. So, today started excellent. In addition, I was able to at least half working clean architectured Frontend. Moreover, My backend was examined and only few changes were adviced, and those changes are completed today. My only worry is that, my project crashes when I try to connect to DB. I hope it is not related with backend. Other than that, Sun is shinning, Birds are chirping.
