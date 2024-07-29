package app;

import Controller.AdminInterfaceController;
import Controller.CourierController;
import Controller.CustomerInterfaceController;
import Controller.SignInController;
import Controller.SignUpController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Greengrocer extends Application {

	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		loadLoginScreen();
	}

	public void loadLoginScreen() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/LoginScreen.fxml"));
			Parent root = loader.load();
			SignInController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.setScene(new Scene(root));
			primaryStage.setTitle("Login Screen");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadSignUpScreen() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/signup.fxml"));
			Parent root = loader.load();
			SignUpController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.setScene(new Scene(root));
			primaryStage.setTitle("Signup Screen");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadCustomerInterface(String username) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/CustomerInterface.fxml"));

			Parent root = loader.load();
			CustomerInterfaceController controller = loader.getController();
			controller.setMainApp(this);

			primaryStage.setScene(new Scene(root, 1100, 600)); // Adjust the scene size as necessary
			primaryStage.setTitle("Customer " + username + " Interface");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadAdminInterface(String username) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/dashboard.fxml"));

			Parent root = loader.load();
			AdminInterfaceController controller = loader.getController();
			controller.setUsername(username);
			controller.setMainApp(this);

			primaryStage.setScene(new Scene(root, 1100, 600)); // Adjust the scene size as necessary
			primaryStage.setTitle("Admin Interface");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadCourierInterface(String username) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/CarrierInterface.fxml"));
			Parent root = loader.load();
			CourierController controller = loader.getController();
			controller.setMainApp(this);
			controller.setUsername(username);

			primaryStage.setScene(new Scene(root, 1100, 600)); // Adjust the scene size as necessary
			primaryStage.setTitle("Courier Interface");
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
