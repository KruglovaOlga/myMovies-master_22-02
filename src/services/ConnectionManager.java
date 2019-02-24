
package services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.swing.JOptionPane;

public final class ConnectionManager {
    
    private EntityManager em;
    private static final String PERSISTENCE_UNIT_NAME = "myMoviesPU";
    //το κλειδι μετά την εγγραφή μας στο site
    private static final String api_key = "f699b77fe2965737a2160df7d5a13e26";
    private static final String baseURL = "https://api.themoviedb.org/3/";
    public static enum REQUEST_TYPE {
        GENRE, MOVIE
    }
    
    public ConnectionManager(){
        this.em = Persistence
            .createEntityManagerFactory(PERSISTENCE_UNIT_NAME)
            .createEntityManager();
    } 
    
    public void initDb(){
        
       try{
            //Δημιουργούμε στη ΒΔ τους πίνακες 
            createDB(); 
            System.out.println("\nΗ αρχικοποίηση της Βάσης Δεδομένων ήταν "
                    + "επιτυχής.\n\n");
        }catch(Exception e){
            e.getLocalizedMessage(); 
            JOptionPane.showMessageDialog(
                    null, 
                    "Η σύνδεση με τη Βάση Δεδομένων απέτυχε! "
                        + "Ελέξτε τη σύνδεση με τον JAVADB server.", 
                    "Αποτυχία σύνδεσης", 
                    JOptionPane.ERROR_MESSAGE);
            //τερματισμός προγράμματος με σφάλμα
            Runtime.getRuntime().exit(1);
        } 
    }// end connectToDB
    
    /**
     * Κατασκευή των πινάκων της Βάσης Δεδομένων δυναμικά με την εκκίνηση 
     * της εφαρμογής. Θα πρέπει να έχουμε ήδη κατασκευάσει χειροκίνητα στο 
     * JAVA DB server, μία βάση δεδομένων με το όνομα MyMoviesDB και με ένα 
     * χρήστη με username και password το "pli24".
     */
    private void createDB(){
        // Δημιουργία πινάκων της Βάσης Δεδομένων.
        try{
            //Διαγραφή βάσης δεδομένων
            createSchema(deleteMovieTable);
            createSchema(deleteGenreTable);
            createSchema(deleteFavListTable);
            
            //Δημιουργία ΒΔ
            createSchema(createGenreTable);
            createSchema(createFavoriteListTable);
            createSchema(createMovieTable);
            createSchema(chooseDBSchema);
            
            System.out.println("\n\nΗ δημιουργία των πινάκων Βάση "
                + "Δεδομένων ήταν επιτυχής.\n");
        }catch(SQLException e){e.getLocalizedMessage();}
    }
    
    /**
     * Εκτελείται επικουρικά για την δημιουργία της Βάσης Δεδομένων. Με τη 
     * βοήθεια του EntityManager κατασκευάζει όλα τα συστατικά της βάσης.
     */
    private static void createSchema(String DbEntity) throws SQLException{
        EntityManager em = new ConnectionManager().getEm();
        try{
            em.getTransaction().begin();
            em.createNativeQuery(DbEntity).executeUpdate();
            em.flush();
            em.getTransaction().commit();       
            em.close();
        }catch(Exception e){e.getLocalizedMessage();}        
    }//end createSchema    
    
    /**
     * Συνδέεται στο API και επιστρέφει JSON
     * @param REQUEST_TYPE type (Enumerator) 
     * @return JsonObject (javax.json.JsonObject)
     */
    public JsonObject connectToAPI(REQUEST_TYPE type, int page){
        JsonObject jsonObject = null;
        URL url = null;
        try{
            // δημιουργούμε το αντικείμενο URL, με τη βοηθητική μέθοδο
            // URLWithAPI() που μας δίνει το URL string
            if(REQUEST_TYPE.GENRE == type){
            page = 1; // override, τα είδη ταινιών είναι μία σελίδα
            url = new URL(URLWithAPI(REQUEST_TYPE.GENRE, page));
            }else {
                url = new URL(URLWithAPI(REQUEST_TYPE.MOVIE, page));
            }
            // ζητάμε σύνδεση σε αυτό το URL. 
            url.openConnection().connect();
            // ξεκινάμε να διαβάσουμε από τη σύνδεση. 
            // Γνωρίζουμε πως επιστρέφει JSON
            // Πρόβλημα στην απεικόνιση κυριλλικού αλφαβήτου, το σετ US-ASCII 
            // δουλευέι μονο 
            JsonReader reader = Json.createReader(
                    new InputStreamReader(url.openStream(), "US-ASCII")); //  "US-ASCII"   "UTF-8"
            jsonObject =  reader.readObject();
            if(jsonObject.isEmpty()){
                throw new UnsupportedOperationException(
                    "Σφάλμα στην ανάκτηση του JSON");
            }
            reader.close();
            
        } catch(IOException | UnsupportedOperationException e){
            e.getLocalizedMessage();
        }
        
        return jsonObject;
    }// end connectAPI
     

