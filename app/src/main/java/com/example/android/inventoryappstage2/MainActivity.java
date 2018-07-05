package com.example.android.inventoryappstage2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventoryappstage2.data.Contract;
import com.example.android.inventoryappstage2.data.Contract.ProductEntry;
import com.example.android.inventoryappstage2.data.ProductProvider;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int PRODUCT_LOADER = 1;
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    ItemCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = findViewById(R.id.list_view_products);
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        cursorAdapter = new ItemCursorAdapter(this, null);
        productListView.setAdapter(cursorAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent detailIntent = new Intent(MainActivity.this, ProductDetailActivity.class);
                Uri currentUri = ContentUris.withAppendedId(Contract.ProductEntry.CONTENT_URI, id);
                detailIntent.setData(currentUri);
                startActivity(detailIntent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        Log.v(LOG_TAG, "get LoaderManager called");
    }

    private void insertProduct() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, "Toto");
        contentValues.put(ProductEntry.COLUMN_PRICE, 7);
        contentValues.put(ProductEntry.COLUMN_QUANTITY, 80);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, "Lucky Supplier");
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, "097643124");
        Log.v(LOG_TAG, "Content values put");

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
        Log.v(LOG_TAG, "insert content values to database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(Contract.ProductEntry.CONTENT_URI, null, null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllProducts();
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Log.v(LOG_TAG, "create Loader");
        String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE, ProductEntry.COLUMN_QUANTITY};
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished");
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, "reset Loader");
        cursorAdapter.swapCursor(null);
    }
}
