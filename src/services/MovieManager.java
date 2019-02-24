
package services;

import static java.lang.Integer.parseInt;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.swing.JOptionPane;
import model.Genre;
import model.Movie;

public class MovieManager {
    
    private static int count = 0;
    private enum TABLE{
        Movie, Genre, FavoriteList
    }
    
    public void getMovies(){
        System.out.println("Ανάκτηση ταινιών...");
        //δεν ξέρουμε ακόμα πόσες σελίδες έχουμε σαν συνολικό αποτέλεσμα, 
        // σίγουρα είναι 1. Μετά την πρώτη απάντηση θα γνωρίζουμε ακριβές νούμερο
        int pages = 1;
        java.util.Date checkpoint = null;

        // Επιβεβαίωση ορθότητας κριτηρίου για τις ημερομηνίες, για σύγκριση Date
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-DD");
        try{
            checkpoint = format.parse("2000-01-01");
        }catch(ParseException e){e.getLocalizedMessage();}

        for (int page = 1; page <= (pages); page++){
            JsonObject jsonObject = new ConnectionManager()
                .connectToAPI(ConnectionManager.REQUEST_TYPE.MOVIE, page);
            // παίρνουμε το μισό πλήθος σελίδων των αποτελεσμάτων σελίδων
            // αποτελεί ικανοποιητικό δείγμα
            if (page == 1){
                //pages = jsonObject.getJsonNumber("total_pages").intValue() /2;
                pages = 10;
                System.out.println("\n\t\tTotal Result Pages: " + pages + "\n");
            }

            System.out.println("Page: " + page);
            // τα αντικείμενα - ταινίες 
            JsonArray results = jsonObject.getJsonArray("results");
            int genre_id = 0;
            //δεν μας ενδιαφερει το ονομα του είδους, το έχουμε ηδη 
            String genre_title = "";        

            for(int i = 0; i < results.size(); i++){
                // αντικείμενο που θα του βαλουμε πληροφορίες απο το JSONObject
                // και μετα τα το περάσουμε σαν εγγραφή στη DB
                Movie movie = new Movie();
                JsonObject temp = results.getJsonObject(i);

                // ΑΝΤΙΣΤΟΙΧΙΣΗ ΠΕΔΙΩΝ
                // εύρεση του Movie.release_date
                String relDate  = temp.getJsonString("release_date").toString();
                relDate = new MovieManager().trimQuotes(relDate);
                //για καταχωρήσεις με κενή ημερομηνία
                java.util.Date date = null;
                if (!relDate.isEmpty()){
                    date = Date.valueOf(relDate);
                    // Εαν η ημερομηνία είναι εκτός ορίων, προχωράμε στην 
                    // επόμενη ταινία
                    if(date.before(checkpoint)){
                        System.out.println("\t\t\tDATE OUT OF RANGE: " + date.toString());
                        break;
                    }
                }                    
                movie.setReleaseDate(date);

                // εύρεση του Movie.id
                int id = temp.getInt("id");
                movie.setId(id);

                // εύρεση του Movie.title
                String title = temp.getJsonString("title").toString();
                movie.setTitle(title);

                // εύρεση του Movie.genre_id
                //παίρνουμε το JsonArray genre_ids από το Json
                JsonArray genre_ids = temp.getJsonArray("genre_ids");
                for(JsonValue v : genre_ids){
                    //παιρνουμε τον αριθμό από τον πίνακα genres_ids
                    genre_id = parseInt(v.toString());
                    //ψάχνουμε το πρώτο ταίριασμα
                    if(genre_id == 28 || genre_id == 10749 || genre_id == 878 ){
                       break;
                   }               
                }

                Genre g = new Genre(genre_id);
                g.setName(genre_title);
                movie.setGenreId(g);

                // εύρεση του Movie.rating
                double rating = temp.getJsonNumber("vote_average").doubleValue();
                movie.setRating(rating);

                // εύρεση του Movie.overview
                // περιορισμός στους 500 χαρακτήρες
                int tableOverviewLenght = 500;
                String overview = temp.getJsonString("overview").toString();
                if(overview.length() > tableOverviewLenght) {
                    overview = overview.substring(1,499);
                }
                movie.setOverview(overview);

                //τέλος, προσθέτουμε την ταινία στη ΒΔ
                new MovieManager().insert(movie);
            }//end for -> movie
        }//end for -> page
        JOptionPane.showMessageDialog(
            null,
            "Η ανάκτηση των δεδομένων ολοκληρώθηκε.", 
            "Επιτυχής Ανάκτηση",
            JOptionPane.INFORMATION_MESSAGE);
    }//end getMovies
    
