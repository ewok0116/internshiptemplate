
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






