package UI;

import controller.RegistrationSystem;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Course;
import model.Student;
import javafx.scene.control.*;
import model.Teacher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class UI {
    private Stage window;
    private RegistrationSystem controller;
    private TableView<Student> table;
    private TableView<Course> courseTable;
    private long currentTeacher;

    public UI(Stage window) {
        this.controller = new RegistrationSystem();
        this.window = window;
    }

    /**
     * Starts the application in teacher mode and student mode
     */
    public void displayUi() {
        this.loginWindow(true);
        this.loginWindow(false);
    }

    /**
     * Teacher window for viewing students
     *
     * @param id      teacher's ID
     * @param name    teacher's name
     * @param surname teacher's surname
     */
    private void teacherWindow(long id, String name, String surname) throws SQLException {
        window.setTitle("Teacher " + id + " : " + name + " " + surname);

        TableColumn<Student, String> studentName = new TableColumn<>("Name");
        studentName.setMinWidth(200);
        studentName.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Student, String> studentSurname = new TableColumn<>("Surname");
        studentSurname.setMinWidth(200);
        studentSurname.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Student, Integer> studentCredits = new TableColumn<>("Credits");
        studentCredits.setMinWidth(200);
        studentCredits.setCellValueFactory(new PropertyValueFactory<>("TotalCredits"));

        table = new TableView<>();
        table.getColumns().addAll(studentName, studentSurname, studentCredits);
        table.setItems(this.controller.observableStudents(id));

        Scene scene = new Scene(table);
        window.setScene(scene);
        window.show();
    }

    /**
     * Teacher window for viewing and interacting with courses
     *
     * @param id      student's ID
     * @param name    student's name
     * @param surname student's surname
     */
    private void studentWindow(long id, String name, String surname) throws SQLException {
        Stage studentWindow = new Stage();
        studentWindow.setTitle("Student " + id + " : " + name + " " + surname);

        TableColumn<Course, String> courseId = new TableColumn<>("ID");
        courseId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Course, String> courseName = new TableColumn<>("Course Name");
        courseName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Course, Integer> courseCredits = new TableColumn<>("Credit");
        courseCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));

        TableColumn<Course, Integer> courseEnrollment = new TableColumn<>("Max Enrollment");
        courseEnrollment.setCellValueFactory(new PropertyValueFactory<>("maxEnrollment"));

        courseTable = new TableView<>();
        courseTable.getColumns().addAll(courseId, courseName, courseCredits, courseEnrollment);
        courseTable.setItems(this.controller.observableCourses(id));

        TextField enrollInput = new TextField();
        enrollInput.setPromptText("Course ID");
        Button enrollButton = new Button("Enroll");
        Button leaveButton = new Button("Leave");
        Label studentCredits = new Label("Current credits: " + this.controller.findOneStudent(id).getTotalCredits());


        Label messageBox = new Label("");

        HBox courseInteraction = new HBox();
        courseInteraction.getChildren().addAll(enrollInput, enrollButton, leaveButton,studentCredits);

        VBox layout = new VBox();
        layout.getChildren().addAll(courseTable, courseInteraction, messageBox);

        enrollButton.setOnAction(e -> {
            try {
                this.controller.register(Long.parseLong(enrollInput.getText()), id);
                messageBox.setText("Enrolled successfully");
                studentCredits.setText("Current credits: " + this.controller.findOneStudent(id).getTotalCredits());
                table.setItems(this.controller.observableStudents(this.currentTeacher));
                courseTable.setItems(this.controller.observableCourses(id));

            } catch (Exception ex) {
                //messageBox.setText("Unable to enroll");
            }
            enrollInput.clear();
        });

        leaveButton.setOnAction(e->{
            try {
                this.controller.removeStudentFromCourse(id,Long.parseLong(enrollInput.getText()));
                messageBox.setText("Left course successfully");
                studentCredits.setText("                                                     "
                        + "Current credits: " + this.controller.findOneStudent(id).getTotalCredits());
                //table.setItems(this.controller.observableStudents(this.currentTeacher));
                courseTable.setItems(this.controller.observableCourses(id));
                table.setItems(this.controller.observableStudents2(this.currentTeacher, id));
            } catch (Exception ex) {
                //messageBox.setText("Unable to leave course");
            }
            enrollInput.clear();
        });


        Scene scene = new Scene(layout);
        studentWindow.setScene(scene);
        studentWindow.show();
    }

    /**
     * Creates a confirmation window when a new user might be created
     */
    private boolean confirmationWindow() {
        AtomicBoolean answer = new AtomicBoolean(false);

        Stage confirmation = new Stage();
        confirmation.initModality(Modality.APPLICATION_MODAL);
        confirmation.setTitle("Cancel");

        Label prompt = new Label("No user");
        Button denyButton = new Button("Cancel");


        VBox confirmationLayout = new VBox();
        confirmationLayout.getChildren().addAll(prompt, denyButton);

        Scene scene = new Scene(confirmationLayout, 400, 200);

        denyButton.setOnAction(e -> confirmation.close());

        confirmation.setScene(scene);
        confirmation.showAndWait();

        return answer.get();
    }

    /**
     * Creates a login window
     */
    private void loginWindow(boolean teacherMode) {
        Stage loginStage = new Stage();

        if (teacherMode)
            loginStage.setTitle("Teacher login");
        else
            loginStage.setTitle("Student login");

        GridPane grid = new GridPane();

        Label nameLabel = new Label("First Name:");
        Label surnameLabel = new Label("Last Name:");
        Label idLabel;

        if (teacherMode)
            idLabel = new Label("Teacher Id:");
        else
            idLabel = new Label("Student Id:");

        TextField nameField = new TextField();
        TextField surnameField = new TextField();
        TextField idField = new TextField();

        nameField.setPromptText("First Name");
        surnameField.setPromptText("Last Name");
        idField.setPromptText("ID");

        Button loginConfirmButton = new Button("Log in");
        Button loginDenyButton = new Button("Cancel");

        Button newEntry = new Button("Create new entry");

        Button deletePerson = new Button("Delete this entry");

        VBox loginButtons = new VBox();

        Label messageBox1 = new Label("");

        loginButtons.getChildren().addAll(loginConfirmButton, loginDenyButton, newEntry, deletePerson, messageBox1);

        GridPane.setConstraints(nameLabel, 0, 0);
        GridPane.setConstraints(nameField, 1, 0);
        GridPane.setConstraints(surnameLabel, 0, 1);
        GridPane.setConstraints(surnameField, 1, 1);
        GridPane.setConstraints(idLabel, 0, 2);
        GridPane.setConstraints(idField, 1, 2);
        GridPane.setConstraints(loginButtons, 1, 3);
        GridPane.setConstraints(newEntry, 1, 4);
        GridPane.setConstraints(newEntry, 1, 5);
        GridPane.setConstraints(messageBox1, 3, 4);

        grid.getChildren().addAll(nameLabel, nameField, surnameLabel, surnameField, idLabel, idField, loginButtons);

        Scene loginScene = new Scene(grid, 600, 200);

        deletePerson.setOnAction(e -> {
            long id = Long.parseLong(idField.getText());

            if (teacherMode) {
                try {
                    this.controller.deleteTeacher(id);
                    messageBox1.setText("Successfully deleted");
                } catch (SQLException ex) {
                    messageBox1.setText("Can't delete this registration");
                    ex.printStackTrace();
                }
            } else {
                try {
                    this.controller.deleteStudent(id);
                    messageBox1.setText("Successfully deleted");
                } catch (SQLException ex) {
                    messageBox1.setText("Can't delete this registration");
                    ex.printStackTrace();
                }
            }


        });

        loginConfirmButton.setOnAction(e -> {
            long id = Long.parseLong(idField.getText());
            String name = nameField.getText();
            String surname = surnameField.getText();

            if (teacherMode) {
                try {
                    if (this.controller.findOneTeacher(id) != null) { //daca exista profesorul asta
                        this.currentTeacher = id;
                        this.teacherWindow(id, name, surname);
                        loginStage.close();
                    } else {
                        boolean newUser = this.confirmationWindow();
                        if (newUser) {
                            this.currentTeacher = id;
                            this.teacherWindow(id, name, surname);
                            loginStage.close();
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    if (this.controller.findOneStudent(id) != null) {
                        this.studentWindow(id, name, surname);
                        loginStage.close();
                    } else {
                        boolean newUser = this.confirmationWindow();
                        if (newUser) {
                            this.studentWindow(id, name, surname);
                            loginStage.close();
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        loginDenyButton.setOnAction(e -> loginStage.close());


        newEntry.setOnAction(e -> {
            long id = Long.parseLong(idField.getText());
            String name = nameField.getText();
            String surname = surnameField.getText();
            Teacher t = new Teacher(name, surname, id);
            Student s = new Student(name, surname, id, 0, new ArrayList<>());


            if (teacherMode) {
                try {
                    this.controller.saveTeacher(t);
                    messageBox1.setText("Successfully saved");
                } catch (SQLException ex) {
                    messageBox1.setText("Can't save this registration");
                    ex.printStackTrace();
                }
            } else {
                try {
                    this.controller.save(s);
                    messageBox1.setText("Successfully saved");
                } catch (SQLException ex) {
                    messageBox1.setText("Can't save this registration");
                    ex.printStackTrace();
                }
            }

        });


        loginStage.setScene(loginScene);
        loginStage.show();
    }

}
