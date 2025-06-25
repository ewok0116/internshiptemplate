namespace MyFoodOrderingAPI.Core.Models
{
    public class OrderItemFilter
    {
        public int? OrderId { get; set; }
        public int? ProductId { get; set; }
        public int? MinQuantity { get; set; }
        public int? MaxQuantity { get; set; }
        public decimal? MinPrice { get; set; }
        public decimal? MaxPrice { get; set; }
        public int PageNumber { get; set; } = 1;
        public int PageSize { get; set; } = 10;
    }
}