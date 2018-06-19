package com.oblac.vertecor.fx;


import com.oblac.vertecor.VertecCredentials;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LoginController {

	public TextField username;
	public PasswordField password;
	public Button signUpButton;

	public App app;

	public void signup(final ActionEvent e) {
		signUpButton.setDisable(true);

		final Node source = (Node) e.getSource();
		Window window = source.getScene().getWindow();
		Stage stage = (Stage) window;
		stage.close();

		Platform.runLater(() -> {
			VertecCredentials vc = new VertecCredentials();
			vc.setUsername(username.getText());
			vc.setPassword(password.getText());

			app.onVertexCredentialsAvailable(vc);
		});
	}
}
