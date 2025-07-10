##Week 5

#Day 20
- Today, I realized that the encryption I made was not working properly. It was somehow using 2 encryption methodss of AES256, and it was creating 2 different files. It supposedly created 2 files for:
  1: One encrypted file
  2: One non-encrypted file which was not even wanted, in addition the encrypted one was also not encrypted. Therefore, deleted the xml files on data-data-com.example...-shared then tried other methods for encryption this time with one file, but this time had problem with creattion. I started over in the end, however then the shared files went misssing and after I tried several methods, whole com.example went missing. Moreover, I do not know the reason why?
In the end, I decided to do some documentation

The documentation can be found in here:
https://www.notion.so/Pragmatic-Programmer-2245693d203580e59512f2264d2d132a?source=copy_link

Some of the documentation's content:
Personal Notes

Repository: mapper ve remote u birlikte kullanarak data verilerini topladığın bir file gibi bir şey

Repository tam olarak **mapper ve remote'u birleştiren koordinatör dosyası** gibi çalışıyor.

Remote: Connection ı kurduğun yer

Mapper: Bu connection sonucu elde ettiklerini DTO ya çevirdiğin yer

class ProductRepositoryImpl(
private val apiService: ApiService,        // Remote
private val productMapper: ProductMapper,  // Mapper
private val localDatabase: LocalDatabase   // Local (opsiyonel)
) : ProductRepository {

```
override suspend fun getProducts(): List<Product> {
    try {
        // 1. Remote'u çağır
        val dtoList = apiService.getProducts()

        // 2. Mapper'ı kullan
        val productList = dtoList.map {
            productMapper.mapToProduct(it)
        }

        // 3. İsteğe bağlı: Local'e kaydet
        localDatabase.saveProducts(productList)

        return productList

    } catch (exception: Exception) {
        // 4. Hata durumunda local'den getir
        return localDatabase.getProducts()
    }
}

```

}

Hepsini Repository de de yapabilirdim ama SOLID prensipleri gereği 


#Day 21
- Today, I checked the Level of my project which was 24. It is compatible with 95% of newest android devices. In addition, since the older devices are not supported it's performance is better copmpared to older levels. Currently there are 30 levels, therefore using a level not so new and old seemed logical.
- Finally the encryption worked. I used AES256 again. I used AES256 SIV for encrypting/decrypting keys and used AES256 GCA for values. I used lazy loading which reates the encrypted preferences only when first accessed.
this is the schema:
```

🔤 Input: key="server_url", value="https://api.foodapp.com"
    ↓
🔐 ENCRYPT KEY with AES256_SIV:
    "server_url" → "8x9mK2nP7qR4sT6v" (deterministic)
    ↓
🔐 ENCRYPT VALUE with AES256_GCM:
    "https://api.foodapp.com" → "A7sK9mX3nP6qR8tV2wY5z" + auth_tag
    ↓
💾 STORE in SharedPreferences file:
    "8x9mK2nP7qR4sT6v" = "A7sK9mX3nP6qR8tV2wY5z[auth_tag]"

```
- In addition, today I used vertical slicing approach on methods in FoodOrderingViewModel. In other words, I seperated the methods inside FoodOrderingViewmodel, which will lead to an easier implementation of updating or adding features.
- I learned how to create an APK and how to rename it.
- I edited the presentation which will be presentend this thursday.
- I realized that I was using API everytime I try to sselect a category it loads again. Moreover, I also have loadalldata in connection manager. tomorrow bu guidance Ill ask whether it is okay for me to usse this code instead:
// ❌ Current inefficient approach
LaunchedEffect(searchText, selectedCategoryId, uiState.products) {
    if (selectedCategoryId != null) {
        viewModel.getProductsByCategory(selectedCategoryId!!) { products ->
            // This hits the API again!
        }
    }
}

// ✅ Better approach - filter locally
LaunchedEffect(searchText, selectedCategoryId, uiState.products) {
    filteredProducts = if (selectedCategoryId != null) {
        // Filter from already loaded products
        val categoryProducts = uiState.products.filter { it.categoryId == selectedCategoryId }
        if (searchText.isNotBlank()) {
            categoryProducts.filter {
                it.name.contains(searchText, ignoreCase = true) ||
                it.description.contains(searchText, ignoreCase = true)
            }
        } else {
            categoryProducts
        }
    } else {
        // Search all products
        if (searchText.isNotBlank()) {
            uiState.products.filter {
                it.name.contains(searchText, ignoreCase = true) ||
                it.description.contains(searchText, ignoreCase = true)
            }
        } else {
            uiState.products
        }
    }
}

#Day 22

- Today, I started editing the presentation. I had to recheck what I did with the architecture and approach research.
- I also checked whether my DB is ANSI or not. While I was checking that; I realized no DBMS can be fully ANSI, however the percentage of change is lower than using SQL. WHat I mean is that when I use ANSI on SQL Server for example, and then I want to use it on MySQL thanks to ANSI I need to make quite a few changes. However, if I used fully SQL that would not be possible.
- I changed how the data was gathered. I had both GetDataByCategory and loadAllData I got rid of GetDataByCategory this one and now the selection of category is also made in the Frontend part. In other words, I do not connect to web service and load data each time I want to filter.
- I started making a Figma model to have another demo presentation. Just in case.

#Day 23


# Day 23 – Presentation Day

- Today, I realised that the dialog for the ConfigPassword should have taken the password I created, and it should appear after the first connection. Therefore, I changed the flow of the program on MainActivity. In addition, updated ConfigScreen and ConfigHelper. So that now the password on ConfigPasswordDialog is the one I enter at the ConfigScreen. In addition, if there is an error, the password is 1234. This is only the case if an error occurs while writing the password!!!
- I did my Demo and Presentation todat. It was great. I'am quite happy with the result. If I could have done unit testing though I would have been thrilled.
- I also created a wiki.
Since I'am not doing much with coding there is not much detail to tell. That's why this week my daily and logs are quite similar.










