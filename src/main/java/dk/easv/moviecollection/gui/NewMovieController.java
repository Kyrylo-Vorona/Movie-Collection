package dk.easv.moviecollection.gui;

import dk.easv.moviecollection.be.Movie;
import dk.easv.moviecollection.bll.Logic;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class NewMovieController {
    @FXML
    private TextField  nameTextField;
    @FXML
    private TextField imdbRatingTextField;
    @FXML
    private TextField personalRatingTextField;
    @FXML
    private TextField filelinkTextField;
    @FXML
    private Label errorLabel;
    private Movie movie;
    private Logic logic = Logic.getInstance();

    public void setMovie(Movie movie) {
        this.movie = movie;
        if (movie != null) {
            nameTextField.setText(movie.getName());
            imdbRatingTextField.setText(String.valueOf(movie.getImdbRating()));
            personalRatingTextField.setText(String.valueOf(movie.getPersonalRating()));
            filelinkTextField.setText(movie.getFilelink());
        }
    }

    @FXML
    private void onSave(ActionEvent actionEvent) {
        if (nameTextField.getText().isEmpty() || imdbRatingTextField.getText().isEmpty() || personalRatingTextField.getText().isEmpty() || filelinkTextField.getText().isEmpty()) {
            errorLabel.setText("Please fill all the fields");
            return;
        }

        if(Float.parseFloat(imdbRatingTextField.getText()) < 0.0 || Float.parseFloat(imdbRatingTextField.getText()) > 10.0) {
            errorLabel.setText("Please enter a valid IMDb rating (0.0 - 10.0)");
        }

        try {
            String name = nameTextField.getText();
            float imdb = Float.parseFloat(imdbRatingTextField.getText());
            int personal = Integer.parseInt(personalRatingTextField.getText());
            String filelink = filelinkTextField.getText();

            if (movie == null) {
                logic.addMovie(name, imdb, personal, filelink, LocalDate.now());
            } else {
                movie.setName(name);
                movie.setImdbRating(imdb);
                movie.setPersonalRating(personal);
                movie.setFilelink(filelink);

                logic.editMovie(movie);
            }

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.close();
        } catch (NumberFormatException e) {
            errorLabel.setText("Ratings must be numbers");
        } catch (SQLException e) {
            errorLabel.setText("Database error");
        }
    }

    @FXML
    private void onCancelButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void chooseMovieButtonClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                filelinkTextField.setText(file.getAbsolutePath());
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
