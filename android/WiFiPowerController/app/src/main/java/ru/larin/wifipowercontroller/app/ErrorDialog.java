package ru.larin.wifipowercontroller.app;

import android.app.AlertDialog;
import android.content.Context;

public class ErrorDialog {
    public static void showError(Context context, String title, Exception ex) {
        AlertDialog aDialog = new AlertDialog.Builder(context).setMessage(ex.getMessage()).setTitle(title).create();
        aDialog.show();
    }}
