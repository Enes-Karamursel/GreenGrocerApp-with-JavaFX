package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import app.Greengrocer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CourierController {

	@FXML
	private TableView<orders> avaliableOrdersTable;

	@FXML
	private TableColumn<orders, String> avaliableOrderID;

	@FXML
	private TableColumn<orders, String> avaliableOrderDT;

	@FXML
	private TableColumn<orders, String> avaliableOrderUser;

	@FXML
	private TableColumn<orders, String> avaliableOrderItems;

	@FXML
	private TableColumn<orders, String> avaliableOrderAddress;
	@FXML
	private TableColumn<orders, String> avaliableOrderTotal;

	@FXML
	private TableView<orders> currentOrdersTable;

	@FXML
	private TableColumn<orders, String> currentOrderID;

	@FXML
	private TableColumn<orders, String> currentOrderDT;

	@FXML
	private TableColumn<orders, String> currentOrderUser;

	@FXML
	private TableColumn<orders, String> currentOrderItems;

	@FXML
	private TableColumn<orders, String> currentOrderAddress;
	@FXML
	private TableColumn<orders, String> currentOrderTotal;

	@FXML
	private TableView<orders> completedOrdersTable;

	@FXML
	private TableColumn<orders, String> completedOrderID;

	@FXML
	private TableColumn<orders, String> completedOrderDT;

	@FXML
	private TableColumn<orders, String> completedOrderUser;

	@FXML
	private TableColumn<orders, String> completedOrderItems;

	@FXML
	private TableColumn<orders, String> completedOrderAddress;
	@FXML
	private TableColumn<orders, String> completedOrderTotal;

	@FXML
	private ListView<String> availableDeliveriesListView; // ListView for available deliveries

	private Connection connect, connect2;
	private PreparedStatement prepare;
	private Statement statement, statement2;
	private ResultSet result;
	private PreparedStatement prepare2;
	private ResultSet result2;

	String username = SignInController.getUsername();

	public void initialize() {
		ordersShowData();
		currentOrdersShowData();
		completedOrdersShowData();
	}

	public void handleSignOut() {
		mainApp.loadLoginScreen();
	}

	public ObservableList<orders> ordersListData() {
		ObservableList<orders> listData = FXCollections.observableArrayList();

		String sql = "SELECT oi.id, oi.ordertime, oi.deliverytime, u.username AS user_name, oi.carrier, oi.isdelivered, oi.totalcost "
				+ "FROM orderinfo oi " + "JOIN users u ON oi.user_id = u.id " + "WHERE oi.carrier IS NULL";

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

		avaliableOrderDT.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
		avaliableOrderUser.setCellValueFactory(new PropertyValueFactory<>("userName"));

		avaliableOrderItems.setCellValueFactory(cellData -> {
			List<orderproducts> orderItemsList = cellData.getValue().getOrderItems();
			String formattedOrderItems = orderItemsList.stream()
					.map(orderProduct -> orderProduct.getName() + ": " + orderProduct.getQuantity())
					.collect(Collectors.joining("\n"));

			// Return the formatted String
			return new SimpleStringProperty(formattedOrderItems);
		});

		avaliableOrderTotal.setCellValueFactory(new PropertyValueFactory<>("cost"));

		avaliableOrdersTable.setItems(availableOrders);
		avaliableOrderID.setCellValueFactory(new PropertyValueFactory<>("orderId"));

	}

	public ObservableList<orders> currentOrdersListData() {
		ObservableList<orders> listData = FXCollections.observableArrayList();

		String sql = "SELECT oi.id, oi.ordertime, oi.deliverytime, u.username AS user_name, oi.carrier, oi.isdelivered, oi.totalcost "
				+ "FROM orderinfo oi " + "JOIN users u ON oi.user_id = u.id "
				+ "WHERE oi.carrier = ? and oi.isdelivered = 0";

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

	private ObservableList<orders> currentAvailableOrders;

	public void currentOrdersShowData() {
		currentAvailableOrders = currentOrdersListData();

		currentOrderDT.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
		currentOrderUser.setCellValueFactory(new PropertyValueFactory<>("userName"));

		currentOrderItems.setCellValueFactory(cellData -> {
			List<orderproducts> orderItemsList = cellData.getValue().getOrderItems();
			String formattedOrderItems = orderItemsList.stream()
					.map(orderProduct -> orderProduct.getName() + ": " + orderProduct.getQuantity())
					.collect(Collectors.joining("\n"));

			// Return the formatted String
			return new SimpleStringProperty(formattedOrderItems);
		});

		currentOrderTotal.setCellValueFactory(new PropertyValueFactory<>("cost"));

		currentOrdersTable.setItems(currentAvailableOrders);
		currentOrderID.setCellValueFactory(new PropertyValueFactory<>("orderId"));

	}

	public ObservableList<orders> completedOrdersListData() {
		ObservableList<orders> listData = FXCollections.observableArrayList();

		String sql = "SELECT oi.id, oi.ordertime, oi.deliverytime, u.username AS user_name, oi.carrier, oi.isdelivered, oi.totalcost "
				+ "FROM orderinfo oi " + "JOIN users u ON oi.user_id = u.id "
				+ "WHERE oi.carrier = ? and oi.isdelivered = 1";

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

	private ObservableList<orders> currentCompletedOrders;

	public void completedOrdersShowData() {
		currentCompletedOrders = completedOrdersListData();

		completedOrderDT.setCellValueFactory(new PropertyValueFactory<>("deliveryTime"));
		completedOrderUser.setCellValueFactory(new PropertyValueFactory<>("userName"));

		completedOrderItems.setCellValueFactory(cellData -> {
			List<orderproducts> orderItemsList = cellData.getValue().getOrderItems();
			String formattedOrderItems = orderItemsList.stream()
					.map(orderProduct -> orderProduct.getName() + ": " + orderProduct.getQuantity())
					.collect(Collectors.joining("\n"));

			// Return the formatted String
			return new SimpleStringProperty(formattedOrderItems);
		});

		completedOrderTotal.setCellValueFactory(new PropertyValueFactory<>("cost"));

		completedOrdersTable.setItems(currentCompletedOrders);
		completedOrderID.setCellValueFactory(new PropertyValueFactory<>("orderId"));

	}

	public void selectItem() {

		orders catData = avaliableOrdersTable.getSelectionModel().getSelectedItem();

		int num = avaliableOrdersTable.getSelectionModel().getSelectedIndex();

		if ((num - 1) < -1) {
			return;
		}

		Integer orderid = catData.getOrderId();

		String sql = "UPDATE orderinfo SET carrier = ? WHERE id = ?";

		connect = database.connectDb();

		try {
			prepare = connect.prepareStatement(sql);
			prepare.setString(1, username);
			prepare.setInt(2, orderid);
			prepare.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		ordersShowData();
		currentOrdersShowData();
		completedOrdersShowData();

	}

	public void completeTransfer() {

		orders catData = currentOrdersTable.getSelectionModel().getSelectedItem();

		int num = currentOrdersTable.getSelectionModel().getSelectedIndex();

		if ((num - 1) < -1) {
			return;
		}

		Integer orderid = catData.getOrderId();

		String sql = "UPDATE orderinfo SET isdelivered = 1 WHERE id = ?";

		connect = database.connectDb();

		try {
			prepare = connect.prepareStatement(sql);
			prepare.setInt(1, orderid);
			prepare.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		ordersShowData();
		currentOrdersShowData();
		completedOrdersShowData();

	}

	private String getCustomerName(int userId) {
		String customerName = "";
		String query = "SELECT name FROM users WHERE id = ?";

		try (Connection conn = database.connectDb(); PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				customerName = rs.getString("name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return customerName;
	}

	public void setMainApp(Greengrocer mainApp) {
		this.mainApp = mainApp;
	}

	private Greengrocer mainApp;

	public void setUsername(String username) {
		// TODO Auto-generated method stub

	}

}