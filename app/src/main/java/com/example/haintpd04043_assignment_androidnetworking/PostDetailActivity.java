package com.example.haintpd04043_assignment_androidnetworking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haintpd04043_assignment_androidnetworking.adapter.AdapterComments;
import com.example.haintpd04043_assignment_androidnetworking.model.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    ImageView ivUPicture, ivPImage;
    TextView tvName, tvPTime, tvPTitle, tvPDescription, tvPLikes, pCommentTv;
    ImageButton btnMore;
    Button btnLike, btnShare;
    LinearLayout profileLayout;

    EditText edComment;
    ImageButton btnSendComment;
    ImageView ivCAvatar;

    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    //get detail of user and post
    String myUid, myEmail, myName, myDp, postId, pLikes, userDp, userName, userUid, pImage;

    //Progress bar
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //get id of post from intent AdapterPosts
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init view / mapping xml
        ivUPicture = findViewById(R.id.ivPicture2);
        ivPImage = findViewById(R.id.ivImage2);
        tvName = findViewById(R.id.tvName2);
        tvPTime = findViewById(R.id.tvTime2);
        tvPTitle = findViewById(R.id.tvTitle2);
        tvPDescription = findViewById(R.id.tvDescription2);
        tvPLikes = findViewById(R.id.tvLike2);
        pCommentTv = findViewById(R.id.pCommentsTv);
        btnMore = findViewById(R.id.btnMore2);
        btnLike = findViewById(R.id.btnLike2);
        btnShare = findViewById(R.id.btnShare2);
        profileLayout = findViewById(R.id.profileLayout2);
        recyclerView = findViewById(R.id.recyclerView);

        edComment = findViewById(R.id.edComment);
        btnSendComment = findViewById(R.id.btnSendComment);
        ivCAvatar = findViewById(R.id.ivCAvatar);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setLikes();

        loadComments();

        //send comment button click
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //like button click handle
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        //more button click handle
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle = tvPTitle.getText().toString().trim();
                String pDescription = tvPDescription.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable) ivPImage.getDrawable();
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
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(this.getCacheDir(), "images");
        Uri uri = null;
        try{
            imageFolder.mkdir(); //create folder if not exists
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(PostDetailActivity.this, "com.example.haintpd04043_assignment_androidnetworking.fileprovider", file);
        }catch (Exception e){
            Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void loadComments() {
        //init layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init comments list
        commentList = new ArrayList<>();

        //path of the post, to get it's comments
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelComment modelComment = ds.getValue(ModelComment.class);
                    commentList.add(modelComment);

                    //pass myUid and postId as parameter of constructor of AdapterComment


                    //set AdapterComment
                    adapterComments = new AdapterComments(PostDetailActivity.this, commentList, myUid, postId);
                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        //Create a popup menu to delete post of current user
        PopupMenu popupMenu = new PopupMenu(this, btnMore, Gravity.END);

        //show delete option
        if (userUid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit Post");
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //deleted is clicked
                    beginDelete();
                }else if (id == 1){
                    //edit post is clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        //Post have been image or not image
        if (pImage.equals("noImage")){
            //post is not image
            deleteNotImage();
        }else{
            //post with image
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        //Progressbar
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Deleting...");

        //Delete Image use url
        //Delete from database use post id
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //delete image on database
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue(); // remove value on database
                        }
                        Toast.makeText(PostDetailActivity.this, "Deleted Successful!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteNotImage() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Deleting...");
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ds.getRef().removeValue(); // remove value on database
                }
                Toast.makeText(PostDetailActivity.this, "Deleted Successful!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        //when the details of post is loading, also check if current user has clicked it or not
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    btnLike.setText("Liked");
                }else{
                    //user has not liked this post
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    btnLike.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        //get all number of like for a posts, whose like button clicked
        mProcessLike = true;
        //get id of the posts clicked
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike){
                    if (snapshot.child(postId).hasChild(myUid)){
                        //already liked, so remove current button like
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes )- 1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;
                    }else{
                        //not clicked, like it
                        postsRef.child(postId).child("pLikes").setValue("" + (Integer.parseInt(pLikes ) + 1));
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

    private void postComment() {
        pd = new ProgressDialog(PostDetailActivity.this);
        pd.setMessage("Adding Comment...");

        //get data from comment editText
        String comment = edComment.getText().toString().trim();
        //validate
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(PostDetailActivity.this, "Comment is empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        String timStamp = String.valueOf(System.currentTimeMillis());

        //each post will have a child "Comments" that will contain comments of that post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        //put info in hashmap
        hashMap.put("cId", timStamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timStamp);
        hashMap.put("uid", myUid);
        hashMap.put("uDp", myDp);
        hashMap.put("uEmail", myEmail);
        hashMap.put("uName", myName);

        //put this data on firebase
        reference.child(timStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //added
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "Comment Added...", Toast.LENGTH_SHORT).show();
                edComment.setText("");
                updateCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateCommentCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment){
                    String comments = "" + snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue("" + newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        //get current user info
        Query query = FirebaseDatabase.getInstance().getReference("Users");
        query.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    myName = "" + ds.child("name").getValue();
                    myDp = "" + ds.child("image").getValue();

                    //set data
                    try {
                        //if image received then set
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_image).into(ivCAvatar);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_image).into(ivCAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //get post using id of the post
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = reference.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check post until get the request post
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String pTitle = "" + ds.child("pTitle").getValue();
                    String pDescr = "" + ds.child("pDescr").getValue();
                    pLikes = "" + ds.child("pLikes").getValue();
                    String pTimeStamp = "" + ds.child("pTime").getValue();
                    pImage = "" + ds.child("pImage").getValue();
                    userDp = "" + ds.child("uDp").getValue();
                    userUid = "" + ds.child("uid").getValue();
                    String uEmail = "" + ds.child("uEmail").getValue();
                    userName = "" + ds.child("uName").getValue();
                    String commentCount = "" + ds.child("pComments").getValue();

                    //Convert timeStamp to dd/MM/yyyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    //set data
                    tvPTitle.setText(pTitle);
                    tvPDescription.setText(pDescr);
                    tvPLikes.setText(pLikes + " Likes");
                    tvPTime.setText(pTime);
                    tvName.setText(userName);
                    pCommentTv.setText(commentCount + " Comments");

                    //set image the post of user
                    //if no image then hint imageview
                    if (pImage.equals("noImage")){
                        //hint imageview
                        ivPImage.setVisibility(View.GONE);
                    }else{
                        //show imageview
                        ivPImage.setVisibility(View.VISIBLE);
                        try{
                            Picasso.get().load(pImage).into(ivPImage);
                        }catch (Exception e){

                        }
                    }

                    //set user image in comment part
                    try {
                        Picasso.get().load(userDp).placeholder(R.drawable.ic_default_image).into(ivUPicture);
                    }catch (Exception ex){
                        Picasso.get().load(R.drawable.ic_default_image).into(ivUPicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            //user is login
            myEmail = user.getEmail();
            myUid = user.getUid();
        }else{
            //user not login, go to back HomeActivity
            startActivity(new Intent(PostDetailActivity.this, HomeActivity.class));
            finish();
        }
    }
}