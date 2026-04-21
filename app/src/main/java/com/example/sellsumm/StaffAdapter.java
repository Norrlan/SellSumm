package com.example.sellsumm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StaffAdapter extends
        RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    public interface OnStaffDeleteListener {
        void onDelete(String uid, int position);
    }

    private final List<StaffModel> staffList;
    private final OnStaffDeleteListener deleteListener;

    public StaffAdapter(List<StaffModel> staffList,
                        OnStaffDeleteListener deleteListener) {
        this.staffList      = staffList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.manage_staff_row, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StaffViewHolder holder, int position) {
        StaffModel staff = staffList.get(position);

        holder.staffName.setText(staff.getFullName());
        holder.staffRole.setText(staff.getRole());

        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(staff.getUid(), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView  staffName, staffRole;
        ImageView deleteBtn;

        StaffViewHolder(View itemView) {
            super(itemView);
            staffName = itemView.findViewById(R.id.staffName);
            staffRole = itemView.findViewById(R.id.staffRole);
            deleteBtn = itemView.findViewById(R.id.mstaffDeleteBtn);
        }
    }
}