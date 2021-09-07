package com.example.haintpd04043_assignment_androidnetworking.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.haintpd04043_assignment_androidnetworking.adapter.AdapterUsers;
import com.example.haintpd04043_assignment_androidnetworking.model.ModelUser;
import com.example.haintpd04043_assignment_androidnetworking.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    SearchView svSearch;

    FirebaseAuth firebaseAuth;
    DatabaseReference reference;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        svSearch = view.findViewById(R.id.svSearch);

        firebaseAuth = FirebaseAuth.getInstance();

        //init user list
        userList = new ArrayList<>();

        //getAllUsers
        getAllUsers();

        //search text
        search(svSearch.getQuery().toString());

        return view;
    }

    private void search(String searchText){
        //Search listener with a text
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){ // result true if string is null or length by 0
                    //search text by input text
                    searchUsers(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user is not enters(Duoc goi khi nguoi dung khong nhap)
                //Get all users
                getAllUsers();
                adapterUsers.notifyDataSetChanged();
                return false;
            }
        });
    }

    private void searchUsers(String query) {
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);

                    //get all searched users signed on firebase
                    if (!user.getUid().equals(fUser.getUid())){

                        if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                            user.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(user);
                        }
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterUsers);
                    //Update Adapter
                    adapterUsers.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllUsers() {
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelUser user = ds.getValue(ModelUser.class);

                    //get all user exist login on firebase
                    if (!user.getUid().equals(fUser.getUid())){
                        userList.add(user);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}