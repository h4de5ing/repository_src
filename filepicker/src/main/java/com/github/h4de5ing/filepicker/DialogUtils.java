package com.github.h4de5ing.filepicker;

import android.content.Context;
import android.os.Environment;

import com.github.h4de5ing.filepicker.controller.DialogSelectionListener;
import com.github.h4de5ing.filepicker.model.DialogConfigs;
import com.github.h4de5ing.filepicker.model.DialogProperties;
import com.github.h4de5ing.filepicker.view.FilePickerDialog;

import java.io.File;

public class DialogUtils {
    public static void selectDir(Context context, String title, boolean needFileName, DialogSelectionListener dialogSelection) {
        String default_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.isNeedFileName = needFileName ? DialogConfigs.NEED_FILE_NAME : DialogConfigs.Not_NEED_FILE_NAME;
        properties.root = new File(default_dir);
        properties.error_dir = new File(default_dir);
        properties.offset = new File(default_dir);
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(context, properties);
        dialog.setTitle(title);
        dialog.show();
        dialog.setDialogSelectionListener(dialogSelection);
    }

    public static void selectFile(Context context, String title, final DialogSelectionListener dialogSelection) {
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
        dialog.setDialogSelectionListener(dialogSelection);
    }
}
