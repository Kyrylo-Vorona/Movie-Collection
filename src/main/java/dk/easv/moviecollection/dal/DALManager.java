package dk.easv.moviecollection.dal;

public class DALManager {
    private MovieDAO movieDAO;
    private CategoryDAO categoryDAO;
    private static DALManager instance;

    public static DALManager getInstance() {
        if (instance == null) {
            instance = new DALManager();
        }
        return instance;
    }

    private final ConnectionManager cm;

    private DALManager() {
        cm = new ConnectionManager();
    }

    public MovieDAO getMovieDAO() {
        if (movieDAO == null) movieDAO = new MovieDAO();

        return movieDAO;
    }

    public CategoryDAO getCategoryDAO() {
        if (categoryDAO == null) categoryDAO = new CategoryDAO();
        return categoryDAO;
    }
}
