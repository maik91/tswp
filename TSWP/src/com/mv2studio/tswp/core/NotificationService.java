package com.mv2studio.tswp.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mv2studio.tswp.db.Db;
import com.mv2studio.tswp.model.TClass;

public class NotificationService extends Service {
	
	public static String BASE_TAG = "com.mv2studio.tswp.",
						 ALARM_TAG = BASE_TAG + "alarm",
						 TCLASS_KEY = BASE_TAG + "tclass";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}
	
	@Override
	public void onCreate() {
		System.out.println("STARTING SERVICE");
		setAllAlarms();
		super.onCreate();
		stopSelf();
	}
	
	private void setAllAlarms() {
		ArrayList<TClass> classes = new Db(this).getAllClasses();
		for(TClass cl: classes) {
			if(cl.isNotify()) setAlarm(cl, this);
		}
	}
	
	public static void setAlarm(TClass tClass, Context context) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(tClass.getStart());

		Calendar now = Calendar.getInstance();
		
		while(calendar.before(now)) {
			calendar.add(Calendar.DAY_OF_MONTH, 7);
		}
		Log.e("", "SETTING NOTIFICATION AT: "+calendar.getTimeInMillis());
		
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
		intent.setAction(ALARM_TAG);
		intent.putExtra(TCLASS_KEY, tClass);
		PendingIntent pi = PendingIntent.getBroadcast(context, tClass.getId(), intent, 0);
		
		am.setRepeating(AlarmManager.RTC_WAKEUP, 
				calendar.getTimeInMillis()-(Prefs.getIntValue(Prefs.TIME_TAG_INT, context)*60*1000), 
				AlarmManager.INTERVAL_DAY * 7, pi);
	}

	
	public static void cancelAlarm(TClass tClass, Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent updateServiceIntent = new Intent(context, AlarmManagerBroadcastReceiver.class);
	    PendingIntent pendingUpdateIntent = PendingIntent.getBroadcast(context, tClass.getId(), updateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    am.cancel(pendingUpdateIntent);
	}
}