    /**
     *  Σχηματίζει το String URL για την ερώτηση στο API
     * @param REQUEST_TYPE reqType
     * @return String URL
     */    
    private String URLWithAPI(REQUEST_TYPE reqType, int page){  
        // την πρώτη φορά γράφεται με το URL από το CASE GENRE
        // τις επόμενες παραμένει η σημερινή ημορμηνία 
        String url = calcDate();
        
        switch(reqType){
            case GENRE:
                url = baseURL + "genre/movie/list?" + "api_key=" + api_key;
                break;               
            case MOVIE:
                calcDate();
                url = baseURL + "discover/movie?" 
                + "with_genres=28&with_genres=10749&with_genres=878"
                + "&release_date.gte=2000-01-01"
                + "&release_date.lte=" + url
                + "&page=" + page
                + "&api_key=" + api_key;
                break;         
        }
        return url;        
    }//end URLWithAPI    
    
    /**
     *   Επιστρέφεται η σημερινή ημερομηνία σε format (YYYY-MM-DD)
     * @return String
     */
    private String calcDate(){
        String date = null;
        Calendar cal = Calendar.getInstance();
        // περνάμε τα msec της σημερινής ημερομηνίας και τα 
        // μετατρέπουμε σε ακέραιους
        cal.setTime(cal.getTime());
        
        int year = cal.get(Calendar.YEAR);
        // σε Γρηγοριανό/Ιουλιανό ημερολόγιο το Ιανουάριος ειναι το νούμερο 0
        int month = cal.get(Calendar.MONTH); 
        month++; 
        int day = cal.get(Calendar.DAY_OF_MONTH);
        
        date = Integer.toString(year) + "-" 
            + Integer.toString(month) + "-"
            + Integer.toString(day);
        return date;
    } 
    
    //Getters / Setters

    public EntityManager getEm() {
        return em;
    }

    public void setEm(EntityManager em) {
        this.em = Persistence
            .createEntityManagerFactory(PERSISTENCE_UNIT_NAME)
            .createEntityManager();
    }

    //private static final String createDBSchema = "CREATE SCHEMA PLI24";
    private static final String chooseDBSchema = "SET SCHEMA PLI24";
    private static final String createFavoriteListTable = 
        "CREATE TABLE FAVORITE_LIST(id INT PRIMARY KEY, "
        + "name VARCHAR(50))";
    private static final String createGenreTable = 
        "CREATE TABLE GENRE(id INT PRIMARY KEY, "
            + "name VARCHAR(20))";
    private static final String createMovieTable = 
        "CREATE TABLE MOVIE(" 
            + "id INT PRIMARY KEY, " 
            + "title VARCHAR(100), " 
            + "genre_id INT, " 
            + "release_date DATE," 
            + "rating FLOAT," 
            + "overview VARCHAR(500), " 
            + "favorite_list_id INT, " 
            + "FOREIGN KEY (genre_id) REFERENCES GENRE (id), " 
            + "FOREIGN KEY (favorite_list_id) REFERENCES "
                + "FAVORITE_LIST (id))";
    private static final String deleteMovieTable = "DROP TABLE MOVIE";
    private static final String deleteGenreTable = "DROP TABLE GENRE";
    private static final String deleteFavListTable = "DROP TABLE FAVORITE_LIST";
    //private static final String deleteSchema = "DROP SCHEMA PLI24 RESTRICT";    

}//end class
