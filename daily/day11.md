# Day 10 – Finished DB-AndroidStudio Connection

## ✅ Objectives
- [x] Dependency Injection and Inversion for User part
- [x] Dependency Injection and Inversion for Product part
- [x] Dependency Injection and Inversion for Category part
- [x] learned more about Query, Response, Repository etc. (explained under What I learned)
- [x] Added Environment Variable Configuration

## 📘 What I Learned
- Today I edited Backend In order to implement Dependency Injection. While doing so I learned more about what query is? what response is? etc.
  Query: it defines the requeest/input for an operation. like make a search for people start with the letter E. it can be API request GET, POST etc. The Query.cs has the 
query parameters the client can send to filter/sort products.
  Response: this one defines the result/output of the operation. Ecem, Ece, Elif. The Response.cs has the structure of the output data returned by API. it can be success or fail messages aswell
404 Not Found etc.
  Handler: Contains business logic which means apply calculating, sorting, fileting etc. to db queries in order to process the request. In addition it checks whether the request is valid or not.
  Repository: It is layer in which we use to divide Db processes with other processes. In addition to repository I created a folder called interfaces and added IRepository in order to hide the implementation details.
In other words, abstraction. This abstraction usage is the result of the integration of dependency inversion. My high level modules lik Get..Handler are not depended to my lower level modules.
They are depended to an abstraction. Moreover, with this implementation loose coupling is established.
  Service: It is a reusable class that encapsulates specific business logic or technical functionality, such as data processing, external API integrations, or domain-specific operations. used it in higher
workflows such as my Product part.
  Controller: It is a class that handles incoming HTTP requests and returns responses. In additioon, here the endpoints are defined.
  Environment Variable Configuration: It seperates configuratiopn code form application.json. with that I ssecure the sensitive info when sharing.

## ❓ Questions
- No questions for today.

## 💬 Reflection
- Today was a combination of both learning and applying what I learned. Therefore, I was happy with today's result. I was able to finish User, Product and Category part. Tomorrow I'am planning on
finishing the Order and OrderItem part. I also realized my workflow is like this. Meaning first those three parts (User, Category, Product) then Order and OrderItem.
