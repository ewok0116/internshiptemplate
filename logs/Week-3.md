
##Week-3

#Day-10

Today, The connection is finally finished. now the mobile applicaiton can finally show what the db has, and it also sends information to the db. I can GET the products from DB and POST the Orders to DB. Web Service works but there are codes that needs to be checked cause I did not want to break the working code. Therefore, there might be some code blocks or functions that  are not used. I need to edit these. In addition, on Android side I do not have a layout like I did on Backend. I also need to implement it on Frontend. Other than that, I kind of got relaxed after I established the connection yesterday. This should not happen again.

#Day-11 

Today I started the day by learning more about caches I implemented to my program. In android studio I implemented sharedPreferences which are considered caches and they are kept in here:

<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="app_password">101522</string>
    <long name="config_updated" value="1750745229205" />
    <string name="server_url">http://192.168.190.61:5093</string>
</map>

And used in here in the code:

  val sharedPreferences = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)
      val serverUrl = sharedPreferences.getString("server_url", "") ?: ""
      
shared preferences are lightweight storing mechanism in Android Studio. They are storing informations on the persistent storage unlike cache though.
After that tried to connect to the work computer but still have problem on that side. I added Environment Variable Configuration which allows me to securely share this project since when pushing to github I will add this .env file to .gitignore. 
After that, I started implementing Dependency Injection to User, Product and Category. I created new folders such as Infrastructure in which Repository is kept and I also created an Interface folder. In Repository, I establish the middle layer between DB related codes and other codes such as business logic. I was able to integrate Dependency Inversion (I created interface folder in order to establish abstraction, with that I was able to got rid of high level modules being dependent to low level) and Injection (seperated Db with interface) for User, Product and Category part. 

#Day-12

Today, I continued editing Backend. Yesterday I've finished GET methods and today I continued from PUT and POST. While in the process of editing the POST and PUT files, I realized that I had 2 features in Order folder. CreateOrder and GetORder. However, in CreateOrder I was doing much more than just creating. I was updating and calculating etc. Therefore, I needed to create new features to seperate them from CreateOrder. 

- I created: CalculateOrdertotal, UpdateOrderStatus, CancelOrder, ValidateOrderItem. I also created OrderHistory and OrderReport but was not sure whether I will use them or not so they are just staying as a folder for now. In addition aside from UpdateOrderStatus. I might delete other folders aswell cause there is no need for validation cause the OrderItem is doing the validation in itself anyway. Moreover, CalculateOrder is also done by the db so not sure if they are necessary to add. tomorrow they might get deleted aswell.
In the process of editing I learned more about PUT method.

 - PUT: POST changes the data, but creates duplicates; GET does not changes the data and does not create duplicates. Whereas, PUT changes data AND does not create duplicates. PUT is used mostly on Update operations because of that. In my project I'am also using for updating. It is used for updateing the status (UpdateStatus Feature).

 -In the end I was able to create each features' repository, response, query and handler. In addition, each feature acquired their own endpoints. Moreover, for CreateOrder a Service was created. 
 
The web service was working well with the Postman, but when I tried it with Android Studio, no matter what I did it just could not connect with the web service. 
In the end, I learned debugging (I knew debug, but I assumed it would consume more time solving the problem. However, In the end I was wrong and the problem was solved within an hour thanks to that), and I realised that the responses of web service and android studio was different so on web service side, so I convterted all of the responses (For example the name 'user' inside GetUserResponse.cs) to 'data. 
    After that, I was able to get data from db. but this time I had a problem with PaymentDialog. The UpdateStatus was not working. I fixed processPayment method on FoodModels so that when porocess payment is successs it will go into db and State will be seen as Confirmed. However, now in the db there won't be a state aside from 'Confirmed'. Since only the Confirmed ones are accepted to the db. So Tomorrow I need to think more about this issue. 

#Day-13

