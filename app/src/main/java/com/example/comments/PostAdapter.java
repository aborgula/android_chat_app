package com.example.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private OnItemClickListener onItemClickListener;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    public interface OnItemClickListener {
        void onItemClick(Post post);

    }

    public String getUserLoginAtPosition(int position){
        if(position >= 0 && position < postList.size()){
            return postList.get(position).getLogin();
        }
        return null;
    }

    public void onItemDismiss(int position){

        postList.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_row, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(post);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView contentTextView;
        private final TextView loginTextView;
        private final TextView dateTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.comment);
            loginTextView = itemView.findViewById(R.id.login);
            dateTextView = itemView.findViewById(R.id.date);
        }

        public void bind(Post post) {
            contentTextView.setText(post.getContent());
            loginTextView.setText(post.getLogin());
            dateTextView.setText(DateUtil.formatDateString(post.getDate()));
        }
    }
}
