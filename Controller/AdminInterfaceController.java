package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import app.Greengrocer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class AdminInterfaceController {

	private Greengrocer mainApp;

	@FXML
	private TableColumn<Courier, String> courierUsernameColumn;
	@FXML
	private Button DashboardButton; // Button for "Vegetables"
	@FXML
	private Button ItemButton; // Button for "Fruits"
	@FXML
	private Button CourierButton; // Button for "Cart"
	@FXML
	private Button OrderButton; // Button for "SignOut"
	@FXML
	private Label Username; //
	@FXML
	private AnchorPane itemanchor;
	@FXML
	private AnchorPane dashboardanchor;
	@FXML
	private AnchorPane courierAnchor;
	@FXML
	private AnchorPane orderanchor;
	@FXML
	private ComboBox<String> itemTypes;
	@FXML
	private TextField itemName;
	@FXML
	private TextField itemPrice;
	@FXML
	private TextField itemThreshold;
	@FXML
	private TextField courierUsernameField;
	@FXML
	private TextField courierPasswordField;
	@FXML
	private TextField itemStock;
	@FXML
	private TextField imagePath;
	@FXML
	private TableColumn<items, String> availableItemName;

	@FXML
	private TableColumn<items, String> availableItemPrice;

	@FXML
	private TableColumn<items, String> availableItemThreshold;

	@FXML
	private TableColumn<items, String> availableItemStock;

	@FXML
	private TableColumn<items, String> availableItemType;
	@FXML
	private TableColumn<items, String> availableItemPath;
	@FXML
	private TableView<items> availableItemTable;
	@FXML
	private TableView<Courier> courierTable;
	@FXML
	private TableView<orders> allOrdersTable;
	@FXML
	private TableColumn<orders, String> orderTime;

	@FXML
	private TableColumn<orders, String> orderRequestedTime;

	@FXML
	private TableColumn<orders, String> orderUser;

	@FXML
	private TableColumn<orders, String> orderCarrier;

	@FXML
	private TableColumn<orders, String> orderStatus;
	@FXML
	private TableColumn<orders, String> orderTotal;
	@FXML
	private TableColumn<orders, String> orderItems;
	@FXML
	private BarChart<?, ?> noOfOrdersChart;
	@FXML
	private BarChart<?, ?> incomeChart;

	private String[] categories = { "Fruit", "Vegetable" };

	private Connection connect, connect2;
	private PreparedStatement prepare;
	private Statement statement, statement2;
	private ResultSet result;
	private PreparedStatement prepare2;
	private ResultSet result2;
	String username = SignInController.getUsername();
	String password = SignInController.getPassword();

	public void dashboardNOCCChart() {

		try {

			noOfOrdersChart.getData().clear();

			String sql = "SELECT DATE_FORMAT(ordertime, '%Y-%m-%d'), COUNT(id) FROM orderinfo GROUP BY ordertime ORDER BY TIMESTAMP(ordertime) ASC LIMIT 10";

			connect = database.connectDb();

			XYChart.Series chart = new XYChart.Series();

			prepare = connect.prepareStatement(sql);
			result = prepare.executeQuery();

			while (result.next()) {
				chart.getData().add(new XYChart.Data<String, Integer>(result.getString(1), result.getInt(2)));
			}

			noOfOrdersChart.getData().add(chart);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void dashboardIncomeChart() {

		try {

			incomeChart.getData().clear();

			String sql = "SELECT DATE_FORMAT(ordertime, '%Y-%m-%d'), SUM(totalcost) FROM orderinfo GROUP BY ordertime ORDER BY TIMESTAMP(ordertime) ASC LIMIT 10";

			connect = database.connectDb();

			XYChart.Series chart = new XYChart.Series();

			prepare = connect.prepareStatement(sql);
			result = prepare.executeQuery();

			while (result.next()) {
				chart.getData().add(new XYChart.Data<String, Double>(result.getString(1), result.getDouble(2)));
			}

			incomeChart.getData().add(chart);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void availableItemType() {
		List<String> listCat = new ArrayList<>();

		for (String data : categories) {
			listCat.add(data);
		}

		ObservableList listData = FXCollections.observableArrayList(listCat);
		itemTypes.setItems(listData);

	}

	public void setUsername(String username) {
		Username.setText("Welcome, " + username + "!");
	}

	public void setMainApp(Greengrocer mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void initialize() {
		availableItemShowData();
		dashboardNOCCChart();
		dashboardIncomeChart();
		showCourierData();
	}

	public void switchForm(ActionEvent event) {

		if (event.getSource() == DashboardButton) {
			dashboardanchor.setVisible(true);
			itemanchor.setVisible(false);
			orderanchor.setVisible(false);
			courierAnchor.setVisible(false);
			dashboardNOCCChart();
			dashboardIncomeChart();

		} else if (event.getSource() == ItemButton) {
			dashboardanchor.setVisible(false);
			itemanchor.setVisible(true);
			orderanchor.setVisible(false);
			courierAnchor.setVisible(false);
			availableItemType();
			availableItemShowData();

		} else if (event.getSource() == CourierButton) {
			dashboardanchor.setVisible(false);
			itemanchor.setVisible(false);
			orderanchor.setVisible(false);
			courierAnchor.setVisible(true);

		} else if (event.getSource() == OrderButton) {
			dashboardanchor.setVisible(false);
			itemanchor.setVisible(false);
			orderanchor.setVisible(true);
			courierAnchor.setVisible(false);
			ordersShowData();

		}

	}

	public class Courier {
		private final SimpleStringProperty username;

		public Courier(String username) {
			this.username = new SimpleStringProperty(username);
		}

		public String getUsername() {
			return username.get();
		}

		public void setUsername(String username) {
			this.username.set(username);
		}

		public SimpleStringProperty usernameProperty() {
			return username;
		}
	}

	public ObservableList<Courier> fetchCourierData() {
		ObservableList<Courier> couriers = FXCollections.observableArrayList();
		String sql = "SELECT username FROM users WHERE role = 'courier'";

		try (Connection conn = database.connectDb();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				couriers.add(new Courier(rs.getString("username")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle exception
		}
		return couriers;
	}

	@FXML
	public void showCourierData() {
		ObservableList<Courier> courierList = fetchCourierData();

		// Set up the column to display the username
		courierUsernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

		// Set the data in the TableView
		courierTable.setItems(courierList);
	}

	public void addItem() {

		String sql = "INSERT INTO products (product_name, price, threshold, product_quantity, category,image_path,doubled_price) "
				+ "VALUES(?,?,?,?,?,?,FALSE)";

		connect = database.connectDb();

		try {
			prepare = connect.prepareStatement(sql);
			prepare.setString(1, itemName.getText());
			prepare.setString(2, itemPrice.getText());
			prepare.setString(3, itemThreshold.getText());
			prepare.setString(4, itemStock.getText());
			prepare.setString(5, (String) itemTypes.getSelectionModel().getSelectedItem());
			prepare.setString(6, imagePath.getText());

			Alert alert;

			if (itemName.getText().isEmpty() || itemPrice.getText().isEmpty() || itemThreshold.getText().isEmpty()
					|| itemStock.getText().isEmpty() || itemTypes.getSelectionModel() == null
					|| imagePath.getText().isEmpty()) {

				alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Message");
				alert.setHeaderText(null);
				alert.setContentText("Please fill all blank fields");
				alert.showAndWait();

			} else {

				String checkData = "SELECT product_name FROM products WHERE product_name = '" + itemName.getText()
						+ "'";

				connect = database.connectDb();

				statement = connect.createStatement();
				result = statement.executeQuery(checkData);

				if (result.next()) {
					alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error Message");
					alert.setHeaderText(null);
					alert.setContentText("Product: " + itemName.getText() + " is already exist!");
					alert.showAndWait();
				} else {
					prepare.executeUpdate();

					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Message");
					alert.setHeaderText(null);
					alert.setContentText("Successfully Added!");
					alert.showAndWait();

					// TO SHOW THE DATA
					availableItemShowData();
					// TO CLEAR THE FIELDS
					clearInput();

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteItem() {

		String sql = "DELETE FROM products WHERE product_name = '" + itemName.getText() + "'";

		connect = database.connectDb();

		try {

			Alert alert;

			if (itemName.getText().isEmpty()) {
				alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Message");
				alert.setHeaderText(null);
				alert.setContentText("Please select an item first!");
				alert.showAndWait();
			} else {

				alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Message");
				alert.setHeaderText(null);
				alert.setContentText("Are you sure you want to DELETE Name: " + itemName.getText() + "?");

				Optional<ButtonType> option = alert.showAndWait();

				if (option.get().equals(ButtonType.OK)) {

					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Message");
					alert.setHeaderText(null);
					alert.setContentText("Successfully Deleted!");
					alert.showAndWait();

					statement = connect.createStatement();
					statement.executeUpdate(sql);

					// TO SHOW THE DATA
					availableItemShowData();
					// TO CLEAR THE FIELDS
					clearInput();

				} else {
					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Message");
					alert.setHeaderText(null);
					alert.setContentText("Cancelled.");
					alert.showAndWait();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateItem() {
		String sql = "UPDATE products SET product_name = '" + itemName.getText() + "', category = '"
				+ itemTypes.getSelectionModel().getSelectedItem() + "', price = '" + itemPrice.getText()
				+ "', threshold = '" + itemThreshold.getText() + "', product_quantity = '" + itemStock.getText()
				+ "', image_path = '" + imagePath.getText() + "' WHERE product_name = '" + itemName.getText() + "'";

		connect = database.connectDb();

		try {

			Alert alert;

			if (itemName.getText().isEmpty() || itemPrice.getText().isEmpty() || itemThreshold.getText().isEmpty()
					|| itemStock.getText().isEmpty() || itemTypes.getSelectionModel() == null
					|| imagePath.getText().isEmpty()) {

				alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error Message");
				alert.setHeaderText(null);
				alert.setContentText("Please fill all blank fields");
				alert.showAndWait();
			} else {

				alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Message");
				alert.setHeaderText(null);
				alert.setContentText("Are you sure you want to UPDATE Product: " + itemName.getText() + "?");

				Optional<ButtonType> option = alert.showAndWait();

				if (option.get().equals(ButtonType.OK)) {

					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Message");
					alert.setHeaderText(null);
					alert.setContentText("Successfully Updated!");
					alert.showAndWait();

					statement = connect.createStatement();
					statement.executeUpdate(sql);

					String price = "UPDATE products SET price = price * 2, doubled_price = 1 WHERE product_quantity < threshold AND doubled_price = 0";
					prepare = connect.prepareStatement(price);
					prepare.executeUpdate();

					String price2 = "UPDATE products SET price = price / 2, doubled_price = 0 WHERE product_quantity > threshold AND doubled_price = 1";
					prepare = connect.prepareStatement(price2);
					prepare.executeUpdate();

					// TO SHOW THE DATA
					availableItemShowData();
					// TO CLEAR THE FIELDS
					clearInput();

				} else {
					alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Information Message");
					alert.setHeaderText(null);
					alert.setContentText("Cancelled.");
					alert.showAndWait();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void selectItem() {

		items catData = availableItemTable.getSelectionModel().getSelectedItem();

		int num = availableItemTable.getSelectionModel().getSelectedIndex();

		if ((num - 1) < -1) {
			return;
		}

		itemName.setText(catData.getName());
		itemPrice.setText(Double.toString(catData.getPrice()));
		itemThreshold.setText(Double.toString(catData.getThreshold()));
		itemStock.setText(Double.toString(catData.getStock()));
		itemTypes.getSelectionModel().select(catData.getType());
		imagePath.setText(catData.getPath());

	}

	public void clearInput() {
		itemName.setText("");
		itemPrice.setText("");
		itemThreshold.setText("");
		itemStock.setText("");
		itemTypes.getSelectionModel().clearSelection();
		imagePath.setText("");
	}

	public ObservableList<items> availableItemListData() {

		ObservableList<items> listData = FXCollections.observableArrayList();

		String sql = "SELECT * FROM products";

		connect = database.connectDb();

		try {
			prepare = connect.prepareStatement(sql);
			result = prepare.executeQuery();

			items cat;

			while (result.next()) {
				cat = new items(result.getString("product_name"), result.getDouble("price"),
						result.getDouble("threshold"), result.getDouble("product_quantity"),
						result.getString("category"), result.getString("image_path"),
						result.getBoolean("doubled_price"));

				listData.add(cat);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return listData;
	}

	private ObservableList<items> availableFDList;

	public void availableItemShowData() {
		availableFDList = availableItemListData();

		availableItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
		availableItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
		availableItemThreshold.setCellValueFactory(new PropertyValueFactory<>("threshold"));
		availableItemStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
		availableItemType.setCellValueFactory(new PropertyValueFactory<>("type"));
		availableItemPath.setCellValueFactory(new PropertyValueFactory<>("path"));

		availableItemTable.setItems(availableFDList);

	}

	public ObservableList<orders> ordersListData() {
		ObservableList<orders> listData = FXCollections.observableArrayList();

		String sql = "SELECT oi.id, oi.ordertime, oi.deliverytime, u.username AS user_name, oi.carrier, oi.isdelivered, oi.totalcost "
				+ "FROM orderinfo oi " + "JOIN users u ON oi.user_id = u.id";

		connect = database.connectDb();

		try {
			prepare = connect.prepareStatement(sql);
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
		orderUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
		orderCarrier.setCellValueFactory(new PropertyValueFactory<>("carrier"));
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
	// Method to add a new courier to the database
	public void addCourier() {
		String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'courier')";

		try (Connection conn = database.connectDb(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// Get the username and password from the text fields
			String username = courierUsernameField.getText();
			String password = courierPasswordField.getText(); // You should hash the password before storing it

			// Check if username and password fields are not empty
			if (username.isEmpty() || password.isEmpty()) {
				// Handle the error, show an alert or log
				showAlert("Error", "Username and password fields cannot be empty.", Alert.AlertType.ERROR);
				return;
			}

			pstmt.setString(1, username);
			pstmt.setString(2, password); // Ideally, this should be hashed
			int result = pstmt.executeUpdate();

			if (result > 0) {
				// Show success message
				showAlert("Success", "Courier added successfully.", Alert.AlertType.INFORMATION);
				// Optionally, clear the fields after successful insertion
				courierUsernameField.clear();
				courierPasswordField.clear();
				showCourierData();
			} else {
				// Handle the case where the courier couldn't be added
				showAlert("Error", "Unable to add courier.", Alert.AlertType.ERROR);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			// Show error message
			showAlert("Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
		}
	}

	private void showAlert(String title, String message, Alert.AlertType alertType) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	// Method to remove a courier from the database
	@FXML
	public void removeCourier() {
		Courier selectedCourier = courierTable.getSelectionModel().getSelectedItem();
		if (selectedCourier == null) {
			showAlert("Error", "No courier selected.", Alert.AlertType.ERROR);
			return;
		}

		String sql = "DELETE FROM users WHERE username = ? AND role = 'courier'";

		try (Connection conn = database.connectDb(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, selectedCourier.getUsername());
			int result = pstmt.executeUpdate();

			if (result > 0) {
				showAlert("Success", "Courier removed successfully.", Alert.AlertType.INFORMATION);
				showCourierData(); // Refresh the courier list
			} else {
				showAlert("Error", "Unable to remove courier.", Alert.AlertType.ERROR);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert("Database Error", "An error occurred while accessing the database.", Alert.AlertType.ERROR);
		}
	}

	@FXML
	private void handleSignOutAction() {
		// Logic for handling sign out button click
		if (mainApp != null) {
			mainApp.loadLoginScreen();
		}
	}

	// Additional methods as needed
}