package com.example.haintpd04043_assignment_androidnetworking.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.haintpd04043_assignment_androidnetworking.AddPostActivity;
import com.example.haintpd04043_assignment_androidnetworking.PostDetailActivity;
import com.example.haintpd04043_assignment_androidnetworking.ProfileActivity;
import com.example.haintpd04043_assignment_androidnetworking.R;
import com.example.haintpd04043_assignment_androidnetworking.model.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

    Context context;
    List<ModelPost> postList;

    String myUid;

    private DatabaseReference likesRef; //For like database node
    private DatabaseReference postsRef; //Reference of posts

    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_posts
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, @SuppressLint("RecyclerView") int position) {
        //get data
        String uid = postList.get(position).getUid();
        String uEmail = postList.get(position).getuEmail();
        String uName = postList.get(position).getuName();
        String uDp = postList.get(position).getuDp();
        String pId = postList.get(position).getpId();
        String pTitle = postList.get(position).getpTitle();
        String pDescription = postList.get(position).getpDescr();
        String pImage = postList.get(position).getpImage();
        String pTimeStamp = postList.get(position).getpTime();
        String pLikes = postList.get(position).getpLikes(); // Contains total number of like for a posts
        String pComments = postList.get(position).getpComments();

        //Convert timeStamp to dd/MM/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //set data
        holder.tvName.setText(uName);
        holder.tvTime.setText(pTime);
        holder.tvTitle.setText(pTitle);
        holder.tvDescription.setText(pDescription);
        holder.tvLike.setText(pLikes + " Likes");
        holder.pCommentsTv.setText(pComments + " Comments");

        //set Likes for each post
        setLikes(holder, pId);

        //set avatar user post
        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_default_image).into(holder.ivPicture);
        }catch (Exception e){

        }

        //set post image
        //if no image then hint imageview
        if (pImage.equals("noImage")){
            //hint imageview
            holder.ivImage.setVisibility(View.GONE);
        }else{
            //show imageview
            holder.ivImage.setVisibility(View.VISIBLE);
            try{
                Picasso.get().load(pImage).into(holder.ivImage);
            }catch (Exception e){

            }
        }

        //handle button click
        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
                showMoreOptions(holder.btnMore, uid, myUid, pId, pImage);
            }
        });

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get all number of like for a posts, whose like button clicked
                int pLikes = Integer.parseInt(postList.get(position).getpLikes());
                mProcessLike = true;
                //get id of the posts clicked
                String postId = postList.get(position).getpId();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (mProcessLike){
                            if (snapshot.child(postId).hasChild(myUid)){
                                //already liked, so remove current button like
                                postsRef.child(postId).child("pLikes").setValue("" + (pLikes - 1));
                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLike = false;
                            }else{
                                //not clicked, like it
                                postsRef.child(postId).child("pLikes").setValue("" + (pLikes + 1));
                                likesRef.child(postId).child(myUid).setValue("Liked"); //set any value
                                mProcessLike = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
                //start PostDetailActivity
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", pId); //will get detail of post and use this id, when clicked a post then get id of it
                context.startActivity(intent);
            }
        });

        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.ivImage.getDrawable();
                if (bitmapDrawable == null){
                    //post without image
                    shareTextOnly(pTitle, pDescription);
                }else{
                    //post with image
                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle, pDescription, bitmap);
                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Click to go to ProfileActivity with uid, this uid used to show user specific dara/posts*/
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //collect title and description to share
        String shareBody = pTitle + "\n" + pDescription;
        //fist we will save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdir(); //create folder if not exists
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.example.haintpd04043_assignment_androidnetworking.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //collect title and description to share
        String shareBody = pTitle + "\n" + pDescription;

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "subject"); //This is ... in case share via an email app(Trong truong hop minh share qua email app)
        intent.putExtra(Intent.EXTRA_TEXT, shareBody); //share to text when get
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void setLikes(MyHolder holder, String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postKey).hasChild(myUid)){
                    //user has liked this post
                    holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.btnLike.setText("Liked");
                }else{
                    //user has not liked this post
                    holder.btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.btnLike.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions(ImageButton btnMore, String uid, String myUid, String pId, String pImage) {
        //Create a popup menu to delete post of current user
        PopupMenu popupMenu = new PopupMenu(context, btnMore, Gravity.END);

        //show delete option
        if (uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit Post");
        }

        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Detail");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //deleted is clicked
                    beginDelete(pId, pImage);
                }else if (id == 1){
                    //edit post is clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }else if(id == 2){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //will get detail of post and use this id, when clicked a post then get id of it
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
        //Post have been image or not image
        if (pImage.equals("noImage")){
            //post is not image
            deleteNotImage(pId);
        }else{
            //post with image
            deleteWithImage(pId, pImage);
        }
    }

    private void updateData(List<ModelPost> postList){
        this.postList.clear();
        this.postList = postList;
        this.notifyDataSetChanged();
    }

    private void deleteWithImage(String pId, String pImage) {
        //Progressbar
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Deleting...");

        //Delete Image use url
        //Delete from database use post id
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //delete image on database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue(); // remove value on database
                        }
                        Toast.makeText(context, "Deleted Successful!", Toast.LENGTH_SHORT).show();
                        updateData(postList);
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteNotImage(String pId) {
        //Progressbar
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Deleting...");
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().removeValue(); // remove value on database
                }
                Toast.makeText(context, "Deleted Successful!", Toast.LENGTH_SHORT).show();
                updateData(postList);
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //views holder class
    class MyHolder extends RecyclerView.ViewHolder{
        ImageView ivPicture, ivImage;
        TextView tvName, tvTime, tvTitle, tvDescription, tvLike, pCommentsTv;
        ImageButton btnMore;
        Button btnLike, btnComment, btnShare;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //mapping xml for views from row_post
            ivPicture = itemView.findViewById(R.id.ivPicture);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLike = itemView.findViewById(R.id.tvLike);
            pCommentsTv = itemView.findViewById(R.id.pCommentsTv);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnShare = itemView.findViewById(R.id.btnShare);
            profileLayout = itemView.findViewById(R.id.profileLayout);
        }
    }
}
