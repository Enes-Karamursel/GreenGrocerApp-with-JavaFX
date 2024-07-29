package Controller;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import app.Greengrocer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomerInterfaceController {
	private Greengrocer mainApp;
	private String currentCategory = "fruit"; // Default category
	private ObservableList<VBox> allProductBoxes = FXCollections.observableArrayList();
	String username = SignInController.getUsername();

	@FXML
	private TableColumn<orders, String> orderRequestedTime;
	@FXML
	private TableColumn<orders, String> orderTime;
	@FXML
	private TableColumn<orders, String> orderCarrier;
	@FXML
	private TableColumn<orders, String> orderUser;

	@FXML
	private TableColumn<orders, String> orderTotal;

	@FXML
	private TableColumn<orders, String> orderStatus;

	@FXML
	private TableColumn<orders, String> orderItems;

	@FXML
	private ListView<String> availableDeliveriesListView;
	@FXML
	private AnchorPane productsAnchor, tableAnchor;
	@FXML
	private TextField searchField;
	@FXML
	private TilePane productDisplayArea;
	@FXML
	private Label groupLabel, userLabel;
	@FXML
	private Button signOutButton1, viewCartButton;
	@FXML
	private TableView<orders> allOrdersTable;
	@FXML
	private Button cancelOrderButton;

	private Connection connect, connect2;
	private PreparedStatement prepare;
	private Statement statement, statement2;
	private ResultSet result;
	private PreparedStatement prepare2;
	private ResultSet result2;

	public void setMainApp(Greengrocer mainApp) {
		this.mainApp = mainApp;
	}

	public static Connection connectDB() {
		final String DB_URL = "jdbc:mysql://localhost:3306/myGreenGrocer";
		final String DB_USER = "root";
		final String DB_PASSWORD = "246835";
		try {
			return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ObservableList<orders> ordersListData() {
		ObservableList<orders> listData = FXCollections.observableArrayList();

		String sql = "SELECT oi.id, oi.ordertime, oi.deliverytime, u.username AS user_name, oi.carrier, oi.isdelivered, oi.totalcost "
				+ "FROM orderinfo oi JOIN users u ON oi.user_id = u.id " + "WHERE u.username = ?";

		connect = database.connectDb();

		try {
			prepare = connect.prepareStatement(sql);
			prepare.setString(1, username);
			result = prepare.executeQuery();

			orders cat;

			while (result.next()) {
				Integer orderid = result.getInt("id");
				String idgetter = "SELECT op.quantity, p.product_name " + "FROM orderproduct op "
						+ "JOIN products p ON op.product_id = p.id " + "WHERE op.order_id = '" + orderid + "'";

				// Move prepare2 initialization here
				prepare2 = connect.prepareStatement(idgetter);
				result2 = prepare2.executeQuery();

				List<orderproducts> orderItemsList = new ArrayList<>();

				// Iterate through result2 to fetch all order items for the current order
				while (result2.next()) {
					orderproducts itemlistget = new orderproducts(result2.getDouble("quantity"),
							result2.getString("product_name"));
					orderItemsList.add(itemlistget);
				}

				cat = new orders(result.getTimestamp("ordertime"), result.getTimestamp("deliverytime"),
						result.getString("user_name"), result.getString("carrier"), result.getBoolean("isdelivered"),
						result.getDouble("totalcost"));

				// Set the list of order items for the current order
				cat.setOrderItems(orderItemsList);
				cat.setOrderId(orderid);

				listData.add(cat);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return listData;
	}

	private ObservableList<orders> availableOrders;

	public void ordersShowData() {
		availableOrders = ordersListData();

		orderTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
		orderRequestedTime.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
		orderCarrier.setCellValueFactory(cellData -> {
			String carrier = cellData.getValue().getCarrier();
			return new SimpleStringProperty(carrier != null ? carrier : "Not Assigned");
		});

		orderStatus.setCellValueFactory(new PropertyValueFactory<>("deliveryStatus"));

		orderItems.setCellValueFactory(cellData -> {
			List<orderproducts> orderItemsList = cellData.getValue().getOrderItems();

			// Convert the list of orderproducts to a formatted String
			String formattedOrderItems = orderItemsList.stream()
					.map(orderProduct -> orderProduct.getName() + ": " + orderProduct.getQuantity())
					.collect(Collectors.joining("\n"));

			// Return the formatted String
			return new SimpleStringProperty(formattedOrderItems);
		});

		orderTotal.setCellValueFactory(new PropertyValueFactory<>("cost"));

		allOrdersTable.setItems(availableOrders);

	}

	@FXML
	private void handleGoBack() {
		productsAnchor.setVisible(true);
		tableAnchor.setVisible(false);
	}

	@FXML
	public void handleOrdersButtonAction() {
		ordersShowData();
		// Set the visibility of relevant AnchorPanes
		// Assuming there are two AnchorPanes: one for product display and one for order
		// display
		productsAnchor.setVisible(false);
		tableAnchor.setVisible(true);
	}

	@FXML
	public void viewCart() {
		try {
			// Load the cart FXML file
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/app/Cart.fxml"));
			Parent cartView = loader.load();

			// Create a new stage for the cart window
			Stage cartStage = new Stage();
			cartStage.setTitle("Shopping " + username + " Cart");

			// Set the scene and show the stage
			Scene scene = new Scene(cartView);
			cartStage.setScene(scene);
			cartStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@FXML
	public void initialize() {
		// Initialize the application, load initial products
		loadProducts(currentCategory);
	}

	public void handleSignOut() {
		mainApp.loadLoginScreen();
	}

	private void loadProducts(String category) {
		currentCategory = category;
		allProductBoxes.clear();
		productDisplayArea.getChildren().clear();

		String query = "SELECT product_name, price, image_path FROM products WHERE category = ? ORDER BY product_name ASC";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, category);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				String name = rs.getString("product_name");
				double price = rs.getDouble("price");
				String imagePath = rs.getString("image_path");

				VBox productBox = createProductBox(name, price, imagePath);
				allProductBoxes.add(productBox);
				productDisplayArea.getChildren().add(productBox);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle the exception properly
		}
	}

	private VBox createProductBox(String name, double price, String imagePath) {
		// Create each product box

		VBox productBox = new VBox(10);
		productBox.setAlignment(Pos.CENTER);
		productBox.setStyle(
				"-fx-padding: 10; -fx-border-color: #b6b6b6; -fx-border-width: 2; -fx-background-color: #ffffff;");

		ImageView imageView = createImageView(imagePath);
		Label nameLabel = new Label(name);
		Label priceLabel = new Label(String.format("$%.2f", price));

		TextField kgField = new TextField();
		kgField.setPromptText("kg");
		kgField.setStyle("-fx-pref-width: 50;");
		kgField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*([\\.]\\d{0,2})?")) {
				kgField.setText(oldValue);
			}
		});

		Button addToCartButton = new Button("Add to Cart");
		addToCartButton.setOnAction(event -> {
			String quantityText = kgField.getText();
			if (!quantityText.isEmpty()) {
				addToCart(name, quantityText);
			} else {
			}
		});
		;

		productBox.getChildren().addAll(imageView, nameLabel, priceLabel, kgField, addToCartButton);

		return productBox;
	}

	private ImageView createImageView(String imagePath) {
		// Create an ImageView from the image path
		try {
			File file = new File(imagePath);
			if (!file.exists()) {
				throw new MalformedURLException("File not found: " + imagePath);
			}

			String imageUrl = file.toURI().toURL().toExternalForm();
			Image image = new Image(imageUrl, true); // true for background loading
			ImageView imageView = new ImageView(image);
			imageView.setFitHeight(100);
			imageView.setFitWidth(100);
			imageView.setPreserveRatio(true);
			return imageView;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean checkProductQuantity(String productName, double requestedQuantity) {
		String query = "SELECT product_quantity FROM products WHERE product_name = ?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, productName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				double availableQuantity = rs.getDouble("product_quantity");
				return requestedQuantity <= availableQuantity;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle the exception properly
		}
		return false;
	}

	private void showNotEnoughProductAlert() {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Not Enough Product");
		alert.setHeaderText(null);
		alert.setContentText("Not enough product in stock.");
		alert.showAndWait();
	}

	private void updateProductQuantity(String productName, double quantityToDeduct) {
		String updateQuery = "UPDATE products SET product_quantity = product_quantity - ? WHERE product_name = ? AND product_quantity >= ?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

			pstmt.setDouble(1, quantityToDeduct);
			pstmt.setString(2, productName);
			pstmt.setDouble(3, quantityToDeduct);

			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Quantity updated for " + productName);
			} else {
				System.out.println("No quantity update was made for " + productName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle the exception properly
		}
	}

	private void addToCart(String productName, String quantityText) {
		double quantity;
		double availableQuantity = 0;
		try {
			quantity = Double.parseDouble(quantityText);
		} catch (NumberFormatException e) {
			showInvalidQuantityAlert();
			return;
		}

		// Retrieve the latest product details from the database
		Map<String, Object> productDetails = getProductDetails(productName);
		if (productDetails.isEmpty()) {
			// Handle the case where the product is not found in the database
			return;
		}

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

		if (quantity > availableQuantity) {
			showNotEnoughStockAlert();
			return;
		}

		double price = (double) productDetails.get("price");

		// Check if the product is already in the cart
		ObservableList<Map<String, Object>> cartItems = CartDataService.getInstance().getCartItems();
		boolean productFound = false;
		for (Map<String, Object> cartItem : cartItems) {
			if (cartItem.get("productName").equals(productName)) {
				// Update the quantity of the existing product
				double existingQuantity = (double) cartItem.get("quantity");
				if (existingQuantity + quantity > availableQuantity) {
					showNotEnoughStockAlert();
					return;
				}
				cartItem.put("quantity", existingQuantity + quantity);
				productFound = true;
				break;
			}
		}

		if (!productFound) {
			if (quantity <= availableQuantity) {
				Map<String, Object> cartItem = new HashMap<>();
				cartItem.put("productName", productName);
				cartItem.put("price", price);
				cartItem.put("quantity", quantity);
				CartDataService.getInstance().addCartItem(cartItem);
			} else {
				showNotEnoughStockAlert();
			}
		}
	}

	private void showNotEnoughStockAlert() {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Insufficient Stock");
		alert.setHeaderText(null);
		alert.setContentText("Requested quantity exceeds available stock.");
		alert.showAndWait();
	}

	private void showInvalidQuantityAlert() {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Invalid Quantity");
		alert.setHeaderText(null);
		alert.setContentText("Please enter a valid quantity.");
		alert.showAndWait();
	}

	@FXML
	private void handleSearch() {
		// Handle search functionality
		String searchText = searchField.getText().toLowerCase();
		if (!searchText.isEmpty()) {
			filterProducts(searchText);
		} else {
			loadProducts(currentCategory);
		}
	}

	private void filterProducts(String searchText) {
		// Filter products based on search text
		ObservableList<Node> filteredProductBoxes = FXCollections.observableArrayList();

		for (VBox productBox : allProductBoxes) {
			Label nameLabel = (Label) productBox.getChildren().get(1); // assuming nameLabel is the second child
			if (nameLabel.getText().toLowerCase().contains(searchText)) {
				filteredProductBoxes.add(productBox);
			}
		}

		productDisplayArea.getChildren().setAll(filteredProductBoxes);
	}

	private Map<String, Object> getProductDetails(String productName) {
		Map<String, Object> productDetails = new HashMap<>();
		String query = "SELECT product_name, price, image_path, product_quantity FROM products WHERE product_name = ?";

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, productName);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				productDetails.put("productName", rs.getString("product_name"));
				productDetails.put("price", rs.getDouble("price"));
				productDetails.put("imagePath", rs.getString("image_path"));
				productDetails.put("productQuantity", rs.getDouble("product_quantity"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle the exception properly
		}

		return productDetails;
	}

	@FXML
	private void handleCancelOrderAction() {
		orders selectedOrder = allOrdersTable.getSelectionModel().getSelectedItem();
		if (selectedOrder == null) {
			showAlert("No Order Selected", "Please select an order to cancel.", Alert.AlertType.WARNING);
			return;
		}

		// Check if the order is within 48 hours
		Timestamp orderTime = selectedOrder.getOrderTime();
		long hoursDifference = ChronoUnit.HOURS.between(orderTime.toLocalDateTime(), LocalDateTime.now());
		if (hoursDifference > 48) {
			showAlert("Cancellation Failed", "You can only cancel orders within 48 hours of placement.",
					Alert.AlertType.ERROR);
			return;
		}

		Boolean isOnWay = selectedOrder.getDeliveryStatus();
		if (isOnWay) {
			showAlert("Cancellation Failed", "You can only cancel orders that are delivered!", Alert.AlertType.ERROR);
			return;
		}

		String carrier = selectedOrder.getCarrier();
		if (carrier != "") {
			showAlert("Cancellation Failed", "You can only cancel orders that are not assigned to a carrier.",
					Alert.AlertType.ERROR);
			return;
		}

		// Use getOrderId() to retrieve the order ID
		int orderId = selectedOrder.getOrderId();
		cancelOrder(orderId); // Call the cancelOrder method with the retrieved order ID
	}

	private void cancelOrder(int orderId) {
		String sql = "DELETE FROM orderinfo WHERE id = ?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, orderId);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				showAlert("Order Cancelled", "Your order has been successfully cancelled.",
						Alert.AlertType.INFORMATION);
				ordersShowData(); // Refresh the orders table
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Database Error", "Error occurred while accessing the database.", Alert.AlertType.ERROR);
		}
	}

	private void showAlert(String title, String content, Alert.AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	@FXML
	private void displayFruits() {
		loadProducts("fruit");
	}

	@FXML
	private void displayVegetables() {
		loadProducts("vegetable");
	}

}
