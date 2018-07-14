package io.lundie.michael.freshpots;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import io.lundie.michael.freshpots.utilities.Counter;
import io.lundie.michael.freshpots.utilities.DbBitmapUtility;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter implements CounterDialog.OnButtonClick {

    public static final String LOG_TAG = ItemCursorAdapter.class.getName();

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
        final Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, rowId);

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
            itemImageView.setVisibility(View.GONE);
        } else {
            // decode our image and set it to the image view
            Bitmap image = DbBitmapUtility.getImage(imageByteArray);
            itemImageView.setImageBitmap(image);
        }

        Button editButton = view.findViewById(R.id.edit_item);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditorActivity.class);
                intent.setData(currentItemUri);
                context.startActivity(intent);
            }
        });

        Button saleButton = view.findViewById(R.id.sell_item);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemStock == 0) {
                    Toast.makeText(context, context.getString(R.string.toast_nostock), Toast.LENGTH_SHORT).show();
                } else {
                    sellItemDialogue(context, view, currentItemUri, itemStock, itemSales).show();
                }
            }
        });
    }

    @Override
    public void onConfirmButtonClick() {
        // Create a ContentValues object and attach key/value pair
        int quantityToSell = ;
        int newTotalSalesQuantity = sales + quantityToSell;
        int newTotalStockQuantity = stock - quantityToSell;
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_SALES, newTotalSalesQuantity);
        values.put(ItemEntry.COLUMN_ITEM_STOCK,newTotalStockQuantity);
        context.getContentResolver().update(itemUri, values,
                null, null );
        Toast.makeText(context, "confirmed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelButtonClick() {

    }

    /**
     * This method creates, displays and handles our sales dialogue.
     */
    private Dialog sellItemDialogue(final Context context, View view,
                                    final Uri itemUri, final int stock, final int sales) {

        final ViewGroup viewRoot = view.findViewById(R.id.sell_item_dialogue);

        // Let's inflate our dialogue from the XML script
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialogue_sell, viewRoot);


        // Begin a new AlertDialog builder.
        AlertDialog.Builder sellItemDialogBuilder = new AlertDialog.Builder(context);

        // Assign our inflate view to the dialog
        sellItemDialogBuilder.setView(dialogView);

        //Set up our increment and decrement buttons.
        final Button incrementSaleQuantity = dialogView.findViewById(R.id.sell_button_plus);
        final Button decrementSaleQuantity = dialogView.findViewById(R.id.sell_button_minus);
        final TextView quantityTextView = dialogView.findViewById(R.id.textview_salequantity);
        final Counter saleCounter = new Counter(context, stock,
                context.getString(R.string.toast_salecounter_max) + stock + ".", 1,
                context.getString(R.string.toast_salecounter_min));

        // Check which language has previously been selected. (Default is English)
        // Set our buttons appropriately.
        decrementSaleQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saleCounter.decrement();
                quantityTextView.setText(saleCounter.getQuantityAsString());
            }
        });

        incrementSaleQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saleCounter.increment();
                quantityTextView.setText(saleCounter.getQuantityAsString());
            }
        });

        // Let's build the rest of our dialog
        sellItemDialogBuilder
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.sell_dialogue_confirm),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // Create a ContentValues object and attach key/value pair
                                int quantityToSell = saleCounter.getQuantity();
                                int newTotalSalesQuantity = sales + quantityToSell;
                                int newTotalStockQuantity = stock - quantityToSell;
                                ContentValues values = new ContentValues();
                                values.put(ItemEntry.COLUMN_ITEM_SALES, newTotalSalesQuantity);
                                values.put(ItemEntry.COLUMN_ITEM_STOCK,newTotalStockQuantity);
                                context.getContentResolver().update(itemUri, values,
                                        null, null );
                               Toast.makeText(context, "confirmed", Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton(context.getResources().getString(R.string.sell_dialogue_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // Create the dialog from the builder
        return sellItemDialogBuilder.create();
    }
}