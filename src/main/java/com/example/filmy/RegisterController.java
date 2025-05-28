package com.example.filmy;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class RegisterController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    @FXML
    protected void onRegisterClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            infoLabel.setText("Wypełnij wszystkie pola!");
            return;
        }

        try (Connection conn = Database.connect()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)");
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.executeUpdate();

            infoLabel.setText("Rejestracja udana!");


            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) emailField.getScene().getWindow();
                        stage.close();
                    });
                } catch (Exception e) {}
            }).start();

        } catch (SQLException e) {
            infoLabel.setText("Błąd - taki email już istnieje!");
        }
    }

    @FXML
    protected void onCancelClick() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
}