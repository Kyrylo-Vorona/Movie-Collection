package dk.easv.moviecollection.dal;

import dk.easv.moviecollection.be.Category;
import dk.easv.moviecollection.be.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class CategoryDAO {
    private ConnectionManager cm;

    public CategoryDAO() {
        cm = new ConnectionManager();
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        try (Connection con = cm.getConnection()) {
            String sql = "SELECT * FROM Category";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }

        } catch (SQLException e) {
            throw new SQLException("Could not get the list of categories", e);
        }
        return categories;
    }

    public void addCategory(String name) throws SQLException {
        try (Connection con = cm.getConnection()) {
            String add = "INSERT INTO Category (name) VALUES (?)";
            PreparedStatement pstmt = con.prepareStatement(add);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
        catch (SQLException e)  {
            throw new SQLException("Could not add category", e);
        }
    }

    public void deleteCategory(Category category) throws SQLException {
        try (Connection con = cm.getConnection()) {
            con.setAutoCommit(false);
            try (
                    PreparedStatement pstmt1 = con.prepareStatement("DELETE FROM CatMovie WHERE category_id = ?");
                    PreparedStatement pstmt2 = con.prepareStatement("DELETE FROM Category WHERE id = ?")
            ) {
                pstmt1.setInt(1, category.getId());
                pstmt1.executeUpdate();
                pstmt2.setInt(1, category.getId());
                pstmt2.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw new SQLException("Could not delete category", e);
            }
        }
    }

    public void editCategory(Category category) throws SQLException {
        try (Connection con = cm.getConnection()) {
            String edit = "UPDATE Category SET name = ? WHERE Id = ?";
            PreparedStatement pstmt = con.prepareStatement(edit);
            pstmt.setString(1, category.getCategoryName());
            pstmt.setInt(2, category.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Could not edit category", e);
        }
    }
}
