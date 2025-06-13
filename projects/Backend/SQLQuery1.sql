-- Simplified Food Ordering Database (No Restaurant)

USE FoodOrdering;

-- 1. Categories Table (Hamburger, Drinks, Extras)
CREATE TABLE Categories (
    CID INT PRIMARY KEY IDENTITY(1,1),
    CName NVARCHAR(50) NOT NULL,
    CDescription NVARCHAR(255),
    ImageUrl NVARCHAR(255),
    CreatedAt DATETIME2 DEFAULT GETDATE()
);

-- 2. Products/MenuItems Table (Big King, SassyBurger, etc.)
CREATE TABLE Products (
    PID INT PRIMARY KEY IDENTITY(1,1),
    PName NVARCHAR(100) NOT NULL,
    PDescription NVARCHAR(500),
    Price DECIMAL(10,2) NOT NULL,
    PImageUrl NVARCHAR(255),
    CID INT FOREIGN KEY REFERENCES Categories(CID),
    IsAvailable BIT DEFAULT 1,
    CreatedAt DATETIME2 DEFAULT GETDATE()
);

-- 3. Users Table
CREATE TABLE Users (
    UID INT PRIMARY KEY IDENTITY(1,1),
    UName NVARCHAR(100) NOT NULL,
    UEmail NVARCHAR(100) UNIQUE NOT NULL,
    UPhone NVARCHAR(20),
    UPassword NVARCHAR(255) NOT NULL,
    UAddress NVARCHAR(255),
    CreatedAt DATETIME2 DEFAULT GETDATE()
);

-- 4. Orders Table (Simplified)
CREATE TABLE Orders (
    OID INT PRIMARY KEY IDENTITY(1,1),
    UID INT FOREIGN KEY REFERENCES Users(UID),
    OrderStatus NVARCHAR(20) DEFAULT 'Pending',
    TotalAmount DECIMAL(10,2) NOT NULL,
    DeliveryAddress NVARCHAR(255),
    OrderDate DATETIME2 DEFAULT GETDATE(),
    PaymentMethod NVARCHAR(20)
);

-- 5. OrderItems Table
CREATE TABLE OrderItems (
    OrderItemID INT PRIMARY KEY IDENTITY(1,1),
    OID INT FOREIGN KEY REFERENCES Orders(OID),
    PID INT FOREIGN KEY REFERENCES Products(PID),
    Quantity INT NOT NULL,
    UnitPrice DECIMAL(10,2) NOT NULL,
    TotalPrice AS (Quantity * UnitPrice) PERSISTED
);

-- Insert Sample Data matching your mock data
INSERT INTO Categories (CName, CDescription) VALUES 
('Hamburgers', 'Delicious hamburgers and burgers'),
('Drinks', 'Refreshing beverages and soft drinks'),
('Extras', 'Side dishes and additional items');

-- Insert Products matching your mock data
INSERT INTO Products (PName, PDescription, Price, CID) VALUES 
-- Hamburgers (CID = 1)
('Big King Menu', 'Special Price • Big King Burger + Fries', 285.99, 1),
('Classic Burger', 'Juicy beef patty with lettuce and tomato', 12.99, 1),
('Chicken Deluxe', 'Grilled chicken with avocado', 11.49, 1),
('Veggie Burger', 'Plant-based patty with fresh vegetables', 10.99, 1),
('BBQ Bacon Burger', 'BBQ sauce with crispy bacon', 14.99, 1),
('Double Cheese Burger', 'Double beef with extra cheese', 13.49, 1),

-- Drinks (CID = 2)
('Coca Cola', 'Classic refreshing cola drink', 2.49, 2),
('Orange Juice', 'Fresh squeezed orange juice', 3.99, 2),
('Water', 'Pure spring water', 1.99, 2),
('Coffee', 'Premium roasted coffee', 4.49, 2),

-- Extras (CID = 3)
('French Fries', 'Crispy golden french fries', 4.99, 3),
('Onion Rings', 'Crispy battered onion rings', 5.49, 3),
('Chicken Nuggets', '6-piece chicken nuggets', 6.99, 3),
('Mozzarella Sticks', 'Crispy mozzarella cheese sticks', 7.49, 3);

-- Sample User
INSERT INTO Users (UName, UEmail, UPhone, UPassword, UAddress) VALUES 
('John Doe', 'john@example.com', '+90-555-987-6543', 'hashed_password', 'Kadıköy, Istanbul');

-- Test Queries
-- Get all categories
SELECT * FROM Categories;

-- Get products by category
SELECT p.*, c.CName as CategoryName 
FROM Products p
JOIN Categories c ON p.CID = c.CID
WHERE c.CName = 'Hamburgers';

-- Get all available products
SELECT p.PID, p.PName, p.PDescription, p.Price, c.CName as Category
FROM Products p
JOIN Categories c ON p.CID = c.CID
WHERE p.IsAvailable = 1
ORDER BY c.CName, p.PName;
