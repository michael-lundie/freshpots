package io.lundie.michael.freshpots.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.lundie.michael.freshpots.R;
import io.lundie.michael.freshpots.utilities.Counter;

public class GeneralDialog extends BaseDialog<GeneralDialog.OnClickListener> {

    public GeneralDialog(@NonNull Context context) {
        super(context);
    }

    // interface to handle the dialog click back to the Activity
    public interface OnDialogClickListener {
        public void onConfirmClicked(GeneralDialog dialog);
        public void onCancelClicked(GeneralDialog dialog);
    }

    // Create an instance of the Dialog with the input
    public static GeneralDialogFragment newInstance(String title, String message, int max,
                                                    String maxToast, int min, String minToast) {
        GeneralDialogFragment frag = new GeneralDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("counterMax", max);
        args.putString("toastMessageMax", maxToast);
        args.putInt("counterMin", min);
        args.putString("toastMessageMin", minToast);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // Let's inflate our dialogue from the XML script
        View dialogView = inflater.inflate(R.layout.dialogue_counter, container, false);

        Context context = getActivity().getApplicationContext();

        // Begin a new AlertDialog builder.
        AlertDialog.Builder generalDialogBuilder = new AlertDialog.Builder(context);

        // Assign our inflate view to the dialog
        generalDialogBuilder.setView(dialogView);

        //Set up our increment and decrement buttons.
        final Button incrementSaleQuantity = dialogView.findViewById(R.id.restock_button_plus);
        final Button decrementSaleQuantity = dialogView.findViewById(R.id.restock_button_minus);
        final TextView quantityTextView = dialogView.findViewById(R.id.textview_restock_quantity);

        //TODO: Replace String literals.
        final Counter restockCounter = new Counter(context, getArguments().getInt("counterMin"),
                this.getString(R.string.dialogue_restock_toast_counter_max)
                        + getArguments().getInt("counterMax") + ".", 1,
                this.getString(R.string.dialogue_restock_toast_counter_min));

        decrementSaleQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restockCounter.decrement();
                quantityTextView.setText(restockCounter.getQuantityAsString());
            }
        });

        incrementSaleQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restockCounter.increment();
                quantityTextView.setText(restockCounter.getQuantityAsString());
            }
        });

        generalDialogBuilder
                .setTitle(getArguments().getString("title"))
                .setMessage(getArguments().getString("message"))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Positive button clicked
                                getActivityInstance().onConfirmClicked(GeneralDialogFragment.this);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // negative button clicked
                                getActivityInstance().onCancelClicked(GeneralDialogFragment.this);
                            }
                        }
                ).create();

        return dialogView;
    }
}