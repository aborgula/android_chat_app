package com.example.comments.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.comments.EditCommentActivity;
import com.example.comments.JsonPlaceHolderApi;
import com.example.comments.Post;
import com.example.comments.PostAdapter;
import com.example.comments.PostSend;
import com.example.comments.R;
import com.example.comments.databinding.FragmentGalleryBinding;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GalleryFragment extends Fragment {

    Activity context;
    private FragmentGalleryBinding binding;
    private List<Post> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private EditText editTextComment;
    private ImageButton imageButtonSend;
    private SwipeRefreshLayout swipeRefreshLayout;

    private JsonPlaceHolderApi jsonPlaceHolderApi;
    private String userLogin;

    private static final int EDIT_COMMENT_REQUEST_CODE = 1;


    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                String login = postAdapter.getUserLoginAtPosition(position);
                String postId = postList.get(position).getId();

                if (login != null && login.equals(userLogin)) {
                    postAdapter.onItemDismiss(position);
                    deletePost(postId); // extra

                } else {
                    Toast.makeText(requireContext(), "Nie masz uprawnień do usunięcia tego komentarza", Toast.LENGTH_SHORT).show();
                    postAdapter.notifyItemChanged(position);
                }
            }
        }
    };
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        context = getActivity();

        recyclerView = binding.recyclerViewGallery;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(!isConnected()){
            Toast.makeText(requireContext(), "No Internet Access", Toast.LENGTH_SHORT).show();
        }

        editTextComment = root.findViewById(R.id.editTextComment);
        imageButtonSend = root.findViewById(R.id.imageButtonSend);
        swipeRefreshLayout = binding.swipeRefreshLayout;

        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://tgryl.pl/shoutbox/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);

        postAdapter.setOnItemClickListener(new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Post post) {
                if (userLogin.equals(post.getLogin())) {
                    openEditCommentActivity(post);
                } else {
                    Toast.makeText(requireContext(), "Nie masz uprawnień do edycji tego komentarza", Toast.LENGTH_SHORT).show();
                }
            }
        });


        userLogin = getUserLoginFromSharedPreferences();

        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentContent = editTextComment.getText().toString();
                PostSend newPost = new PostSend();
                newPost.setContent(commentContent);
                newPost.setLogin(userLogin);
                sendNewPost(newPost);
            }
        });

        editTextComment.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String commentContent = editTextComment.getText().toString();
                    PostSend newPost = new PostSend();
                    newPost.setContent(commentContent);
                    newPost.setLogin(userLogin);
                    sendNewPost(newPost);
                    return true;
                }
                return false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getExistingPosts();
            }
        });

        getExistingPosts();

        itemTouchHelper.attachToRecyclerView(recyclerView);

        return root;
    }

    private void openEditCommentActivity(Post post) {
        Intent intent = new Intent(getContext(), EditCommentActivity.class);
        intent.putExtra("postId", post.getId());
        intent.putExtra("commentContent", post.getContent());
        intent.putExtra("userLogin", post.getLogin());
        intent.putExtra("commentDate", post.getDate());
        startActivityForResult(intent, EDIT_COMMENT_REQUEST_CODE);
    }

    private void deletePost(String postId) {
        Call<ResponseBody> call = jsonPlaceHolderApi.deletePost(postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Log.e("GalleryFragment", "Błąd usuwania komentarza");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("GalleryFragment", "Błąd usuwania komentarza", t);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_COMMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getExistingPosts();
        }
    }



    private String getUserLoginFromSharedPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_login", "login");
    }

    private void sendNewPost(PostSend newPost) {
        Call<ResponseBody> call = jsonPlaceHolderApi.createPost(newPost);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    getExistingPosts();
                    Toast.makeText(getContext(), "Komentarz wysłany pomyślnie", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("GalleryFragment", "Błąd wysyłania komentarza", t);
                Toast.makeText(getContext(), "Błąd wysyłania komentarza", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getExistingPosts() {
        Call<List<Post>> call = jsonPlaceHolderApi.getPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful()) {
                    List<Post> posts = response.body();
                    if (posts != null) {
                        postList.clear();
                        postList.addAll(posts);
                        postAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void startAutoRefresh() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                getExistingPosts();
                startAutoRefresh();
            }
        }, 60 * 1000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
