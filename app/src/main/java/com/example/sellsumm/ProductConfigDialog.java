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

    public interface ProductSaveListener {
        void onProductSaved(ProductModel product);
    }

    private ProductSaveListener listener;
    private ProductModel existingProduct;
    private String storeId;

    public static ProductConfigDialog newInstanceForAdd(String storeId)
    {
        ProductConfigDialog dialog = new ProductConfigDialog();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        dialog.setArguments(args);
        return dialog;
    }

    // For editng the product on dialog
    public static ProductConfigDialog newInstanceForEdit(ProductModel product, String storeId) {
        ProductConfigDialog dialog = new ProductConfigDialog();
        Bundle args = new Bundle();
        args.putString("storeId", storeId);
        args.putString("product_id", product.getProductId());
        args.putString("price", String.valueOf(product.getPrice()));
        args.putString("product_name", product.getProductName());
        args.putString("product_category", product.getProductCategory());
        args.putString("product_type", product.getProductType());
        dialog.setArguments(args);
        return dialog;
    }

    // save the edited product
    public void setProductSaveListener(ProductSaveListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);

        if (getArguments() != null) {

            storeId = getArguments().getString("storeId");

            String id       = getArguments().getString("product_id");
            String priceStr = getArguments().getString("price", "0");
            String name     = getArguments().getString("product_name");
            String category = getArguments().getString("product_category");
            String type     = getArguments().getString("product_type");

            if (id != null) {
                double price = 0;
                try { price = Double.parseDouble(priceStr); } catch (Exception ignored) {}

                boolean isAddon = type != null && type.equalsIgnoreCase("Add-on");

                existingProduct = new ProductModel(
                        id,
                        price,
                        name,
                        category,
                        type,
                        isAddon
                );
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.product_dialog_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnClose      = view.findViewById(R.id.closeDialogBtn);
        EditText  inputPrice    = view.findViewById(R.id.productpriceInput);
        EditText  inputName     = view.findViewById(R.id.productNameInput);
        EditText  inputCategory = view.findViewById(R.id.productCategoryInput);
        Spinner   spinnerType   = view.findViewById(R.id.spinner3);
        Button    btnSave       = view.findViewById(R.id.saveProductButton);

        // Dropdown logic for the default and add-on product types
        String[] productTypes = {"Default", "Add-on"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, productTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spinnerAdapter);


        if (existingProduct != null) {
            inputPrice.setText(String.valueOf(existingProduct.getPrice()));
            inputName.setText(existingProduct.getProductName());
            inputCategory.setText(existingProduct.getProductCategory());

            spinnerType.setSelection(existingProduct.isAddon() ? 1 : 0);
        }

        btnClose.setOnClickListener(v -> dismiss());

        //onclicklistener saves the product details to the product firestore collection

        btnSave.setOnClickListener(v -> {

            String priceStr = inputPrice.getText() != null ? inputPrice.getText().toString().trim() : "0";
            String name = inputName.getText() != null ? inputName.getText().toString().trim() : "";
            String category = inputCategory.getText() != null ? inputCategory.getText().toString().trim() : "";
            String type = spinnerType.getSelectedItem().toString();

            if (name.isEmpty()) {
                inputName.setError("Product name is required");
                return;
            }

            double price = 0;
            try { price = Double.parseDouble(priceStr); } catch (Exception ignored) {}

            String productId =
                    (existingProduct != null && existingProduct.getProductId() != null)
                            ? existingProduct.getProductId()
                            : UUID.randomUUID().toString();

            boolean isAddon = type.equalsIgnoreCase("Add-on");

            ProductModel saved = new ProductModel(productId, price, name, category, type, isAddon);

            saveToFirestore(saved);

            if (listener != null) listener.onProductSaved(saved);

            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    // Method to save each product to its specific store in firestore
    private void saveToFirestore(ProductModel product)
    {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("productId", product.getProductId());
        data.put("price", product.getPrice());
        data.put("productName", product.getProductName());
        data.put("productCategory", product.getProductCategory());
        data.put("productType", product.getProductType());
        data.put("isAddon", product.isAddon());

        db.collection("stores").document(storeId).collection("products").document(product.getProductId()).set(data)
             .addOnSuccessListener(aVoid ->
                        android.util.Log.d("ProductConfig", "Product saved: " + product.getProductName()))
                .addOnFailureListener(e ->
                        android.util.Log.e("ProductConfig", "Save failed: " + e.getMessage()));
    }
}
