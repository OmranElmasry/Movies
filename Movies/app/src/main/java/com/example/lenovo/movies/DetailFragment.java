package com.example.lenovo.movies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class DetailFragment extends Fragment {


    public  ArrayList<Trailer> mUrls ;
    private  ArrayAdapter<String> mReviewAdapter;
    public TrailerAdapter mTrailerAdapter = new TrailerAdapter(getActivity(), mUrls);

    public void getTrailers(String id) {
        GettingTrailerTask gettingTrailerTask = new GettingTrailerTask();
        gettingTrailerTask.execute(id);
    }

    public void getReviews(String id) {
        GettingReviewTask gettingReviewTask = new GettingReviewTask();
        gettingReviewTask.execute(id);
    }

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mUrls = new ArrayList<>();
        List<String> reviews = new ArrayList<String>();

        mReviewAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_review, // The name of the layout ID.
                        R.id.list_item_review_textview, // The ID of the textview to populate.
                        reviews);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        String title, overview, poster, rate, ID, releaseyear;
        double voteaverage;
        boolean fave, connection;
        int id;

        if(MainActivity.mTwoPane){
            Bundle bundle = getArguments();
            title = bundle.getString("title");
            overview = bundle.getString("overview");
            poster = bundle.getString("poster");
            releaseyear = bundle.getString("releaseyear");
            voteaverage = bundle.getDouble("voteaverage");
            id = bundle.getInt("id");
            fave = bundle.getBoolean("fave");//true if movie is fave
            connection = bundle.getBoolean("connection");
        }else{
            title = intent.getStringExtra("title");
            releaseyear = intent.getStringExtra("releaseyear");
            voteaverage = intent.getDoubleExtra("voteaverage", 0);
            overview = intent.getStringExtra("overview");
            poster = intent.getStringExtra("poster");
            fave = intent.getBooleanExtra("fave",false);
            id = intent.getIntExtra("id", 0);

            connection = intent.getBooleanExtra("connection", true);
        }
        rate = Double.toString(voteaverage);
        rate += "/10";
        ID = Integer.toString(id);

        if(connection){
            getTrailers(ID);
            getReviews(ID);
        }
        else Toast.makeText(getContext(),"Check your connection!", Toast.LENGTH_SHORT).show();

        ((TextView) rootView.findViewById(R.id.tvTitle))
                .setText(title);
        ((TextView) rootView.findViewById(R.id.tvRate))
                .setText(rate);
        ((TextView) rootView.findViewById(R.id.tvReleaseYear))
                .setText(releaseyear);
        ((TextView) rootView.findViewById(R.id.tvDescription))
                .setText(overview);
        ImageView ivmovieposter = (ImageView) rootView.findViewById(R.id.ivImage);
        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185//" + poster).into(ivmovieposter);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_trailers);
        ListView listViewreviews = (ListView) rootView.findViewById(R.id.listview_reviews);
        listView.setAdapter(mTrailerAdapter);
        listViewreviews.setAdapter(mReviewAdapter);

        final Movie movie = new Movie(poster,title,overview,releaseyear,voteaverage,id);
        if(mReviewAdapter.isEmpty())mReviewAdapter.add("No reviews available for "+title);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //youtube
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String key = mUrls.get(position).Trailer_key;

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key)));
            }
        });

        Switch aSwitch = (Switch) rootView.findViewById(R.id.switch1);
        aSwitch.setChecked(fave);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    if(!MovieFragment.movieDbHelper.isFaveorite(MovieFragment.movieDbHelper.getReadableDatabase(),movie.id)){
                        Toast.makeText(getContext(), "Successfully added " + movie.TITLE + " to favorite list!", Toast.LENGTH_LONG).show();
                        MovieFragment.movieDbHelper.addMovies(movie);
                    }
                }
                else{
                    Toast.makeText(getContext(),"Successfully removed " + movie.TITLE + " from favorite list!", Toast.LENGTH_LONG).show();
                    MovieFragment.movieDbHelper.deleteMovies(movie.TITLE);
                }
            }
        });

        return rootView;
    }

    public class TrailerAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public TrailerAdapter(Context context, ArrayList<Trailer> urls) {
            mUrls = urls;
        }

        public int getCount() {

            if(mUrls == null)return 0;
            else return mUrls.size();

        }

        public Object getItem(int position) {
            return mUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 16;//test
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item_trailer, null);
            }

            TextView tvTrailer = (TextView) view.findViewById(R.id.list_item_trailer_textview);
            tvTrailer.setText(mUrls.get(position).Trailer_name);
            return view;
        }
    }

    public class GettingTrailerTask extends AsyncTask<String, Void, ArrayList<Trailer>> {
        public final String LOG_TAG = GettingTrailerTask.class.getSimpleName();

        private ArrayList<Trailer> getMovieTrailersFromJson(String trailerJsonStr)
                throws JSONException {

            final String J_RESULTS = "results";//the array of JSON movie objects
            final String J_KEY = "key";//JSON object of poster
            final String J_NAME = "name";//JSON object of poster
            final String J_ID = "id";//JSON object of poster

            JSONObject J_response = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = J_response.getJSONArray(J_RESULTS);

            ArrayList<Trailer> trailers = new ArrayList<>();

            for (int i = 0; i < trailerArray.length(); i++) {

                JSONObject trailerJSONObject = trailerArray.getJSONObject(i);
                Trailer trailer = new Trailer(trailerJSONObject.getString(J_NAME)
                        , trailerJSONObject.getString(J_ID)
                        , trailerJSONObject.getString(J_KEY)
                );
                trailers.add(i, trailer);
            }
            return trailers;
        }

        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri builtUri;

            // Will contain the raw JSON response as a string.
            String trailerJsonStr = null;

            //http://api.themoviedb.org/3/movie/550?api_key=d0bf7851526637966927d37771eea127
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";

            try {
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon().appendPath(params[0]).appendPath("videos")
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
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the trailer data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
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

                return getMovieTrailersFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> trailers) {
            mUrls = trailers;
            mTrailerAdapter.notifyDataSetChanged();
        }
    }
    public class GettingReviewTask extends AsyncTask<String, Void, String[]> {
        public final String LOG_TAG = GettingReviewTask.class.getSimpleName();

        private String[] getMovieReviewsFromJson(String reviewJsonStr)
                throws JSONException {

            final String J_RESULTS = "results";//the array of JSON movie objects
            final String J_CONTENT = "content";//JSON object of poster

            JSONObject J_response = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = J_response.getJSONArray(J_RESULTS);

            String[] reviews = new String[reviewArray.length()];

            for (int i = 0; i < reviewArray.length(); i++) {

                JSONObject reviewJSONObject = reviewArray.getJSONObject(i);

                reviews[i] = reviewJSONObject.getString(J_CONTENT);
            }
            if(reviews.length == 0) return null;
            else return reviews;
        }

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri builtUri;

            // Will contain the raw JSON response as a string.
            String reviewJsonStr = null;

            //http://api.themoviedb.org/3/movie/550?api_key=d0bf7851526637966927d37771eea127
            final String MOVIE_BASE_URL =
                    "https://api.themoviedb.org/3/movie/";
            final String APPID_PARAM = "api_key";

            try {
                builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon().appendPath(params[0]).appendPath("reviews")
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
                reviewJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
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

                return getMovieReviewsFromJson(reviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] reviews) {
            if (reviews != null) {
                mReviewAdapter.clear();
                for (int i = 0; i < reviews.length; i++) {
                    mReviewAdapter.add(reviews[i]);
                }
            }
        }
    }
}