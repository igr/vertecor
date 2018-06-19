package com.oblac.vertecor.fx;

import com.oblac.vertecor.VertecCredentials;
import com.oblac.vertecor.VertecSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import jodd.system.SystemUtil;
import jodd.util.ClassLoaderUtil;
import jodd.util.function.Maybe;

import javax.swing.*;
import java.net.URL;

public class App extends Application {

	public static VertecSession vertec;

	LoginController loginController;
	EntryController entryController;

	Scene sceneUserPassword;
	Scene sceneEntry;
	Stage stage;

	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle("Vertecor");
		URL iconURL = ClassLoaderUtil.getResourceUrl("vertec.jpg");
		stage.getIcons().add(new Image(ClassLoaderUtil.getResourceAsStream("vertec.jpg")));

		if (SystemUtil.info().isMac()) {
			java.awt.Image image = new ImageIcon(iconURL).getImage();
//			com.apple.eawt.Application.getApplication().setDockIconImage(image);
		}

		this.stage = stage;

		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(ClassLoaderUtil.getResourceUrl("login.fxml"));
			sceneUserPassword = new Scene(loader.<AnchorPane>load());

			loginController = loader.getController();

			// bind
			loginController.app = this;
		}

		{
			final FXMLLoader loader = new FXMLLoader();
			loader.setLocation(ClassLoaderUtil.getResourceUrl("entry.fxml"));
			sceneEntry = new Scene(loader.<AnchorPane>load());

			entryController = loader.getController();

			// bind
			entryController.app = this;
		}

		letsgo();
	}

	private void letsgo() {
		Maybe<VertecCredentials> vcMaybe = vertec.loadCachedVertecCredentials();

		if (vcMaybe.isNothing()) {
			stage.setScene(sceneUserPassword);
			stage.show();
		}
		else {
			vcMaybe.consume(this::onVertexCredentialsAvailable);
		}
	}

	void onVertexCredentialsAvailable(VertecCredentials vc) {
		vertec.authAndLoadUser(vc);

		stage.setOnShown(event -> entryController.init());

		stage.setScene(sceneEntry);
		stage.show();
	}

}