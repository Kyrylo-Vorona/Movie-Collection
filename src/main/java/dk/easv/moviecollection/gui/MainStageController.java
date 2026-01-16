package dk.easv.moviecollection.gui;

import dk.easv.moviecollection.be.Category;
import dk.easv.moviecollection.be.Movie;
import dk.easv.moviecollection.bll.Logic;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainStageController implements Initializable {
    @FXML
    private Label errorLabel;
    @FXML
    private TextField filter;
    @FXML
    private TableColumn<Movie, String> columnName;
    @FXML
    private TableColumn<Movie, Integer> columnPersonalRating;
    @FXML
    private TableColumn<Movie, Float> columnIMDBRating;
    @FXML
    private TableView<Movie> tableMovies;
    private ObservableList<Movie> movieList;
    @FXML
    private TableView<Category> tableCategories;
    @FXML
    private TableColumn<Category, String> columnCategory;
    private ObservableList<Category> categoryList;
    @FXML
    private ListView<Movie> moviesInCategoryList;
    private ObservableList<Movie> MoviesInCategoryList;
    @FXML
    Logic logic = Logic.getInstance();
    private Category currentCategory;
    private Movie selected;

    public void playFromMainList(MouseEvent mouseEvent) throws IOException {
        selected = tableMovies.getSelectionModel().getSelectedItem();
    }

    public void playFromCategoryList(MouseEvent mouseEvent) throws IOException {
        selected = moviesInCategoryList.getSelectionModel().getSelectedItem();
    }

    public void openMovie(ActionEvent actionEvent) throws IOException {
        if (selected == null) {
            errorLabel.setText("No movie selected");
            return;
        }
        Desktop.getDesktop().open(new File(selected.getFilelink()));
        errorLabel.setText("");
    }

    public void deleteMovie(ActionEvent actionEvent) throws SQLException {
        selected = tableMovies.getSelectionModel().getSelectedItem();
        if (selected == null) {
            errorLabel.setText("No movie selected");
            return;
        }
        logic.deleteMovie(selected);
        refreshTable();
        selected = null;
        errorLabel.setText("");
    }

    public void onNewMovie(ActionEvent actionEvent) throws SQLException, IOException {
        openMovieWindow(null);
    }

    public void onEditMovie(ActionEvent actionEvent) throws SQLException, IOException {
        selected = tableMovies.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openMovieWindow(selected);
        }
    }

    @FXML
    private void onAddCategoryClick() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Add a new category");
        dialog.setContentText("Category name:");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) {
                errorLabel.setText("Category name cannot be empty");
                return;
            }
            boolean exists = categoryList.stream()
                    .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(name));

            if (exists) {
                errorLabel.setText("Category already exists");
                return;
            }
            try {
                logic.addCategory(name);
                errorLabel.setText("");
                refreshTable();

            } catch (SQLException e) {
                errorLabel.setText("Could not add category");
                e.printStackTrace();
            }
        });
    }

    public void onDeleteCategory(ActionEvent actionEvent) throws SQLException {
        currentCategory = tableCategories.getSelectionModel().getSelectedItem();
        if (currentCategory == null) {
            errorLabel.setText("No Category selected");
            return;
        }
        logic.deleteCategory(currentCategory);
        refreshTable();
        errorLabel.setText("");
    }

    public void addMovieToCategory(ActionEvent actionEvent) throws SQLException {
        selected = tableMovies.getSelectionModel().getSelectedItem();
        currentCategory = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null || currentCategory == null) {
            errorLabel.setText("Please select a category and a movie to add");
            return;
        }

        try {
            logic.addMovieToCategory(selected, currentCategory);
            updateMoviesInCategoryView(currentCategory);
            errorLabel.setText("");
        } catch (SQLException e) {
            errorLabel.setText("Failed to add movie to category: " + e.getMessage());
        }
    }

    public void deleteMovieFromCategory(ActionEvent actionEvent) throws SQLException {
        selected = moviesInCategoryList.getSelectionModel().getSelectedItem();
        currentCategory = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null || currentCategory == null) {
            errorLabel.setText("Please select a category and a movie to remove");
            return;
        }

        try {
            logic.deleteMovieFromCategory(selected, currentCategory);
            updateMoviesInCategoryView(currentCategory);
            errorLabel.setText("");
        } catch (SQLException e) {
            errorLabel.setText("Failed to remove movie from category: " + e.getMessage());
        }
    }


    private void openMovieWindow(Movie movie) throws SQLException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("NewMovie.fxml"));
        Parent root = loader.load();
        NewMovieController controller = loader.getController();
        controller.setMovie(movie);
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setOnHidden(e -> {
            try {
                refreshTable();
            } catch (SQLException ex) {
                errorLabel.setText("Database error. Please try again.");
            }
        });
        stage.showAndWait();
    }

    @FXML
    private void onOpenFilter(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Filter.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Filter movies");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Database error. Please try again.");
        }
    }

    private void updateMoviesInCategoryView(Category category) {
        try {
            ObservableList<Movie> movies = FXCollections.observableArrayList(
                    logic.getAllMoviesByCategory(category.getId())
            );

            moviesInCategoryList.setItems(movies);

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load movies from database");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            readDataIntoList();
            showStartupWarning();
            tableCategories.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldC, newC) -> {
                        currentCategory = newC;

                        if (newC != null) {
                            updateMoviesInCategoryView(newC);
                        } else {
                            moviesInCategoryList.setItems(FXCollections.observableArrayList());
                        }
                    });

        } catch (SQLException e) {
            errorLabel.setText("Database error. Please try again.");
        }
    }


    private void readDataIntoList() throws SQLException {
        movieList = FXCollections.observableArrayList();
        movieList.addAll(logic.getAllMovies());
        tableMovies.setItems(movieList);
        categoryList = FXCollections.observableArrayList();
        categoryList.addAll(logic.getAllCategories());
        tableCategories.setItems(categoryList);
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnPersonalRating.setCellValueFactory(new PropertyValueFactory<>("personalRating"));
        columnIMDBRating.setCellValueFactory(new PropertyValueFactory<>("imdbRating"));
        columnCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
    }

    public void refreshTable() throws SQLException {
        movieList.setAll(logic.getAllMovies());
        categoryList.setAll(logic.getAllCategories());
        if (currentCategory != null) {
            moviesInCategoryList.setItems(null);
            MoviesInCategoryList = FXCollections.observableArrayList(logic.getAllMoviesByCategory(currentCategory.getId()));
            moviesInCategoryList.setItems(MoviesInCategoryList);
        }
        else {
            if (MoviesInCategoryList != null) {
                MoviesInCategoryList.clear();
            }
            moviesInCategoryList.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void onCloseButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showStartupWarning() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Reminder");
        alert.setHeaderText("Movie cleanup reminder");
        alert.setContentText(
                "Remember to delete movies with a personal rating under 6 and that have not been opened for more than 2 years"
        );
        alert.showAndWait();
    }

}
