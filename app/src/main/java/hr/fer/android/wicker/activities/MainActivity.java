package hr.fer.android.wicker.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import hr.fer.android.wicker.R;
import hr.fer.android.wicker.WickerConstant;
import hr.fer.android.wicker.WickerNotificationService;
import hr.fer.android.wicker.WickerUtils;
import hr.fer.android.wicker.adapters.InfoListAdapter;
import hr.fer.android.wicker.adapters.SwipePageAdapter;
import hr.fer.android.wicker.db.CounterDatabase;
import hr.fer.android.wicker.entity.Counter;


public class MainActivity extends AppCompatActivity {
    CounterDatabase database;

    private boolean isFromOnBackPressed;

    Counter counterWorking;
    Counter counterOriginal;

    private TextView twName;
    private TextView twValue;
    private TextView twStep;
    private Button btnAdd;
    private Button btnSubtract;
    private Button btnReset;
    private Button btnSetValue;
    private Button btnSetStep;

    private ListView lwInfo;

    private DecimalFormat formatting = new DecimalFormat(WickerConstant.DECIMAL_FORMAT);

    public void setFragmentMainComponents(TextView name,
                                          TextView value,
                                          TextView step,
                                          Button add,
                                          Button subtract,
                                          Button reset,
                                          Button setValue,
                                          Button setStep) {
        this.twName = name;
        this.twValue = value;
        this.twStep = step;
        this.btnAdd = add;
        this.btnSubtract = subtract;
        this.btnReset = reset;
        this.btnSetValue = setValue;
        this.btnSetStep = setStep;

        createMainFunctionality();

    }

    public void setFragmentInfoComponents(ListView info) {
        this.lwInfo = info;
        updateInfo();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //in case of for example layout orientation change to preserve data
        if (savedInstanceState != null) {
            counterWorking = (Counter) savedInstanceState.getSerializable(WickerConstant.COUNTER_WORKING_STATE_KEY);
            counterOriginal = (Counter) savedInstanceState.getSerializable(WickerConstant.COUNTER_ORIGINAL_STATE_KEY);
        }
        //in case it is opened from home screen
        else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(WickerConstant.COUNTER_BUNDLE_KEY)) {
            counterWorking = (Counter) getIntent().getExtras().getSerializable(WickerConstant.COUNTER_BUNDLE_KEY);
//            counterOriginal = (Counter) getIntent().getExtras().getSerializable(WickerConstant.COUNTER_BUNDLE_KEY);
            counterOriginal = new CounterDatabase(this).getDatabaseCounterData(counterWorking.getId()); //TODO
        } else {
            counterWorking = new Counter();
            counterOriginal = new Counter();
        }

