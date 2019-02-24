package model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import model.FavoriteList;
import model.Genre;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2019-02-24T17:49:48")
@StaticMetamodel(Movie.class)
public class Movie_ { 

    public static volatile SingularAttribute<Movie, Genre> genreId;
    public static volatile SingularAttribute<Movie, String> overview;
    public static volatile SingularAttribute<Movie, FavoriteList> favoriteListId;
    public static volatile SingularAttribute<Movie, Date> releaseDate;
    public static volatile SingularAttribute<Movie, Double> rating;
    public static volatile SingularAttribute<Movie, Integer> id;
    public static volatile SingularAttribute<Movie, String> title;

}