package io.lundie.michael.freshpots.utilities;

import android.content.Context;
import android.widget.Toast;

public class Counter {
    private int quantity = 1;
    private Context context;
    private int maximumQuantity;
    private int minimumQuantity;
    private String messageMinimum;
    private String messageMaximum;

    public Counter(Context context, int max, String maxToast, int min, String minToast) {
        this.context = context;
        this.maximumQuantity = max-1;
        this.messageMaximum = maxToast;
        this.minimumQuantity = min+1;
        this.messageMinimum = minToast;
    }

    /**
     * This method is called to increment quantity.
     */
    public void increment() {
        if (quantity <=maximumQuantity) {
            quantity++;
        } else {
            Toast.makeText(context, messageMaximum, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method is called to decrement quantity.
     */
    public void decrement() {
        if (quantity >=minimumQuantity) {
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
