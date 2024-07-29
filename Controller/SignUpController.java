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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class SignUpController {

	private Greengrocer mainApp; // Reference to the main application

	@FXML
	private TextField usernameField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private PasswordField confirmPasswordField;

	public void setMainApp(Greengrocer mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handleSignUpAction() {
		String username = usernameField.getText();
		String password = passwordField.getText();
		String confirmPassword = confirmPasswordField.getText();

		if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
			showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
			return;
		}

		if (!password.equals(confirmPassword)) {
			showAlert(Alert.AlertType.ERROR, "Registration Error", "Passwords do not match.");
			return;
		}

		if (!isStrongPassword(password)) {
			showAlert(Alert.AlertType.ERROR, "Registration Error",
					"Password is not strong enough. Please use at least one Upper, Lower, Digit and Special characters.");
			return;
		}

		if (registerNewUser(username, password)) {
			showAlert(Alert.AlertType.INFORMATION, "Registration", "Registration successful.");
			// Add a small delay before navigating to allow the alert to show
			PauseTransition delay = new PauseTransition(Duration.seconds(2));
			delay.setOnFinished(e -> mainApp.loadLoginScreen());
			delay.play();
		} else {
			showAlert(Alert.AlertType.ERROR, "Registration Error", "Registration failed. Username may already exist.");
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

	private boolean registerNewUser(String username, String password) {
		final String DB_URL = "jdbc:mysql://localhost:3306/myGreenGrocer";
		final String DB_USER = "root";
		final String DB_PASSWORD = "246835";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
			// Check if username already exists
			String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ?";
			try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql)) {
				checkUserStmt.setString(1, username);
				ResultSet resultSet = checkUserStmt.executeQuery();
				if (resultSet.next() && resultSet.getInt(1) > 0) {
					return false; // Username exists
				}
			}

			// Insert new user
			String insertSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'customer')";
			try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
				insertStmt.setString(1, username);
				insertStmt.setString(2, password);
				insertStmt.executeUpdate();
				return true; // Success
			}
		} catch (SQLException e) {
			// Log and handle specific database exceptions
			e.printStackTrace();
			return false;
		}
	}

	private boolean isStrongPassword(String password) {
		if (password.length() < 8) {
			return false; // Password too short
		}
		boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c))
				hasUpper = true;
			else if (Character.isLowerCase(c))
				hasLower = true;
			else if (Character.isDigit(c))
				hasDigit = true;
			else
				hasSpecial = true;
		}
		return hasUpper && hasLower && hasDigit && hasSpecial;
	}

	@FXML
	private void handleBackToLoginAction() {
		// Switch back to the login screen
		if (mainApp != null) {
			mainApp.loadLoginScreen();
		}
	}
}
