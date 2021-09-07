package com.example.haintpd04043_assignment_androidnetworking.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haintpd04043_assignment_androidnetworking.ChatActivity;
import com.example.haintpd04043_assignment_androidnetworking.ProfileActivity;
import com.example.haintpd04043_assignment_androidnetworking.model.ModelUser;
import com.example.haintpd04043_assignment_androidnetworking.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get Data
        String userUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

        //set Data
        holder.tvName.setText(userName);
        holder.tvEmail.setText(userEmail);
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image).into(holder.ivAvatar);
        }catch (Exception e){
            Log.d("Error", e.toString());
        }

        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            //profile clicked
                            //Click to go to ProfileActivity with uid, this uid used to show user specific dara/posts*/
                            Intent intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra("uid", userUID);
                            context.startActivity(intent);
                        }

                        if (which == 1){
                            //chat clicked
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("userUid", userUID);
                            context.startActivity(intent);
                            Log.d("UID", userUID);
                        }
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView ivAvatar;
        TextView tvName, tvEmail;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            ivAvatar = itemView.findViewById(R.id.ivAvatarItem);
            tvName = itemView.findViewById(R.id.tvNameItem);
            tvEmail = itemView.findViewById(R.id.tvEmailItem);
        }
    }
}
