package com.example.comments;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


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

public class EditCommentActivity extends AppCompatActivity {


    private EditText editTextComment;
    private JsonPlaceHolderApi jsonPlaceHolderApi;
    private String userLogin;
    private TextView date;
    private TextView login;
    private List<Post> postList;
    private PostAdapter postAdapter;
    private ImageView menu;
    private ImageView bin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);


        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);

        getSupportActionBar().hide();


        menu = findViewById(R.id.menu);
        bin = findViewById(R.id.bin);

        menu.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                NavController navController = Navigation.findNavController(EditCommentActivity.this, R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_gallery);

            }
        });

        bin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                String commentId = getIntent().getStringExtra("postId");
                deletePost(commentId);

            }
        });

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://tgryl.pl/shoutbox/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        userLogin = getIntent().getStringExtra("userLogin");

        date = findViewById(R.id.date);
        login = findViewById(R.id.login);
        editTextComment = findViewById(R.id.editTextComment);
        String commentContent = getIntent().getStringExtra("commentContent");
        String commentAuthorLogin = getIntent().getStringExtra("userLogin");
        String dateIntent = getIntent().getStringExtra("commentDate");
        editTextComment.setText(commentContent);
        date.setText(dateIntent);
        login.setText(commentAuthorLogin);

        editTextComment.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String newCommentContent = editTextComment.getText().toString();
                    String commentId = getIntent().getStringExtra("postId");
                    updateComment(commentId, newCommentContent, userLogin);
                    return true;
                }
                return false;
            }
        });
    }

    private void deletePost(String postId) {
        Call<ResponseBody> call = jsonPlaceHolderApi.deletePost(postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditCommentActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent();
                    intent.putExtra("shouldRefresh", true);
                    setResult(RESULT_OK, intent);

                    finish();
                } else {
                    Toast.makeText(EditCommentActivity.this, "Error deleting post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditCommentActivity.this, "API Call Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateComment(String commentId, String newCommentContent, String userLogin) {
        PostSend editedPost = new PostSend();
        editedPost.setContent(newCommentContent);
        editedPost.setLogin(userLogin);

        Call<ResponseBody> call = jsonPlaceHolderApi.updatePost(commentId, editedPost);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String editedContent = editedPost.getContent();
                    String newLogin = editedPost.getLogin();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("editedContent", editedContent);
                    resultIntent.putExtra("editedCommentId", commentId);
                    resultIntent.putExtra("editedLogin", newLogin);

                    setResult(RESULT_OK, resultIntent);

                    Intent intent = new Intent();
                    intent.putExtra("shouldRefresh", true);
                    setResult(RESULT_OK, intent);

                    Toast.makeText(EditCommentActivity.this, "Komentarz zaktualizowany pomyślnie", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditCommentActivity.this, "Błąd edycji komentarza", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditCommentActivity.this, "Błąd wywołania API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
