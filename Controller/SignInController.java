package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import app.Greengrocer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class SignInController {

	private Greengrocer mainApp;

	@FXML
	private TextField usernameField;
	@FXML
	private TextField passwordField;
	@FXML
	private Button signUpButton;
	@FXML
	private Button signInButton;

	private static String username;

	public void setMainApp(Greengrocer mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handleSignInAction() {
		username = usernameField.getText();
		String password = passwordField.getText();

		// Validate input
		if (username.isEmpty() || password.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "Login Error", "Please fill in all fields.");
			return;
		}

		// Check credentials
		if (validateCredentials(username, password)) {

			String userRole = getUserRole(username);

			// Check user role and load the appropriate interface
			if ("customer".equalsIgnoreCase(userRole)) {
				mainApp.loadCustomerInterface(username);
			} else if ("admin".equalsIgnoreCase(userRole)) {
				mainApp.loadAdminInterface(username);
			} else if ("courier".equalsIgnoreCase(userRole))
				mainApp.loadCourierInterface(username);
		} else {
			// Credentials are invalid
			System.out.println("Login failed. Invalid username or password.");
			showAlert(Alert.AlertType.INFORMATION, "Login Failed", "Login failed. Invalid username or password.");
			// Optionally, clear the fields or take other actions
		}
	}

	private void showAlert(Alert.AlertType alertType, String title, String message) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);

		PauseTransition delay = new PauseTransition(Duration.seconds(2));
		delay.setOnFinished(event -> alert.close());
		delay.play();

		alert.show();
	}

	public boolean validateCredentials(String username, String password) {
		// Database URL, username and password
		final String DB_URL = "jdbc:mysql://localhost:3306/myGreenGrocer";
		final String DB_USER = "root";
		final String DB_PASSWORD = "246835";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setString(1, username);
				statement.setString(2, password); // In real application, use hashed password

				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next() && resultSet.getInt(1) > 0) {
					return true; // User found
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle exception properly in real applications
		}
		return false; // User not found or error occurred
	}

	@FXML
	private void handleSignUpButtonAction() {
		mainApp.loadSignUpScreen();
	}

	public static String getUsername() {
		return username;
	}

	private String getUserRole(String username) {
		final String DB_URL = "jdbc:mysql://localhost:3306/myGreenGrocer";
		final String DB_USER = "root";
		final String DB_PASSWORD = "246835";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			String sql = "SELECT role FROM users WHERE username = ?";
			try (PreparedStatement statement = conn.prepareStatement(sql)) {
				statement.setString(1, username);

				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next()) {
					return resultSet.getString("role");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// Handle exception properly in real applications
		}
		return null; // User not found or error occurred
	}

	public static String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}
}
