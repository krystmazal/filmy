package com.example.filmy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import java.sql.*;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {
    @FXML private TableView<Film> filmTable;
    @FXML private TableColumn<Film, String> titleColumn, genreColumn, actorsColumn, ratingColumn;
    @FXML private TableColumn<Film, Boolean> watchedColumn;
    @FXML private ComboBox<String> genreFilter, ratingFilter;

    private ObservableList<Film> allFilms = FXCollections.observableArrayList();
    private ObservableList<Film> filteredFilms = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));
        actorsColumn.setCellValueFactory(new PropertyValueFactory<>("actors"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("ratingText"));
        watchedColumn.setCellValueFactory(new PropertyValueFactory<>("watched"));

        filmTable.setItems(filteredFilms);

        setupFilters();
        loadFilms();
    }

    private void setupFilters() {
        genreFilter.getItems().addAll("Wszystkie", "Akcja", "Dramat", "Komedia", "Horror", "Sci-Fi", "Romans");
        genreFilter.setValue("Wszystkie");

        ratingFilter.getItems().addAll("Wszystkie","9+", "8+", "7+", "6+", "5+","4+","3+","2+");
        ratingFilter.setValue("Wszystkie");

        genreFilter.setOnAction(e -> applyFilters());
        ratingFilter.setOnAction(e -> applyFilters());
    }

    private void applyFilters() {
        String selectedGenre = genreFilter.getValue();
        String selectedRating = ratingFilter.getValue();

        filteredFilms.clear();

        for (Film film : allFilms) {
            boolean matchesGenre = "Wszystkie".equals(selectedGenre) ||
                    film.getGenre().equals(selectedGenre);

            boolean matchesRating = "Wszystkie".equals(selectedRating);
            if (!matchesRating && film.getAverageRating() > 0) {
                int minRating = Integer.parseInt(selectedRating.replace("+", ""));
                matchesRating = film.getAverageRating() >= minRating;
            }

            if (matchesGenre && matchesRating) {
                filteredFilms.add(film);
            }
        }
    }

    @FXML
    private void onClearFilters() {
        genreFilter.setValue("Wszystkie");
        ratingFilter.setValue("Wszystkie");
        applyFilters();
    }

    private void loadFilms() {
        allFilms.clear();
        try (Connection conn = Database.connect()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM films");

            while (rs.next()) {
                Film film = new Film(rs.getString("title"), rs.getString("genre"), rs.getString("actors"));
                film.setId(rs.getInt("id"));
                film.setWatched(rs.getBoolean("watched"));
                film.setActorsRating(rs.getInt("actors_rating"));
                film.setPlotRating(rs.getInt("plot_rating"));
                film.setSceneryRating(rs.getInt("scenery_rating"));
                film.setAverageRating(rs.getDouble("average_rating"));
                allFilms.add(film);
            }
            applyFilters();
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
        ComboBox<String> genre = new ComboBox<>();
        genre.getItems().addAll("Akcja", "Dramat", "Komedia", "Horror", "Sci-Fi", "Romans");
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
            if (button == ButtonType.OK && !title.getText().isEmpty()) {
                return new Film(title.getText(), genre.getValue(), actors.getText());
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

                allFilms.add(film);
                applyFilters();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onEditFilm() {
        Film selected = filmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Wybierz film do edycji!");
            return;
        }

        Dialog<Film> dialog = new Dialog<>();
        dialog.setTitle("Edytuj film");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField title = new TextField(selected.getTitle());
        ComboBox<String> genre = new ComboBox<>();
        genre.getItems().addAll("Akcja", "Dramat", "Komedia", "Horror", "Sci-Fi", "Romans");
        genre.setValue(selected.getGenre());
        TextField actors = new TextField(selected.getActors());

        grid.add(new Label("Tytuł:"), 0, 0);
        grid.add(title, 1, 0);
        grid.add(new Label("Gatunek:"), 0, 1);
        grid.add(genre, 1, 1);
        grid.add(new Label("Aktorzy:"), 0, 2);
        grid.add(actors, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && !title.getText().isEmpty()) {
                Film editedFilm = new Film(title.getText(), genre.getValue(), actors.getText());
                editedFilm.setId(selected.getId());
                editedFilm.setWatched(selected.isWatched());
                editedFilm.setActorsRating(selected.getActorsRating());
                editedFilm.setPlotRating(selected.getPlotRating());
                editedFilm.setSceneryRating(selected.getSceneryRating());
                editedFilm.setAverageRating(selected.getAverageRating());
                return editedFilm;
            }
            return null;
        });

        Optional<Film> result = dialog.showAndWait();
        result.ifPresent(editedFilm -> {
            try (Connection conn = Database.connect()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE films SET title = ?, genre = ?, actors = ? WHERE id = ?");
                ps.setString(1, editedFilm.getTitle());
                ps.setString(2, editedFilm.getGenre());
                ps.setString(3, editedFilm.getActors());
                ps.setInt(4, editedFilm.getId());
                ps.executeUpdate();

                int index = allFilms.indexOf(selected);
                allFilms.set(index, editedFilm);
                applyFilters();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onDeleteFilm() {
        Film selected = filmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Wybierz film do usunięcia!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Potwierdź usunięcie");
        confirmDialog.setContentText("Czy na pewno chcesz usunąć film: " + selected.getTitle() + "?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = Database.connect()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM films WHERE id = ?");
                ps.setInt(1, selected.getId());
                ps.executeUpdate();

                allFilms.remove(selected);
                applyFilters();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onRateFilm() {
        Film selected = filmTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Wybierz film do oceny!");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Oceń film");
        dialog.setHeaderText("Oceń film: " + selected.getTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Slider actorsSlider = new Slider(1, 10, selected.getActorsRating() > 0 ? selected.getActorsRating() : 5);
        Slider plotSlider = new Slider(1, 10, selected.getPlotRating() > 0 ? selected.getPlotRating() : 5);
        Slider scenerySlider = new Slider(1, 10, selected.getSceneryRating() > 0 ? selected.getSceneryRating() : 5);

        actorsSlider.setShowTickLabels(true);
        plotSlider.setShowTickLabels(true);
        scenerySlider.setShowTickLabels(true);

        grid.add(new Label("Aktorzy (1-10):"), 0, 0);
        grid.add(actorsSlider, 1, 0);
        grid.add(new Label("Fabuła (1-10):"), 0, 1);
        grid.add(plotSlider, 1, 1);
        grid.add(new Label("Scenografia (1-10):"), 0, 2);
        grid.add(scenerySlider, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setActorsRating((int) actorsSlider.getValue());
            selected.setPlotRating((int) plotSlider.getValue());
            selected.setSceneryRating((int) scenerySlider.getValue());
            selected.setWatched(true);

            updateFilm(selected);
            filmTable.refresh();
        }
    }

    private void updateFilm(Film film) {
        try (Connection conn = Database.connect()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE films SET watched = ?, actors_rating = ?, plot_rating = ?, scenery_rating = ?, average_rating = ? WHERE id = ?");
            ps.setBoolean(1, film.isWatched());
            ps.setInt(2, film.getActorsRating());
            ps.setInt(3, film.getPlotRating());
            ps.setInt(4, film.getSceneryRating());
            ps.setDouble(5, film.getAverageRating());
            ps.setInt(6, film.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onShowStats() {
        int total = allFilms.size();
        int watched = (int) allFilms.stream().filter(Film::isWatched).count();
        double avgRating = allFilms.stream()
                .filter(f -> f.getAverageRating() > 0)
                .mapToDouble(Film::getAverageRating)
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

    @FXML
    private void onShowReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPORT FILMÓW ===\n\n");


        report.append("NAJLEPIEJ OCENIONE FILMY:\n");
        allFilms.stream()
                .filter(f -> f.getAverageRating() > 0)
                .sorted((f1, f2) -> Double.compare(f2.getAverageRating(), f1.getAverageRating()))
                .limit(5)
                .forEach(f -> report.append(String.format("• %s - %.1f/10\n", f.getTitle(), f.getAverageRating())));


        report.append("\nSTATYSTYKI GATUNKÓW:\n");
        allFilms.stream()
                .collect(Collectors.groupingBy(Film::getGenre, Collectors.counting()))
                .forEach((genre, count) -> report.append(String.format("• %s: %d filmów\n", genre, count)));


        double avgActors = allFilms.stream().filter(f -> f.getActorsRating() > 0).mapToInt(Film::getActorsRating).average().orElse(0);
        double avgPlot = allFilms.stream().filter(f -> f.getPlotRating() > 0).mapToInt(Film::getPlotRating).average().orElse(0);
        double avgScenery = allFilms.stream().filter(f -> f.getSceneryRating() > 0).mapToInt(Film::getSceneryRating).average().orElse(0);

        report.append("\nŚREDNIE OCENY ASPEKTÓW:\n");
        report.append(String.format("• Aktorzy: %.1f/10\n", avgActors));
        report.append(String.format("• Fabuła: %.1f/10\n", avgPlot));
        report.append(String.format("• Scenografia: %.1f/10\n", avgScenery));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Raport");
        alert.setHeaderText("Szczegółowy raport kolekcji");
        alert.setContentText(report.toString());
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }
}