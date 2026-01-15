package dk.easv.moviecollection.bll;

import dk.easv.moviecollection.be.Category;
import dk.easv.moviecollection.be.Movie;
import dk.easv.moviecollection.dal.CategoryDAO;
import dk.easv.moviecollection.dal.DALManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Logic {
    private static Logic instance;
    private Logic() {}

    public static Logic getInstance() {
        if (instance == null) {
            instance = new Logic();
        }
        return instance;
    }

    public void addMovie(String name, float imdbRating, int personalRating, String filelink, LocalDate lastview) throws SQLException {
        DALManager.getInstance().getMovieDAO().addMovie(name, imdbRating, personalRating, filelink, lastview);
    }

    public void deleteMovie(Movie movie) throws SQLException {
        DALManager.getInstance().getMovieDAO().deleteMovie(movie);
    }

    public void addMovieToCategory(Movie movie, Category category) throws SQLException {
        DALManager.getInstance().getMovieDAO().addMovieToCategory(movie, category);
    }

    public void deleteMovieFromCategory(Movie movie, Category category) throws SQLException {
        DALManager.getInstance().getMovieDAO().deleteMovieFromCategory(movie, category);
    }

    public void addCategory(String name) throws SQLException {
        DALManager.getInstance().getCategoryDAO().addCategory(name);
    }


    public void deleteCategory(Category category) throws SQLException {
        DALManager.getInstance().getCategoryDAO().deleteCategory(category);
    }

    public void editMovie(Movie movie) throws SQLException {
        DALManager.getInstance().getMovieDAO().editMovie(movie);
    }

    public List<Movie> getAllMovies() throws SQLException {
        return DALManager.getInstance().getMovieDAO().getAllMovies();
    }

    public List<Category>  getAllCategories() throws SQLException {
        return DALManager.getInstance().getCategoryDAO().getAllCategories();
    }

    public List<Movie> getAllMoviesByCategory(int categoryId) throws SQLException {
        return DALManager.getInstance().getMovieDAO().getMoviesByCategory(categoryId);
    }
}
