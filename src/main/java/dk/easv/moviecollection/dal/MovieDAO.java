package dk.easv.moviecollection.dal;

import dk.easv.moviecollection.be.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    private final ConnectionManager cm;
    public MovieDAO() { cm  = new ConnectionManager(); }

    public void deleteMovie(Movie movie) throws SQLException {
        try (Connection con = cm.getConnection()) {
            con.setAutoCommit(false);
            try (
                    PreparedStatement pstmt1 = con.prepareStatement("DELETE FROM CatMovie WHERE movie_id = ?");
                    PreparedStatement pstmt2 = con.prepareStatement("DELETE FROM Movie WHERE id = ?")
            ) {
                pstmt1.setInt(1, movie.getId());
                pstmt1.executeUpdate();
                pstmt2.setInt(1, movie.getId());
                pstmt2.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new SQLException("Could not delete movie", e);
            }
        }
    }

    public void addMovie(String name, float imdbRating, int personalRating, String filelink, LocalDate lastview) throws SQLException {
        try (Connection con = cm.getConnection()) {
            String add = "INSERT INTO Movie (name, imdb_rating, personal_rating, filelink, lastview) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(add);
            pstmt.setString(1, name);
            pstmt.setFloat(2, imdbRating);
            pstmt.setInt(3, personalRating);
            pstmt.setString(4, filelink);
            pstmt.setDate(5, Date.valueOf(lastview));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Could not add movie", e);
        }
    }

    public void editMovie(Movie movie) throws SQLException {
        try (Connection con = cm.getConnection()) {
            String edit = "UPDATE Movie SET name = ?, imdb_rating = ?, personal_rating = ?, filelink = ?, lastview = ? WHERE Id = ?";
            PreparedStatement pstmt = con.prepareStatement(edit);
            pstmt.setString(1, movie.getName());
            pstmt.setFloat(2, movie.getImdbRating());
            pstmt.setInt(3, movie.getPersonalRating());
            pstmt.setString(4, movie.getFilelink());
            pstmt.setDate(5, Date.valueOf(movie.getLastview()));
            pstmt.setInt(6, movie.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Could not edit movie", e);
        }
    }

    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();

        try (Connection con = cm.getConnection()) {
            String select = "SELECT * FROM Movie";
            PreparedStatement pstmt = con.prepareStatement(select);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getFloat("imdb_rating"),
                        rs.getInt("personal_rating"),
                        rs.getString("filelink"),
                        rs.getDate("lastview").toLocalDate()
                ));
            }

        } catch (SQLException e) {
            throw new SQLException("Could not get the list of movies", e);
        }
        return movies;
    }

    public List<Movie> getMoviesByCategory(int categoryId) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String getMoviesInCategory = "SELECT m.* FROM Movie m JOIN CatMovie cm ON m.id = cm.movie_id WHERE cm.category_id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement pstmt = con.prepareStatement(getMoviesInCategory)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getFloat("imdb_rating"),
                        rs.getInt("personal_rating"),
                        rs.getString("filelink"),
                        rs.getDate("lastview").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            throw new SQLException("Could not get the list of movies", e);
        }
        return movies;
    }

    public void addMovieToCategory(Movie movie, Category category) throws SQLException {
        try (Connection con = cm.getConnection()) {
            String insertSql =
                    "INSERT INTO CatMovie (movie_id, category_id) VALUES (?, ?)";
            PreparedStatement insert = con.prepareStatement(insertSql);
            insert.setInt(1, movie.getId());
            insert.setInt(2, category.getId());
            insert.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Could not add movie to selected category", e);
        }
    }

    public void deleteMovieFromCategory(Movie movie, Category category) throws SQLException {
        try (Connection con = cm.getConnection()) {
            String deleteSql =
                    "DELETE FROM CatMovie WHERE movie_id = ? AND category_id = ?";
            PreparedStatement insert = con.prepareStatement(deleteSql);
            insert.setInt(1, movie.getId());
            insert.setInt(2, category.getId());
            insert.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Could not delete movie from selected category", e);
        }
    }

    public List<Movie> getMoviesByFilters(String name, Float minImdbRating, List<Category> categories) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT m.*  FROM Movie m LEFT JOIN CatMovie cm ON m.id = cm.movie_id WHERE 1=1");

        if (name != null && !name.isBlank()) {
            sql.append(" AND m.name LIKE ?");
        }
        if (minImdbRating != null) {
            sql.append(" AND m.imdb_rating >= ?");
        }
        if (categories != null && !categories.isEmpty()) {
            sql.append(" AND cm.category_id IN (");
            sql.append("?,".repeat(categories.size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");
        }
        try (Connection con = cm.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            int index = 1;
            if (name != null && !name.isBlank()) {
                stmt.setString(index++, "%" + name + "%");
            }
            if (minImdbRating != null) {
                stmt.setFloat(index++, minImdbRating);
            }
            if (categories != null && !categories.isEmpty()) {
                for (Category c : categories) {
                    stmt.setInt(index++, c.getId());
                }
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                movies.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getFloat("imdb_rating"),
                        rs.getInt("personal_rating"),
                        rs.getString("filelink"),
                        rs.getDate("lastview").toLocalDate()
                ));
            }
        }
        return movies;
    }

    public List<Category> getCategoriesByMovie(int movieId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT c.id, c.name FROM Category c JOIN CatMovie cm ON c.id = cm.category_id WHERE cm.movie_id = ?";

        try (Connection con = cm.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new SQLException("Could not get categories for movie with id " + movieId, e);
        }
        return categories;
    }
}
