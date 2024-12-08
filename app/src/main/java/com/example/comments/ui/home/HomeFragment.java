package com.example.comments.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.net.ConnectivityManager;

import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.comments.PostSend;
import com.example.comments.R;
import com.example.comments.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    ImageView noInternet;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String storedLogin = sharedPreferences.getString("user_login", "");
        EditText editText = root.findViewById(R.id.textView2);
        editText.setText(storedLogin);

        noInternet = root.findViewById(R.id.internet);
        if(!isConnected()){
            Toast.makeText(requireContext(), "No Internet Access", Toast.LENGTH_SHORT).show();
            noInternet.setVisibility(View.VISIBLE);
        }else{
            noInternet.setVisibility(View.GONE);
        }

        Button login = root.findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                handleLoginClick();

            }
        });

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    handleLoginClick();

                    return true;
                }
                return false;
            }
        });

        return root;
    }


    private void handleLoginClick() {
        String enteredText = binding.textView2.getText().toString();
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_login", enteredText);
        editor.apply();

        Navigation.findNavController(requireView()).navigate(R.id.nav_gallery);
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