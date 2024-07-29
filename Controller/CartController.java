package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Map;
import java.sql.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Controller.database;
import Controller.SignInController;

public class CartController {
	@FXML
	private VBox cartItemsVBox; // This VBox is inside the ScrollPane for cart items
	@FXML
	private Label subtotalLabel, taxLabel, totalLabel;
	@FXML
	private DatePicker deliveryDate;
	

	@FXML
	
	private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    String username = SignInController.getUsername();
    
	public void initialize() {
		loadCartItems();
		calculateTotals();
	}

	private void loadCartItems() {
		cartItemsVBox.getChildren().clear();
		for (Map<String, Object> item : CartDataService.getInstance().getCartItems()) {
			VBox itemBox = new VBox(10);
			itemBox.setPadding(new Insets(5));

			String productName = (String) item.get("productName");
			double price = (double) item.get("price");
			double quantity = (double) item.get("quantity");

			Label nameLabel = new Label("Product: " + productName);
			Label priceLabel = new Label(String.format("Price: $%.2f", price));
			Label quantityLabel = new Label(String.format("Quantity: %.2f kg", quantity));

			// Buttons for increasing, decreasing, and deleting
			Button increaseButton = new Button("+");
			increaseButton.setOnAction(event -> increaseQuantity(productName));
			Button decreaseButton = new Button("-");
			decreaseButton.setOnAction(event -> decreaseQuantity(productName));
			Button deleteButton = new Button("Delete");
			deleteButton.setOnAction(event -> deleteItem(productName));

			HBox buttonBox = new HBox(5, increaseButton, decreaseButton, deleteButton);
			itemBox.getChildren().addAll(nameLabel, priceLabel, quantityLabel, buttonBox);
			cartItemsVBox.getChildren().add(itemBox);
		}
	}

	private void deleteItem(String productName) {
		CartDataService.getInstance().deleteCartItem(productName);
		loadCartItems();
		calculateTotals();
	}

	private void increaseQuantity(String productName) {
		CartDataService.getInstance().increaseQuantity(productName);
		loadCartItems();
		calculateTotals();
	}

	private void decreaseQuantity(String productName) {
		CartDataService.getInstance().decreaseQuantity(productName);
		loadCartItems();
		calculateTotals();
	}

	private void calculateTotals() {
		double subtotal = 0.0;
		for (Map<String, Object> item : CartDataService.getInstance().getCartItems()) {
			double price = (double) item.get("price");
			double quantity = (double) item.get("quantity");
			subtotal += price * quantity;
		}
		double tax = subtotal * 0.10; // 10% tax
		double total = subtotal + tax;

		subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
		taxLabel.setText(String.format("Tax: $%.2f", tax));
		totalLabel.setText(String.valueOf(total));

	}
	
	

	@FXML
	private void handlePayment(ActionEvent event) {
		

	        String sql = "INSERT INTO orderinfo (ordertime, deliverytime, user_id, carrier, isdelivered,totalcost) "
	                + "VALUES(CURRENT_TIMESTAMP,?,(SELECT id FROM users WHERE username = ?),NULL,FALSE,?)";

	        connect = database.connectDb();
	        

	        try {

	            Alert alert;
	            LocalDate today = LocalDate.now();

	            if (deliveryDate.getValue() == null || deliveryDate.getValue().isBefore(today))
	            {

	                alert = new Alert(AlertType.ERROR);
	                alert.setTitle("Error Message");
	                alert.setHeaderText(null);
	                alert.setContentText("Please fill all fields correctly!");
	                alert.showAndWait();

	            } else {
	            	prepare = connect.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
		            prepare.setDate(1, java.sql.Date.valueOf(deliveryDate.getValue()));
		            prepare.setString(2, username);
		            prepare.setDouble(3, Double.parseDouble(totalLabel.getText()));
		            int affectedRows = prepare.executeUpdate();
		            int orderId = 0;

		            if (affectedRows > 0) {
		                ResultSet generatedKeys = prepare.getGeneratedKeys();
		                if (generatedKeys.next()) {
		                    orderId = generatedKeys.getInt(1);
		                }
		            }
	            	
	            	for (Map<String, Object> item : CartDataService.getInstance().getCartItems()) {
		                insertOrderProductIntoDatabase(orderId, item);
		            }
	            	
	            	for (Map<String, Object> item : CartDataService.getInstance().getCartItems()) {
	            		String productName = (String) item.get("productName");
	            		double quantity = (double) item.get("quantity");

	            		String stock = "UPDATE products SET product_quantity = product_quantity - ? WHERE product_name = ?";
	            		prepare = connect.prepareStatement(stock);
	            		prepare.setDouble(1, quantity);
	            		prepare.setString(2, productName);
	            		prepare.executeUpdate();

		            }
	            	
	            	String price = "UPDATE products SET price = price * 2, doubled_price = 1 WHERE product_quantity < threshold AND doubled_price = 0";
	            	prepare = connect.prepareStatement(price);
	            	prepare.executeUpdate();
	            	
   	

                    alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Paid!");
                    alert.showAndWait();
                    handleClose();

	            }

	        } catch (Exception e) {
	           e.printStackTrace();
	        }
	           
	    }

	 private void insertOrderProductIntoDatabase(int orderId, Map<String, Object> item) {
		 	connect = database.connectDb();
	        
	        String productName = (String) item.get("productName");
	        int productId = getProductId(productName);
	        double quantity = (double) item.get("quantity");

	        String query = "INSERT INTO orderproduct (order_id, product_id, quantity) VALUES (?, ?, ?)";

	        try {
	            PreparedStatement pstmt = connect.prepareStatement(query);  
	            pstmt.setInt(1, orderId);
	            pstmt.setInt(2, productId);
	            pstmt.setInt(3, (int) quantity); // Cast to int if your quantity column is an int

	            pstmt.executeUpdate();
	        } catch (SQLException e) {
	            e.printStackTrace();
	            // Handle exception - e.g., show an error message to the user
	        }
	    }

	    // Implement this method to retrieve the product ID based on the product name
	 private int getProductId(String productName) {
	        connect = database.connectDb();
		    String query = "SELECT id FROM products WHERE product_name = ?";

		    try {
		    	prepare = connect.prepareStatement(query);
		    	prepare.setString(1, productName);
		        ResultSet rs = prepare.executeQuery();

		        if (rs.next()) {
		            return rs.getInt("id");
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		        // Handle exception - e.g., log the error or show an error message
		    }
		    return -1; // Return -1 or throw an exception if the product is not found
		}
	

	@FXML
	private void handleClose() {
		Stage stage = (Stage) cartItemsVBox.getScene().getWindow();
		stage.close();
	}


}