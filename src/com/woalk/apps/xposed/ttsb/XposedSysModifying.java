package com.woalk.apps.xposed.ttsb;

import com.woalk.apps.xposed.ttsb.Helpers;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.readystatesoftware.systembartint.SystemBarTintManager.SystemBarConfig;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


public class XposedSysModifying implements IXposedHookZygoteInit {

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		
		final Class<?> ActivityClass = XposedHelpers.findClass("android.app.Activity", null);
		
		findAndHookMethod(ActivityClass, "onCreate", android.os.Bundle.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				//de.robv.android.xposed.XposedBridge.log(">TTSB: Hooked in onCreate().");
				
				Object currentObj = param.thisObject;
				Activity currentActivity;
				if (currentObj instanceof Activity) {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] The created object is an activity. Got the instance. Proceed.");
					currentActivity = (Activity) currentObj;
				}
				else {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [ ERROR ] The created object is not an activity. Return.");
					return;
				}
				
				//de.robv.android.xposed.XposedBridge.log(">TTSB: [ INFO: ] Package is " + currentActivity.getPackageName());
				
				/*if (currentActivity.getPackageName().equals("de.robv.android.xposed.installer")) {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [ ERROR ] Package is Xposed Installer, which does not work fine with the mod. Return.");
					return;
				}*/
				
				// read dynamic settings
				String activityFullName = currentActivity.getPackageName() + "." + currentActivity.getLocalClassName();
				XSharedPreferences XsPref = new XSharedPreferences(Helpers.TTSB_PACKAGE_NAME, Helpers.TTSB_PREFERENCES);

				// allow to overwrite settings or deny it
				if (!XsPref.getBoolean(Helpers.TTSB_OVERWRITE_EXISTING, false) && !Helpers.isTranslucencyAllowed(currentActivity)) {
					return;
				}
				
				if (!XsPref.contains(activityFullName + "+n") && !XsPref.contains(currentActivity.getPackageName() + ".[ALL]+n") && !XsPref.contains(activityFullName) && !XsPref.contains(currentActivity.getPackageName() + ".[ALL]")) return;
				
				// statusbar
				if (XsPref.contains(activityFullName) || XsPref.contains(currentActivity.getPackageName() + ".[ALL]")) {
					String activityParsedName = activityFullName;
					if (!XsPref.contains(activityFullName)) activityParsedName = currentActivity.getPackageName() + ".[ALL]";
					
					String colorHex = XsPref.getString(activityParsedName, "FF000000");
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Retrieved color " + colorHex + ".");
					int colorA = Integer.valueOf(colorHex.substring(0, 2), 16);
					int colorR = Integer.valueOf(colorHex.substring(2, 4), 16);
					int colorG = Integer.valueOf(colorHex.substring(4, 6), 16);
					int colorB = Integer.valueOf(colorHex.substring(6, 8), 16);
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Converted color to " + "A:" + String.valueOf(colorA) + " R:" + String.valueOf(colorR) + " G:" + String.valueOf(colorG) + " B:" + String.valueOf(colorB) + ".");
					int sPrior = XsPref.getInt(activityParsedName + "+s", 2);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						Helpers.setTranslucentStatus(currentActivity, true);
					}

					SystemBarTintManager tintManager = new SystemBarTintManager(currentActivity);
					SystemBarConfig sysBarConf = tintManager.getConfig();

					tintManager.setStatusBarTintEnabled(true);

					tintManager.setStatusBarTintColor(Color.argb(colorA, colorR, colorG, colorB));

					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Set tint and translucency, everything should be working here.");

					// Helpers.logContentView(currentActivity.getWindow().getDecorView(), "");

					//de.robv.android.xposed.XposedBridge.log(">TTSB: [ INFO: ] Activity is " + activityFullName);

