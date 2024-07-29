package Controller;

public class items {
	private String product_name;
    private Double price;
    private Double threshold;
    private Double product_quantity;
    private String category;
    private String image_path;
    private Boolean doubled_price;
    
    public items(String product_name, Double price, Double threshold, Double product_quantity, String category, String image_path,Boolean doubled_price){
        this.product_name = product_name;
        this.price = price;
        this.threshold = threshold;
        this.product_quantity = product_quantity;
        this.category = category;
        this.image_path = image_path;
        this.doubled_price = doubled_price;
    }
    
    public String getName(){
        return product_name;
    }
    public String getType(){
        return category;
    }
    public Double getPrice(){
        return price;
    }
    public Double getThreshold(){
        return threshold;
    }
    public Double getStock(){
        return product_quantity;
    }
    public String getPath(){
        return image_path;
    }
    public Boolean getDoubled(){
        return doubled_price;
    }
    

}
