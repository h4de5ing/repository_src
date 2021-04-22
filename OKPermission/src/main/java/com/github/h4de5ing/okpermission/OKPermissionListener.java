package com.github.h4de5ing.okpermission;


import androidx.annotation.NonNull;

//权限申请结束的监听，用于判断权限是否申请成功或者失败
public interface OKPermissionListener {
    void onOKPermission(@NonNull String[] permissions, @NonNull int[] grantResults);
}