					ViewGroup decV = (ViewGroup) currentActivity.getWindow().getDecorView();
					if (sPrior == 1) {
						View mainV = Helpers.getContentView(decV);
						mainV.setPadding(mainV.getPaddingLeft(), mainV.getPaddingTop() + sysBarConf.getStatusBarHeight(),
								mainV.getPaddingRight(), mainV.getPaddingBottom());
					}
					else if (sPrior == 2) {
						View mainV = Helpers.getContentView(decV);
						mainV.setPadding(mainV.getPaddingLeft(), mainV.getPaddingTop() + sysBarConf.getActionBarHeight() + sysBarConf.getStatusBarHeight(),
								mainV.getPaddingRight(), mainV.getPaddingBottom());
					}


					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Layout should now be adjusted.");
				}
				
				// navbar
				if (XsPref.contains(activityFullName + "+n") || XsPref.contains(currentActivity.getPackageName() + ".[ALL]+n")) {
					String activityParsedName = activityFullName;
					if (!XsPref.contains(activityFullName)) activityParsedName = currentActivity.getPackageName() + ".[ALL]";
					
					String colorHex = XsPref.getString(activityParsedName + "+n", "FF000000");
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Retrieved color " + colorHex + ".");
					int colorA = Integer.valueOf(colorHex.substring(0, 2), 16);
					int colorR = Integer.valueOf(colorHex.substring(2, 4), 16);
					int colorG = Integer.valueOf(colorHex.substring(4, 6), 16);
					int colorB = Integer.valueOf(colorHex.substring(6, 8), 16);
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Converted color to " + "A:" + String.valueOf(colorA) + " R:" + String.valueOf(colorR) + " G:" + String.valueOf(colorG) + " B:" + String.valueOf(colorB) + ".");
					int sPrior = XsPref.getInt(activityParsedName + "+sn", 2);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						Helpers.setTranslucentNavigation(currentActivity, true);
					}

					SystemBarTintManager tintManager = new SystemBarTintManager(currentActivity);
					SystemBarConfig sysBarConf = tintManager.getConfig();

					tintManager.setNavigationBarTintEnabled(true);

					tintManager.setNavigationBarTintColor(Color.argb(colorA, colorR, colorG, colorB));

					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Set tint and translucency, everything should be working here.");

					// Helpers.logContentView(currentActivity.getWindow().getDecorView(), "");

					//de.robv.android.xposed.XposedBridge.log(">TTSB: [ INFO: ] Activity is " + activityFullName);

					ViewGroup decV = (ViewGroup) currentActivity.getWindow().getDecorView();
					if (sPrior == 1 && sysBarConf.hasNavigtionBar()) {
						if (sysBarConf.isNavigationAtBottom()) {
					 		View mainV = Helpers.getContentView(decV);
					 		mainV.setPadding(mainV.getPaddingLeft(), mainV.getPaddingTop(),
								mainV.getPaddingRight(), mainV.getPaddingBottom() + sysBarConf.getNavigationBarHeight());
					 	}
					 	else {
					 		View mainV = Helpers.getContentView(decV);
					 		mainV.setPadding(mainV.getPaddingLeft(), mainV.getPaddingTop(),
								mainV.getPaddingRight() + sysBarConf.getNavigationBarWidth(), mainV.getPaddingBottom());
					 	}
					}

					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] Layout should now be adjusted.");
				}
			}
		});
		
		findAndHookMethod(ActivityClass, "performResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Object currentObj = param.thisObject;
				Activity currentActivity;
				if (currentObj instanceof Activity) {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] The created object is an activity. Got the instance. Proceed.");
					currentActivity = (Activity) currentObj;
				}
				else {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [ ERROR ] The created object is not an activity. Return.");
					return;
				}
				
				XSharedPreferences XsPref = new XSharedPreferences(Helpers.TTSB_PACKAGE_NAME, Helpers.TTSB_PREFERENCES);
				
				boolean showToast = XsPref.getBoolean(Helpers.TTSB_SHOW_ACTIVITY_TOAST, false);
				if (showToast) {
					Toast toast = Toast.makeText(currentActivity.getApplicationContext(), "Current Activity:\n" + currentActivity.getPackageName() + "." + currentActivity.getLocalClassName(), Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});
		
		findAndHookMethod(ActivityClass, "onConfigurationChanged", Configuration.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if (param.args.length == 0 ||
						!(param.args[0] instanceof Configuration) ||
						!(((Configuration) param.args[0]).orientation == Configuration.ORIENTATION_LANDSCAPE) ||
						!(((Configuration) param.args[0]).orientation == Configuration.ORIENTATION_PORTRAIT)) {
					return;
				}
				
				Object currentObj = param.thisObject;
				Activity currentActivity;
				if (currentObj instanceof Activity) {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] The created object is an activity. Got the instance. Proceed.");
					currentActivity = (Activity) currentObj;
				}
				else {
					//de.robv.android.xposed.XposedBridge.log(">TTSB: [ ERROR ] The created object is not an activity. Return.");
					return;
				}
				
				// read dynamic settings
				String activityFullName = currentActivity.getPackageName() + "." + currentActivity.getLocalClassName();
				XSharedPreferences XsPref = new XSharedPreferences(Helpers.TTSB_PACKAGE_NAME, Helpers.TTSB_PREFERENCES);

				if (XsPref.contains(activityFullName + "+n") || XsPref.contains(currentActivity.getPackageName() + ".[ALL]+n")) {
					String activityParsedName = activityFullName;
					if (!XsPref.contains(activityFullName)) activityParsedName = currentActivity.getPackageName() + ".[ALL]";
					
					SystemBarTintManager tintManager = new SystemBarTintManager(currentActivity);
					SystemBarConfig sysBarConf = tintManager.getConfig();
					
					int sPrior = XsPref.getInt(activityParsedName + "+sn", 2);
					
					ViewGroup decV = (ViewGroup) currentActivity.getWindow().getDecorView();
					if (sPrior == 1 && sysBarConf.hasNavigtionBar()) {
					 	if (sysBarConf.isNavigationAtBottom()) {
					 		View mainV = Helpers.getContentView(decV);
					 		mainV.setPadding(mainV.getPaddingLeft(), mainV.getPaddingTop(),
								mainV.getPaddingRight() - sysBarConf.getNavigationBarWidth(), mainV.getPaddingBottom() + sysBarConf.getNavigationBarHeight());
					 	}
					 	else {
					 		View mainV = Helpers.getContentView(decV);
					 		mainV.setPadding(mainV.getPaddingLeft(), mainV.getPaddingTop(),
								mainV.getPaddingRight() + sysBarConf.getNavigationBarWidth(), mainV.getPaddingBottom() - sysBarConf.getNavigationBarHeight());
					 	}
					}
				}
			}
		});
	}

}
