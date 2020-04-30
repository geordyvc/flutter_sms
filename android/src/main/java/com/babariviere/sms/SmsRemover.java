package com.babariviere.sms;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.babariviere.sms.permisions.Permissions;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.content.ContentValues.TAG;

public class SmsRemover implements PluginRegistry.RequestPermissionsResultListener, MethodChannel.MethodCallHandler {
	private final PluginRegistry.Registrar registrar;
	private final Permissions permissions;

	SmsRemover(PluginRegistry.Registrar registrar) {
		this.registrar = registrar;
		this.permissions = new Permissions(registrar.activity());
		registrar.addRequestPermissionsResultListener(this);
	}

	private boolean deleteSms(String fromAddress) {
		Context context = registrar.context();
		Uri uriSMS = Uri.parse("content://sms/inbox");
		try {
			Cursor cursor = context.getContentResolver().query(uriSMS, null, null, null, null);
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				int ThreadId = cursor.getInt(1);
				Log.d("Thread Id", ThreadId + " id - " + cursor.getInt(0));
				Log.d("contact number", cursor.getString(2));
				Log.d("column name", cursor.getColumnName(2));

				context.getContentResolver().delete(Uri.parse("content://sms/conversations/" + ThreadId), "address=?",
						new String[] { fromAddress });
				Log.d("Message Thread Deleted", fromAddress);
			}
			cursor.close();
			return true;

		} catch (Exception e) {
			Log.e(TAG, "deleteSms: " + fromAddress, e);
			return false;
		}
		return true;
	}

	@Override
	public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
		switch (methodCall.method) {
			case "removeSms":
				if (methodCall.hasArgument("id")) {
					Log.i("SMSREMOVER", "method called for removing sms: " + methodCall.argument("id"));
					result.success(this.deleteSms(Integer.parseInt(methodCall.argument("id").toString()),
							Integer.parseInt(methodCall.argument("thread_id").toString())));
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
