# Day 6 – Learning more about Patterns and Architecture

## ✅ Objectives
- [x] Update FoodOrdering Project Task on Git
- [x] Learn Dependency Injection
- [x] Learn Stateless, Statefull Rest
- [x] create .gitignore for backend
- [x] Download SSMS and SQL server on work computer


## 📘 What I Learned
-Dependency Injection: It is a principle in which objects get their dependencies from external source instead of creating each object under the class. It is like you pass a value but you actually pass the dependency of the object. In addition, dependency is what is needed for a class object that we normally write under class like "this.db = new DataBase('db.sqlite')" this, however in dependency injection, we pass it into as db:Database like a parameter and then just say this.db = db. This was also an example for Constructor injection. In DI there are several injection types. such as Setter injection adn Interface injection. main logic is the same. in which we get dependencies from an external soruce. 
            -Pros of using DI: It is flexible. In other words, it is easy to change app's behaviour and objects are reusable. For example in the example I gave above about consturctor injection. thanks to DI, I would have been able to change the db without changing the code. I can also use other design patterns such as singleton or factory. Since the code does not change but how we use it up to us.
            -Cons of using DI: Since, the dependencies are gathered from an external source it would be hard to track which object comes from where. In addition, it becomes hard to understand what that class needs so I need to give a clear name to the object.
            
-When to use? When working on a complex app. It would save you from useless codes and since it is easy to change teams can easily implement on that project. therefore it is a good principle for big companies to use.
        
-Stateful architecture: When there are many users there should be that many servers and each server should remember the user process, data. If a problem occurs and that server crashed. Then that user's data and all the process is lost. The user might be directed to another server, however her/his data is lost so he/she needs to start all over. It sounds bad right now but there are also pros for this. First of all it is faster since the data is already in the server. Moreover, since server has the user data it can maintain user preferance thtoughout the session. 
        -Real Life examples: Live gaming, video conferencing, trading apps.
        
-Stateless architecture: This time instead of several servers for each user there is a server and a shared Data Base that server is connected to. Therefore, when a user had a problem he/she can go to another server, and since their data will be in the DB they can continue the process. It will be slower than stateful since the fetching will take more time though. Additionally, every request must include all necessary context (like user ID, session tokens, or authentication data) since the server doesn't remember previous interactions. The server treats each request as completely independent and must retrieve any needed information from the database or external storage systems. Multiple identical servers can handle any user's request, making it much easier to scale horizontally by simply adding more servers. However, this puts more load on the database since every request requires data retrieval.
-Real Life examples: Google Search, Translation APIs.
        
-Another option could be hybrid architecture which combines stateless and stateful components within the same system, using each approach where it makes the most sense.
        Use stateful when real-time interactions and fast responses are needed ; use stateless when long term data storage is needed.

## ❓ Questions
- How to implement these on my code? Should I go with Stateful on cart page and stateless on history of the orders?

## 💬 Reflection
- I learned some of the must known terms today, especially tried to learn about dependency injection. But still not sure how to implement it onto my code. My pace got slower and this resulted in a way that makes me feel like I am not accomplishing anything. Therefore, tomorrow I'd like like to at least organize the Web service and make it work with the GET. Moreover, I would like to spend less time on setting up the work computer's connection. Last time I tried it with another windows device it cost my whole day.
