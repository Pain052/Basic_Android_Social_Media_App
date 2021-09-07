package com.example.haintpd04043_assignment_androidnetworking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.haintpd04043_assignment_androidnetworking.fragment.ChatListFragment;
import com.example.haintpd04043_assignment_androidnetworking.fragment.HomeFragment;
import com.example.haintpd04043_assignment_androidnetworking.fragment.ProfileFragment;
import com.example.haintpd04043_assignment_androidnetworking.fragment.UsersFragment;
import com.example.haintpd04043_assignment_androidnetworking.loginAndRegister.LoginActivity;
import com.example.haintpd04043_assignment_androidnetworking.notification.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    String mUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //init FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //Bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //Home Fragment is default fragment on start
        HomeFragment fragmentHome = new HomeFragment();
        FragmentTransaction transactionHome = getSupportFragmentManager().beginTransaction();
        transactionHome.replace(R.id.content, fragmentHome, "");
        transactionHome.commit();

        checkUserStatus();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //handle item clicks
            switch (item.getItemId()){
                case R.id.nav_home:
                    //Home Fragment
                    HomeFragment fragmentHome = new HomeFragment();
                    FragmentTransaction transactionHome = getSupportFragmentManager().beginTransaction();
                    transactionHome.replace(R.id.content, fragmentHome, "");
                    transactionHome.commit();
                    return true;
                case R.id.nav_users:
                    //Users Fragment
                    UsersFragment fragmentUsers = new UsersFragment();
                    FragmentTransaction transactionUsers = getSupportFragmentManager().beginTransaction();
                    transactionUsers.replace(R.id.content, fragmentUsers, "");
                    transactionUsers.commit();
                    return true;
                case R.id.nav_profile:
                    //Profile Fragment
                    ProfileFragment fragmentProfile = new ProfileFragment();
                    FragmentTransaction transactionProfile = getSupportFragmentManager().beginTransaction();
                    transactionProfile.replace(R.id.content, fragmentProfile, "");
                    transactionProfile.commit();
                    return true;
//                case R.id.nav_chat:
//                    //Chats Fragment
//                    ChatListFragment fragmentChat = new ChatListFragment();
//                    FragmentTransaction transactionChat = getSupportFragmentManager().beginTransaction();
//                    transactionChat.replace(R.id.content, fragmentChat, "");
//                    transactionChat.commit();
//                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus(){
        //get User current
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            mUID = user.getUid();

            //save uid of user login in shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //update token
            updateToken(FirebaseMessaging.getInstance().getToken().toString());
        }else{
            //go to back Login Activity
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
    }

    public void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        reference.child(mUID).setValue(mToken);
    }
}