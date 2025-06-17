using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

var builder = WebApplication.CreateBuilder(args);

// Configure to listen on all interfaces and localhost
builder.WebHost.UseUrls("http://0.0.0.0:5093", "http://localhost:5093");

// Add services to the container.
builder.Services.AddControllers();

// Add CORS services - VERY IMPORTANT for Android connections
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

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Enable CORS FIRST - This is crucial for Android
app.UseCors("AllowAllOrigins");
app.UseRouting();
app.UseAuthorization();
app.MapControllers();

app.MapGet("/", () => "Food Ordering API is running!");

app.Run();