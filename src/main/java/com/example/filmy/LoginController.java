package com.example.filmy;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    protected void onLoginClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Wypełnij wszystkie pola!");
            return;
        }

        try (Connection conn = Database.connect()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getString("password").equals(password)) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Moje Filmy");
                stage.setScene(new Scene(fxmlLoader.load(), 800, 500));
                stage.show();

                Stage currentStage = (Stage) emailField.getScene().getWindow();
                currentStage.close();
            } else {
                errorLabel.setText("Błędne dane!");
            }
        } catch (Exception e) {
            errorLabel.setText("Błąd!");
            e.printStackTrace(); // Dodaj logi do debuggowania
        }
    }

    @FXML
    protected void onRegisterClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("register-view.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Rejestracja");
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}