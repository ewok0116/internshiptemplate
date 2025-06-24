using System.Reflection;
using DotNetEnv;
using MyFoodOrderingAPI.Core.Interfaces;           // 🎯 ADD - For IUserRepository
using MyFoodOrderingAPI.Infrastructure.Repositories; // 🎯 ADD - For UserRepository

// Load .env file FIRST - add this line at the very beginning
Env.Load();

var builder = WebApplication.CreateBuilder(args);

// Configure to listen on all interfaces and localhost
builder.WebHost.UseUrls("http://0.0.0.0:5093", "http://localhost:5093");

// Add services to the container
builder.Services.AddControllers();

// MediatR 12.4.0+ için - artık ayrı package gerekmez
builder.Services.AddMediatR(cfg => cfg.RegisterServicesFromAssembly(Assembly.GetExecutingAssembly()));

// 🎯 ADD THIS LINE - Register the repository for dependency injection
builder.Services.AddScoped<IUserRepository, UserRepository>();
// Add this line after the UserRepository registration
builder.Services.AddScoped<IProductRepository, ProductRepository>();

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