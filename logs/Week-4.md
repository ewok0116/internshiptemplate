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

#Day-17

- Today I continued trying to fix the connection error. When I press the go to homepage button the program was crashing. In the end with some guidance, I learned how to use logcat. Thanks to that, I was able to find the problem. The problem was that I try to establish connection after I try to initialize viewmodel. Moreover, viewmodel needs conenection. so what needs to be done was that on ConfigScreen, it should first establish the connection and after going back to StartScreenpage a feature called test connection can be add and if the test connection returns succesful, then the MainScrollable.kt should be available for usage.
      - The Test connection was used to show that the connection is established in Config. I was told to get rid of it afterwards.
  
- After connection to web service was complete, I tried to Order. Even though the orders were shown in the DB, on Android Studio "Failed Payment" dialog was appearing. Turns out the problem was with my hidePayment method.
    - This was resetting success to NONE which lead to showing Failed Payment Dialog. In the end, by not resetting success to NONE I was able to obtain the receipt and PaymentSuccess

-     fun hidePayment() {
        uiState = uiState.copy(
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE  // ❌ Wiped out SUCCESS state
    )
}

- Now, since I have a working Frontend. I have 3 things to focus on. First one is adapt vertical slicing to current project. Second one is add encryption, and third is adding eye toggle to password side and get rid of test connection on StartScreen.

#Day-18

- Today, I had implemented a test connection button to see whether the connection was wortking upon screens. Yesterday, did not have the time to delete that button. Therefore, first thing first, I deleted that button from StartScreen.kt. After that I implemented eye icon in order to see the password.

- Then I had review session and got some advice:
  - I realized that my password does not stay on the password bar in the config screen therefore I need to implement that specialty.
  - Another advice was on putting a password before Database Settings page which is ConfigScreen.kt. The password will be custom 1234 or 2025 smt like that.
  - Another advice was on automatically going back to StartScreen from Config Page at first after that back button will appear.
  - Lastly, the failed message after probelm with the connection could be a dialog instead of a toast.
  - I was also informed more about where to implement Encryption.
    
- I learned about System Hierarchy and the principles of it.
    What is System?
    - Behaviour of entities or parts defines the system and the system can be used to learn more about patterns, and System Theory tries to answer how does the pattern created by the system
  - Principles of GST (General System Theory): Isomorphism, Dynamics, Holism, Emergence, Boundary, Information and Feedback, Dependence of the parts on the whole, Hierarchy, Organization, Continuity, Adaptation and evolution. with these principles of system it analyses patterns.
 
    In System we have parts and those parts have relationship. This actually what makes the system; system.
    - Isomorphism: when 2 parts are similar such as planes wing and a birds wing. This is isomorphism.
    
    - Dynamic: There is a continuation in system till its creation and that continuation is provided by Dynamic principle. Part should be dynamic so that they can be changed whereas this principle determines that whether that change affected the whole system or not.

    - Continuity: Like it was explained below it means the systems will to keep going.
   
    - Organization: We said that parts have relationships. For organization this is what's important. Moreover, a part can have relation with other parts.
   
    - Boundary: This defines the limits of each part. If it did not exist, there would not be a reason for multiple parts.
   
    - Adaptation and evolution: When there are environmental changes, for surviva; parts should adapt to environment. Those changes and that adaptation process goes into this principle.
   
    - Holism: This and emergence are the most core principles. By changing one part, it might affect the whole system or nothing might happen. Holism look into this. It is the concept of seeing the bigger picture.
   
    - Emergence: After the change of a part or parts, th eresult from relations also changed. Emergence is 2 or more part relations outcome.
   
    - Information and Feedback: this principle is important because in life one of the most necessity is to communicate. Therefore in system parts feedback is also important. It might lead chabnges to be better or worse.
   
    - Dependence of the parts on the whole: this principle is paralel with the Dependency Injection in Computer sciences. Parts are dependent to an object. this principle analyses that relation and dependent object etc.
   
    - Hierarchy: This principle is similar to what we have in Computer Sciences. The structure or architecture of the program. For example trees etc. This principle analyses the parts relation and whether there is hierarchy between parts. If there is it ties ti define the hierarchy.
   
These were the principle of the System Theory. This concept appraoch can be used in many fields. Even in our daily lifes. In addition, like I mentioned; some of the principles are quite similar to what we have in computer sciences.

#Day-19

- Started the day by implementing the advised security features from yesterday's review session. First priority was adding a password protection dialog before accessing ConfigScreen.kt with a hardcoded password of '1234' to prevent unauthorized configuration changes. 

- Redesigned the program flow to improve first-time user experience. Modified the navigation so that ConfigScreen.kt initially appears without a back button, forcing new users to complete the database setup process.

  After successful connection establishment, the app automatically navigates to MainScrollablePage instead of returning to StartScreen. In addition, After the initial setup is complete, implemented logic so that the back button becomes visible now. Moreover, password and URLs in ConfigPage are now stored in cache so that they do not disappeear after leaving the ConfigScreen, and the error message for failed connection is converted fom toast to card.

- In terms of encryption: Implemented  encryption architecture using AES-256. Selected AES-256-GCM encryption for data values because it generates unique encrypted outputs for each operation, preventing pattern recognition attacks. Chose AES-256-SIV for preference keys to ensure consistent encrypted outputs for reliable data lookup.

  In addition, designed dual storage architecture separating non-sensitive data from encrypted sensitive information. Created "food_app_settings.xml" for regular data (server URLs, connection flags, timestamps) and "food_app_encrypted_settings.xml" for sensitive data (passwords, tokens, credentials). Created ConfigHelper file for these operations and on that used singleton desgin pattern which ensures thread-safe access to encrypted preferences across the entire application and preventing race conditions.









