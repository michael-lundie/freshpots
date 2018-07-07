package io.lundie.michael.freshpots;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_goto_catalogue:
                Intent catalogueIntent = new Intent(DashboardActivity.this,
                        CatalogueActivity.class);
                startActivity(catalogueIntent);
                break;
            case R.id.action_goto_editor:
                Intent editorIntent = new Intent(DashboardActivity.this,
                        EditorActivity.class);
                startActivity(editorIntent);
                break;
        } return super.onOptionsItemSelected(item);
    }
}