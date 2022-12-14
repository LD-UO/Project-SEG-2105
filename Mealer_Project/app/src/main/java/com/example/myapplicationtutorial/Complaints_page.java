package com.example.myapplicationtutorial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Complaints_page extends AppCompatActivity {

    ListView complaintlist;
    DatabaseReference complaint_reference;
    ArrayList<Complaint> complaints = new ArrayList<Complaint>();
    ArrayAdapter<Complaint> adapter;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints_page);
        back = (ImageView) findViewById(R.id.button);

        complaint_reference = FirebaseDatabase.getInstance().getReference("Complaint");

        complaintlist = (ListView) findViewById(R.id.complaint_list);

        onItemClick();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This should go back to the admin main menu, should not log them out
                finish();
            }
        });
        }

    protected void onStart() {
        super.onStart();
        complaint_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                complaints.clear();
                for (DataSnapshot complaintSnapshot : snapshot.getChildren()) {
                    String chefUsername = complaintSnapshot.child("chefUsername").getValue(String.class);
                    String description = complaintSnapshot.child("description").getValue(String.class);
                    String endDate = complaintSnapshot.child("endDate").getValue(String.class);
                    String id = complaintSnapshot.child("id").getValue(String.class);
                    boolean isAssessed = (Boolean) complaintSnapshot.child("addressed").getValue();
                    Complaint complaint = new Complaint(description, chefUsername, endDate, id);
                    if (!isAssessed) {
                        complaints.add(complaint);
                    }
                }

                ComplaintList complaintAdapter = new ComplaintList(Complaints_page.this, complaints);
                complaintlist.setAdapter(complaintAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
        private void onItemClick() {

            complaintlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Complaint complaint = complaints.get(i);
                    showUpdateDeleteDialog(complaint.getDescription(),complaint.getChefUsername(),complaint.getId());
                //return true;
                }
            });
        }
    private void showUpdateDeleteDialog(String description, String chefUsername,final String id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.view_complaint_diaglog, null);
        dialogBuilder.setView(dialogView);

        //make these in xml
        final EditText editTextEndDate = (EditText) dialogView.findViewById(R.id.endDate);
        final Button buttonApprove = (Button) dialogView.findViewById(R.id.buttonApprove);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDelete);


        dialogBuilder.setTitle(chefUsername);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String endDate = editTextEndDate.getText().toString();

                    approveComplaint(description,chefUsername,endDate,id);
                    b.dismiss();

            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteComplaint(id);
                b.dismiss();
            }
        });
    }

    private void approveComplaint(String description, String chefUsername,
                                  String endDate, String id) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("Complaint").child(id);

        Complaint complaint = new Complaint( description,  chefUsername,
                 endDate,  id);

        complaint.approve();

        dR.setValue(complaint);

        Toast.makeText(getApplicationContext(), "Complaint Approved", Toast.LENGTH_LONG).show();
    }

    private boolean deleteComplaint(String id) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Complaint").child(id);

        databaseReference.removeValue();

        Toast.makeText(getApplicationContext(), "Complaint Deleted", Toast.LENGTH_LONG).show();
        return true;
    }
}