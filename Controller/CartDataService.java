
package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import Controller.SignInController;

public class CartDataService {
	private final ObservableList<Map<String, Object>> cartItems = FXCollections.observableArrayList();
	private static final CartDataService instance = new CartDataService();

	private CartDataService() {
	}

	public static CartDataService getInstance() {
		return instance;
	}

	public ObservableList<Map<String, Object>> getCartItems() {
		return cartItems;
	}

	public void addCartItem(Map<String, Object> item) {
		cartItems.add(item);
	}

	public void increaseQuantity(String productName) {
	    for (Map<String, Object> item : cartItems) {
	        if (item.get("productName").equals(productName)) {
	            double currentQuantityInCart = (double) item.get("quantity");
	            double availableQuantity = getAvailableQuantity(productName);

	            if (currentQuantityInCart + 0.1 <= availableQuantity) {
	                item.put("quantity", currentQuantityInCart + 0.1);
	            } else {
	                // Show alert or handle the case when there's not enough stock
	                showNotEnoughStockAlert();
	            }
	            break;
	        }
	    }
	}

	private Double getAvailableQuantity(String productName) {
		Double availableQuantity = 0.0;
		String sql = "SELECT product_quantity FROM products WHERE product_name = ?";
	    try (Connection connection = database.connectDb();
	            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

	           preparedStatement.setString(1, productName);

	           try (ResultSet resultSet = preparedStatement.executeQuery()) {
	               if (resultSet.next()) {
	            	   availableQuantity = resultSet.getDouble("product_quantity");
	               }
	           }
	       } catch (SQLException e) {
	           e.printStackTrace(); // Handle the exception appropriately
	       }
		return availableQuantity;
	}

	private void showNotEnoughStockAlert() {
	    Alert alert = new Alert(Alert.AlertType.WARNING);
	    alert.setTitle("Insufficient Stock");
	    alert.setHeaderText(null);
	    alert.setContentText("Requested quantity exceeds available stock.");
	    alert.showAndWait();
	}


	public void decreaseQuantity(String productName) {
		for (Iterator<Map<String, Object>> it = cartItems.iterator(); it.hasNext();) {
			Map<String, Object> item = it.next();
			if (item.get("productName").equals(productName)) {
				double quantity = (double) item.get("quantity");
				if (quantity > 0.1) {
					item.put("quantity", quantity - 0.1);
				}
				break;
			}
		}
	}

	public double getQuantity(String productName) {
		for (Iterator<Map<String, Object>> it = cartItems.iterator(); it.hasNext();) {
			Map<String, Object> item = it.next();
			if (item.get("productName").equals(productName)) {
				return (double) item.get("quantity");
			}
		}

		return 0;
	}

	public void deleteCartItem(String productName) {
		Iterator<Map<String, Object>> iterator = cartItems.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> item = iterator.next();
			if (item.get("productName").equals(productName)) {
				iterator.remove();
				break;
			}
		}
	}
}