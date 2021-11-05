package com.github.h4de5ing.filepicker;

import android.content.Context;
import android.os.Environment;

import com.github.h4de5ing.filepicker.model.DialogConfigs;
import com.github.h4de5ing.filepicker.model.DialogProperties;
import com.github.h4de5ing.filepicker.view.FilePickerDialog;

import java.io.File;

public class DialogUtils {
    public interface DialogSelection {
        void onSelectedFilePaths(String[] files);
    }

    public static void selectFile(Context context, String title, final DialogSelection dialogSelection) {
        String default_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(default_dir);
        properties.error_dir = new File(default_dir);
        properties.offset = new File(default_dir);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(context, properties);
        dialog.setTitle(title);
        dialog.show();
        dialog.setDialogSelectionListener(files -> dialogSelection.onSelectedFilePaths(files));
    }
}
