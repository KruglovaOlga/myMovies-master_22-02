package model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import model.Movie;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2019-02-24T17:49:48")
@StaticMetamodel(FavoriteList.class)
public class FavoriteList_ { 

    public static volatile SingularAttribute<FavoriteList, String> name;
    public static volatile SingularAttribute<FavoriteList, Integer> id;
    public static volatile ListAttribute<FavoriteList, Movie> movieList;

}