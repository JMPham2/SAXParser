public class Star {
    private String name;
    private Integer dob;

    private String id;

    public void setName(String name){
        this.name = name.replace("'", "\'");
    }

    public void setDob(Integer dob){
        this.dob = dob;
    }

    public void setId(String id) { this.id = id; }

    public String getName(){
        return name;
    }

    public int getDob(){
        return dob;
    }
    public String getId() { return id; }

    public String getQuery(){
        if(dob == null) {
            // INSERT INTO stars (id, name) VALUES('nm0000083','Alan Miller');
            return String.format("INSERT INTO stars (id, name) VALUES('%s', '%s');", id, name);
        } else {
            // INSERT INTO stars (id, name, birthYear) VALUES('nm0000056','Paul Newman',1925);
            return String.format("INSERT INTO stars (id, name, birthYear) VALUES('%s', '%s', %d);", id, name, dob);
        }
    }

    public String toString(){
        return String.format("%s - %s %s",
                name,
                dob == null ? "unknown" : dob.toString(),
                id == null ? "unknown" : id);
    }
}
