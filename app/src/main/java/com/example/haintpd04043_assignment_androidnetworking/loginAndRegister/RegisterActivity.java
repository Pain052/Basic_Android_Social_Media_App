package com.example.haintpd04043_assignment_androidnetworking.loginAndRegister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.haintpd04043_assignment_androidnetworking.HomeActivity;
import com.example.haintpd04043_assignment_androidnetworking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText edEmail, edPassword1, edConfirmPassword;
    Button btnCreateAccount, btnBackLogin;

    ProgressDialog mDialog;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edEmail = findViewById(R.id.edEmail1);
        edPassword1 = findViewById(R.id.edPassword1);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        btnBackLogin = findViewById(R.id.btnBackLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Registering User...");
        mDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();
    }

    public void backLogin(View view){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }

    public void registerAc(View view){
        String email = edEmail.getText().toString();
        String password = edPassword1.getText().toString();
        String confirmPassword = edConfirmPassword.getText().toString();

        if (email.isEmpty()){
            edEmail.setError("Please email!");
            edEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 4){
            edPassword1.setError("Please password!");
            edPassword1.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()){
            edConfirmPassword.setError("Please Confirm password!");
            return;
        }

        if (confirmPassword.equals(password)){
            mDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        mDialog.dismiss();

                        FirebaseUser user = mAuth.getCurrentUser();
                        String email = user.getEmail();
                        String uid = user.getUid();
                        HashMap<Object, String> hashMap = new HashMap<>();
                        hashMap.put("email", email);
                        hashMap.put("uid", uid);
                        hashMap.put("name", ""); //This properties will add later (edit profile)
                        hashMap.put("onlineStatus", "online"); //This online status
                        hashMap.put("typingTo", "noOne"); //This typing status(Trạng thái trò chuyện)
                        hashMap.put("phone", ""); //This properties will add later (edit profile)
                        hashMap.put("image", ""); //This properties will add later (edit profile)
                        hashMap.put("cover", ""); //This properties will add later (edit profile)
                        //Use FirebaseDatabase instance to realtime database of firebase
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        //path t store user data named "Users"
                        DatabaseReference reference = database.getReference("Users");
                        //put data within hashmap in database
                        reference.child(uid).setValue(hashMap);

                        Toast.makeText(RegisterActivity.this, "Registered...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    }else {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            edConfirmPassword.setError("Confirm password does not match password!");
            edConfirmPassword.requestFocus();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
        }
    }
}