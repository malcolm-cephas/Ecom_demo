package com.malcolm.ecomagent.model;

public class ShoppingModels 
{
    public record WishlistItem(String sku, String name, double retailPrice) {}
    public record StockStatus(String sku, boolean isAvailable) {}
    public record OfferStatus(String sku, boolean isOnSale, double salePrice) {}
}
