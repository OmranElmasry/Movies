package com.example.lenovo.movies;

/**
 * Created by lenovo on 10/24/2016.
 */
public class Movie {

    public String poster ;
    public String TITLE ;
    public String OVERVIEW ;
    public String RELEASEYEAR ;
    public double VOTEAVERAGE ;
    public int id;
    public String Trailer_name ;
    public String Trailer_key ;
    public String review ;

    public Movie(String poster, String TITLE, String OVERVIEW, String RELEASEYEAR, double VOTEAVERAGE, int id) {
        this.poster = poster;
        this.TITLE = TITLE;
        this.OVERVIEW = OVERVIEW;
        this.RELEASEYEAR = RELEASEYEAR;
        this.VOTEAVERAGE = VOTEAVERAGE;
        this.id = id;
    }

    public Movie(String poster, String TITLE, String OVERVIEW, String RELEASEYEAR, double VOTEAVERAGE, int id, String trailer_name, String trailer_key,String review) {
        this.poster = poster;
        this.TITLE = TITLE;
        this.OVERVIEW = OVERVIEW;
        this.RELEASEYEAR = RELEASEYEAR;
        this.VOTEAVERAGE = VOTEAVERAGE;
        this.id = id;
        Trailer_name = trailer_name;
        Trailer_key = trailer_key;
        this.review = review;
    }
}
