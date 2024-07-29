package Controller;

public class orderproducts{

    private Double quantity;
    private String product_name;
    
    public orderproducts(Double quantity, String product_name){
        this.quantity = quantity;
        this.product_name = product_name;
    }
    
    public Double getQuantity(){
        return quantity;
    }
    public String getName(){
        return product_name;
    }
    
}