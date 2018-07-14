package io.lundie.michael.freshpots.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import io.lundie.michael.freshpots.R;
import io.lundie.michael.freshpots.utilities.Counter;

public class CounterDialog extends Dialog implements View.OnClickListener {

    private TextView dialogTitleView;
    private Button dialogConfirmButton, dialogCancelButton;
    private CounterDialog.OnButtonClick onButtonClick;
    private Context mContext;
    private String mDialogTitle;
    private int mCounterMax;
    private String mCounterMaxToast;
    private int mCounterMin;
    private String mCounterMinToast;
    private Counter counter;


    public interface OnButtonClick {
        void onConfirmButtonClick();
        void onCancelButtonClick();
    }

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

    //initialise views
    private void initViews() {
        dialogTitleView = findViewById(R.id.dialog_title);
        dialogConfirmButton = findViewById(R.id.dialog_counter_confirm);
        dialogCancelButton = findViewById(R.id.dialog_counter_cancel);
        dialogConfirmButton.setOnClickListener(this);
        dialogCancelButton.setOnClickListener(this);

        //Set up our increment and decrement buttons.
        final Button incrementQuantity = findViewById(R.id.dialog_counter_button_plus);
        final Button decrementQuantity = findViewById(R.id.dialog_counter_button_minus);
        final TextView quantityTextView = findViewById(R.id.dialog_textview_counter_quantity);

        //TODO: Replace String literals.
        counter = new Counter(mContext, mCounterMax,
                mCounterMaxToast + mCounterMax + ".",
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

    }

    //set title for dialog
    public void setAlertTitle(String title) {
        dialogTitleView.setText(mDialogTitle);
    }

    public void setListener(CounterDialog.OnButtonClick onButtonClick){
        this.onButtonClick = onButtonClick;
    }

    public void onClick(View v) {
        if(onButtonClick !=null){
            if (v == dialogConfirmButton) {
                onButtonClick.onConfirmButtonClick();
            }
            if (v == dialogCancelButton) {
                onButtonClick.onCancelButtonClick();
            }
        }
    }

    public int getCounterValue() { return counter.getQuantity(); }
}
