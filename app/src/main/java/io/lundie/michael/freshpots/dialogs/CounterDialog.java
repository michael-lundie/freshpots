package io.lundie.michael.freshpots.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.lundie.michael.freshpots.R;
import io.lundie.michael.freshpots.utilities.Counter;

public class CounterDialog extends Dialog implements View.OnClickListener {

    private static final String LOG_TAG = CounterDialog.class.getName();

    private Button dialogConfirmButton, dialogCancelButton;
    private CounterDialog.OnButtonClick onButtonClick;
    private Context mContext;
    private String mDialogTitle;
    private int mCounterMax;
    private String mCounterMaxToast;
    private int mCounterMin;
    private String mCounterMinToast;
    private Counter counter;

    /**
     * OnButtonClick interface. Allows us to specify specific action behaviour on a per instance
     * basis.
     */
    public interface OnButtonClick {
        void onConfirmButtonClick(CounterDialog dialog);
        void onCancelButtonClick(CounterDialog dialog);
    }

    /**
     * Class constructor
     * @param context context (where) current method is called from.
     * @param title String title for our dialog box
     * @param max the maximum amount our counter should reach as an integer
     * @param maxToast String toast message
     * @param min the minimum value our counter can be as an integer
     * @param minToast String toast message
     */
    public CounterDialog(Context context, String title, int max,
                         String maxToast, int min, String minToast) {
        super(context);
        mContext = context;
        mDialogTitle = title;
        mCounterMax = max;
        mCounterMaxToast = maxToast;
        mCounterMin = min;
        mCounterMinToast = minToast;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialogue_counter);
        initViews();
    }

    private void initViews() {
        TextView dialogTitleView = findViewById(R.id.dialog_title);
        dialogConfirmButton = findViewById(R.id.dialog_counter_confirm);
        dialogCancelButton = findViewById(R.id.dialog_counter_cancel);
        dialogConfirmButton.setOnClickListener(this);
        dialogCancelButton.setOnClickListener(this);

        //Set up our increment and decrement buttons.
        final Button incrementQuantity = findViewById(R.id.dialog_counter_button_plus);
        final Button decrementQuantity = findViewById(R.id.dialog_counter_button_minus);
        final TextView quantityTextView = findViewById(R.id.dialog_textview_counter_quantity);

        Log.i(LOG_TAG, "TEST, max var:" +mCounterMax);

        dialogTitleView.setText(mDialogTitle);

        counter = new Counter(mContext, mCounterMax,
                mCounterMaxToast + " " + mCounterMax + ".",
                mCounterMin, mCounterMinToast);

        decrementQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.decrement();
                quantityTextView.setText(counter.getQuantityAsString());
            }
        });

        incrementQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter.increment();
                quantityTextView.setText(counter.getQuantityAsString());
            }
        });

        // Let's programmatically set the minimum width of our dialog window based on the
        // current display width.
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        findViewById(R.id.counter_dialogue).setMinimumWidth(6 * width/7);
    }

    // Solved interface problems using code from https://stackoverflow.com/a/31066725
    public void setListener(CounterDialog.OnButtonClick onButtonClick){
        this.onButtonClick = onButtonClick;
    }

    public void onClick(View view) {
        if(onButtonClick !=null){
            if (view == dialogConfirmButton) {
                onButtonClick.onConfirmButtonClick(this);
            }
            if (view == dialogCancelButton) {
                onButtonClick.onCancelButtonClick(this);
            }
        }
    }

    public int getCounterValue() { return counter.getQuantity(); }
}