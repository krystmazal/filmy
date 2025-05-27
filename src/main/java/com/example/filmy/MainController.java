package com.example.filmy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.sql.*;
import java.util.Optional;

public class MainController {
    @FXML private TableView<Film> filmTable;
    @FXML private TableColumn<Film, String> titleColumn, genreColumn, actorsColumn, ratingColumn;
    @FXML private TableColumn<Film, Boolean> watchedColumn;

    private ObservableList<Film> films = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        actorsColumn.setCellValueFactory(new PropertyValueFactory<>("actors"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("ratingText"));
        watchedColumn.setCellValueFactory(new PropertyValueFactory<>("watched"));

        filmTable.setItems(films);
        loadFilms();
    }

    private void loadFilms() {
        films.clear();
        try (Connection conn = Database.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM films");

            while (rs.next()) {
                Film film = new Film(rs.getString("title"), rs.getString("genre"), rs.getString("actors"));
                film.setId(rs.getInt("id"));
                film.setWatched(rs.getBoolean("watched"));
                film.setRating(rs.getInt("rating"));
                films.add(film);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddFilm() {
        Dialog<Film> dialog = new Dialog<>();
        dialog.setTitle("Dodaj film");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField title = new TextField();
        TextField genre = new TextField();
        TextField actors = new TextField();

        grid.add(new Label("Tytuł:"), 0, 0);
        grid.add(title, 1, 0);
        grid.add(new Label("Gatunek:"), 0, 1);
        grid.add(genre, 1, 1);
        grid.add(new Label("Aktorzy:"), 0, 2);
        grid.add(actors, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Film(title.getText(), genre.getText(), actors.getText());
            }
            return null;
        });

        Optional<Film> result = dialog.showAndWait();
        result.ifPresent(film -> {
            try (Connection conn = Database.connect()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO films (title, genre, actors) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getTitle());
                ps.setString(2, film.getGenre());
                ps.setString(3, film.getActors());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    film.setId(keys.getInt(1));
                }

                films.add(film);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onRateFilm() {
        Film selected = filmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Wybierz film do oceny!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Oceń film");
        dialog.setHeaderText("Oceń film: " + selected.getTitle());
        dialog.setContentText("Ocena (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(rating -> {
            try {
                int rate = Integer.parseInt(rating);
                if (rate >= 1 && rate <= 10) {
                    selected.setRating(rate);
                    selected.setWatched(true);
                    updateFilm(selected);
                    filmTable.refresh();
                }
            } catch (NumberFormatException e) {
                showAlert("Wpisz liczbę od 1 do 10!");
            }
        });
    }

    private void updateFilm(Film film) {
        try (Connection conn = Database.connect()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE films SET watched = ?, rating = ? WHERE id = ?");
            ps.setBoolean(1, film.isWatched());
            ps.setInt(2, film.getRating());
            ps.setInt(3, film.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onShowStats() {
        int total = films.size();
        int watched = (int) films.stream().filter(Film::isWatched).count();
        double avgRating = films.stream()
                .filter(f -> f.getRating() > 0)
                .mapToInt(Film::getRating)
                .average()
                .orElse(0);

        String stats = "Łączna liczba filmów: " + total + "\n" +
                "Obejrzane: " + watched + "\n" +
                "Średnia ocena: " + String.format("%.1f", avgRating);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statystyki");
        alert.setContentText(stats);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }
}