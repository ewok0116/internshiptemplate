#Day-15

- Started the day by continuing edit of frontend structure. Last time I was working, I was trying to implement both clean architecture and vertical slicing at the same time, however it became complicated after a while, so today new approach was decided to use.The new approach is to implement clean architecture first, then add vector slicing. 

- After some time working on this new approach, I encountered with UpdateStatus function. Which made me remembered that I needed to got rid of "status" in Orders since I do not have a restaurant side etc. In the end, I deleted the column and its constraints (which was just a default db constraint) in SQL Server. Then I started implementing the backend side in order to get rid of these "status" feature. After I finished the status situation, I turned back to editing Frontend structure but this time I had problem with dependencies that were giving errors. In addition, I was having error with imports aswell. Specifically .compose which I could not figure out why.

- Moreover, I had problem with "di" package which was containning the Dependency injection files. At first, I tried to use Hilt injection,  which is a library for android built on top of dagger. I tried to add dagger to my build gradle but had errors. In the end, I decided not to use Hilt. In addition, this library may be out of date later on. Therefore, like I said; I decided to stick with the personal dependency injection.



