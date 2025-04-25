 

# **Mama's kitchen- Documentation**

## **1. Overview**

MealMate is an Android application designed to help users plan their meals efficiently. It integrates with TheMealDB API to provide meal suggestions, allows users to search meals by category, ingredient, or country, and saves favorite meals for offline access. The app also includes a calendar-based meal planner where users can schedule meals for specific dates. Additionally, users can log in with Google and view their favorites offline.

### **Guest Mode**
- **Guest users** can access the **Home Screen** and perform searches by category, ingredient, or country without logging in.
- Only logged-in users can save favorites, view meal details, and plan meals for the week.

## **2. Features**

### **2.1 Home Screen**
- Displays the "Meal of the Day" retrieved from TheMealDB API.
- Clicking on the meal shows detailed information, including ingredients, an image, and a YouTube tutorial.

### **2.2 Google Sign-In**
- Users can log in using their Google account for a personalized experience.
- After logging in, the app stores user data (e.g., favorite meals) locally in the Room database.

### **2.3 Search Functionality**
- Users can browse meals by:
  - **Categories** (e.g., Seafood, Vegan, Dessert) with search and filter functionality.
  - **Ingredients** (e.g., Chicken, Rice, Cheese) with filtering options.
  - **Countries** (e.g., Italian, Mexican, Indian) with search and filter capabilities.
- Enhanced filtering and searching to quickly find meals within categories and countries.
- Efficiently fetches data using TheMealDB API.

### **2.4 Favorites**
- Users can save their favorite meals for offline access.
- Favorite meals are stored in **Room Database** to allow offline access.
- Users can view their favorites anytime without needing an internet connection.

### **2.5 Meal Planner**
- Users can plan meals for the week with a calendar-based meal planner.
- Meals can be scheduled for specific days (e.g., breakfast, lunch, or dinner).
- The planner resets weekly to allow for new meal planning each week.
- Users can add their planned meals directly to their phone's calendar (not Google Calendar) for easy reference.

## **3. Tech Stack**

### **3.1 Languages & Frameworks**
- **Kotlin** (Primary language for Android development)
- **XML** (For UI design)

### **3.2 APIs & Libraries**
- **TheMealDB API** (Fetch meals and meal details)
- **Retrofit** (For network calls)
- **Gson** (For JSON parsing)
- **Room Database** (For local storage of favorite and planned meals)
- **Glide** (For loading images efficiently)
- **Material Components** (For UI elements like buttons, dialogs, and menus)
- **Firebase Authentication** (For Google Sign-In)

## **4. Database Schema**

### **4.1 Meal Table** (For storing favorite meals)

| Column          | Type    | Description           |
| --------------- | ------- | --------------------- |
| idMeal          | String  | Unique ID of the meal |
| strMeal         | String  | Name of the meal      |
| strCategory     | String  | Category of the meal  |
| strArea         | String  | Country of origin     |
| strInstructions | String  | Cooking instructions  |
| strMealThumb    | String  | Image URL             |
| isFavorite      | Boolean | Mark as favorite      |

### **4.2 Planned Meals Table** (For storing planned meals)

| Column    | Type   | Description                        |
| --------- | ------ | ---------------------------------- |
| mealId    | String | Unique ID of the meal              |
| mealName  | String | Name of the meal                   |
| mealImage | String | Image URL                          |
| date      | String | Date for which the meal is planned |

## **5. API Integration**

### **5.1 Fetch Meal of the Day**
```kotlin
@GET("random.php")
suspend fun getMealOfTheDay(): Response<MealResponse>
```

### **5.2 Search Meals by Category**
```kotlin
@GET("filter.php")
suspend fun getMealsByCategory(@Query("c") category: String): Response<MealResponse>
```

### **5.3 Search Meals by Country**
```kotlin
@GET("filter.php")
suspend fun getMealsByCountry(@Query("a") country: String): Response<MealResponse>
```

### **5.4 Get Meal Details**
```kotlin
@GET("lookup.php")
suspend fun getMealById(@Query("i") mealId: String): Response<MealResponse>
```

## **6. Conclusion**

MealMate is a robust and user-friendly meal planning application that helps users explore and organize their meals seamlessly. Using modern Android development practices, APIs, and a structured database approach, it delivers a smooth user experience, enabling users to plan meals, store favorites offline, and view meal suggestions based on categories and countries.

---

## **7. Screenshots**

Here are some screenshots showcasing the different screens and features of the **MealMate** app:


   ![Home Screen](https://github.com/user-attachments/assets/bd5238c9-5f7e-450d-a0fa-9eecdd61ae4c)


   ![Meal Planner](https://github.com/user-attachments/assets/105098ef-849a-4742-acd6-b5bbb5141fab)


   ![Favorites Screen](https://github.com/user-attachments/assets/96dbd174-7e1a-4b2f-9a56-cd7f6792f006)


   ![Search Screen](https://github.com/user-attachments/assets/2425f885-99be-40ed-ad00-6d48d7063fca)


   ![Meal Details](https://github.com/user-attachments/assets/e2ee76df-1eb3-4e96-98cc-3575ef0f0cdc)

---

### **How to Run the Project**

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repository/MealMate.git
   ```
2. Open the project in **Android Studio**.
3. Sync dependencies and build the project.
4. Run the application on an emulator or a physical device.

---

This structure includes the Guest Mode feature, ensuring users can browse the app's main functionalities and perform searches even without logging in. Let me know if you need further changes or additional details!
