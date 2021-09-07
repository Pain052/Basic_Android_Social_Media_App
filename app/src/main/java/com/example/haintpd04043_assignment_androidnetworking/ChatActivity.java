package com.example.haintpd04043_assignment_androidnetworking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.haintpd04043_assignment_androidnetworking.adapter.AdapterChat;
import com.example.haintpd04043_assignment_androidnetworking.model.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView ivProfile;
    TextView tvName, tvUserStatus;
    EditText edMessage;
    ImageButton btnSend;

    //firebase auth
    FirebaseAuth firebaseAuth;

    //init to use realtime database of firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;

    String userUid;
    String myUid;
    String userImage;

    //For checking if user has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView = findViewById(R.id.chat_recyclerView);
        ivProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        edMessage = findViewById(R.id.edMessage);
        btnSend = findViewById(R.id.btnSend);
        tvUserStatus = findViewById(R.id.tvUserStatus);

        //Init Layout(LinearLayout) for recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); //Show message new
        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //get from Intent of event click user (AdapterUsers)
        //get Uid here to get profile picture, name and to start chat
        Intent intent = getIntent();
        userUid = intent.getStringExtra("userUid");

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        //Search user to get user's info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(userUid);
        //get user (picture and name)
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required info is received
                for (DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String name ="" + ds.child("name").getValue();
                    userImage ="" + ds.child("image").getValue();
                    String typingStatus ="" + ds.child("typingTo").getValue();

                    //check typing status
                    if (typingStatus.equals(myUid)){
                        tvUserStatus.setText("Typing...");
                    }else{
                        //get value of online status
                        String onlineStatus = "" + ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            tvUserStatus.setText(onlineStatus);
                        }else{
                            //convert timestamp to proper time day(Chuyển đổi dấu thời gian thành ngày giờ thích hợp)
                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            tvUserStatus.setText("Last seen at " + dateTime);
                        }
                    }
                    //set data
                    tvName.setText(name);
                    try {
                        //image received, set imageview on toolbar
                        Picasso.get().load(userImage).placeholder(R.drawable.ic_icon_default).into(ivProfile);
                    }catch (Exception e){
                        //if error then load image default
                        Picasso.get().load(R.drawable.ic_icon_default).into(ivProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Click button send message
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit text
                String message = edMessage.getText().toString().trim();
                //check if text empty
                if (TextUtils.isEmpty(message)){
                    //text empty
                    Toast.makeText(ChatActivity.this, "Text is not empty! Cannot send message!", Toast.LENGTH_SHORT).show();
                }else{
                    //text not empty
                    sendMessage(message);
                }
            }
        });

        //check edittext change listener
        edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(userUid);//uid of receiver(gửi lên firebase loại trang thái thông qua uid của user)

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessage(); // check user has read message or not
        seenMessage(); // check user has seen message or not
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(myUid) && modelChat.getSender().equals(userUid)){
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(myUid) && modelChat.getSender().equals(userUid) ||
                        modelChat.getReceiver().equals(userUid) && modelChat.getSender().equals(myUid)){
                        chatList.add(modelChat);
                    }

                    //new a adapter and update it
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, userImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        //When user send message then it will create new child "Chats" node and that child will contain the
        // following key values
        //1. UID of sender
        //2. UID of receiver
        //3. message
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis()); // get time current system

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver",  userUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timeStamp); //put add timestamp(Dấu thời gian) and isSeen status
        hashMap.put("isSeen", false);
        reference.child("Chats").push().setValue(hashMap);

        //reset edittext
        edMessage.setText("");
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //get Uid of user signed
            myUid = user.getUid();
        }else{
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update onlineStatus value of user
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update onlineStatus value of user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //check status
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline with last seen timestamp(Sửa trạng thái ofline theo dấu thời gian nhìn cuối)
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener); // delete event listener on firebase
    }

    @Override
    protected void onResume() {
        //check status
        checkOnlineStatus("online");
        super.onResume();
    }
}