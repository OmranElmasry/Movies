package com.example.lenovo.movies;

/**
 * Created by lenovo on 11/23/2016.
 */

public interface MovieListener {
    void updateData(Movie movie, boolean connection, boolean fave);
}
