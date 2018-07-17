package io.lundie.michael.freshpots.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.lundie.michael.freshpots.data.ItemsContract.ItemEntry;

public class ItemsDbHelper extends SQLiteOpenHelper {

    /** Current database version. Increment upon database change. Note: For uprades,
     *  implement upgrade code in onUpgrade method.
     */
    public static final int DATABASE_VERSION = 1;

    /** Name of our database, of which stores various coffee products. */
    public static final String DATABASE_NAME = "ItemsContract.db";

    /** Set up the SQL code for creating our ITEMS database table. */
    private static final String SQL_CREATE_ITEMS_TABLE =
            "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                    ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL," +
                    ItemEntry.COLUMN_ITEM_TYPE + " TEXT," +
                    ItemEntry.COLUMN_ITEM_COST + " INTEGER NOT NULL," +
                    ItemEntry.COLUMN_ITEM_IMAGE + " BLOB," +
                    ItemEntry.COLUMN_ITEM_STOCK + " INTEGER NOT NULL," +
                    ItemEntry.COLUMN_ITEM_ORDERQUANTITY + " INTEGER," +
                    ItemEntry.COLUMN_ITEM_ORDERFLAG + " INTEGER," +
                    ItemEntry.COLUMN_ITEM_AVAILABILITY + " TEXT NOT NULL," +
                    ItemEntry.COLUMN_ITEM_SALES + " INTEGER DEFAULT 0);";

    /** Class constructor */
    public ItemsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}