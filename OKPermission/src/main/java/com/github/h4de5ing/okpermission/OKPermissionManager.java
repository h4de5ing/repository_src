package com.github.h4de5ing.okpermission;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

import static com.github.h4de5ing.okpermission.OKPermissionActivity.INTENT_KEY_DIALOG_ITEMS;
import static com.github.h4de5ing.okpermission.OKPermissionActivity.INTENT_KEY_DIALOG_MSG;
import static com.github.h4de5ing.okpermission.OKPermissionActivity.INTENT_KEY_DIALOG_TITLE;
import static com.github.h4de5ing.okpermission.OKPermissionActivity.INTENT_KEY_MULTIPLE_PERMISSIONS;
import static com.github.h4de5ing.okpermission.OKPermissionActivity.INTENT_KEY_SHOW_DIALOG;

//OKPermission的管理类，用于申请权限、配置各种参数
public class OKPermissionManager {

    private Bundle mBundle = new Bundle();
    private OKPermissionListener mOKPermissionListener;
    private OKPermissionFinishListener mOKPermissionFinishListener;
    private OKPermissionKeyBackListener mKeyBackListener;

    private OKPermissionManager(Builder builder) {
        mBundle.putStringArray(INTENT_KEY_MULTIPLE_PERMISSIONS, builder.mPermissions);

        mBundle.putBoolean(INTENT_KEY_SHOW_DIALOG, builder.mShowDialog);

        mBundle.putString(INTENT_KEY_DIALOG_TITLE, builder.mDialogTitle);
        mBundle.putString(INTENT_KEY_DIALOG_MSG, builder.mDialogMsg);
        mBundle.putSerializable(INTENT_KEY_DIALOG_ITEMS, builder.mDialogItems);

        mOKPermissionListener = builder.mOKPermissionListener;
        mOKPermissionFinishListener = builder.mOKPermissionFinishListener;
        mKeyBackListener = builder.mKeyBackListener;
    }

    /**
     * 应用权限
     *
     * @param context
     */
    public void applyPermission(Context context) {
        OKPermissionActivity.setOKPermissionListener(mOKPermissionListener);
        OKPermissionActivity.setOKPermissionFinishListener(mOKPermissionFinishListener);
        OKPermissionActivity.setKeyBackListener(mKeyBackListener);
        Intent intent = new Intent(context, OKPermissionActivity.class);
        intent.putExtras(mBundle);
        context.startActivity(intent);
    }

    public static class Builder {

        private String[] mPermissions;
        private String mDialogTitle;
        private String mDialogMsg;
        private ArrayList<PermissionItem> mDialogItems;
        private boolean mShowDialog = false;
        private OKPermissionListener mOKPermissionListener;
        private OKPermissionFinishListener mOKPermissionFinishListener;
        private OKPermissionKeyBackListener mKeyBackListener;

        public Builder(PermissionItem[] permissions) {
            mPermissions = new String[permissions.length];
            mDialogItems = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                mPermissions[i] = permissions[i].permission;
                mDialogItems.add(permissions[i]);
            }
        }

        public Builder setShowDialog(boolean showDialog) {
            mShowDialog = showDialog;
            return this;
        }

        public Builder setDialogTitle(String dialogTitle) {
            mDialogTitle = dialogTitle;
            return this;
        }

        public Builder setDialogMsg(String dialogMsg) {
            mDialogMsg = dialogMsg;
            return this;
        }

        public Builder setOKPermissionListener(OKPermissionListener permissionListener) {
            mOKPermissionListener = permissionListener;
            return this;
        }

        public Builder setOKPermissionFinishListener(OKPermissionFinishListener okPermissionFinishListener) {
            mOKPermissionFinishListener = okPermissionFinishListener;
            return this;
        }

        public Builder setKeyBackListener(OKPermissionKeyBackListener keyBackListener) {
            mKeyBackListener = keyBackListener;
            return this;
        }

        public OKPermissionManager builder() {
            return new OKPermissionManager(this);
        }
    }

    /**
     * 申请权限快捷方法，申请权限不弹出对话框
     *
     * @param context
     * @param permissions
     * @param permissionListener
     */
    public static void applyPermissionNoDialog(Context context, String[] permissions, OKPermissionListener permissionListener) {
        PermissionItem[] permissionItems = new PermissionItem[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            permissionItems[i] = new PermissionItem(permissions[i], "", 0);
        }
        OKPermissionManager okPermissionManager = new Builder(permissionItems)
                .setOKPermissionListener(permissionListener)
                .setShowDialog(false)
                .builder();
        okPermissionManager.applyPermission(context);
    }


}
