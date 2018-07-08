package io.lundie.michael.freshpots.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import io.lundie.michael.freshpots.data.ItemsContract.ItemEntry;

/**
 * {@link ContentProvider} for the FreshPots app.
 */
public class ItemsProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ItemsProvider.class.getSimpleName();

    /** Initialise database helper variable */
    private ItemsDbHelper mDbHelper;


    /** URI matcher code for the content URI for our items table */
    private static final int ITEMS = 100;

    /** URI matcher code for the content URI a single item contained within the items table */
    private static final int ITEM_ID = 101;

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

        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY, ItemsContract.PATH_ITEMS, ITEMS);

        sUriMatcher.addURI(ItemsContract.CONTENT_AUTHORITY,
                ItemsContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        mDbHelper = new ItemsDbHelper(getContext());

        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Query Items table with given projection. The returned cursor can 
                // contain multiple rows.
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                // Extract ID from URI. Here only one row is returned, matching the requested ID 
                // from the URI.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // Return our cursor with the given id (extracted from the URI).
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Let's define our data insert method.
     * This will only apply to URI case "ITEMS" (which returns our entire  table) since new 
     * data will be automatically assigned a new ID on insertion.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Sorry! " +
                        "We cannot insert data using the given URI: " + uri);
        }
    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("An item name must be assigned.");
        }

        // Check that the item type is not null
        String type = values.getAsString(ItemEntry.COLUMN_ITEM_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("An item type must be assigned.");
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer cost = values.getAsInteger(ItemEntry.COLUMN_ITEM_COST);
        if (cost == null && cost < 0) {
            throw new IllegalArgumentException("A valid integer value for cost is required.");
        }

        // Check that any stock value given is valid.
        // Stock can be null
        Integer stock = values.getAsInteger(ItemEntry.COLUMN_ITEM_STOCK);
        if (stock != null && stock < 0 && stock > 100) {
            throw new IllegalArgumentException("A valid stock assignment " +
                    "from 1 to 100 is required. (Null is accepted)");
        }

        // Check for valid availability
        Integer availability = values.getAsInteger(ItemEntry.COLUMN_ITEM_AVAILABILITY);
        if (availability == null || !ItemEntry.isValidAvailablitity(availability)) {
            throw new IllegalArgumentException("A valid availability entry is required.");
        }

        // Checking for a valid order quantity if it was provided.
        // Field can be null.
        Integer orderQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_ORDERQUANTITY);
        if (orderQuantity != null && orderQuantity < 0 && orderQuantity > 100) {
            throw new IllegalArgumentException("A valid order quantity " +
                    "from 1 to 100 is required.");
        }

        // If there has been a sale, validate entry before insertion.
        // Field can be null.
        Integer sales = values.getAsInteger(ItemEntry.COLUMN_ITEM_SALES);
        if (sales != null && sales < 0) {
            throw new IllegalArgumentException("Item requires a valid sale quantity.");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // Extract out the ID from the ITEM_ID appended URI. This informs us which row we
                // will update. Selection arguments will in turn be a String Array, which will also
                // include the ID.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ItemEntry#COLUMN_ITEM_NAME} key exists in {@link values} variable, check
        // for a null value
        if (values.containsKey(ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("An item name must be assigned.");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_TYPE} key exists , check for a null value.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_TYPE)) {
            String type = values.getAsString(ItemEntry.COLUMN_ITEM_TYPE);
            if (type == null) {
                throw new IllegalArgumentException("An item type must be assigned.");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_COST} key exists, throw error if null or below 0.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_COST)) {
            Integer cost = values.getAsInteger(ItemEntry.COLUMN_ITEM_COST);
            if (cost == null && cost < 0) {
                throw new IllegalArgumentException("A valid integer value for cost is required.");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_STOCK} key exists, throw error if null or below 0
        // or greater than 100.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_STOCK)) {
            Integer stock = values.getAsInteger(ItemEntry.COLUMN_ITEM_STOCK);
            if (stock != null && stock < 0 && stock > 100) {
                throw new IllegalArgumentException("A valid stock assignment " +
                        "from 1 to 100 is required. (Null is accepted)");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_AVAILABILITY} key exists, check for a valid entry.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_AVAILABILITY)) {
            Integer availability = values.getAsInteger(ItemEntry.COLUMN_ITEM_AVAILABILITY);
            if (availability == null || !ItemEntry.isValidAvailablitity(availability)) {
                throw new IllegalArgumentException("A valid availability entry is required.");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_ORDERQUANTITY} key exists and is not null
        // make sure that the item not is below 0 or greater than 100.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_ORDERQUANTITY)) {
            Integer orderQuantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_ORDERQUANTITY);
            if (orderQuantity != null && orderQuantity < 0 && orderQuantity > 100) {
                throw new IllegalArgumentException("A valid order quantity " +
                        "from 1 to 100 is required.");
            }
        }

        // If the {@link ItemEntry#COLUMN_ITEM_SALES} key exists and is not null
        // make sure that the item is not below 0.
        if (values.containsKey(ItemEntry.COLUMN_ITEM_SALES)) {
            Integer sales = values.getAsInteger(ItemEntry.COLUMN_ITEM_SALES);
            if (sales != null && sales < 0) {
                throw new IllegalArgumentException("Item must have a valid sale quantity.");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        getContext().getContentResolver().notifyChange(uri, null);

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        // Track the number of rows that were deleted
        int rowsDeleted;

        switch (match) {
            case ITEMS:
                // Delete all rows matching selection and selectionArgs.
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row which matches the ID contained in the URI.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Delete a single row, based on the ID extracted from the URI
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Oops! Deletion is not supported for " + uri);
        }

        //Let's check to see if any rows were changes and then notify listeners appropriately.
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return number of rows deleted.
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}