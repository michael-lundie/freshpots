package io.lundie.michael.freshpots;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.lundie.michael.freshpots.data.ItemsContract.ItemEntry;
import io.lundie.michael.freshpots.dialogs.CounterDialog;
import io.lundie.michael.freshpots.utilities.DbBitmapUtility;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter implements CounterDialog.OnButtonClick {

    private static final String LOG_TAG = ItemCursorAdapter.class.getName();

    private Uri mCurrentItemUri;
    private Context mContext;
    private CounterDialog salesDialog;
    private int currentItemStock;
    private int currentItemSales;

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * Method to bind item data with our list item view.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor with which to collect our data from.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

        mContext = context;

        // Find individual views that we want to modify in the list item layout
        ImageView itemImageView = view.findViewById(R.id.list_image);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView typeTextView = view.findViewById(R.id.productType);
        TextView stockTextView = view.findViewById(R.id.stock);
        TextView salesTextView = view.findViewById(R.id.sales);
        TextView costTextView = view.findViewById(R.id.cost);

        // Let's get the column index of the attributes we are interested in from the database.
        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int typeColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_TYPE);
        int stockColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_STOCK);
        int salesColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SALES);
        int costColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_COST);

        // Read the item attributes from the Cursor for the current item
        final int rowId = cursor.getInt(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        String itemType = cursor.getString(typeColumnIndex);
        final int itemStock = cursor.getInt(stockColumnIndex);
        final int itemSales = cursor.getInt(salesColumnIndex);
        String itemCost = cursor.getString(costColumnIndex);

        //Grab the URI of the current item
        final Uri itemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, rowId);

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        typeTextView.setText(context.getString(R.string.catalogue_item_type_text)
                + " " + itemType);
        stockTextView.setText(context.getString(R.string.catalogue_stock_text)
                + " " + String.valueOf(itemStock));
        salesTextView.setText(context.getString(R.string.catalogue_sales_text)
                + " " + String.valueOf(itemSales));
        costTextView.setText(itemCost + context.getString(R.string.currency_item_cost));

        // The image view requires some special attention to check for null
        byte imageByteArray[] = cursor.getBlob(imageColumnIndex);
        if (imageByteArray == null || imageByteArray.length <= 1){
            // If there is no image for the item, we will hide the image view.
            itemImageView.setVisibility(View.INVISIBLE);
        } else {
            // decode our image and set it to the image view
            Bitmap image = DbBitmapUtility.getImage(imageByteArray);
            itemImageView.setVisibility(View.VISIBLE);
            itemImageView.setImageBitmap(image);
        }

        Button editButton = view.findViewById(R.id.edit_item);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditorActivity.class);
                intent.setData(itemUri);
                context.startActivity(intent);
            }
        });

        final Button saleButton = view.findViewById(R.id.sell_item);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salesDialog = new CounterDialog(context, mContext.getString(R.string.sell_dialogue_title),
                        itemStock,
                        context.getString(R.string.toast_salecounter_max),
                        1,
                        context.getString(R.string.toast_salecounter_min));
                salesDialog.setListener(ItemCursorAdapter.this);
                if (itemStock == 0) {
                    Toast.makeText(context, context.getString(R.string.toast_nostock), Toast.LENGTH_SHORT).show();
                } else {
                    // Pass the values from our final variable for this cursor item, to the floating
                    // variable value, in order for our sales dialog to get access to correct values.
                    currentItemStock = itemStock;
                    currentItemSales = itemSales;
                    mCurrentItemUri = itemUri;
                    salesDialog.show();
                    Log.i(LOG_TAG, "TEST, item stock is:" +itemStock);
                }
            }
        });
    }

    @Override
    public void onConfirmButtonClick(CounterDialog salesDialog) {
        // Create a ContentValues object and attach key/value pair
        int quantityToSell = salesDialog.getCounterValue();
        int newTotalSalesQuantity = currentItemSales + quantityToSell;
        int newTotalStockQuantity = currentItemStock - quantityToSell;
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_SALES, newTotalSalesQuantity);
        values.put(ItemEntry.COLUMN_ITEM_STOCK,newTotalStockQuantity);
        mContext.getContentResolver().update(mCurrentItemUri, values,
                null, null );
        Toast.makeText(mContext, "confirmed", Toast.LENGTH_SHORT).show();
        salesDialog.dismiss();
    }

    @Override
    public void onCancelButtonClick(CounterDialog salesDialog) {
        salesDialog.dismiss();
    }
}