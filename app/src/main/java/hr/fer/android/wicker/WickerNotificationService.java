package hr.fer.android.wicker;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.text.DecimalFormat;

import hr.fer.android.wicker.activities.MainActivity;
import hr.fer.android.wicker.db.CounterDatabase;
import hr.fer.android.wicker.entity.Counter;


public class WickerNotificationService extends IntentService {
    protected Handler handler;
    private Toast mToast;

    public WickerNotificationService() {
        super(WickerNotificationService.class.getName());
        handler = new Handler();
    }

    private static final int NOTIFICATION_ID = 304;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, getEmptyNotification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Counter counter = (Counter) intent.getExtras().getSerializable(WickerConstant.COUNTER_BUNDLE_KEY);
        if (action == null) {
            updateNotification(counter);
        } else if (action.equals(WickerConstant.INCREASE)) {
            counter.increase();
            updateNotification(counter); //TODO overflow
        } else if (action.equals(WickerConstant.DECREASE)) {
            counter.decrease();
            updateNotification(counter);
        } else {
            CounterDatabase db = new CounterDatabase(this);
            db.updateCounter(counter); //TODO
            updateNotification(counter);
            showToast();
        }
    }

    private static final String WICKER_CHANNEL_ID = "WICKER_CHANNEL_ID";

    protected void updateNotification(Counter counter) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, WICKER_CHANNEL_ID);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    WICKER_CHANNEL_ID,
                    getString(R.string.notifications),
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(mChannel);
        }

        builder.setAutoCancel(true);
        builder.setContentTitle(getString(R.string.app_name));

        DecimalFormat formatting = new DecimalFormat(WickerConstant.DECIMAL_FORMAT);
        builder.setContentText(counter.getName() + ": " + formatting.format(counter.getValue()));

        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setWhen(0);
        builder.setPriority(Notification.PRIORITY_MAX);

        //open task from notification
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(WickerConstant.COUNTER_BUNDLE_KEY, counter);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        //adding action
        Intent increaseIntent = new Intent(this, WickerNotificationService.class).setAction(WickerConstant.INCREASE);
        increaseIntent.putExtra(WickerConstant.COUNTER_BUNDLE_KEY, counter);
        PendingIntent increasePendingIntent = PendingIntent.getService(this, 101, increaseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_add_btn, getString(R.string.increase), increasePendingIntent));

        Intent decreaseIntent = new Intent(this, WickerNotificationService.class).setAction(WickerConstant.DECREASE);
        decreaseIntent.putExtra(WickerConstant.COUNTER_BUNDLE_KEY, counter);
        PendingIntent decreasePendingIntent = PendingIntent.getService(this, 101, decreaseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_subtract_btn, getString(R.string.decrease), decreasePendingIntent));

        Intent saveIntent = new Intent(this, WickerNotificationService.class).setAction(WickerConstant.SAVE);
        saveIntent.putExtra(WickerConstant.COUNTER_BUNDLE_KEY, counter);
        PendingIntent savePendingIntent = PendingIntent.getService(this, 101, saveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(new NotificationCompat.Action(R.drawable.ic_save_btn, getString(R.string.save), savePendingIntent));

        Notification notification = builder.build();
        manager.notify(counter.getId().intValue(), notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getEmptyNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel = new NotificationChannel(
                WICKER_CHANNEL_ID,
                getString(R.string.notifications),
                NotificationManager.IMPORTANCE_NONE
        );
        manager.createNotificationChannel(mChannel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, WICKER_CHANNEL_ID);

        builder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.random_welcome_text2))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        return builder.build();
    }

    private void showToast() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mToast = WickerUtils.addToast(mToast, WickerNotificationService.this, R.string.success_saved, true);
            }
        });
    }
}
