package se.smartgallery.app;

import android.content.Context;
import android.content.DialogInterface;

@SuppressWarnings("deprecation")
public class ProgressDialog extends android.app.ProgressDialog {
    private volatile boolean cancelled = false;

    public ProgressDialog(Context context) {
        super(context);
        super.setOnCancelListener(dialog -> cancelled = true);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        super.setOnCancelListener(dialog -> {
            cancelled = true;
            if (listener != null) listener.onCancel(dialog);
        });
    }
}
