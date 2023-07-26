package com.sheoran.broadcasterreciverapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

public class MyBroadCastReceiver extends BroadcastReceiver {
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        String actionString = intent.getAction();
        Toast.makeText(context, "action "+actionString, Toast.LENGTH_LONG).show();
        if (actionString.equals("android.intent.action.AIRPLANE_MODE")){
            Toast.makeText(context, "You implemented golbal reciver", Toast.LENGTH_SHORT).show();
        }else
        {
            showwifidailog("Internet","INTERNET");
        }

    }

    private void showwifidailog(String title, String message) {
        AlertDialog.Builder alertbuilder = new AlertDialog.Builder(context);
        alertbuilder.setMessage(message);
        alertbuilder.setTitle(title);
        alertbuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertbuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertbuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertbuilder.show();
    }
}