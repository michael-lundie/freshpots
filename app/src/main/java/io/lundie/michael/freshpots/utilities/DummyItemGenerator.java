package io.lundie.michael.freshpots.utilities;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

import io.lundie.michael.freshpots.R;
import io.lundie.michael.freshpots.data.ItemsContract.ItemEntry;

/**
 * A simple class responsible for partially randomly generating dummy data used for
 * testing of our database.
 */
public class DummyItemGenerator {

    private Context mContext;
    public DummyItemGenerator(Context context) {mContext = context;}

    public ContentValues generateItem(){
        switch (generateNumber(1, 4)) {
            case 1:
                return itemOne();
            case 2:
                return itemTwo();
            default:
                return itemThree();
        }
    }
    private ContentValues itemOne(){
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dummy_coffee_1);
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Hills Bros Rich ");
        values.put(ItemEntry.COLUMN_ITEM_TYPE, "Fresh Coffee");
        values.put(ItemEntry.COLUMN_ITEM_COST, generateNumber(1,3000));
        values.put(ItemEntry.COLUMN_ITEM_STOCK, generateNumber(0,8));
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, DbBitmapUtility.getBytes(bitmap));
        values.put(ItemEntry.COLUMN_ITEM_AVAILABILITY, ItemEntry.AVAILABILITY_ALL);
        return values;
    }
    private ContentValues itemTwo(){
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dummy_coffee_3);
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "StarBucks Sakura Tumbler");
        values.put(ItemEntry.COLUMN_ITEM_TYPE, "Travel Mug");
        values.put(ItemEntry.COLUMN_ITEM_COST, generateNumber(1,3000));
        values.put(ItemEntry.COLUMN_ITEM_STOCK, generateNumber(0,8));
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, DbBitmapUtility.getBytes(bitmap));
        values.put(ItemEntry.COLUMN_ITEM_AVAILABILITY, ItemEntry.AVAILABILITY_ALL);
        return values;
    }

    private ContentValues itemThree(){
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dummy_coffee_2);
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, "Tokyo Coffee");
        values.put(ItemEntry.COLUMN_ITEM_TYPE, "Fresh Coffee");
        values.put(ItemEntry.COLUMN_ITEM_COST, generateNumber(1,3000));
        values.put(ItemEntry.COLUMN_ITEM_STOCK, generateNumber(0,8));
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, DbBitmapUtility.getBytes(bitmap));
        values.put(ItemEntry.COLUMN_ITEM_AVAILABILITY, ItemEntry.AVAILABILITY_ALL);
        return values;
    }

    private int generateNumber(int low, int high) {
        Random rInt = new Random();
        return rInt.nextInt(high-low) + low;
    }
}
