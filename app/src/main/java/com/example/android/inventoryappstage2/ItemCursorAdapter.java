package com.example.android.inventoryappstage2;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.Contract;
import com.example.android.inventoryappstage2.data.Contract.ProductEntry;
import com.example.android.inventoryappstage2.data.ProductProvider;

import javax.xml.datatype.Duration;


public class ItemCursorAdapter extends CursorAdapter {
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView tvProductName = (TextView) view.findViewById(R.id.product_name);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity_value);
        TextView tvPrice = (TextView) view.findViewById(R.id.price_value);
        Button btSell = (Button) view.findViewById(R.id.sell_button);
        Log.v(LOG_TAG, "Views found");

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        final int qualityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
        Log.v(LOG_TAG, "getColumnIndex with cursor");

        String nameString = cursor.getString(nameColumnIndex);
        String qualityString = cursor.getString(qualityColumnIndex);
        String priceString = cursor.getString(priceColumnIndex);
        Log.v(LOG_TAG, "get String by columnIndex");
        int quantity = Integer.parseInt(qualityString);

        tvProductName.setText(nameString);
        tvQuantity.setText(qualityString);
        tvPrice.setText(priceString);
        Log.v(LOG_TAG, "TV set text");

        final int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_QUANTITY));
        String currentId = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry._ID));
        final Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, Long.parseLong(currentId));

        btSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = currentQuantity - 1;
                if (quantity < 0) {
                    quantity = 0;
                    Toast toast = Toast.makeText(context, R.string.all_items_sold, Toast.LENGTH_SHORT);
                    toast.show();
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProductEntry.COLUMN_QUANTITY, quantity);

                context.getContentResolver().update(currentUri, contentValues, null, null);
            }
        });
    }
}
