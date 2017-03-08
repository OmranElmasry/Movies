package com.example.lenovo.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lenovo.movies.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//Created by Omran Elmasry on 10/24/2016.

public class MovieFragment extends Fragment {
    public MovieListener mListener ;
    static MovieDbHelper movieDbHelper;
    static SQLiteDatabase sqLiteDatabase;
    static Cursor cursor ;

    public MovieFragment(){}

    public void onStart(){
        super.onStart();

        movieDbHelper = new MovieDbHelper(getContext(),"movies",null,1);
        if(isNetworkAvailable())
            updateMovies();
        else{
            Toast.makeText(getContext(),"Check your connection!", Toast.LENGTH_SHORT).show();
            getFaveMovies();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MovieListener) {
            mListener = (MovieListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implemenet MyListFragment.OnItemSelectedListener");
        }
    }
    private void getFaveMovies() {
        imageAdapter.clear();
        sqLiteDatabase = movieDbHelper.getReadableDatabase();
        cursor = movieDbHelper.readMovies(sqLiteDatabase);
        imageAdapter.notifyDataSetChanged();
        if(cursor.moveToFirst()){

            do{
                String poster = cursor.getString(0);
                String TITLE = cursor.getString(1);
                String OVERVIEW = cursor.getString(2);
                String RELEASEYEAR = cursor.getString(3);
                double VOTEAVERAGE = cursor.getDouble(4);
                int id= cursor.getInt(5);
                String Trailer_name = cursor.getString(6);
                String Trailer_key = cursor.getString(7);
                String review = cursor.getString(8);

                Movie movie = new Movie(poster,TITLE,OVERVIEW,RELEASEYEAR,VOTEAVERAGE,id,Trailer_name,Trailer_key,review);
                mUrls.add(movie);

            }while (cursor.moveToNext());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public void updateMovies() {

        String data;
        GettingMovieTask movieTask = new GettingMovieTask();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortType = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_Popular));

        if(sortType.equals(getString(R.string.pref_sort_Popular))){
            data = "popular";
        } else if(sortType.equals(getString(R.string.pref_sort_Top_rated))){
            data = "top_rated";
        }else{
            data = "favorite";
        }
        if(data != "favorite") movieTask.execute(data);
        else getFaveMovies();
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.Sort_by) {
            if(isNetworkAvailable())
                updateMovies();
            else {
                Toast.makeText(getContext(),"Check your connection!", Toast.LENGTH_SHORT).show();
                getFaveMovies();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<Movie> mUrls ;
    public ImageAdapter imageAdapter = new ImageAdapter(getActivity(),mUrls);

    public GridView gridview;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUrls = new ArrayList<>();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Movie movie = mUrls.get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                boolean connection;
                if(isNetworkAvailable()) {
                    intent.putExtra("connection", true);
                    connection = true;
                }else {
                    Toast.makeText(getContext(),"Check your connection!", Toast.LENGTH_SHORT).show();
                    intent.putExtra("connection", false);
                    connection = false;
                }
                boolean fave = movieDbHelper.isFaveorite(movieDbHelper.getReadableDatabase(),movie.id);
                intent.putExtra("title", movie.TITLE);
                intent.putExtra("overview", movie.OVERVIEW);
                intent.putExtra("poster", movie.poster);
                intent.putExtra("releaseyear", movie.RELEASEYEAR);
                intent.putExtra("voteaverage", movie.VOTEAVERAGE);
                intent.putExtra("id", movie.id);
                intent.putExtra("fave", fave);//true if movie is fave

                if(MainActivity.mTwoPane) mListener.updateData(movie, connection, fave);
                else startActivity(intent);
            }
        });

        return rootView;
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public ImageAdapter(Context context, ArrayList<Movie> urls) {

            mUrls = urls;
        }

        public void clear(){
            mUrls = new ArrayList<>();
        }

        public int getCount() {
            return mUrls.size();
        }

        public Object getItem(int position) {
            return mUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mUrls.get(position).id;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            View view =  convertView;

            if (convertView == null) {
                inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view =  inflater.inflate(R.layout.grid_item_movie, null);
            }

            ImageView ivPoster = (ImageView) view.findViewById(R.id.ivPoster);
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w342//" + mUrls.get(position).poster).into(ivPoster);
            return view;
        }
    }

    public class GettingMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {
        public final String LOG_TAG = GettingMovieTask.class.getSimpleName();

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            final String J_ID = "id";//JSON object for movie id
            final String J_RESULTS = "results";//the array of JSON movie objects
            final String J_POSTER = "poster_path";//JSON object of poster
            final String J_TITLE = "original_title";//JSON object of title
            final String J_OVERVIEW = "overview";//JSON object of poster
            final String J_RELEASEYEAR = "release_date";//JSON object of poster
            final String J_VOTEAVERAGE = "vote_average";//JSON object of poster

            JSONObject J_response = new JSONObject(movieJsonStr);
            JSONArray movieArray = J_response.getJSONArray(J_RESULTS);

            ArrayList<Movie> movies = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {

                JSONObject movieJSONObject = movieArray.getJSONObject(i);

                Movie movie = new Movie( movieJSONObject.getString(J_POSTER)
                        ,movieJSONObject.getString(J_TITLE)
                        ,movieJSONObject.getString(J_OVERVIEW)
                        ,movieJSONObject.getString(J_RELEASEYEAR)
                        ,movieJSONObject.getDouble(J_VOTEAVERAGE)
                        ,movieJSONObject.getInt(J_ID)
                );
                movies.add(i,movie);
            }

            return movies;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri builtUri;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            //http://api.themoviedb.org/3/movie/550?api_key=d0bf7851526637966927d37771eea127
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";
            try{
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon().appendPath(params[0])
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            }catch(IOException e){
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            }finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            mUrls = movies;
            imageAdapter.notifyDataSetChanged();
        }
    }
}