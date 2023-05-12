import java.util.List;
import java.util.ArrayList;
public class Movie {
    private String title;
    private Integer year;
    private String director;

    private List<String> genres = new ArrayList<String>();

    private String id;

    public void setTitle(String title){
        this.title = title.replace("'", "\'");
    }

    public void setYear(Integer year){
        this.year = year;
    }

    public void setDirector(String director){
        this.director = director.replace("'", "\'");
    }

    public void setGenresList(List<String> genres) {
        this.genres = genres;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear(){
        return year;
    }

    public String getDirector() {
        return director;
    }

    public String getId() {
        return id;
    }

    public String getQuery() {
        return String.format("INSERT INTO movies VALUES('%s','%s',%d,'%s');", id, title, year, director);

    }

    public String getRatingsQuery() {
        return String.format("INSERT INTO ratings VALUES('%s', 0.0, 0);", id);
    }

    public List<String> getGenresList() {
        return genres;
    }

    public String toString(){
        return String.format("%s - %s (%s) %s %s",
                title,
                director == null ? "unknown" : director,
                year == null ? "unknown" : year.toString(),
                genres == null ? "unknown" : genres,
                id == null ? "unknown" : id);
    }
}
