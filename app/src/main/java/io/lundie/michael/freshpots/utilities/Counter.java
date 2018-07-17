package io.lundie.michael.freshpots.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.lundie.michael.freshpots.ItemCursorAdapter;

/**
 * A simple counter class containing the logic to construct a simple UI counter.
 */
public class Counter {

    private static final String LOG_TAG = Counter.class.getName();

    private int quantity;
    private Context context;
    private int maximumQuantity;
    private int minimumQuantity;
    private String messageMinimum;
    private String messageMaximum;

    /**
     * Counter constructor method
     * @param context context from which this constructor is called from.
     * @param max the maximum amount our counter should reach as an integer
     * @param maxToast String toast message
     * @param min the minimum value our counter can be as an integer
     * @param minToast String toast message
     */
    public Counter(Context context, int startValue, int max, String maxToast, int min, String minToast) {
        this.context = context;
        this.quantity = startValue;
        this.maximumQuantity = max;
        this.messageMaximum = maxToast;
        this.minimumQuantity = min;
        this.messageMinimum = minToast;
        Log.i(LOG_TAG, "TEST, max is" + max);
    }

    /**
     * This method is called to increment quantity.
     */
    public void increment() {
        if (quantity < maximumQuantity) {
            quantity++;
        } else {
            Toast.makeText(context, messageMaximum, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is called to decrement quantity.
     */
    public void decrement() {
        if (quantity > minimumQuantity) {
            quantity --;
        } else {
            Toast.makeText(context, messageMinimum, Toast.LENGTH_SHORT).show();
        }
    }

    public int getQuantity() {
        return quantity;
    }

    public String getQuantityAsString() {
        return String.valueOf(quantity);
    }
}