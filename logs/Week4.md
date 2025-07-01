##Week-4

#Day-15

- Started the day by continuing edit of frontend structure. Last time I was working, I was trying to implement both clean architecture and vertical slicing at the same time, however it became complicated after a while, so today new approach was decided to use.The new approach is to implement clean architecture first, then add vector slicing. 

- After some time working on this new approach, I encountered with UpdateStatus function. Which made me remembered that I needed to got rid of "status" in Orders since I do not have a restaurant side etc. In the end, I deleted the column and its constraints (which was just a default db constraint) in SQL Server. Then I started implementing the backend side in order to get rid of these "status" feature. After I finished the status situation, I turned back to editing Frontend structure but this time I had problem with dependencies that were giving errors. In addition, I was having error with imports aswell. Specifically .compose which I could not figure out why.

- Moreover, I had problem with "di" package which was containning the Dependency injection files. At first, I tried to use Hilt injection,  which is a library for android built on top of dagger. I tried to add dagger to my build gradle but had errors. In the end, I decided not to use Hilt. In addition, this library may be out of date later on. Therefore, like I said; I decided to stick with the personal dependency injection.

#Day-16

- Started the day by fixing the implementation errors on Frontend (FoodOrderingApp_ver2). Thanks to Esra, the compose problem was able to be solved. the problem was quite simple that I felt ashamed. Turns out I did not add the dependency for the compose. After that; I also realized that most of the errors were due to imports being wrong. What I mean is, while I was transfering files from FoodOrderingApp to FoodOrderingApp_ver2, I accidentally forgot to change the names of the imports. To demonstrate the problem, imports were like:
    - package com.example.foodorderingapp.data.repositories
this. But they should have been imported like:
    - package com.example.foodorderingapp_ver2.data.repositories

- After I fixed these issues, had another problem that occured because of build variant. After several trials, realized that the problem was again occured because of th enaming foodorderingapp instead of foodorderingapp_ver2. Then changed this block to ver2.

    - private fun getCompanyTheme(): String {
            return try {
                com.example.foodorderingapp_ver2.BuildConfig.COMPANY_THEME
            } catch (e: Exception) {
                "ORANGE" // Default
            }
        }  
  - After that, had a meeting about the backend part. After the meeting an advice was given about creating webAPI folder and it should contains the controller of the features. The reason for that is to have the flexibility to change the platform. For example, from mobile to windows.
 
  - In addition, after the meeting I was adviced to look for Service Lifetimes which are Dependency Injection methods used in .NET
 
      - AddScoped: After a HTTP request new object is created. The same instance is then reused till the request process ends. Different request gets different instances. It is used when we dont want to mix the data between requests. Stateful services that should not be shared across requests.
   
      - AddTransient: A new instance is created everytime service requests. However, unlike AddScoped; In AddTransient no reusage of instance. Each dependency has its own copy. Safest one since new instance everytime service is requested.
   
      - AddSingleton: Only one instance is created for the entire application lifetime, amnd it is reused for all requests and dependencies. Best for configuration login.
   
  Also Terms CRQS and CQS were mentioned a quick search was done for these terms also
             
  - CQS: Commands and queries have seperation in which read and write operations are in the same model. Commands-->Write, Queries-->Read.
  - CQRS: Upgraded CQS. Now the read write operations are seperated (Different models or different DBs)

- In addition, My FoodOrderingApp_ver2 is crashing after I say Connect to DB on StartingPage. Tomorrow, I will debug it and plan to finish the structure of Frontend.   
