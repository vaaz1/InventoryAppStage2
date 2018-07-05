package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
//import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
//import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.example.android.inventoryappstage2.data.Contract;
import com.example.android.inventoryappstage2.data.Contract.ProductEntry;

import java.util.zip.Inflater;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int PRODUCT_LOADER = 0;
    private EditText productNameEditText;
    private EditText priceEditText;
    private EditText quantityEditText;
    private EditText supplierNameEditText;
    private EditText phoneEditText;
    private TextView productNameDisplayText;
    private TextView priceDisplayText;
    private TextView quantityDisplayText;
    private TextView supplierNameDisplayText;
    private TextView phoneDisplayText;
    private Button addOneButton;
    private Button reduceOneButton;
    private ImageView callButton;
    private int quantity;
    private Uri currentProductUri;

    private boolean productEditable = false;

    private boolean productHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent intent = getIntent();
        currentProductUri = intent.getData();
        if (currentProductUri == null) {
            setTitle(R.string.detail_activity_title_new_product);
            productEditable = true;
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.detail_activity_title_edit_product);
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
            productEditable = false;
            invalidateOptionsMenu();
        }
        productNameEditText = findViewById(R.id.edit_product_name);
        quantityEditText = findViewById(R.id.edit_quantity);
        priceEditText = findViewById(R.id.edit_price);
        supplierNameEditText = findViewById(R.id.edit_supplier_name);
        phoneEditText = findViewById(R.id.edit_phone);

        productNameDisplayText = findViewById(R.id.tv_product_name);
        quantityDisplayText = findViewById(R.id.tv_quantity);
        priceDisplayText = findViewById(R.id.tv_price);
        supplierNameDisplayText = findViewById(R.id.tv_supplier_name);
        phoneDisplayText = findViewById(R.id.tv_phone);

        addOneButton = findViewById(R.id.add_item_button);
        reduceOneButton = findViewById(R.id.reduce_item_button);
        callButton = findViewById(R.id.call_button);

        setViewVisibiliy();

        productNameEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        phoneEditText.setOnTouchListener(touchListener);

        quantity = 0;

        addOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(quantityEditText.getText())) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
                }
                quantity += 1;
                quantityEditText.setText(Integer.toString(quantity));
            }
        });

        reduceOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(quantityEditText.getText())) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityEditText.getText().toString().trim());
                }
                quantity -= 1;
                if (quantity <= 0) {
                    quantity = 0;
                }
                quantityEditText.setText(Integer.toString(quantity));
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneText = phoneDisplayText.getText().toString().trim();
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneText));
                if (callIntent.resolveActivity(v.getContext().getPackageManager()) != null) {
                    v.getContext().startActivity(callIntent);
                }
            }
        });
    }

    private void setViewVisibiliy() {
        if (productEditable) {
            productNameEditText.setVisibility(View.VISIBLE);
            quantityEditText.setVisibility(View.VISIBLE);
            priceEditText.setVisibility(View.VISIBLE);
            supplierNameEditText.setVisibility(View.VISIBLE);
            phoneEditText.setVisibility(View.VISIBLE);

            productNameDisplayText.setVisibility(View.GONE);
            quantityDisplayText.setVisibility(View.GONE);
            priceDisplayText.setVisibility(View.GONE);
            supplierNameDisplayText.setVisibility(View.GONE);
            phoneDisplayText.setVisibility(View.GONE);

            addOneButton.setVisibility(View.VISIBLE);
            reduceOneButton.setVisibility(View.VISIBLE);
            callButton.setClickable(false);
        } else {
            productNameEditText.setVisibility(View.GONE);
            quantityEditText.setVisibility(View.GONE);
            priceEditText.setVisibility(View.GONE);
            supplierNameEditText.setVisibility(View.GONE);
            phoneEditText.setVisibility(View.GONE);

            productNameDisplayText.setVisibility(View.VISIBLE);
            quantityDisplayText.setVisibility(View.VISIBLE);
            priceDisplayText.setVisibility(View.VISIBLE);
            supplierNameDisplayText.setVisibility(View.VISIBLE);
            phoneDisplayText.setVisibility(View.VISIBLE);

            addOneButton.setVisibility(View.GONE);
            reduceOneButton.setVisibility(View.GONE);
            callButton.setClickable(true);
        }
    }

    private void saveProduct() {
        String nameString = productNameEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String supplierString = supplierNameEditText.getText().toString().trim();
        String phoneString = phoneEditText.getText().toString().trim();

        if (currentProductUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) && TextUtils.isEmpty(phoneString)) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        int quantityInt = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantityInt = Integer.parseInt(quantityString);
        }
        contentValues.put(ProductEntry.COLUMN_QUANTITY, quantityInt);
        int priceInt = 0;
        if (!TextUtils.isEmpty(priceString)) {
            priceInt = Integer.parseInt(priceString);
        }
        contentValues.put(ProductEntry.COLUMN_PRICE, priceInt);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierString);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, phoneString);

        if (currentProductUri == null) {
            Uri newUri;
            newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
            if (newUri == null) {
                Toast toast = Toast.makeText(this, R.string.error_saving_product, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, R.string.product_saved, Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentProductUri, contentValues, null, null);
            if (rowsAffected == 0) {
                Toast toast = Toast.makeText(this, R.string.error_saving_product, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, R.string.product_saved, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // This method passes you the Menu object as it currently exists so you can modify it, such as add, remove, or disable items
        super.onPrepareOptionsMenu(menu);
        if (currentProductUri == null) {
            // new product - actions save
            MenuItem menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDelete.setVisible(false);
            MenuItem menuItemEdit = menu.findItem(R.id.action_edit);
            menuItemEdit.setVisible(false);
        }
        if (productEditable == false) {
            // Product overview - actions delete and edit
            MenuItem menuItemSave = menu.findItem(R.id.action_save);
            menuItemSave.setVisible(false);
        } else {
            // Product edit mode - actions delete and save
            MenuItem menuItemEdit = menu.findItem(R.id.action_edit);
            menuItemEdit.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // call save
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                // call delete
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_edit:
                // change to edit mode
                productEditable = true;
                setTitle(R.string.detail_activity_title_edit_product);
                invalidateOptionsMenu();
                setViewVisibiliy();
                return true;
            case android.R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(ProductDetailActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(currentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_successful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_NAME, ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE, ProductEntry.COLUMN_SUPPLIER_NAME, ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        return new CursorLoader(this, currentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String productName = cursor.getString(productNameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String phone = cursor.getString(phoneColumnIndex);

            productNameEditText.setText(productName);
            quantityEditText.setText(Integer.toString(quantity));
            priceEditText.setText(Integer.toString(price));
            supplierNameEditText.setText(supplierName);
            phoneEditText.setText(phone);

            productNameDisplayText.setText(productName);
            quantityDisplayText.setText(Integer.toString(quantity));
            priceDisplayText.setText(Integer.toString(price));
            supplierNameDisplayText.setText(supplierName);
            phoneDisplayText.setText(phone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEditText.setText("");
        quantityEditText.setText("");
        priceEditText.setText("");
        supplierNameEditText.setText("");
        phoneEditText.setText("");

        productNameDisplayText.setText("");
        quantityDisplayText.setText("");
        priceDisplayText.setText("");
        supplierNameDisplayText.setText("");
        phoneDisplayText.setText("");
    }
}
