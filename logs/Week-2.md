
##Week-2 

#Day-5

I solved the connection problem. For authentication I used mixed which is both windows and SQL server authentication. After that, I wrote a basic code that approves my connection to the SSMS server. Then, I created a folder called MyDataBaseAPI which is the web service for my project. In the folder I have 2 files. DataBaseController.cs in which I keep the controllers (But I need to separate each controller for my code to become more clear for others) and Program.cs in which server is set to listen to port 5093 and configures controllers. Then I established connection with the SSMS and checked it on Postman. Thanks to Postman I was able to change the controller file in a wanted format. In other words, I had problems with the column, row names but with Postman it was easier to detect parts that needed change. Then, I started implementing my code in Android Studio, I was able to do the GET part, and to handle all the network connection I created a file called ApiService.kt that creates an interface that maps HTTP endpoints to Kotlin functions, moreover, this file acts as a translator that converts my app's Kotlin objects into JSON for sending to the server, makes HTTP calls to my  API endpoints, and then converts the JSON responses back into Kotlin objects that my Android app can use. However, I got stuck on POST part, therefore this part might need implementation. Then, before making any more changes, I decided to push it all to git, but I must have made a mistake that led me to lost the progress I made. Therefore, I needed to start over from establishing connection part. In the end I was able to achieve it but my code became messy. So, tomorrow I prioritize organizing my code first. 

#Day-6

I started the day by trying to fix the POST issue. Then learning more about terms such as dependency injection, stateful and stateless became priority, therefore I started to study about them.

Dependency Injection: It is a principle in which objects get their dependencies from external resource instead of creating each object under the class. It is like you pass a value but you actually pass the dependency of the object. In addition, dependency is what is needed for a class object that we normally write under class like "this.db = new DataBase('db.sqlite')" this, however in dependency injection, we pass it into as db:Database like a parameter and then just say this.db = db. This was also an example for Constructor injection. In DI there are several injection types. such as Setter injection adn Interface injection. main logic is the same. in which we get dependencies from an external soruce. 
            Pros of using DI: It is flexible. In other words, it is easy to change app's behaviour and objects are reusable. For example in the example I gave above about consturctor injection. thanks to DI, I would have been able to change the db without changing the code. I can also use other design patterns such as singleton or factory. Since the code does not change but how we use it up to us.
            Cons of using DI: Since it the dependencies are gathered from an external source it would be hard to track which object comes from where. In addition, it becomes hard to understand what that class needs so I need to give a clear name to the object.
            
When to use? When working on a complex app. It would save you from useless codes and since it is easy to change teams can easily implement on that project. therefore it is a good pattern for big companies to use.
        
Stateful architecture: When there are many users there should be that many servers and each server should remember the user process, data. If a problem occurs and that server crashed. Then that user's data and all the process is lost. The user might be directed to another server, however her/his data is lost so he/she needs to start all over. It sounds bad right now but there are also pros for this. First of all it is faster since the data is already in the server. Moreover, since server has the user data it can maintain user preferance thtoughout the session. 
        Real Life examples: Live gaming, video conferencing, trading apps.
        
Stateless architecture: This time instead of several servers for each user there is a server and a shared Data Base that server is connected to. Therefore, when a user had a problem he/she can go to another server, and since their data will be in the DB they can continue the process. It will be slower than stateful since the fetching will take more time though. Additionally, every request must include all necessary context (like user ID, session tokens, or authentication data) since the server doesn't remember previous interactions. The server treats each request as completely independent and must retrieve any needed information from the database or external storage systems. Multiple identical servers can handle any user's request, making it much easier to scale horizontally by simply adding more servers. However, this puts more load on the database since every request requires data retrieval.
        Real Life examples: Google Search, Translation APIs.
        
Another option could be hybrid architecture which combines stateless and stateful components within the same system, using each approach where it makes the most sense.
        Use stateful when real-time interactions and fast responses are needed ; use stateless when long term data storage is needed.

#Day-7

I studied more about architectures, approaches and design patterns. In the end I gained a broader perspective on how to write a program. Until now, I was writing programs without any order.

I decided to use Clean Architecture with Vertical Slice approach. To extend on that, I studied vertical slice on internet and learned that it encapsulates all the aspects of a feature and each feature is like a slice through layers, and my backend's structure is created upon that. Right now it only has feature for Categories. There is GetCategories feature and Handlers, Validators etc. will be under that feature's folder. In addition I decided to use Clean Architecture because it has extra MediarT and Handler layer instead of Service layer that way unlike in traditional architecture DB can be changed in an eassy way and the newcomers can understand the code in an easier way.
I decided to create the folders and then I tried to write the codes starting from domain to application layer. It was hard. Then I decided to do it in the opposite way and tried to start from application layer to domain. Again it was hard for me to implement. After that, I decided to go with step by step, and I decided to start from connection. Thought the rest would come to me naturally. It did not. 

<img width="217" alt="Screenshot 2025-06-18 at 14 25 09" src="https://github.com/user-attachments/assets/76d97fb0-f78a-4759-8c88-b53ec0d3d02f" />

Then I got some advices on how to impelent the project. After that, I decided to just start writing the code and seperate the code when I see there is a need for seperation. Tomorrow I will try that. 
Other than that. I learned more about dependency injection. I thought the idea of DI was to get rid of unnecessary code and make the program flexible by just changing the dependencies and not touch the code. What I thought was true. However, the thing I missed was the usage of if else. By using DI I do not need those hundreds and hundreds of if else blocks. It is important because those blocks cover huge amount size if the project is big. In addition, it is hard for newcomers to understand the code. Moreover, if they change the code; there will be chaos. 

#Day-8

I chose one entity to work on which was user. from that I started seperating it. First I created the features folder in order to get the Vertical Slice architecture and added MediarT which injected some dependencies. MediarT mioght be unncessary for small project like mine but for bigger projects it is suggested and also it was a good practice for me to use it and it helped me to understand bit more about dependencies. 
After that, I tried to test it on postman but login failed.
In addition, I studied terms: Dependency Inversion, plug-in principle.
Dependency Inversion: High-level modules such as files in domain level which contains business logic etc. should not rely on low level modules such as UserRepo in infrastructure layer, therefore both should depend on abstraction. Moreover, abstractions should not rely on details; details should rely on abstraction. Since modules are connected to abstract, it is low coupling meaning if in a project a low coupling design is wanted dependency inversion can be used. We should ask whether the dependency will be changed or not and if the answer is yes then it is logical to use Dependency Inversion since abstract module is used.
When to use Dependency Injection?
When to use Dependency Inversion?
They are both used in cases when we want our code to be flexible. meaning we might change the dependencies later on. These to principle helps us with that. However, if we want to use ASP .NET there are frameworks that depends on dependency injection. In addition, unlike dependency inversion, dependency injection base upon a class, therefore if we want our modules 
to be based upon a class we might choose injection instead of inversion since it uses abstract. Agasin, in inversion it is mostly used for decoupling.
Note to self: Coupling measures how closely connected two modules  (classes, components, services) are and how much one module relies on the internal workings of another. 
low coupling means it is flexible; tight is hanging one module forces changes in another.
In addition, today I did the user part but could not tesst it because there was a connection problem with mac and the windows device. Tomorrow I will try it with another windows device to identify the problem.








