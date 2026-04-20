package com.example.sellsumm;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProductConfigDialog extends DialogFragment {

    // Listener interface
    public interface ProductSaveListener {
        void onProductSaved(ProductModel product);
    }

    private ProductSaveListener listener;
    private ProductModel existingProduct;

    // ── Factory methods ──────────────────────────────────────────

    public static ProductConfigDialog newInstanceForAdd() {
        return new ProductConfigDialog();
    }

    public static ProductConfigDialog newInstanceForEdit(ProductModel product) {
        ProductConfigDialog dialog = new ProductConfigDialog();
        Bundle args = new Bundle();
        args.putString("product_id",   product.getProductId());
        args.putString("sku",          product.getSku());
        args.putString("price",        String.valueOf(product.getPrice()));
        args.putString("product_name", product.getProductName());
        args.putString("product_type", product.getProductType());
        dialog.setArguments(args);
        return dialog;
    }

    public void setProductSaveListener(ProductSaveListener listener) {
        this.listener = listener;
    }

    // ── Lifecycle ────────────────────────────────────────────────

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);

        if (getArguments() != null) {
            existingProduct = new ProductModel(
                    getArguments().getString("product_id"),
                    getArguments().getString("sku"),
                    Double.parseDouble(
                            getArguments().getString("price", "0")),
                    getArguments().getString("product_name"),
                    getArguments().getString("product_type")
            );
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.product_dialog_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── View references ──────────────────────────────────────
        ImageView btnClose      = view.findViewById(R.id.closeDialogBtn);
        EditText  inputSku      = view.findViewById(R.id.SKUInput);
        EditText  inputPrice    = view.findViewById(R.id.productpriceInput);
        EditText  inputName     = view.findViewById(R.id.productNameInput);
        Spinner   spinnerType   = view.findViewById(R.id.spinner3);
        Button    btnSave       = view.findViewById(R.id.saveProductButton);

        // ── Spinner setup ────────────────────────────────────────
        // Two options only — Default and Add-on
        String[] productTypes = {"Default", "Add-on"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                productTypes);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinnerAdapter);

        // ── Pre-fill if editing ──────────────────────────────────
        if (existingProduct != null) {
            inputSku.setText(existingProduct.getSku());
            inputPrice.setText(String.valueOf(existingProduct.getPrice()));
            inputName.setText(existingProduct.getProductName());

            // Pre-select correct spinner position
            if ("Add-on".equals(existingProduct.getProductType())) {
                spinnerType.setSelection(1);
            } else {
                spinnerType.setSelection(0); // Default
            }
        }

        // ── Close button ─────────────────────────────────────────
        btnClose.setOnClickListener(v -> dismiss());

        // ── Save button ──────────────────────────────────────────
        btnSave.setOnClickListener(v -> {

            // Collect values
            String sku  = inputSku.getText() != null ?
                    inputSku.getText().toString().trim() : "";
            String priceStr = inputPrice.getText() != null ?
                    inputPrice.getText().toString().trim() : "0";
            String name = inputName.getText() != null ?
                    inputName.getText().toString().trim() : "";
            String type = spinnerType.getSelectedItem().toString();

            // Validate
            if (sku.isEmpty()) {
                inputSku.setError("SKU is required");
                return;
            }
            if (name.isEmpty()) {
                inputName.setError("Product name is required");
                return;
            }

            double price = 0;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                price = 0;
            }

            // Build product ID
            String productId = (existingProduct != null
                    && existingProduct.getProductId() != null
                    && !existingProduct.getProductId().isEmpty())
                    ? existingProduct.getProductId()
                    : UUID.randomUUID().toString();

            // Build product model
            ProductModel saved = new ProductModel(
                    productId, sku, price, name, type);

            // Save to Firestore
            saveToFirestore(saved);

            // Fire callback to update UI
            if (listener != null) listener.onProductSaved(saved);
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    // ── Firestore save ───────────────────────────────────────────
    private void saveToFirestore(ProductModel product) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("productId",   product.getProductId());
        data.put("sku",         product.getSku());
        data.put("price",       product.getPrice());
        data.put("productName", product.getProductName());
        data.put("productType", product.getProductType());

        db.collection("products")
                .document(product.getProductId())
                .set(data)
                .addOnSuccessListener(aVoid ->
                        android.util.Log.d("ProductConfig",
                                "Product saved: " + product.getProductName()))
                .addOnFailureListener(e ->
                        android.util.Log.e("ProductConfig",
                                "Save failed: " + e.getMessage()));
    }
}