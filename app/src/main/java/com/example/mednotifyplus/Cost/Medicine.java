package com.example.mednotifyplus.Cost;

public class Medicine {
    private String name;
    private String type;
    private double priceThisYear;
    private double priceLastYear;
    private String category;
    private boolean isFavorite;
    private String instructions;
    private String reference;
    private int maxIntakeBeforeConsult;

    // ✅ Full constructor with all fields
    public Medicine(String name, String type, double priceThisYear, double priceLastYear,
                    String category, boolean isFavorite, String instructions,
                    String reference, int maxIntakeBeforeConsult) {
        this.name = name;
        this.type = type;
        this.priceThisYear = priceThisYear;
        this.priceLastYear = priceLastYear;
        this.category = category;
        this.isFavorite = isFavorite;
        this.instructions = instructions;
        this.reference = reference;
        this.maxIntakeBeforeConsult = maxIntakeBeforeConsult;
    }

    // ✅ Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public double getPriceThisYear() { return priceThisYear; }
    public double getPriceLastYear() { return priceLastYear; }
    public String getCategory() { return category; }
    public boolean isFavorite() { return isFavorite; }
    public String getInstructions() { return instructions; }
    public String getReference() { return reference; }
    public int getMaxIntakeBeforeConsult() { return maxIntakeBeforeConsult; }

    // ✅ Setters
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setPriceThisYear(double priceThisYear) { this.priceThisYear = priceThisYear; }
    public void setPriceLastYear(double priceLastYear) { this.priceLastYear = priceLastYear; }
    public void setCategory(String category) { this.category = category; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setReference(String reference) { this.reference = reference; }
    public void setMaxIntakeBeforeConsult(int maxIntakeBeforeConsult) {
        this.maxIntakeBeforeConsult = maxIntakeBeforeConsult;
    }
}
