package hr.fer.android.wicker.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hr.fer.android.wicker.R;
import hr.fer.android.wicker.WickerConstant;
import hr.fer.android.wicker.adapters.HomeScreenListAdapter;
import hr.fer.android.wicker.db.CounterDatabase;
import hr.fer.android.wicker.entity.Counter;

public class HomeScreenActivity extends AppCompatActivity {

    int numOfCounters;

    ListAdapter dataAdapter;
    ListView dataListView;

    Menu menu;

    boolean sync = false;

    boolean isSearch;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        FloatingActionButton btnSync = (FloatingActionButton) findViewById(R.id.btn_sync);
        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync = true;
                updateDataListView();
            }
        });

        new AsyncGetDataTask().execute("Get data");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            isSearch = true;
            query = intent.getExtras().getString(SearchManager.QUERY).toLowerCase().trim();
            menu.setGroupVisible(R.id.home_group_in_search_shown, false);
        }
        if (Intent.ACTION_DEFAULT.equals(intent.getAction())) {
            isSearch = false;
            menu.setGroupVisible(R.id.home_group_in_search_shown, true);
        }
        updateDataListView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            updateDataListView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu_no_login, menu);

        this.menu = menu;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.home_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.home_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Intent intent = new Intent(HomeScreenActivity.this, HomeScreenActivity.class);
                intent.setAction(Intent.ACTION_DEFAULT);
                intent.removeExtra(SEARCH_SERVICE);
                startActivity(intent);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_new:
                Intent intentNew = new Intent(HomeScreenActivity.this, MainActivity.class);
                startActivityForResult(intentNew, WickerConstant.REQUEST_CODE);
                break;
            case R.id.home_order:
                orderBy();
                break;
            case R.id.home_clear_all:
                clearAll();
                break;
            case R.id.home_import:
                importAddCounter();
                break;
            case R.id.home_settings:
                startActivity(new Intent(HomeScreenActivity.this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void orderBy() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.order_by));
        final SharedPreferences pref = getSharedPreferences(WickerConstant.PREFS_ORDER, MODE_PRIVATE);
        builder.setSingleChoiceItems(R.array.spinner_order, pref.getInt(WickerConstant.ORDER, 0), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(WickerConstant.ORDER, which);
                editor.apply();
                updateDataListView();
            }
        });
        builder.show();
    }

    private void clearAll() {
        if (numOfCounters == 0) {
            Toast.makeText(HomeScreenActivity.this, R.string.no_counters_to_delete, Toast.LENGTH_SHORT).show();
            return;
        }
        final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(HomeScreenActivity.this);
        deleteAlert.setTitle(getString(R.string.delete));
        deleteAlert.setMessage(numOfCounters + " "
                + (numOfCounters == 1 ? getString(R.string.deleted_counter_single) : getString(R.string.deleted_counter_multiple)));
        deleteAlert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new AsyncTask<String, Integer, Long>() {
                    @Override
                    protected Long doInBackground(String... params) {
                        CounterDatabase database = new CounterDatabase(HomeScreenActivity.this);
                        for (Counter tmp : database.getDatabaseCounterListData())
                            NotificationManagerCompat.from(HomeScreenActivity.this).cancel(tmp.getId().intValue());
                        database.deleteAllData();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Long retValue) {
                        dataListView.setAdapter(null);
                        ActionBar actionBar = getSupportActionBar();
                        numOfCounters = 0;
                        actionBar.setTitle(getString(R.string.all_data) + " (0)");
                    }
                }.execute("Delete all");
            }
        });
        deleteAlert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
                dialog.cancel();
            }
        });
        deleteAlert.show();

    }

    private void importAddCounter() {
        final AlertDialog.Builder builderImportCounter = new AlertDialog.Builder(HomeScreenActivity.this);
        builderImportCounter.setTitle(R.string.import_counter_alert);
        builderImportCounter.setMessage(R.string.exported_copy);

        final EditText inputImport = new EditText(HomeScreenActivity.this);
        inputImport.setInputType(InputType.TYPE_CLASS_TEXT);
        inputImport.setVerticalScrollBarEnabled(true);
        builderImportCounter.setView(inputImport);

        builderImportCounter.setPositiveButton(R.string.import_counter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String importText = inputImport.getText().toString();
                StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
                encryptor.setPassword(WickerConstant.ENCRYPTION_PASSWORD);
                Pattern pattern = Pattern.compile("(.+|\\p{Punct}),(\\w+\\.\\w+),(\\w+\\.\\w+),(\\w+),(\\w+),(.*)");
                try {
                    final Matcher matcher = pattern.matcher(encryptor.decrypt(importText));
                    if (matcher.find()) {
                        final Counter counter = new Counter(null,
                                matcher.group(1),
                                Double.parseDouble(matcher.group(2)),
                                Double.parseDouble(matcher.group(3)),
                                Long.parseLong(matcher.group(4)),
                                Long.parseLong(matcher.group(5)),
                                matcher.group(6));
                        new AsyncTask<String, Integer, Long>() {

                            @Override
                            protected Long doInBackground(String... params) {
                                CounterDatabase db = new CounterDatabase(HomeScreenActivity.this);
                                return db.addCounter(counter);
                            }

                            @Override
                            protected void onPostExecute(Long id) {
                                if (WickerConstant.ERROR_CODE_LONG.equals(id)) {
                                    importUpdateCounter(counter);
                                    return;
                                }
                                Toast.makeText(HomeScreenActivity.this, R.string.imported, Toast.LENGTH_SHORT).show();
                                updateDataListView();
                            }
                        }.execute();
                    } else {
                        Toast.makeText(HomeScreenActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(HomeScreenActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builderImportCounter.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderImportCounter.show();
    }

    private void importUpdateCounter(final Counter counter) {
        final AlertDialog.Builder builderImportCounter = new AlertDialog.Builder(HomeScreenActivity.this);
        builderImportCounter.setTitle(R.string.counter_exists);
        builderImportCounter.setMessage(R.string.update_existing);

        builderImportCounter.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new AsyncTask<String, Integer, Long>() {

                            @Override
                            protected Long doInBackground(String... params) {
                                CounterDatabase db = new CounterDatabase(HomeScreenActivity.this);
                                for (Counter tmp : db.getDatabaseCounterListData()) {
                                    if (tmp.getName().equals(counter.getName())) {
                                        counter.setId(tmp.getId());
                                        NotificationManagerCompat.from(HomeScreenActivity.this).cancel(tmp.getId().intValue());
                                        break;
                                    }
                                }
                                return db.updateCounter(counter);
                            }

                            @Override
                            protected void onPostExecute(Long id) {
                                if (WickerConstant.ERROR_CODE_LONG.equals(id)) {
                                    Toast.makeText(HomeScreenActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Toast.makeText(HomeScreenActivity.this, R.string.imported, Toast.LENGTH_SHORT).show();
                                updateDataListView();
                            }
                        }.execute();
                    }
                }
        );
        builderImportCounter.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderImportCounter.show();
    }


    private class AsyncGetDataTask extends AsyncTask<String, Integer, List<Counter>> {
        @Override
        protected List<Counter> doInBackground(String... params) {
            CounterDatabase database = new CounterDatabase(HomeScreenActivity.this);
            return database.getDatabaseCounterListData();
        }

        @Override
        protected void onPostExecute(final List<Counter> data) {
            List<Counter> finalData = new LinkedList<>();
            if (isSearch) {
                for (Counter counter : data)
                    if (counter.getName().toLowerCase().contains(query))
                        finalData.add(counter);
            } else {
                finalData = data;
            }

            dataAdapter = new HomeScreenListAdapter(HomeScreenActivity.this, finalData);
            dataListView = (ListView) findViewById(R.id.home_screen_list);

            TextView emptyTextView1 = (TextView) findViewById(R.id.home_screen_tw_empty);
            String[] emptyText = {getString(R.string.random_welcome_text0_a) + '\n' + getString(R.string.random_welcome_text0_b),
                    getString(R.string.random_welcome_text1),
                    getString(R.string.random_welcome_text2),
                    getString(R.string.random_welcome_text3)};
            Random rand = new Random();
            emptyTextView1.setText(emptyText[rand.nextInt(4)]);
            dataListView.setEmptyView(findViewById(R.id.home_screen_empty_view));

            dataListView.setAdapter(dataAdapter);

            numOfCounters = finalData.size();
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle(getString(R.string.all_data) + " (" + numOfCounters + ")");

            Collections.sort(finalData, new Comparator<Counter>() {
                @Override
                public int compare(Counter counter1, Counter counter2) {
                    switch (getSharedPreferences(WickerConstant.PREFS_ORDER, MODE_PRIVATE).getInt(WickerConstant.ORDER, 0)) {
                        case 0://desc
                            return -1 * counter1.getDateModified().compareTo(counter2.getDateModified());
                        case 1://desc
                            return -1 * counter1.getDateCreated().compareTo(counter2.getDateCreated());
                        case 2:
                            return counter1.getName().toLowerCase().compareTo(counter2.getName().toLowerCase());
                        case 3://desc
                            return Double.compare(counter2.getValue(), counter1.getValue());
                        default:
                            return -1 * counter1.getDateModified().compareTo(counter2.getDateModified());
                    }
                }
            });

            final List<Counter> sortedList = finalData;
            dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Counter counter = sortedList.get(position);
                    Intent intentLoad = new Intent(HomeScreenActivity.this, MainActivity.class);
                    intentLoad.putExtra(WickerConstant.COUNTER_BUNDLE_KEY, counter);
                    startActivityForResult(intentLoad, WickerConstant.REQUEST_CODE);
                }
            });

            if (sync) {
                Toast.makeText(HomeScreenActivity.this, R.string.data_synced, Toast.LENGTH_SHORT).show();
                sync = !sync;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WickerConstant.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                updateDataListView();
            }
        }
    }

    public void updateDataListView() {
        new AsyncGetDataTask().execute("update");
    }
}
