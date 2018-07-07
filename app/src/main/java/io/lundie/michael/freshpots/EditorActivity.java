package io.lundie.michael.freshpots;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import io.lundie.michael.freshpots.data.ItemsContract.ItemEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    /** Identifier for item laoder (uses the same identifier as CatalogueActivity */
    private static final int ITEM_LOADER = 0;

    /** Content URI for the existing item (null if it is new) */
    private Uri mCurrentItemUri;

    /** TextView field for name */
    private TextView mNameTextView;

    /** EditText field for Item Name */
    private EditText mNameEditText;

    //** TextView field for item Type */
    private TextView mTypeTextView;

    /** EditText field for Item Type */
    private EditText mTypeEditText;

    /** TextView for Item Cost */
    private TextView mCostTextView;

    /** EditText field for Item Cost*/
    private EditText mCostEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mAvailabilitySpinner;

    /** TextView for the stock field */
    private TextView mStockTextView;

    /** TextView for the order field */
    private TextView mOrderTextView;

    /** Variable for stock quantity */
    private int mStockQuantity = 0;

    /** Variable for order quantity */
    private int mOrderQuantity = 0;

    /**
     * Gender of the pet. The possible valid values are in the PetContract.java file:
     * {@link ItemEntry#AVAILABILITY_UNAVAILABLE}, {@link ItemEntry#AVAILABILITY_ONLINE}, or
     * {@link ItemEntry#AVAILABILITY_INSTORE, {@link ItemEntry#AVAILABILITY_ALL}}.
     */
    private int mAvailability = ItemEntry.AVAILABILITY_UNAVAILABLE;

    /** Boolean flag keeps track of any edits made */
    private boolean mItemEntriesHaveChanged = false;

    /** Boolean value to flag any input errors */

    private static boolean isInputError = false;

    /**
     * Setup OnTouchListener to check for user interactions which suggest data has changed.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.i("TEST", "Logged touch event");
            mItemEntriesHaveChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Lets get our URI from intent to check if we are editing an item or making a new one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mTypeEditText = (EditText) findViewById(R.id.edit_item_type);
        mCostEditText = (EditText) findViewById(R.id.edit_item_cost);
        mAvailabilitySpinner = (Spinner) findViewById(R.id.spinner_availability);
        mStockTextView = (TextView) findViewById(R.id.textview_stock);
        mOrderTextView = (TextView) findViewById(R.id.textview_order);

        // If it looks like there is no URI - lets create a new item.
        if (mCurrentItemUri == null) {
            // Set the title to "Add new product".
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Set the initial stock quantity to 0;
            mStockTextView.setText("0");
            mOrderTextView.setText("0");

            // Display all of the edit fields.
            showAllEditFields();

            // Invalidate the options menu, to hide the delete button.
            invalidateOptionsMenu();

            // Setup OnTouchListeners on all the input fields, so we can determine if the user
            // has touched or modified them. This will let us know if there are unsaved changes
            // or not, if the user tries to leave the editor without saving.
            mNameEditText.setOnTouchListener(mTouchListener);
            mTypeEditText.setOnTouchListener(mTouchListener);
            mCostEditText.setOnTouchListener(mTouchListener);
            mAvailabilitySpinner.setOnTouchListener(mTouchListener);
        } else {
            // If the above is not true, it looks like we will be editing an item.
            //Let's set up accordingly.
            setTitle(getString(R.string.editor_activity_title_edit_item));

            mNameTextView = (TextView) findViewById(R.id.item_name);
            mTypeTextView = (TextView) findViewById(R.id.item_type);
            mCostTextView = (TextView) findViewById(R.id.cost);

            mNameTextView.setOnClickListener(this);
            mTypeEditText.setOnTouchListener(mTouchListener);
            mCostEditText.setOnTouchListener(mTouchListener);
            mAvailabilitySpinner.setOnTouchListener(mTouchListener);


            // Initialize loader and read data from database. We'll be using the same loader ID as
            // the dashboard and catalogue.
            getLoaderManager().initLoader(ITEM_LOADER, null, this);


        }

        setupSpinner();
        setupEditStockView();
        setupOrderStockView();

    }

    private void setupEditStockView() {
        Button plusButton = findViewById(R.id.button_stock_plus1);
        Button minusButton = findViewById(R.id.button_stock_minus1);

        //TODO: Add error messages for stock input using variable.
        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemEntriesHaveChanged = true;
                mStockQuantity = increment(mStockQuantity, 99);
                mStockTextView.setText(Integer.toString(mStockQuantity));
            }
        });
        minusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemEntriesHaveChanged = true;
                mStockQuantity = decrement(mStockQuantity, 1);
                mStockTextView.setText(Integer.toString(mStockQuantity));
            }
        });
    }

    private void setupOrderStockView() {
        Button plusButton = findViewById(R.id.button_order_plus1);
        Button minusButton = findViewById(R.id.button_order_minus1);

        //TODO: Add error messages for stock input using variable.
        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemEntriesHaveChanged = true;
                mOrderQuantity = increment(mOrderQuantity, 99);
                mOrderTextView.setText(Integer.toString(mOrderQuantity));
            }
        });
        minusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemEntriesHaveChanged = true;
                mOrderQuantity = decrement(mOrderQuantity, 1);
                mOrderTextView.setText(Integer.toString(mOrderQuantity));
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Let's create a spinner adapter using the default layout and simple drop down style.
        ArrayAdapter availabilitySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_availability_options, android.R.layout.simple_spinner_item);

        availabilitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Set adapter to spinner.
        mAvailabilitySpinner.setAdapter(availabilitySpinnerAdapter);

        // Check selected strings and set the constant values appropriately
        mAvailabilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.availability_unavailable))) {
                        mAvailability = ItemEntry.AVAILABILITY_UNAVAILABLE;
                    } else if (selection.equals(getString(R.string.availability_instore))) {
                        mAvailability = ItemEntry.AVAILABILITY_INSTORE;
                    } else if (selection.equals(getString(R.string.availability_online))) {
                        mAvailability = ItemEntry.AVAILABILITY_ONLINE;
                    } else {
                        mAvailability = ItemEntry.AVAILABILITY_ALL;
                    }
                }
            }

            // Implementing method for abstract class (AdapterView)
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mAvailability = ItemEntry.AVAILABILITY_UNAVAILABLE;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.item_name:
                Log.i("TEST", "Logged touch event");
                mNameTextView.setVisibility(View.GONE);
                mNameEditText.setVisibility(View.VISIBLE);
                mItemEntriesHaveChanged = true;
                break;
            case R.id.item_type:
                mTypeTextView.setVisibility(View.GONE);
                mTypeEditText.setVisibility(View.VISIBLE);
                mItemEntriesHaveChanged = true;
                break;
            case R.id.item_cost:
                mCostTextView.setVisibility(View.GONE);
                mCostEditText.setVisibility(View.VISIBLE);
                mItemEntriesHaveChanged = true;
                break;
        }
    }

    /**
     * Checks to see if input fields are empty.
     * TODO: There must be a nicer way to write this. Thsi feels somewhat convoluted.
     */

    private void checkForEmptyInputs(String name, String type, String cost) {
        // Check to see if the name input is empty.
        if (TextUtils.isEmpty(name)) {
            mNameEditText.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
            setInputError(true);
        }
        // Check to see if the type input is empty.
        if (TextUtils.isEmpty(type)) {
            mTypeEditText.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
            setInputError(true);
        }
        // check to see if the cost input is empty
        if (TextUtils.isEmpty(cost)) {
            mCostEditText.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
            setInputError(true);
        }

        if (mAvailability == ItemEntry.AVAILABILITY_INSTORE && mStockQuantity == 0 ||
                mAvailability == ItemEntry.AVAILABILITY_ONLINE && mStockQuantity == 0 ||
                mAvailability == ItemEntry.AVAILABILITY_ALL && mStockQuantity == 0) {
            // TODO: Automatically update this in the editor.
            showAvailabilityConfirmationDialog();
        }
    }

    /**
     * Checks input fields. Saves item to database user inputted data, or returns error using.
     */
    private boolean saveItem() {
        // Let's get the input text from our input fields
        String nameString = mNameEditText.getText().toString().trim();
        String typeString = mTypeEditText.getText().toString().trim();
        String costString = mCostEditText.getText().toString().trim();

        //Is a new item being added?
        //Check to see if all fields are blank.
        if (mCurrentItemUri == null && mAvailability == ItemEntry.AVAILABILITY_UNAVAILABLE &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(typeString) &&
                TextUtils.isEmpty(costString) && mStockQuantity == 0 && mOrderQuantity == 0 ){
                // Since no fields were modified, we can return early without creating a new pet.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                Toast.makeText(this, getString(R.string.editor_nothing_added),
                        Toast.LENGTH_SHORT).show();
                return true;
        }

        // Check to see if the any inputs are empty is empty.
        checkForEmptyInputs(nameString, typeString, costString);
        if (isInputError) { // An input error was returned.
            return false;  // Don't continue save.
        }

        // Create a ContentValues object wwith column names as key and item attributes as value.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_TYPE, typeString);
        values.put(ItemEntry.COLUMN_ITEM_COST, costString);
        values.put(ItemEntry.COLUMN_ITEM_AVAILABILITY, mAvailability);
        values.put(ItemEntry.COLUMN_ITEM_STOCK, mStockQuantity);
        values.put(ItemEntry.COLUMN_ITEM_ORDERQUANTITY, mOrderQuantity);

        // Cost should not be null.
        int cost = Integer.parseInt(costString);

        values.put(ItemEntry.COLUMN_ITEM_COST, cost);

        // Check if this is a new item, or an item being updated.
        if (mCurrentItemUri == null) {
            // Looks like this is a new item
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_item_insert_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_item_insert_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Looks like this item exists. Let's update using the URI specified in mCurrentItemUri
            // Null for the selection and selection args since we already know the row we'll update.
            int rowsAffected = getContentResolver().update
                    (mCurrentItemUri, values, null, null);

            // Show a toast on success or failure.
            if (rowsAffected == 0) {
                // Error with update
                Toast.makeText(this, getString(R.string.editor_item_update_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Success!
                Toast.makeText(this, getString(R.string.editor_item_update_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // Item hasn't changed - so continue to process request.
        if (!mItemEntriesHaveChanged) {
            super.onBackPressed();
            return;
        }

        // If there are unsaved changes, show a prompt to gain user confirmation to discard changes.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Discard clicked. Close activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // IF we are inserting a new item, hide the delete button.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if(saveItem()) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, navigate to parent activity.
                if (!mItemEntriesHaveChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Looks like there are unsaved changes. Let's warn the user with a prompt.
                //Get confirmation to discard edits.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Set-up projection so we can populate the editor fields if necessary.
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_TYPE,
                ItemEntry.COLUMN_ITEM_COST,
                ItemEntry.COLUMN_ITEM_STOCK,
                ItemEntry.COLUMN_ITEM_ORDERQUANTITY,
                ItemEntry.COLUMN_ITEM_AVAILABILITY };

        // Return a new CursorLoader, executing the ContentProvider query on background thread.
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // If there is no data, make a prompt exit. Get out while you still can!
        if (data == null || data.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int typeColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_TYPE);
            int costColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_COST);
            int stockColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_STOCK);
            int orderColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_ORDERQUANTITY);
            int availabilityColumnIndex = data.getColumnIndex(ItemEntry.COLUMN_ITEM_AVAILABILITY);

            // Extract out the value from the cursor for the given column index
            String name = data.getString(nameColumnIndex);
            String type = data.getString(typeColumnIndex);
            int cost = data.getInt(costColumnIndex);
            mStockQuantity = data.getInt(stockColumnIndex);
            mOrderQuantity = data.getInt(orderColumnIndex);
            int availability = data.getInt(availabilityColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTypeEditText.setText(type);
            mCostEditText.setText(Integer.toString(cost));
            mStockTextView.setText(Integer.toString(mStockQuantity));
            mOrderTextView.setText(Integer.toString(mOrderQuantity));

            //Use a switch to map constant values to one of the spinner drop down options.
            // Set appropriately to display on screen.
            switch (availability) {
                case ItemEntry.AVAILABILITY_INSTORE:
                    mAvailabilitySpinner.setSelection(1);
                    break;
                case ItemEntry.AVAILABILITY_ONLINE:
                    mAvailabilitySpinner.setSelection(2);
                    break;
                case ItemEntry.AVAILABILITY_ALL:
                    mAvailabilitySpinner.setSelection(3);
                    break;
                default:
                    mAvailabilitySpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clear out data from input fields on loader invalidation.
        mNameEditText.setText("");
        mTypeEditText.setText("");
        mCostEditText.setText("");
        mAvailabilitySpinner.setSelection(0); //Set back to default.
        mStockTextView.setText("0");
        mOrderTextView.setText("0");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * Dialogue Code is from Udacity Pets App: https://github.com/udacity/ud845-Pets
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set message, clickListener and confirmation buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to get confirmation of item/product deletion.
     */
    private void showAvailabilityConfirmationDialog() {
        // Create an AlertDialog.Builder and set message, clickListener and confirmation buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.stock_conflict_dialog_msg);
        builder.setPositiveButton(R.string.btn_change_availability, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked change availability button. So change and save.
                // Return false. (no error)
                mAvailability = ItemEntry.AVAILABILITY_UNAVAILABLE;
            }
        });
        builder.setNegativeButton(R.string.btn_change_stock, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User opted to change the stock. Go back to editor and highlight errors.
                // Return true. Input error.
                setInputError(true);
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to get confirmation of item/product deletion.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set message, clickListener and confirmation buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Execute deletion of database item at given URI/row ID.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Request ContentResolver to delete item at the given content URI.
            // Null for the selection and selection args since we already know the row we'll update.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // Yikes - nothing deleted. Error.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Success!
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    // [HELPER METHODS] //

    /**
     * Method to display either edit fields
     */

    private void showAllEditFields() {
        mNameEditText.setVisibility(View.VISIBLE);
        mTypeEditText.setVisibility(View.VISIBLE);
        mCostEditText.setVisibility(View.VISIBLE);
        mAvailabilitySpinner.setVisibility(View.VISIBLE);

    }

    /**
     * Method to set the input error variable
     * @param status
     */
    private void setInputError(boolean status) {
        // If an input error has already been set, we don't want to override the value with a
        // following false value. In other words, if there is an error - lock error value.
        if(isInputError && !status) {
            return;
        } isInputError = status;
    }

    /**
     * This method is called to increment quantity.
     */
    public int increment(int quantity, int limit) {
        if (quantity <=limit) {
            quantity++;
        } else {
            Toast.makeText(this, "Sorry, you have reached the stock limit.",Toast.LENGTH_SHORT).show();
        }
        return quantity;
    }

    /**
     * This method is called to decrement quantity.
     */
    public int decrement(int quantity, int limit) {
        if (quantity >=limit) {
            quantity --;
        } else {
            Toast.makeText(this, "Sorry, it is not possible to have negative stock.",Toast.LENGTH_SHORT).show();
        }
        return quantity;
    }
}