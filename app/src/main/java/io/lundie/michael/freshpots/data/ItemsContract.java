package io.lundie.michael.freshpots.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ItemsContract {

    /** Set up content authority constant */
    public static final String CONTENT_AUTHORITY = "io.lundie.michael.freshpots";

    /** Initialise and set-up our URI */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Set-up the items table constant */

    public static final String PATH_ITEMS = "items";

    public static final class ItemEntry implements BaseColumns {

        /** Initialise and set up ItemEntry URI constant */

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for an item list.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // Table and column constants.
        public static final String TABLE_NAME = "items";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_TYPE = "type";
        public static final String COLUMN_ITEM_COST = "cost";
        public static final String COLUMN_ITEM_IMAGE = "image";
        public static final String COLUMN_ITEM_STOCK = "stock";
        public static final String COLUMN_ITEM_AVAILABILITY = "availability";
        public static final String COLUMN_ITEM_ORDERQUANTITY = "order_quantity";
        public static final String COLUMN_ITEM_ORDERFLAG = "order_flag";
        public static final String COLUMN_ITEM_SALES = "sales";

        // Column fixed integer value constants
        public static final int AVAILABILITY_ALL = 3;
        public static final int AVAILABILITY_INSTORE = 1;
        public static final int AVAILABILITY_ONLINE = 2;
        public static final int AVAILABILITY_UNAVAILABLE = 0;

        public static final int ORDERFLAG_ACTIVE = 1;
        public static final int ORDERFLAG_INACTIVE = 0;

        /**
         * Returns whether or not item availability is {@link #AVAILABILITY_ALL}, {@link #AVAILABILITY_ONLINE},
         * or {@link #AVAILABILITY_INSTORE}.
         */
        public static boolean isValidAvailablitity(int availability) {
            return availability == AVAILABILITY_INSTORE || availability == AVAILABILITY_ONLINE ||
                    availability == AVAILABILITY_ALL || availability == AVAILABILITY_UNAVAILABLE;
        }

        /**
         * Returns whether or not an orderflag {@link #ORDERFLAG_ACTIVE}, {@link #ORDERFLAG_INACTIVE},
         * is valid.
         */
        public static boolean isValidFlag(int orderflag) {
            return orderflag == ORDERFLAG_ACTIVE || orderflag == ORDERFLAG_INACTIVE;
        }
    }
}