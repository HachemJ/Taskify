package com.example.jads;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class DeleteUnverifiedAccountWorker extends Worker {

    public DeleteUnverifiedAccountWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");
        if (userId == null) {
            return Result.failure();
        }

        // Reload user and check email verification status
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getUid().equals(userId)) {
            user.reload();
            if (!user.isEmailVerified()) {
                // Delete the unverified account from Firebase Authentication and Database
                FirebaseDatabase.getInstance().getReference("unverified_users").child(userId).removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user.delete();
                            }
                        });
            }
        }
        return Result.success();
    }
}
