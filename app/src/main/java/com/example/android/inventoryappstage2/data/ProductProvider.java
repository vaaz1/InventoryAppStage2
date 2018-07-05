package com.example.android.inventoryappstage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import com.example.android.inventoryappstage2.R;
import com.example.android.inventoryappstage2.data.Contract.ProductEntry;


/**
 * {@link ContentProvider} for Pets app.
 */
public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PROD = 100;
    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PROD_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // Add 2 content URIs to URI matcher
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_PRODUCTS, PROD);
        sUriMatcher.addURI(Contract.CONTENT_AUTHORITY, Contract.PATH_PRODUCTS + "/#", PROD_ID);
    }

    private DBHelper productDBHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // initialize a PetDbHelper object to gain access to the pets database.
        productDBHelper = new DBHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = productDBHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PROD_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {

        // Check that name is not null
        String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), R.string.missing_name_msg,Toast.LENGTH_SHORT).show();
            return null;
        }

        // Check that supplier name is not null
        String supplierName = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(getContext(), R.string.missing_supplier_name_msg, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Check that phone is not null
        String phone = values.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(),R.string.missing_phone_msg, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Check that quantity is not null
        Integer price = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (price != null && price < 0) {
            Toast.makeText(getContext(), R.string.wrong_price_msg, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Insert a new product into the inventory database table with the given ContentValues
        SQLiteDatabase database = productDBHelper.getWritableDatabase();
        long id = database.insert(ProductEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PROD_ID:
                // For the PROD_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Update the selected product in the inventory database table with the given ContentValues
        if (values.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(),R.string.missing_name_msg,Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String name = values.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(),R.string.missing_supplier_name_msg,Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String phone = values.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(getContext(),R.string.missing_phone_msg, Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                Toast.makeText(getContext(),R.string.wrong_quantity_msg,Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        if (values.containsKey(ProductEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ProductEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                Toast.makeText(getContext(),R.string.wrong_price_msg,Toast.LENGTH_SHORT).show();
                return 0;
            }
        }

        // Return the number of rows that were affected
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = productDBHelper.getWritableDatabase();
        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = productDBHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROD_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PROD:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PROD_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}