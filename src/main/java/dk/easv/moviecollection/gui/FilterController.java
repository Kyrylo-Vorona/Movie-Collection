package dk.easv.moviecollection.gui;

import dk.easv.moviecollection.be.Category;
import dk.easv.moviecollection.be.Movie;
import dk.easv.moviecollection.bll.Logic;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FilterController implements Initializable {
    @FXML
    private TextField nameTextField;
    @FXML
    private ComboBox<Float> ratingComboBox;
    @FXML
    private ListView<Category> categoriesList;
    @FXML
    private TableView<Movie> tableMovies;
    @FXML
    private TableColumn<Movie, String> columnName;
    @FXML
    private TableColumn<Movie, Integer> columnPersonalRating;
    @FXML
    private TableColumn<Movie, Float> columnIMDBRating;
    @FXML
    private TableColumn<Movie, String> columnCategories;

    private ObservableList<Movie> movieList;
    private ObservableList<Category> categoryList;
    private Logic logic = Logic.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        readDataIntoComboBox();
        setupTable();
        try {
            loadCategories();
            loadAllMovies();
        } catch (SQLException e) {
            showError("Could not load data");
        }
    }

    private void readDataIntoComboBox() {
        ObservableList<Float> ratings = FXCollections.observableArrayList();
        for (float i = 1; i <= 9; i++) {
            ratings.add(i);
        }
        ratingComboBox.setItems(ratings);
    }

    private void setupTable() {
        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnPersonalRating.setCellValueFactory(new PropertyValueFactory<>("personalRating"));
        columnIMDBRating.setCellValueFactory(new PropertyValueFactory<>("imdbRating"));
        columnCategories.setCellValueFactory(cellData -> {
            Movie movie = cellData.getValue();
            try {
                List<Category> categories = logic.getCategoriesByMovie(movie.getId());
                String categoriesStr = categories.stream()
                        .map(Category::getCategoryName)
                        .collect(Collectors.joining(", "));
                return new ReadOnlyStringWrapper(categoriesStr);
            } catch (SQLException e) {
                e.printStackTrace();
                return new ReadOnlyStringWrapper("");
            }
        });
    }

    private void loadCategories() throws SQLException {
        categoryList = FXCollections.observableArrayList(logic.getAllCategories());
        categoriesList.setItems(categoryList);
        categoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadAllMovies() throws SQLException {
        try {
            movieList = FXCollections.observableArrayList(logic.getAllMovies());
            tableMovies.setItems(movieList);
        } catch (SQLException e) {
            throw new SQLException("Could not load all movies", e);
        }
    }

    @FXML
    private void onApplyFilter() {
        try {
            String nameFilter = nameTextField.getText();
            Float minRating = ratingComboBox.getValue();
            if (minRating == null) {
                minRating = 0f;
            }
            List<Category> selectedCategories =
                    categoriesList.getSelectionModel().getSelectedItems();

            List<Movie> filteredMovies =
                    logic.getMoviesByFilters(nameFilter, minRating, selectedCategories);

            tableMovies.setItems(FXCollections.observableArrayList(filteredMovies));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onClearFilter() throws SQLException {
        nameTextField.clear();
        ratingComboBox.setValue(null);
        categoriesList.getSelectionModel().clearSelection();
        loadAllMovies();
    }

    @FXML
    private void onCloseButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
