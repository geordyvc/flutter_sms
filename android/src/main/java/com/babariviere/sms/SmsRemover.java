package com.babariviere.sms;

import android.Manifest;
import android.content.Context;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;
import com.babariviere.sms.permisions.Permissions;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.content.ContentValues.TAG;

public class SmsRemover implements PluginRegistry.RequestPermissionsResultListener, MethodChannel.MethodCallHandler {
	private final PluginRegistry.Registrar registrar;
	private final Permissions permissions;

	private final String[] permissionsList = new String[] { Manifest.permission.READ_SMS,
			Manifest.permission.READ_PHONE_STATE };

	SmsRemover(PluginRegistry.Registrar registrar) {
		this.registrar = registrar;
		this.permissions = new Permissions(registrar.activity());
		registrar.addRequestPermissionsResultListener(this);
	}

	void handle(Permissions permissions, String fromAddress) {
		if (permissions.checkAndRequestPermission(permissionsList, Permissions.SEND_SMS_ID_REQ)) {
			deleteSms(fromAddress);
		}
	}

	private boolean deleteSms(String fromAddress) {
		Context context = registrar.context();
		boolean isDeleted = false;
		try {
			Uri uriSms = Uri.parse("content://sms/inbox");
			Cursor c = context.getContentResolver().query(uriSms,
					new String[] { "_id", "thread_id", "address", "person", "date", }, "read=0", null, null);

			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(0);
					long threadId = c.getLong(1);
					String address = c.getString(2);
					String date = c.getString(3);
					Log.d("log>>>", "0--->" + c.getString(0) + "1---->" + c.getString(1) + "2---->" + c.getString(2)
							+ "3--->" + c.getString(3) + "4----->" + c.getString(4));
					Log.d("log>>>", "date" + c.getString(0));

					ContentValues values = new ContentValues();
					values.put("read", true);
					context.getContentResolver().update(Uri.parse("content://sms/"), values, "_id=" + id, null);

					if (address.equals(fromAddress)) {
						// mLogger.logInfo("Deleting SMS with id: " + threadId);
						context.getContentResolver().delete(Uri.parse("content://sms/" + id), "date=?",
								new String[] { c.getString(4) });
						Log.d("log>>>", "Delete success.........");
					}
				} while (c.moveToNext());
			}
			isDeleted = true;
		} catch (Exception e) {
			isDeleted = false;
			Log.e("log>>>", e.toString());
		}
		return isDeleted;
	}

	@Override
	public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
		switch (methodCall.method) {
			case "removeSms":
				if (methodCall.hasArgument("fromAddress")) {
					Log.i("SMSREMOVER", "method called for removing sms: " + methodCall.argument("fromAddress"));
					deleteSms(methodCall.argument("fromAddress").toString());
				}
		}

	}

	@Override
	public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode != Permissions.READ_SMS_ID_REQ) {
			return false;
		}
		boolean isOk = true;
		for (int res : grantResults) {
			if (res != PackageManager.PERMISSION_GRANTED) {
				isOk = false;
				break;
			}
		}
		if (isOk) {
			return true;
		}
		return false;
	}
}
