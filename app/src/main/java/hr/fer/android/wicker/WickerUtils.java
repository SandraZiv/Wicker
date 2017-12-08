package hr.fer.android.wicker;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import hr.fer.android.wicker.entity.Counter;

public class WickerUtils {

    /**
     * Method to create intent for sharing counterWorking's data
     * It uses extractData() method from {@link Counter} to create string
     */
    public static void shareCounter(Context context, Counter counter) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, counter.extractData(context));
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
        } else {
            Toast.makeText(context, R.string.not_supported, Toast.LENGTH_LONG).show();
        }
    }

    public static void exportCounter(Context context, Counter counter) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(WickerConstant.ENCRYPTION_PASSWORD);

        ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), encryptor.encrypt(counter.toString()));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.export_copied, Toast.LENGTH_LONG).show();
    }

    public static Toast addToast(Toast toast, Context context, String msg, boolean isShort) {
        if (toast != null) {
            toast.cancel();
        }

        toast = Toast.makeText(context, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.show();

        return toast;
    }

    public static Toast addToast(Toast toast, Context context, int id, boolean isShort) {
        String msg = context.getString(id);

        return addToast(toast, context, msg, isShort);
    }
}
