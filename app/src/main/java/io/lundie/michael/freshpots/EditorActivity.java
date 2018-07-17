package io.lundie.michael.freshpots;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import io.lundie.michael.freshpots.data.ItemsContract.ItemEntry;
import io.lundie.michael.freshpots.dialogs.CounterDialog;
import io.lundie.michael.freshpots.utilities.Counter;
import io.lundie.michael.freshpots.utilities.DbBitmapUtility;
import io.lundie.michael.freshpots.utilities.DecodeByteArrayAsyncTask;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, CounterDialog.OnButtonClick{

    public static final String LOG_TAG = EditorActivity.class.getName();

    /** Static constant for max order quantity, image request and item loader identifier */
    private static final int MAX_ORDER_QUANTITY = 30, IMAGE_REQUEST = 3, ITEM_LOADER = 0;

    /** Content URI for the existing item (null if it is new) */
    private Uri mCurrentItemUri;

    /** ImageView for item image */
    private ImageView mImagePictureView, mBackgroundImagePictureView;

    /** Image ByteArray */
    private byte[] imageByteArray;

    /** Image for item image */
    private Bitmap mImage;

    /** EditText field for Item Name, Item Type, Item Cost */
    private EditText mNameEditText, mTypeEditText, mCostEditText;

    /** EditText field to enter the pet's gender */
    private Spinner mAvailabilitySpinner;

    /** TextView for the stock field, Order Field */
    private TextView mStockTextView, mOrderTextView;

    private Button addImageButton, restockButton, plusButton, minusButton;

    private MenuItem deleteItemButton, editItemButton, saveItemButton;

    /** Variable for stock quantity, order quantity */
    private int mStockQuantity = 0, mOrderQuantity = 0;

    /** Variable for restock dialogue */
    private CounterDialog restockDialog;

    private int pendingQuantity;

    /**
     * Item availability. The possible valid values are in the ItemContract.java file:
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

    /**
     * Method override for our restock dialog. Passes information via intent for item restock order
     * @param restockDialog object instance ID of our dialog
     */
    @Override
    public void onConfirmButtonClick(CounterDialog restockDialog) {
        // Create a ContentValues object and attach key/value pair
        int orderQuantity = restockDialog.getCounterValue();

        // Build our e-mail using string builder
        StringBuilder emailBuilder = new StringBuilder();
        emailBuilder.append(getString(R.string.email_order_content));
        emailBuilder.append(mNameEditText.getText());
        emailBuilder.append('\n');
        emailBuilder.append(getString(R.string.email_order_content_quantity));
        emailBuilder.append('\n');
        emailBuilder.append(orderQuantity);

        //Solution to setting intent for e-mail only found at:
        //https://stackoverflow.com/a/14671082/9738433
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("vnd.android.cursor.item/email");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"test@lundie.io"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.email_order_subject));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailBuilder.toString());
        startActivity(Intent.createChooser(emailIntent, getString(R.string.email_order_chooser)));
        pendingQuantity = orderQuantity;
        restockDialog.dismiss();
    }

    /**
     * Method override for restock dialog dismissal
     * @param restockDialog object instance ID of our dialog
     */
    @Override
    public void onCancelButtonClick(CounterDialog restockDialog) {
        restockDialog.dismiss();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Lets get our URI from intent to check if we are editing an item or making a new one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_item_name);
        mTypeEditText = findViewById(R.id.edit_item_type);
        mCostEditText = findViewById(R.id.edit_item_cost);
        mAvailabilitySpinner = findViewById(R.id.spinner_availability);
        mStockTextView = findViewById(R.id.textview_stock);
        mOrderTextView = findViewById(R.id.textview_order);
        restockButton = findViewById(R.id.restock_item);
        plusButton = findViewById(R.id.button_stock_plus1);
        minusButton = findViewById(R.id.button_stock_minus1);

        // Get image views
        mImagePictureView = findViewById(R.id.item_image_view);
        mBackgroundImagePictureView = findViewById(R.id.edit_header_background);

        // Setup OnTouchListeners on all the input fields to check for any possible user edits
        // Checking of counter fields happens later.
        mNameEditText.setOnTouchListener(mTouchListener);
        mTypeEditText.setOnTouchListener(mTouchListener);
        mCostEditText.setOnTouchListener(mTouchListener);
        mAvailabilitySpinner.setOnTouchListener(mTouchListener);

        // Set up spinner
        setupSpinner();

        // Tutorial for getting image from gallery followed: https://youtu.be/_xIWkCJZCu0
        addImageButton = findViewById(R.id.add_item_image);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get image gallery implicitly
                Intent chooseImageIntent = new Intent(Intent.ACTION_PICK);

                // Find data in the following location
                File imagesDirectory = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                String imageDirectoryPath = imagesDirectory.getPath();
                // Get URI representation
                Uri data = Uri.parse(imageDirectoryPath);

                // Set Data and MIME type
                chooseImageIntent.setDataAndType(data, "image/*");

                startActivityForResult(chooseImageIntent, IMAGE_REQUEST);
            }
        });

        // If it looks like there is no URI - lets create a new item.
        if (mCurrentItemUri == null) {
            // Set the title to "Add new product".
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Set the initial stock quantity to 0;
            mStockTextView.setText("0");
            mOrderTextView.setText("0");

            // Setup our edit stock view
            setupEditStockView();

            // Invalidate the options menu, to hide the delete button.
            invalidateOptionsMenu();
            mAvailabilitySpinner.setOnTouchListener(mTouchListener);

            // Hide the restock field for adding new products
            findViewById(R.id.container_order).setVisibility(View.GONE);
        } else {
            // Disable automatic pop-up of our keyboard
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            // Set up the counter dialog for restock view
            restockDialog = new CounterDialog(this,
                    1,
                    getString(R.string.dialogue_restock_title),
                    MAX_ORDER_QUANTITY,
                    this.getString(R.string.dialogue_restock_toast_counter_max) + MAX_ORDER_QUANTITY + ".",
                    1,
                    this.getString(R.string.dialogue_restock_toast_counter_min));
            restockDialog.setListener(this);
            restockButton.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restockDialog.show();
                }
            });

            // Disable editing until user clicks edit button
            disableEditing();

            // We are in view mode, so set the title accordingly
            setTitle(getString(R.string.editor_activity_title_view_item));

            // Initialize loader and read data from database. Use consistent loader ID
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }
    }

    /**
     * Override method for our image gallery intent.
     * @param requestCode
     * @param resultCode
     * @param data The resulting intent data returned from our intent.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // success!
            if (requestCode == IMAGE_REQUEST) { // Data returned from image gallery
                Uri imageUri = data.getData();
                // Declare a stream to read the image data
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    // Decode bitmap from input stream
                    mImage = BitmapFactory.decodeStream(inputStream);
                    mImage = Bitmap.createScaledBitmap(mImage, 220, 220, true);
                    // display image in the editor ui
                    mImagePictureView.setImageBitmap(mImage);
                    mBackgroundImagePictureView.setImageBitmap(mImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, this.getString(R.string.toast_image_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Just in case the app is paused for a long enough period of time that our local
        // variables are trashed to clean out space, we'll pop our values in outState bundle
        if(pendingQuantity > 0){
            outState.putInt("pending_value", pendingQuantity);
        }
        Log.i(LOG_TAG, "TEST: save called");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(LOG_TAG, "TEST: Restore called");
        pendingQuantity = savedInstanceState.getInt("pending_value");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(LOG_TAG, "TEST: on resume called");
        if(pendingQuantity != 0 ) {
            showOrderConfirmationDialog();
        }
    }

    /**
     * Simple method used for setting up the stock input counter views.
     */
    private void setupEditStockView() {

        // Create a new counter object and assign buttons appropriately
        final Counter stockCounter = new Counter(this, mStockQuantity,
                99, getString(R.string.toast_stockcounter_max) ,
                1, getString(R.string.toast_stockcounter_min));

        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemEntriesHaveChanged = true;
                stockCounter.increment();
                mStockQuantity = stockCounter.getQuantity();
                mStockTextView.setText(stockCounter.getQuantityAsString());
            }
        });
        minusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemEntriesHaveChanged = true;
                stockCounter.decrement();
                mStockQuantity = stockCounter.getQuantity();
                mStockTextView.setText(stockCounter.getQuantityAsString());
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

    /**
     * Method for checking to see if any inputs are empty and set up warning prompts appropriately.
     */
    private void checkForEmptyInputs(String name, String type, String cost) {
        LinearLayout spinnerContainer, stockContainer;
        spinnerContainer = findViewById(R.id.container_spinner);
        stockContainer = findViewById(R.id.container_stock);

        // Check to see if the name input is empty.
        if (TextUtils.isEmpty(name)) {
            mNameEditText.setBackgroundResource(R.drawable.rounded_left_solid_error);
            setInputError(true);
        } else {
            mNameEditText.setBackgroundResource(R.drawable.rounded_left_solid_details);
        }

        // Check to see if the type input is empty.
        if (TextUtils.isEmpty(type)) {
            mTypeEditText.setBackgroundResource(R.drawable.rounded_left_solid_error);
            setInputError(true);
        } else {
            mTypeEditText.setBackgroundResource(R.drawable.rounded_left_solid_details);
        }

        // check to see if the cost input is empty
        if (TextUtils.isEmpty(cost)) {
            mCostEditText.setBackgroundResource(R.drawable.rounded_left_solid_error);
            setInputError(true);
        } else {
            mCostEditText.setBackgroundResource(R.drawable.rounded_left_solid_details);
        }

        //Check to make sure there are no availability/stock conflicts
        if (mAvailability == ItemEntry.AVAILABILITY_INSTORE && mStockQuantity == 0 ||
                mAvailability == ItemEntry.AVAILABILITY_ONLINE && mStockQuantity == 0 ||
                mAvailability == ItemEntry.AVAILABILITY_ALL && mStockQuantity == 0 ||
                mAvailability == ItemEntry.AVAILABILITY_UNAVAILABLE && mStockQuantity > 0) {
            // TODO: Automatically update this in the editor.
            Toast.makeText(this, getString(R.string.toast_stock_conflict), Toast.LENGTH_SHORT).show();
            stockContainer.setBackgroundResource(R.drawable.rounded_solid_error);
            spinnerContainer.setBackgroundResource(R.drawable.rounded_left_solid_error);
            setInputError(true);
        } else {
            stockContainer.setBackgroundResource(R.drawable.rounded_solid_white);
            spinnerContainer.setBackgroundResource(R.drawable.rounded_left_solid_details);
        }
    }

    /**
     * Saves item to database user inputted data, or returns error using.
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
            isInputError = false; // Reset error variable
            return false;  // Don't continue save.
        }
        if (mImage != null) {
            // Compress and encode image before data insertion.
            // Not executing on separate thread, since that thread will complete after data insertion
            // causing the image not ot be updated.
            //TODO: Find a solution to the above problem so the UI thread is not held up.
            imageByteArray = DbBitmapUtility.getBytes(mImage);
        }

        // Create a ContentValues object with column names as key and item attributes as value.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_TYPE, typeString);
        values.put(ItemEntry.COLUMN_ITEM_COST, costString);
        values.put(ItemEntry.COLUMN_ITEM_IMAGE, imageByteArray);
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
        deleteItemButton = menu.findItem(R.id.action_delete);
        editItemButton = menu.findItem(R.id.action_edit);
        saveItemButton = menu.findItem(R.id.action_save);
        if (mCurrentItemUri == null) {
            deleteItemButton.setVisible(false);
            editItemButton.setVisible(false);
            saveItemButton.setVisible(true);
        } else {
            saveItemButton.setVisible(false);
            saveItemButton.setVisible(true);
            deleteItemButton.setVisible(false);
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
            case R.id.action_edit:
                editItemButton.setVisible(false);
                deleteItemButton.setVisible(true);
                saveItemButton.setVisible(true);
                setTitle(getString(R.string.editor_activity_title_edit_item));
                enableEditing();
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
                ItemEntry.COLUMN_ITEM_IMAGE,
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // If there is no data, make a prompt exit. Get out while you still can!
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int typeColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_TYPE);
            int costColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_COST);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);
            int stockColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_STOCK);
            int orderColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_ORDERQUANTITY);
            int availabilityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_AVAILABILITY);

            // Extract out the value from the cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String type = cursor.getString(typeColumnIndex);
            int cost = cursor.getInt(costColumnIndex);
            mStockQuantity = cursor.getInt(stockColumnIndex);
            mOrderQuantity = cursor.getInt(orderColumnIndex);
            int availability = cursor.getInt(availabilityColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mTypeEditText.setText(type);
            mCostEditText.setText(Integer.toString(cost));
            mStockTextView.setText(Integer.toString(mStockQuantity));
            mOrderTextView.setText(Integer.toString(mOrderQuantity));

            // Set up edit stock view now we have received our data.
            setupEditStockView();

            // The image view requires some special attention to check for null
            imageByteArray = cursor.getBlob(imageColumnIndex);

            if (imageByteArray != null && imageByteArray.length != 0) {
                // Let's recreate our image from a ByteArray using AsyncTask to prevent UI lag
                new DecodeByteArrayAsyncTask(new DecodeByteArrayAsyncTask.Listener() {
                    @Override
                    public void onImageRetrieved(Bitmap bitmap) {
                        mImagePictureView.setImageBitmap(bitmap);
                        mBackgroundImagePictureView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onImageRetrievalError() {

                    }
                }).execute(imageByteArray);
                Button myButton = this.findViewById(R.id.add_item_image);
                myButton.setText(getString(R.string.editor_button_edit_image));
            }

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
        mImagePictureView.setImageBitmap(null);
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
     * Prompt the user to get confirmation of item/product deletion.
     */
    private void showOrderConfirmationDialog() {
        // Create an AlertDialog.Builder and set message, clickListener and confirmation buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_email_order_title);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                mOrderTextView.setText(String.valueOf(pendingQuantity));
                mItemEntriesHaveChanged = true;
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_email_tryagain),
                            Toast.LENGTH_SHORT).show();
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
     * Helper method used to disable editing capabilities in the edit view.
     */
    private void disableEditing() {
        mNameEditText.setEnabled(false);
        mTypeEditText.setEnabled(false);
        mCostEditText.setEnabled(false);
        mAvailabilitySpinner.setEnabled(false);
        addImageButton.setVisibility(View.GONE);
        plusButton.setVisibility(View.GONE);
        minusButton.setVisibility(View.GONE);
    }

    /**
     * Helper method used to enable editing capabilities in the edit view.
     */
    private void enableEditing() {
        mNameEditText.setEnabled(true);
        mTypeEditText.setEnabled(true);
        mCostEditText.setEnabled(true);
        mAvailabilitySpinner.setEnabled(true);
        addImageButton.setVisibility(View.VISIBLE);
        restockButton.setVisibility(View.VISIBLE);
        plusButton.setVisibility(View.VISIBLE);
        minusButton.setVisibility(View.VISIBLE);
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
}