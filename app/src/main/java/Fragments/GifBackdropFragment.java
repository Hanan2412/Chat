package Fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.woofmeow.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import Adapters.GifAdapter;
import Adapters.GifSuggestionsAdapter;
import NormalObjects.Gif;

@SuppressWarnings({"Convert2Lambda", "AnonymousHasLambdaAlternative"})
public class GifBackdropFragment extends BottomSheetDialogFragment {

    private static final String API_KEY = "YZ7I01G6NJ1F";
    private static final String LogTag = "TenorTest";
    public static GifBackdropFragment newInstance() {
        Bundle args = new Bundle();
        GifBackdropFragment fragment = new GifBackdropFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private GifAdapter adapter;
    private GifSuggestionsAdapter gifSuggestionsAdapter;
    public interface onGifView{
        void onGifClick(Gif gif);
    }
    private onGifView callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            callback = (onGifView) context;
        }catch (ClassCastException e)
        {
            throw new ClassCastException("Activity must implement onGifView");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.gif_layout, container, false);
        GridView gifs = linearLayout.findViewById(R.id.grid_view);
        adapter = new GifAdapter();
        gifSuggestionsAdapter = new GifSuggestionsAdapter();
        gifs.setAdapter(adapter);
        gifs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (callback!=null)
                    callback.onGifClick(adapter.getGif2s().get(position));
            }
        });
        EditText searchField = linearLayout.findViewById(R.id.searchField);
        ImageButton searchButton = linearLayout.findViewById(R.id.searchBtn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchSuggestionRequest(searchField.getText().toString(),15);
                InputMethodManager manager = (InputMethodManager)requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(linearLayout.getWindowToken(),0);
                searchGifs(searchField.getText().toString());
            }
        });
        getTrendingGifs(20);
        RecyclerView suggestions = linearLayout.findViewById(R.id.suggestions);
        suggestions.setHasFixedSize(true);
        suggestions.setItemViewCacheSize(20);
        suggestions.setDrawingCacheEnabled(true);
        suggestions.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        suggestions.setLayoutManager(layoutManager);
        suggestions.setAdapter(gifSuggestionsAdapter);
        gifSuggestionsAdapter.setListener(new GifSuggestionsAdapter.onItemClickListener() {
            @Override
            public void onItemClick(String text) {
                searchField.setText(text);
                searchButton.performClick();
            }
        });
        return linearLayout;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return  dialog;
    }
    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet!=null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

            int windowHeight = getWindowHeight();
            if (layoutParams != null) {
                layoutParams.height = windowHeight / 2;
            }
            bottomSheet.setLayoutParams(layoutParams);
            behavior.setDraggable(false);
            bottomSheet.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    if (layoutParams != null)
                        layoutParams.height = windowHeight / 2;
                    return true;
                }
            });
            behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
    }

    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }


    /**
     * Get trending GIFs
     */
    public void getTrendingGifs(int limit) {

        // get the trending GIFS - using the default locale of en_US
        final String url = String.format("https://g.tenor.com/v1/trending?key=%1$s&limit=%2$s",
                API_KEY, limit);
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    populateGifs(get(url));
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setName("trending gifs");
        thread.start();

    }
    /**
     * Autocomplete Request
     */
    public void searchSuggestionRequest(String lastSearch, int limit) {

        // make an autocomplete request - using default locale of EN_US
        final String url = String.format("https://g.tenor.com/v1/search_suggestions?key=%1$s&q=%2$s&limit=%3$s",
                API_KEY, lastSearch, limit);
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    JSONObject suggestions =  get(url);
                    JSONArray array = suggestions.getJSONArray("results");
                    Handler handler = new Handler(requireContext().getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                gifSuggestionsAdapter.clear();
                                for (int i = 0; i < array.length(); i++)
                                    gifSuggestionsAdapter.addSuggestion(array.getString(i));
                            }catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setName("suggestions");
        thread.start();
    }

    public void populateGifs(JSONObject searchResult)
    {
        if(searchResult!=null) {
            try {
                Handler handler1 = new Handler(Looper.getMainLooper());
                handler1.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                    }
                });

                JSONArray array = searchResult.getJSONArray("results");
                for (int i = 0; i < array.length(); i++)
                {
                    JSONObject object = array.getJSONObject(i);
                    JSONArray media = object.getJSONArray("media");
                    JSONObject mediaObject = media.getJSONObject(0);
                    JSONObject innerObject = mediaObject.getJSONObject("gif");
                    String preview = innerObject.getString("preview");
                    JSONArray dims = innerObject.getJSONArray("dims");
                    String url = innerObject.getString("url");
                    int size = innerObject.getInt("size");
//                            double duration = innerObject.getDouble("duration");
                    Gif gif = new Gif();
//                            gif.setDuration(duration);
                    gif.setUrl(url);
                    gif.setDimensions(dims);
                    gif.setPreview(preview);
                    gif.setSize(size);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.addGif(gif);
                        }
                    });
                }
                System.out.println(array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // load the results for the user
            Log.v(LogTag, "Search Results: " + searchResult);
        }
    }

    private void searchGifs(String searchTerm)
    {
        new Thread() {
            @Override
            public void run() {

                // make initial search request for the first 8 items
                JSONObject searchResult = getSearchResults(searchTerm, 30);
                populateGifs(searchResult);
            }
        }.start();
    }

    /**
     * Get Search Result GIFs
     */
    public JSONObject getSearchResults(String searchTerm, int limit) {

        // make search request - using default locale of EN_US

        final String url = String.format("https://g.tenor.com/v1/search?q=%1$s&key=%2$s&limit=%3$s",
                searchTerm, API_KEY, limit);
        try {
            return get(url);
        } catch (IOException | JSONException ignored) {
        }
        return null;
    }

    /**
     * Construct and run a GET request
     */
    private JSONObject get(String url) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            // Get request
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            // Handle failure
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED) {
                String error = String.format("HTTP Code: '%1$s' from '%2$s'", statusCode, url);
                throw new ConnectException(error);
            }

            // Parse response
            return parser(connection);
        } catch (Exception e) {
           e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONObject("");
    }

    /**
     * Parse the response into JSONObject
     */
    private static JSONObject parser(HttpURLConnection connection) throws JSONException {
        char[] buffer = new char[1024 * 4];
        int n;
        try (InputStream stream = new BufferedInputStream(connection.getInputStream())) {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            StringWriter writer = new StringWriter();
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            return new JSONObject(writer.toString());
        } catch (IOException ignored) {
        }
        return new JSONObject("");
    }
}