    public void getGenres(){
        JsonObject genreJson = new ConnectionManager()
            .connectToAPI(ConnectionManager.REQUEST_TYPE.GENRE, 1);
        
        JsonArray array = genreJson.getJsonArray("genres");
        for (int i = 0; i < array.size(); i++) {
            int id = array.getJsonObject(i).getInt("id");
            // όποιο απο τα τρία είδη συναντήσουμε πρώτο, έτσι χαρακτηρίζεται η ταινία
            if(id == 28 || id == 10749 || id == 878){
                // αναζητηση στο JSON βάσει του GENRE.ID και GENRE.NAME
                String name = array.getJsonObject(i).getJsonString("name").toString();
                // αφαιρούνται τα "" απο το string
                name = new MovieManager().trimQuotes(name);
               // εισαγωγή μέσω του Manager στη ΒΔ
                Genre g = new Genre(id);
                g.setName(name);
                EntityManager em = new ConnectionManager().getEm();
                try{
                    em.getTransaction().begin();
                    em.persist(g);
                    em.flush();
                    em.getTransaction().commit();
                    em.close();
                }catch(Exception e){e.getStackTrace();}
            } //end if           
        }//end for     
        System.out.println("Η ανάκτηση κατηγοριών ήταν επιτυχής");
    }//end getMovies
    
    /**
     *  Αφαιρεί πρώτο και τελευταίο χαρακτήρα απο το string
     * @param String string
     * @return String string
     */
    private String trimQuotes(String string){
        String temp = string;
        if(temp.startsWith("\"")){
            temp = temp.substring(1, (temp.length() - 1));
        }
        return temp;
    }
    
    /**
     * Σβήσιμο ΟΛΩΝ των πινάκων
     * 
     */
    public void eraseTables(){
        // πρώτα ο πίνακας με τα FK (λειτουργεί ανάποδα !! )
        new MovieManager().deleteTable(TABLE.Movie);
        new MovieManager().deleteTable(TABLE.Genre);
        new MovieManager().deleteTable(TABLE.FavoriteList);   
        ++count;
        if (count != 1){
           System.out.println("\nΟ καθαρισμός των πινάκων ήταν επιτυχής.\n");
        }
    } //end eraseTables
  
    /**
     * Σβήσιμο όλων των εγγραφών από πίνακα
     * @param enum TABLE tableName
     */
    public void deleteTable(TABLE tableName){
        EntityManager em = new ConnectionManager().getEm();
        String test = null;
        try{
            em.getTransaction().begin();
            switch(tableName){
                case Movie:
                    em.createNamedQuery("Movie.clearTable").executeUpdate();
                    break;
                case Genre:
                    em.createNamedQuery("Genre.clearTable").executeUpdate();
                    break;
                case FavoriteList:
                    em.createNamedQuery("FavoriteList.clearTable").executeUpdate();
                    break;
            }                     
            em.flush();
            em.getTransaction().commit();
            em.close();
        }catch(Exception e){
            e.getLocalizedMessage();
            em.getTransaction().rollback();
        }
    } //end deleteTable
    
    /**
      * Εισαγωγή εγγραφής στον πίνακα MOVIE
      */
    public void insert(Movie m){
        //Για κάθε είσαγωγή, χρησιμοποιείται νεός EntityManager
        EntityManager em = new ConnectionManager().getEm();
        try{
            em.getTransaction().begin();
            em.persist(m);
            em.flush();
            em.getTransaction().commit();
            em.close();
        }catch(Exception e){
            e.getLocalizedMessage();
            System.out.println("\n\t\tΑποτυχία στην εισαγωγή...\n\n");
        }       
    }//end insert
    
}//end class
