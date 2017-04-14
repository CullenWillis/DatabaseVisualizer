package uk.ac.abertay.androiddevelopmentproject;

public class DatabaseSales {

    String storeName;
    int sales;
    String month;

    public DatabaseSales(String storeName, int sales, String month) {

        this.storeName = storeName;
        this.sales = sales;
        this.month = month;
    }

    public String getStoreName() { return storeName;}

    public void setstoreName(String storeName) { this.storeName = storeName; }

    public int getsales() { return sales; }

    public void setsales(int sales) { this.sales = sales; }

    public String getmonth() { return month; }

    public void setmonth(String month) { this.month = month; }

}
