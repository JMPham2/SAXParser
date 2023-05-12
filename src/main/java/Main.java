import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.sql.*;
import javax.sql.DataSource;
import java.util.ArrayList;

import java.lang.StringBuilder;

public class Main {
    private static Map<String, Star> stars;
    private static Map<String, Movie> movies;
    private static Map<String, List<String>> cast;

    public static void main(String[] args) {
        SAXStarParser spe = new SAXStarParser();
        // 'stars' maps star names to star information
        stars = spe.run();
        System.out.println("Parsed stars");

        SAXMovieParser mpe = new SAXMovieParser();
        // 'movies' maps movie ids (from XML file) to movies
        movies = mpe.run();
        System.out.println("Parsed movies");

        // 'cast' maps movie ids to star names
        SAXCastParser cpe = new SAXCastParser();
        cast = cpe.run();
        System.out.println("Parsed cast");

        // Build Error Message Log

        // Build Query
        ArrayList<String> query = new ArrayList<String>();

        // Establish Data Source
        try {
            Class.forName(LoginInfo.driver).newInstance();
            Connection connection = DriverManager.getConnection(LoginInfo.loginUrl, LoginInfo.loginUser, LoginInfo.loginPasswd);

            // Begin statements

            // Insert stars statements
            for(Entry<String, Star> entry : stars.entrySet()) {
                // If star is already in database, update the mapping
                // Otherwise, insert a new star
                PreparedStatement statement = connection.prepareStatement("SELECT * from stars WHERE name = ?");
                statement.setString(1, entry.getKey());
                ResultSet rs = statement.executeQuery();
                if(rs.next()) {
                    stars.get(entry.getKey()).setId(rs.getString("id"));
                    // query.append(String.format("# %s already in database.\n", entry.getKey()));
                } else {
                    query.add(entry.getValue().getQuery());
                }
                statement.close();
                rs.close();
            }

            System.out.println("Added star statements");

            // Insert movies statements
            for(Entry<String, Movie> entry : movies.entrySet()) {
                // If movie is already in database, update the mapping
                // Otherwise, insert a new movie
                PreparedStatement statement = connection.prepareStatement("SELECT * from movies WHERE title = ?");
                statement.setString(1, entry.getValue().getTitle());
                ResultSet rs = statement.executeQuery();
                if(rs.next()) {
                    movies.get(entry.getKey()).setId(rs.getString("id"));
                    // query.append(String.format("# %s already in database.\n", entry.getValue().getTitle()));
                } else {
                    query.add(entry.getValue().getQuery());
                    query.add(entry.getValue().getRatingsQuery());
                }
                statement.close();
                rs.close();
            }
            PreparedStatement maxGenreIdStatement = connection.prepareStatement("SELECT MAX(id) from genres");
            ResultSet grs = maxGenreIdStatement.executeQuery();
            grs.next();
            int currentGenreId = grs.getInt(1) + 1;
            maxGenreIdStatement.close();
            grs.close();

            System.out.println("Added movie statements");

            // Insert genres_in_movies statements
            for(Entry<String, Movie> entry : movies.entrySet()){
                String movieId = entry.getValue().getId();
                for(String genre : entry.getValue().getGenresList()){
                    PreparedStatement statement = connection.prepareStatement("SELECT * from genres where name = ?");
                    statement.setString(1, genre);
                    ResultSet rs = statement.executeQuery();
                    if(rs.next()) {
                        // Genre already exists in table
                        int genreId = Integer.parseInt(rs.getString("id"));
                        query.add(String.format("INSERT IGNORE INTO genres_in_movies VALUES(%d,'%s');", genreId, movieId));
                    } else {
                        // Genre does not already exist in table, add the new genre
                        // INSERT INTO genres VALUES(1,'Action');
                        PreparedStatement newGenreStatement = connection.prepareStatement("INSERT INTO genres VALUES(?,?)");
                        newGenreStatement.setInt(1, currentGenreId);
                        newGenreStatement.setString(2, genre);
                        newGenreStatement.executeUpdate();
                        newGenreStatement.close();
                        query.add(String.format("INSERT IGNORE INTO genres_in_movies VALUES(%d,'%s');", currentGenreId++, movieId));
                    }
                    statement.close();
                    rs.close();
                }
            }

            System.out.println("Added genres and genres_in_movies statements");

            int currentStarId = spe.getCurrentId();
            // Insert stars_in_movies statements
            // cast maps movie ids (from XML) to actor names
            // if movie is not
            for(Entry<String, List<String>> entry : cast.entrySet()){
                if(movies.containsKey(entry.getKey())) {
                    String movieId = movies.get(entry.getKey()).getId();
                    for(String starName : entry.getValue()) {
                        if(stars.containsKey(starName)) {
                            // star is already in stars mapping
                            String starId = stars.get(starName).getId();
                            query.add(String.format("INSERT IGNORE INTO stars_in_movies VALUES('%s','%s');", starId, movieId));
                        } else {
                            // star is not in mapping, but is in database
                            PreparedStatement starsQuery = connection.prepareStatement("SELECT * from stars WHERE name = ?");
                            starsQuery.setString(1, starName);
                            ResultSet srs = starsQuery.executeQuery();
                            if(srs.next()){
                                String starId = srs.getString("id");
                                query.add(String.format("INSERT IGNORE INTO stars_in_movies VALUES('%s','%s');", starId, movieId));
                            } else {
                                // star is not in database
                                PreparedStatement newStarStatement = connection.prepareStatement("INSERT INTO stars (id, name) VALUES(?,?)");
                                newStarStatement.setString(1, String.format("nm%07d", currentStarId));
                                newStarStatement.setString(2, starName);
                                newStarStatement.executeUpdate();
                                newStarStatement.close();
                                query.add(String.format("INSERT IGNORE INTO stars_in_movies VALUES('nm%07d','%s');", currentStarId, movieId));
                                Star star = new Star();
                                star.setName(starName);
                                star.setId(String.format("nm%07d", currentStarId++));
                                stars.put(starName, star);
                            }
                            starsQuery.close();
                            srs.close();
                        }
                    }
                } else {
                    System.out.println(String.format("Unable to find movie with id '%s'.\n", entry.getKey()));
                }
            }

            System.out.println("Added stars and stars_in_movies statements");

            // Execute and Commit Statements

            connection.setAutoCommit(false);
            System.out.println("Executing all updates");
            for(String statement : query) {
                PreparedStatement ps = connection.prepareStatement(statement);
                ps.executeUpdate();
                ps.close();
            }
            connection.commit();
            System.out.println("Committing all updates");

            connection.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
