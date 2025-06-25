using System.Reflection;
using DotNetEnv;
using MyFoodOrderingAPI.Core.Interfaces; // 🎯 For all repository interfaces
using MyFoodOrderingAPI.Infrastructure.Repositories; // 🎯 For all repository implementations
using MyFoodOrderingAPI.Core.Services; // 🎯 For service interfaces
using MyFoodOrderingAPI.Infrastructure.Services; // 🎯 For service implementations

// Load .env file FIRST - add this line at the very beginning
Env.Load();

var builder = WebApplication.CreateBuilder(args);

// Configure to listen on all interfaces and localhost
builder.WebHost.UseUrls("http://0.0.0.0:5093", "http://localhost:5093");

// Add services to the container
builder.Services.AddControllers();

// MediatR 12.4.0+ için - artık ayrı package gerekmez
builder.Services.AddMediatR(cfg => cfg.RegisterServicesFromAssembly(Assembly.GetExecutingAssembly()));

// 🎯 REPOSITORY REGISTRATIONS - All entities now use clean repository pattern
builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<IProductRepository, ProductRepository>();
builder.Services.AddScoped<ICategoryRepository, CategoryRepository>();
builder.Services.AddScoped<IOrderRepository, OrderRepository>(); // 🆕 NEW - Order repository
builder.Services.AddScoped<IOrderItemRepository, OrderItemRepository>(); // 🆕 NEW - OrderItem repository

// 🎯 SERVICE REGISTRATIONS - Business logic services
builder.Services.AddScoped<IOrderService, OrderService>(); // 🆕 NEW - Order business service

// Add CORS services
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAllOrigins", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAllOrigins");
app.UseRouting();
app.UseAuthorization();
app.MapControllers();

app.MapGet("/", () => "Food Ordering API is running!");

app.Run();