        //open db
        new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... params) {
                database = new CounterDatabase(MainActivity.this);
                return null;
            }
        }.execute("Open db");
        //clear notification
        if (counterWorking.getId() != WickerConstant.ERROR_CODE)
            NotificationManagerCompat.from(this).cancel(counterWorking.getId().intValue());

        SwipePageAdapter swipePageAdapter = new SwipePageAdapter(getSupportFragmentManager(), this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.container_fragment);
        viewPager.setAdapter(swipePageAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.setupWithViewPager(viewPager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_home);
    }

    /**
     * Method to create all the functionality including text view setup and button listeners
     */
    public void createMainFunctionality() {
        updateOnNameChanged();
        updateOnValueChanged();
        updateOnStepChanged();

        twName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogSetName(counterWorking);
            }
        });

        twValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogSetValue();
            }
        });

        twStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogSetStep();
            }
        });


        //settings for add btn
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double retValue = counterWorking.increase();
                //in case of overflow
                if (retValue == WickerConstant.ERROR_CODE) {
                    Toast.makeText(MainActivity.this, R.string.overflow, Toast.LENGTH_LONG).show();
                }
                updateOnValueChanged();
                updateInfo();
            }
        });

        //settings for subtract btn
        btnSubtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double retValue = counterWorking.decrease();
                if (retValue < 0) {
                    Toast.makeText(MainActivity.this, R.string.positive_alert, Toast.LENGTH_SHORT).show();
                }
                updateOnValueChanged();
                updateInfo();
            }
        });

        //settings for btnReset btn
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counterWorking.setValue(WickerConstant.DEFAULT_VALUE);
                counterWorking.setStep(WickerConstant.DEFAULT_STEP);
                updateOnValueChanged();
                updateOnStepChanged();
                updateInfo();
                Toast.makeText(MainActivity.this, R.string.reset, Toast.LENGTH_SHORT).show();
            }
        });

        //setting for set number btn
        btnSetValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogSetValue();
            }
        });

        //settings for twStep btn
        btnSetStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogSetStep();
            }
        });
    }

    /**
     * Method to update twName
     */
    private void updateOnNameChanged() {
        twName.setText(counterWorking.getName());
    }

    /**
     * Method to update twStep
     */
    private void updateOnStepChanged() {
        twStep.setText(getString(R.string.step) + ": " + formatting.format(counterWorking.getStep()));
    }

    /**
     * Method to update twValue
     */
    private void updateOnValueChanged() {
        twValue.setText(formatting.format(counterWorking.getValue()));
    }

    /**
     * Method to update twInfo
     */
    public void updateInfo() {
        InfoListAdapter adapter = new InfoListAdapter(this, counterWorking.getCounterDataList());
        lwInfo.setAdapter(adapter);

        lwInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case WickerConstant.INFO_NAME:
                        openDialogSetName(counterWorking);
                        break;
                    case WickerConstant.INFO_VALUE:
                        openDialogSetValue();
                        break;
                    case WickerConstant.INFO_STEP:
                        openDialogSetStep();
                        break;
                    case WickerConstant.INFO_NOTE:
                        openDialogSetNote();
                        break;
                    default:
                        break;
                }
            }
        });

        lwInfo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText(getString(R.string.app_name), counterWorking.getCounterDataList().get(position));
                clipboardManager.setPrimaryClip(data);
                Toast.makeText(MainActivity.this, R.string.data_copied, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    /**
     * Method to handle back press so it can send RESULT_OK
     */
    @Override
    public void onBackPressed() {
        //if counterWorking has changed since last save or create
        if ((counterWorking.getId() != WickerConstant.ERROR_CODE && !counterWorking.equals(counterOriginal))
                || (counterWorking.getId() == WickerConstant.ERROR_CODE)) {
            final AlertDialog.Builder builderBackPressed = new AlertDialog.Builder(MainActivity.this);
            builderBackPressed.setTitle(R.string.save_changes_alert);

            builderBackPressed.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isFromOnBackPressed = true;
                    saveAs();
                }
            });
            builderBackPressed.setNegativeButton(R.string.dont_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //don't create notification for newly created
                    if (counterWorking.getId() != WickerConstant.ERROR_CODE)
                        createNotification();
                    setupForOnBackPressed();
                }
            });
            builderBackPressed.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builderBackPressed.show();
        }
        //if counterWorking hasn't been changed since last save
        else {
            if (counterWorking.getId() != WickerConstant.ERROR_CODE)
                createNotification();
            setupForOnBackPressed();
        }
    }

    /**
     * Method creates intent with result for HomeScreenActivity
     */
    private void setupForOnBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        MainActivity.super.onBackPressed();
    }

    /**
     * Method to save counterWorking data when for example changing orientation
     *
     * @param outState bundle outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(WickerConstant.COUNTER_WORKING_STATE_KEY, counterWorking);
        outState.putSerializable(WickerConstant.COUNTER_ORIGINAL_STATE_KEY, counterOriginal);
    }

    /**
     * Method to create option menu when activity is started
     *
     * @param menu Menu menu to inflate
     * @return returns true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Method to process chosen option in menu
     *
     * @param item item from menu
     * @return result from super class method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.note:
                openDialogSetNote();
                break;
            case R.id.export:
                WickerUtils.exportCounter(this, counterWorking);
                break;
            case R.id.share:
                WickerUtils.shareCounter(this, counterWorking);
                break;
            case R.id.delete:
                delete();
                break;
            case R.id.save_as:
                saveAs();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to save or update counterWorking data in database
     */
    private void saveAs() {
        //if there already exists counterWorking in table just update that same counterWorking
        if (counterWorking.getId() != WickerConstant.ERROR_CODE) {
            updateCounterGeneral(counterWorking);
        }
        //new counterWorking has been created and needs to be saved
        else {
            openDialogSetName(counterWorking);
        }
    }


    /**
     * Method to delete specific counterWorking
     */
    private void delete() {
        AlertDialog.Builder deleteAlert = new AlertDialog.Builder(MainActivity.this);
        deleteAlert.setTitle(getString(R.string.delete));
        deleteAlert.setMessage(getString(R.string.counter) + " " + counterWorking.getName() + " " + getString(R.string.will_be_deleted));
        deleteAlert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //check if there is data in database
                if (counterWorking.getId() != WickerConstant.ERROR_CODE) {
                    deleteCounterGeneral(counterWorking);
                } else {
                    finish();
                }
            }
        });
        deleteAlert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteAlert.show();
    }

    public void saveCounterGeneral(final Counter counter) {
        new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... params) {
                return database.addCounter(counter);
            }

            @Override
            protected void onPostExecute(Long counterId) {
                //handle unique saving
                if (WickerConstant.ERROR_CODE_LONG.equals(counterId)) {
                    Toast.makeText(MainActivity.this, R.string.counter_exists, Toast.LENGTH_SHORT).show();
                    return;
                }
                counter.setId(counterId);
                Toast.makeText(MainActivity.this, R.string.success_saved, Toast.LENGTH_SHORT).show();
                updateOnNameChanged();
                //update original
                updateOriginal(counter);
                //update info to update name and info section
                updateInfo();

                //if it has been called on onBackPressed() method close activity
                if (isFromOnBackPressed) {
                    createNotification();
                    setupForOnBackPressed();
                }
            }
        }.execute("Save");
    }

    public void updateCounterGeneral(final Counter counter) {
        new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... params) {
                return database.updateCounter(counter);
            }

            @Override
            protected void onPostExecute(Long counterId) {
                if (WickerConstant.ERROR_CODE_LONG.equals(counterId)) {
                    Toast.makeText(MainActivity.this, R.string.counter_exists, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(MainActivity.this, R.string.success_saved, Toast.LENGTH_SHORT).show();
                updateInfo();
                //update original
                updateOriginal(counter);

                if (isFromOnBackPressed) {
                    createNotification();
                    setupForOnBackPressed();
                }
            }
        }.execute("Update counterWorking");
    }

    public void deleteCounterGeneral(final Counter counter) {
        new AsyncTask<String, Integer, Long>() {
            @Override
            protected Long doInBackground(String... params) {
                database.deleteCounter(counter);
                return null;
            }

            @Override
            protected void onPostExecute(Long retValue) {
                Toast.makeText(MainActivity.this, R.string.success_delete, Toast.LENGTH_SHORT).show();
                NotificationManagerCompat.from(MainActivity.this).cancel(counter.getId().intValue());
                //generate result ok since back pressed wont be called
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }.execute("Delete");
    }

    private void updateOriginal(Counter counter) {
        counterOriginal.setId(counter.getId());
        counterOriginal.setName(counter.getName());
        counterOriginal.setValue(counter.getValue());
        counterOriginal.setStep(counter.getStep());
        counterOriginal.setNote(counter.getNote());
    }

    public void createNotification() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean(WickerConstant.PREF_NOTIFICATION, true)) return;

        //cancel previous notification
        for (Counter tmp : database.getDatabaseCounterListData())
            if (!tmp.getId().equals(counterOriginal.getId()))
                NotificationManagerCompat.from(this).cancel(tmp.getId().intValue());

        Intent intent = new Intent(this, WickerNotificationService.class);
        intent.putExtra(WickerConstant.COUNTER_BUNDLE_KEY, counterOriginal);
        startService(intent);
    }

    /**
     * Method is called when brand new counterWorking is created and needs to be saved
     * or previously saved counter will have its name changed.
     *
     * @param counter to be saved in database
     */
    private void openDialogSetName(final Counter counter) {
        final AlertDialog.Builder builderSaveAs = new AlertDialog.Builder(this);
        builderSaveAs.setTitle(R.string.enter_name);

        final EditText etName = new EditText(this);
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        etName.setText(counter.getName());
        etName.setSelection(counter.getName().length());
        builderSaveAs.setView(etName);

        builderSaveAs.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String inputName = etName.getText().toString().trim();
                if (inputName.isEmpty()) {
                    Toast.makeText(builderSaveAs.getContext(), R.string.please_enter_name, Toast.LENGTH_SHORT).show();
                } else {
                    counter.setName(inputName);
                    updateOnNameChanged();
                    updateInfo();
                    //if counter is newly created
                    if (WickerConstant.ERROR_CODE_LONG.equals(counter.getId())) {
                        saveCounterGeneral(counter);
                    }
                }
            }
        });
        builderSaveAs.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builderSaveAs.show();
    }

    private void openDialogSetValue() {
        AlertDialog.Builder builderSetValue = new AlertDialog.Builder(this);
        builderSetValue.setTitle(R.string.enter_num);

        final EditText etValue = new EditText(MainActivity.this);
        etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etValue.setText(counterWorking.getValue() == WickerConstant.DEFAULT_VALUE ? "" : formatting.format(counterWorking.getValue()));
        etValue.setSelection(etValue.getText().toString().length());
        builderSetValue.setView(etValue);

        builderSetValue.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputValue = etValue.getText().toString();
                if (inputValue.isEmpty()) {
                    dialog.cancel();
                } else {
                    double newValue;
                    try {
                        newValue = Double.parseDouble(inputValue);
                        if (newValue < 0) {
                            Toast.makeText(MainActivity.this, R.string.positive_alert, Toast.LENGTH_SHORT).show();
                        } else {
                            if (newValue != counterWorking.getValue()) {
                                counterWorking.setValue(newValue);
                                updateOnValueChanged();
                                updateInfo();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, R.string.overflow, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builderSetValue.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderSetValue.show();
    }

    private void openDialogSetStep() {
        AlertDialog.Builder builderSetStep = new AlertDialog.Builder(this);
        builderSetStep.setTitle(R.string.enter_step);

        final EditText etStep = new EditText(MainActivity.this);
        etStep.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etStep.setText(formatting.format(counterWorking.getStep()));
        etStep.setSelection(etStep.getText().toString().length());
        builderSetStep.setView(etStep);

        builderSetStep.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String inputStep = etStep.getText().toString();
                if (inputStep.isEmpty()) {
                    dialog.cancel();
                } else {
                    double newStep;
                    try {
                        newStep = Double.parseDouble(inputStep);
                        if (newStep < 0) {
                            Toast.makeText(MainActivity.this, R.string.positive_alert, Toast.LENGTH_SHORT).show();
                        } else if (newStep == 0) {
                            Toast.makeText(MainActivity.this, R.string.not_zero_alert, Toast.LENGTH_SHORT).show();
                        } else {
                            if (newStep != counterWorking.getStep()) {
                                counterWorking.setStep(newStep);
                                updateOnStepChanged();
                                updateInfo();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, R.string.overflow, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builderSetStep.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderSetStep.show();
    }

    private void openDialogSetNote() {
        AlertDialog.Builder builderSetNote = new AlertDialog.Builder(this);
        builderSetNote.setTitle(R.string.enter_note);

        final EditText noteText = new EditText(this);
        noteText.setText(counterWorking.getNote());
        noteText.setSelection(counterWorking.getNote().length());
        builderSetNote.setView(noteText);

        builderSetNote.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String note = noteText.getText().toString().trim();
                if (!note.equals(counterWorking.getNote())) {
                    counterWorking.setNote(note);
                    updateInfo();
                }
            }
        });

        builderSetNote.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builderSetNote.show();
    }
}
