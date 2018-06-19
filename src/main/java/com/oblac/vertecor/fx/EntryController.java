package com.oblac.vertecor.fx;

import com.oblac.vertecor.model.Phase;
import com.oblac.vertecor.model.Project;
import com.oblac.vertecor.model.ServiceType;
import com.oblac.vertecor.model.TimeEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class EntryController implements Initializable {

	public ComboBox<Project> projectCombo;
	public ComboBox<Phase> phaseCombo;
	public ComboBox<ServiceType> serviceTypeCombo;
	public TextArea descriptionText;
	public TextField hoursText;
	public DatePicker datePicker;
	public Label title;
	public Button submitButton;

	public App app;
	private Project selectedProject;
	private Phase selectedPhase;
	private ServiceType selectedServiceType;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		datePicker.setConverter(new StringConverter<LocalDate>() {
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return dateFormatter.format(date);
				} else {
					return "<Today>";
				}
			}

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, dateFormatter);
				} else {
					return null;
				}
			}
		});
	}

	public void init() {
		title.setText(App.vertec.getUser().getFullName());
		List<Project> allProjects = App.vertec.loadAllProjects();

		ObservableList<Project> options =
			FXCollections.observableArrayList(allProjects);

		projectCombo.setItems(options);
	}

	public void onProjectSelected(ActionEvent e) {
		this.selectedProject = projectCombo.getValue();

		List<Phase> allPhases = App.vertec.loadAllProjectPhases(selectedProject);

		ObservableList<Phase> options =
			FXCollections.observableArrayList(allPhases);

		phaseCombo.setItems(options);
	}

	public void onPhaseSelected(ActionEvent e) {
		this.selectedPhase = phaseCombo.getValue();

		List<ServiceType> allServiceTypes = App.vertec.loadAllServiceTypes(selectedProject);

		ObservableList<ServiceType> options =
			FXCollections.observableArrayList(allServiceTypes);

		serviceTypeCombo.setItems(options);
	}

	public void onServiceSelected(ActionEvent e) {
		this.selectedServiceType = serviceTypeCombo.getValue();
	}

	public void onSubmitClicked(final ActionEvent e) {

		// validation

		if (selectedProject == null) {
			System.err.println("Project not selected.\n");
			return;
		}
		if (selectedPhase == null) {
			System.err.println("Phase not selected.\n");
			return;
		}
		if (selectedServiceType == null) {
			System.err.println("Service type not selected.\n");
			return;
		}

		double hours;
		try {
			hours = Double.parseDouble(hoursText.getText());
		}
		catch (NumberFormatException ignore) {
			System.err.println("Invalid hours: " + hoursText.getText() + "\n");
			return;
		}

		// entering

		submitButton.setDisable(true);

		final Node source = (Node) e.getSource();
		Window window = source.getScene().getWindow();
		Stage stage = (Stage) window;
		stage.close();

		LocalDate localDate = datePicker.getValue();
		if (localDate == null) {
			localDate = LocalDate.now();
		}

		TimeEntry timeEntry = new TimeEntry();

		timeEntry
			.setProject(selectedProject)
			.setPhase(selectedPhase)
			.setServiceType(selectedServiceType)
			.setDescription(descriptionText.getText())
			.setMinutes((int) (hours * 60))
			.setDate(localDate.toString());

		Platform.runLater(() -> {
			boolean success = App.vertec.storeTimeEntry(timeEntry);

			Platform.exit();
			System.exit(success ? 0 : 1);
		});
	}

}