- Today, I realized that my db was not fully ANSI SQL. It was partial ANSI SQL. First, I thought about fixing the db without creating a new one, however some of the features might encounter problems, so I decided not to take that risk in the end. I'am not sure whether any specific feature would have a problem if I tried changing the db instead of creating a new one. But like I said, I did not want to take any risks.
  - There is ANSI SQL and non-ANSI SQL. ANSI SQL can be used with multiple DBs. In other words, it is compatible with most of the known DBs such as MySQL, SQL Server etc. If someone wants to run their code on multiple DBs; Then using ANSI SQL would be a better approach. Since, non-ANSI is not compatibel with other DBs and specifically used with that specific DB. However, the pros of it is sometimes DBs have their native funstions etc. In that case, if someone needs the whole specific functions of a DB, and will use only that DB; Then, using non-ANSI would be a better solution.
  
- I realized how important it is to have an architecture in the process of making changes on backend to obtain compatibility with ANSI SQL. My work was quite easy thanks to the clean architecturte I made. Because, I had seperated repositories from other code blocks. I only made changes on repository files such as UserRepository.cs and easily changed the whole DB in backend just like that.
       
      var sql = @"INSERT INTO Orders (UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod) 
           OUTPUT INSERTED.OID 
           VALUES (@UserId, @OrderStatus, @TotalAmount, @DeliveryAddress, @OrderDate, @PaymentMethod)";

  these lines were changed in order to gather ANSI SQL format.

      var sql = @"INSERT INTO Orders (UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod) 
           VALUES (@UserId, @OrderStatus, @TotalAmount, @DeliveryAddress, CURRENT_TIMESTAMP, @PaymentMethod);
           SELECT OID, UID, OrderStatus, TotalAmount, DeliveryAddress, OrderDate, PaymentMethod 
           FROM Orders WHERE OID = SCOPE_IDENTITY();";
            

- I realized that the features I created such as UpdateState were useless since I do not have a receiver like restaurant that would change the order status. Therefore, I decided to get rid of them.

- I realized that aside from coding, writing your accomplishments is also an important job. Therefore, spent rest of the day fixing my mistakes on daily, weekly and projects tab.

#Day-14

Today I started to make some changes on Project Tab in github because I got some advice yesterday about to give more detail in my project tab issues. So that even a random person could understand what is my project about. After I finished the project tab, I started to edit Frontend Structure to be more neat, more understandable by others. To do that I needed to decide on which architecture and approach to use and in the end I was told that the important was to show the features the project has. Because it would be easier to implement or make changes on the projects later on. Therefore, In the end it was decided to use MVVM like architecture with vertical slicing approach like I did with Backend. I chose MVVM architecture. MVVM seperates the code in a three layers.

View: My Composes screens such as CartScreen, HomeScreen, and it only displays UI and handles user interactions. For example when you tap "Add to Cart", the View tells the ViewModel

ViewModel: In the project it's DemoFoodOrderingViewModel, and it processes business logic, manages data, and prepares it for the View For example: Calculates cart totals, handles payment processing, and updates product lists

Model inside Data: In my project it's Product/CartItem classes and API network calls, and stores raw data and handles data operations such as product details, API responses from my backend

I use MVVM with vertical slicing and thanks to that gather a team-friendly project. In other words it is understandable and with vector slicing it is easier to implement and add features without changing the code. MVVM was also searched in the beginning but could not get the idea about implementing architectures to databases. Therefore, I was not able to implement it in beginning. I pushed the final folder structure to the git, however got many errors while editing code. So, it is not completed just yet.

#Day-15

- Started the day by continuing edit of frontend structure. Last time I was working, I was trying to implement both clean architecture and vertical slicing at the same time, however it became complicated after a while, so today new approach was decided to use.The new approach is to implement clean architecture first, then add vector slicing. 

- After some time working on this new approach, I encountered with UpdateStatus function. Which made me remembered that I needed to got rid of "status" in Orders since I do not have a restaurant side etc. In the end, I deleted the column and its constraints (which was just a default db constraint) in SQL Server. Then I started implementing the backend side in order to get rid of these "status" feature. After I finished the status situation, I turned back to editing Frontend structure but this time I had problem with dependencies that were giving errors. In addition, I was having error with imports aswell. Specifically .compose which I could not figure out why.

- Moreover, I had problem with "di" package which was containning the Dependency injection files. At first, I tried to use Hilt injection,  which is a library for android built on top of dagger. I tried to add dagger to my build gradle but had errors. In the end, I decided not to use Hilt. In addition, this library may be out of date later on. Therefore, like I said; I decided to stick with the personal dependency injection.






