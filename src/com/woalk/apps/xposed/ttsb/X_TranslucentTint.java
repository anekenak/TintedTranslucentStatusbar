package com.woalk.apps.xposed.ttsb;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class X_TranslucentTint implements IXposedHookZygoteInit {

	public static final StatusBarTintViewTag statusview_tag = new StatusBarTintViewTag();
	public static final NavigationBarTintViewTag navview_tag = new NavigationBarTintViewTag();

	public static final class StatusBarTintViewTag {
		public final long id;

		public StatusBarTintViewTag() {
			id = System.currentTimeMillis();
		}
	}

	public static final class NavigationBarTintViewTag {
		public final long id;

		public NavigationBarTintViewTag() {
			id = System.currentTimeMillis();
		}
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {

		final Class<?> ActivityClass = XposedHelpers.findClass(
				"android.app.Activity", null);

		findAndHookMethod(ActivityClass, "onPostCreate",
				android.os.Bundle.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						XSharedPreferences XsPref = new XSharedPreferences(
								Helpers.TTSB_PACKAGE_NAME,
								Helpers.TTSB_PREFERENCES);

						boolean log = XsPref.getBoolean(
								Helpers.TTSB_PREF_DEBUGLOG, false);

						if (log)
							de.robv.android.xposed.XposedBridge
									.log(">TTSB: Hooked in onPostCreate().");

						Object currentObj = param.thisObject;
						Activity currentActivity;
						if (currentObj instanceof Activity) {
							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: [SUCCESS] The created object is an activity. Got the instance. Proceed.");
							currentActivity = (Activity) currentObj;
						} else {
							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: [ ERROR ] The created object is not an activity. Return.");
							return;
						}

						String activityFullName = currentActivity
								.getComponentName().getClassName();
						String packageName = currentActivity.getPackageName();
						if (log)
							de.robv.android.xposed.XposedBridge
									.log(">TTSB: [ INFO: ] Activity is "
											+ activityFullName);

						if (log)
							Helpers.logContentView(currentActivity.getWindow()
									.getDecorView(), "|>-");

						if (Settings.Loader.contains(XsPref, packageName,
								activityFullName)) {
							Settings.Parser settings = Settings.Loader.load(
									(SharedPreferences) XsPref, packageName,
									activityFullName);
							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: [ INFO: ] Code is: "
												+ settings.getLine());
							setEverything(
									currentActivity,
									settings.getSetting(),
									(currentActivity.getResources()
											.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));
						} else if (Settings.Loader.containsAll(XsPref,
								packageName)) {
							Settings.Parser settings = Settings.Loader.loadAll(
									(SharedPreferences) XsPref, packageName);
							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: [ INFO: ] Code is: "
												+ settings.getLine());
							setEverything(
									currentActivity,
									settings.getSetting(),
									(currentActivity.getResources()
											.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE));
						}

						if (log)
							de.robv.android.xposed.XposedBridge
									.log(">TTSB: [SUCCESS] Set tint and translucency, everything should be working here.");
					}
				});

		findAndHookMethod(ActivityClass, "performResume", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				Object currentObj = param.thisObject;
				Activity currentActivity;
				if (currentObj instanceof Activity) {
					// de.robv.android.xposed.XposedBridge.log(">TTSB: [SUCCESS] The created object is an activity. Got the instance. Proceed.");
					currentActivity = (Activity) currentObj;
				} else {
					// de.robv.android.xposed.XposedBridge.log(">TTSB: [ ERROR ] The created object is not an activity. Return.");
					return;
				}

				XSharedPreferences XsPref = new XSharedPreferences(
						Helpers.TTSB_PACKAGE_NAME, Helpers.TTSB_PREFERENCES);

				boolean showToast = XsPref.getBoolean(
						Helpers.TTSB_PREF_SHOW_ACTIVITY_TOAST, false);
				if (showToast) {
					Toast toast = Toast.makeText(
							currentActivity.getApplicationContext(),
							"Current Activity:\n"
									+ currentActivity.getPackageName() + "."
									+ currentActivity.getLocalClassName(),
							Toast.LENGTH_SHORT);
					toast.show();
				}
			}
		});

		findAndHookMethod(ActivityClass, "onConfigurationChanged",
				Configuration.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						if (param.args.length == 0
								|| !(param.args[0] instanceof Configuration)
								|| !(((Configuration) param.args[0]).orientation == Configuration.ORIENTATION_LANDSCAPE)
								&& !(((Configuration) param.args[0]).orientation == Configuration.ORIENTATION_PORTRAIT)) {
							de.robv.android.xposed.XposedBridge
									.log("config changed");
							return;
						} else {
							XSharedPreferences XsPref = new XSharedPreferences(
									Helpers.TTSB_PACKAGE_NAME,
									Helpers.TTSB_PREFERENCES);

							boolean log = XsPref.getBoolean(
									Helpers.TTSB_PREF_DEBUGLOG, false);

							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: Hooked in onConfigurationChanged().");

							Object currentObj = param.thisObject;
							Activity currentActivity;
							if (currentObj instanceof Activity) {
								if (log)
									de.robv.android.xposed.XposedBridge
											.log(">TTSB: [SUCCESS] The changed object is an activity. Got the instance. Proceed.");
								currentActivity = (Activity) currentObj;
							} else {
								if (log)
									de.robv.android.xposed.XposedBridge
											.log(">TTSB: [ ERROR ] The changed object is not an activity. Return.");
								return;
							}

							String activityFullName = currentActivity
									.getComponentName().getClassName();
							String packageName = currentActivity
									.getPackageName();
							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: [ INFO: ] Activity is "
												+ activityFullName);

							if (log)
								Helpers.logContentView(currentActivity
										.getWindow().getDecorView(), "|>-");

							if (Settings.Loader.contains(XsPref, packageName,
									activityFullName)) {
								Settings.Parser settings = Settings.Loader
										.load((SharedPreferences) XsPref,
												packageName, activityFullName);
								if (log)
									de.robv.android.xposed.XposedBridge
											.log(">TTSB: [ INFO: ] Code is: "
													+ settings.getLine());
								setEverything(
										currentActivity,
										settings.getSetting(),
										((Configuration) param.args[0]).orientation == Configuration.ORIENTATION_LANDSCAPE);
							} else if (Settings.Loader.containsAll(XsPref,
									packageName)) {
								Settings.Parser settings = Settings.Loader
										.loadAll((SharedPreferences) XsPref,
												packageName);
								if (log)
									de.robv.android.xposed.XposedBridge
											.log(">TTSB: [ INFO: ] Code is: "
													+ settings.getLine());
								setEverything(
										currentActivity,
										settings.getSetting(),
										((Configuration) param.args[0]).orientation == Configuration.ORIENTATION_LANDSCAPE);
							}

							if (log)
								de.robv.android.xposed.XposedBridge
										.log(">TTSB: [SUCCESS] Set tint and translucency, everything should be working here.");
						}
					}
				});

		ActivityHookings ahk = new ActivityHookings();


		final Class<?> viewClass = XposedHelpers.findClass("android.view.View",
				null);
		
		findAndHookMethod(viewClass, "setBackgroundDrawable", Drawable.class,
				ahk.getViewSetBackgroundHook());

		final Class<?> actionBarClass = XposedHelpers.findClass(
				"com.android.internal.app.ActionBarImpl", null);

		findAndHookMethod(actionBarClass, "setBackgroundDrawable",
				Drawable.class, ahk.getActionBarSetBackgroundHook());

		XposedHelpers.findAndHookMethod(ActivityClass, "onCreate",
				android.os.Bundle.class, ahk.getActivityChangeHook());
		XposedHelpers.findAndHookMethod(ActivityClass, "onResume",
				ahk.getActivityChangeHook());
	}

	private class ActivityHookings {

		final List<String> actionBarClasses = new ArrayList<String>();

		public ActivityHookings() {
			actionBarClasses.add("com.android.internal.widget.ActionBarView");
			actionBarClasses
					.add("android.support.v7.internal.widget.ActionBarView");
			actionBarClasses
					.add("android.support.v7.internal.widget.ActionBarContainer");
			actionBarClasses.add("android.support.v7.widget.Toolbar");
			actionBarClasses
					.add("android.support.v7.widget.ActionBarContextView");
		}

		private Activity currentActivity;

		public Activity getCurrentActivity() {
			return currentActivity;
		}

		public void setCurrentActivity(Activity currentActivity) {
			this.currentActivity = currentActivity;
		}

		private XC_MethodHook viewSetBackgroundHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				String className = param.thisObject.getClass().getName();
				if (actionBarClasses.contains(className)) {
					XposedBridge
							.log(className + " has changed its background.");
					Drawable d = (Drawable) param.args[0];
					if (d != null)
						try {
							Activity cA = getCurrentActivity();
							if (cA != null) {
								View statusView = cA.getWindow().getDecorView()
										.findViewWithTag(statusview_tag);
								statusView.setBackground(d);
								XposedBridge
										.log("Status view color updated. (OVERRIDE)");
							} else {
								XposedBridge
										.log(">TTSB [ ERROR ] No Activity for autotint search!");
							}
						} catch (Throwable e) {
							XposedBridge.log(e);
							XposedBridge
									.log("No status view was found to auto-apply the color to.");
						}
				}
			}
		};

		public XC_MethodHook getViewSetBackgroundHook() {
			return viewSetBackgroundHook;
		}

		private XC_MethodHook ActionBarSetBackgroundHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				String className = param.thisObject.getClass().getName();
				XposedBridge.log(className + " has changed its background.");
				Drawable d = (Drawable) param.args[0];
				if (d != null)
					try {
						Activity cA = getCurrentActivity();
						if (cA != null) {
							View statusview = cA.getWindow().getDecorView()
									.findViewWithTag(statusview_tag);
							statusview.setBackground(d);
							XposedBridge
									.log("Status view color updated. (OVERRIDE)");
						} else {
							XposedBridge
									.log(">TTSB [ ERROR ] No Activity for autotint search!");
						}
					} catch (Throwable e) {
						XposedBridge.log(e);
						XposedBridge
								.log("No status view was found to auto-apply the color to.");
					}
			}
		};

		public XC_MethodHook getActionBarSetBackgroundHook() {
			return ActionBarSetBackgroundHook;
		}

		private XC_MethodHook activityChangeHook = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				setCurrentActivity((Activity) param.thisObject);
			}
		};

		public XC_MethodHook getActivityChangeHook() {
			return activityChangeHook;
		}
	}

	/**
	 * Sets all TTSB settings in the Settings Parser object to the chosen
	 * Activity.
	 * 
	 * @param currentActivity
	 *            The Activity to apply the settings to.
	 * @param settings
	 *            The settings to apply.
	 */
	public void setEverything(Activity currentActivity,
			Settings.Setting settings, boolean landscape) {
		SystemBarTintManager tintMan = null;
		if ((currentActivity.getIntent().getFlags() & Helpers.FLAG_FLOATING_WINDOW) != 0)
			return;
		if (settings.status)
			Helpers.setTranslucentStatus(currentActivity, true);
		if (settings.nav)
			Helpers.setTranslucentNavigation(currentActivity, true);
		if (settings.status || settings.nav) {
			tintMan = new SystemBarTintManager(currentActivity, statusview_tag,
					navview_tag);
		}
		if (tintMan != null) {
			tintMan.setStatusBarTintEnabled(settings.status);
			tintMan.setNavigationBarTintEnabled(settings.nav);

			if (!settings.s_autocolor)
				tintMan.setStatusBarTintColor(settings.s_color);
			else {
				final int actionBarId = currentActivity.getResources()
						.getIdentifier("action_bar", "id", "android");
				tintMan.setStatusBarTintDrawable(currentActivity.findViewById(
						actionBarId).getBackground());
			}
			tintMan.setNavigationBarTintColor(settings.n_color);

			if (settings.rules.s_plus != 0) {
				LayoutParams s_params = tintMan.mStatusBarTintView
						.getLayoutParams();
				final float scale = currentActivity.getResources()
						.getDisplayMetrics().density;
				int s_plus = (int) (settings.rules.s_plus * scale + 0.5f);
				s_params.height += s_plus;
				tintMan.mStatusBarTintView.setLayoutParams(s_params);
			}
			if (settings.rules.n_plus != 0) {
				LayoutParams n_params = tintMan.mNavBarTintView
						.getLayoutParams();
				final float scale = currentActivity.getResources()
						.getDisplayMetrics().density;
				int n_plus = (int) (settings.rules.n_plus * scale + 0.5f);
				n_params.height += n_plus;
				tintMan.mNavBarTintView.setLayoutParams(n_params);
			}
		}

		ViewGroup content = (ViewGroup) currentActivity
				.findViewById(android.R.id.content);
		ViewGroup cview = (ViewGroup) content.getChildAt(0);
		ViewGroup decview = (ViewGroup) currentActivity.getWindow()
				.getDecorView();

		if (settings.rules.cview != null) {
			setViewSettings(cview, settings.rules.cview, landscape, tintMan);
		}
		if (settings.rules.content != null) {
			setViewSettings(content, settings.rules.content, landscape, tintMan);
		}
		if (settings.rules.decview != null) {
			setViewSettings(decview, settings.rules.decview, landscape, tintMan);
		}

		if (settings.rules.view == null)
			return;
		for (int i = 0; i < settings.rules.view.size(); i++) {
			if (settings.rules.view.get(i).levels == 0)
				continue;
			setViewSettingsPack(cview, content, decview,
					settings.rules.view.get(i), landscape, tintMan);
		}
	}

	/**
	 * Sets all TTSB layout options from a ViewSettingsPack in an Activity.
	 * ViewSettingsPack contains how to find the View in the Activity, and a
	 * ViewSettings object that will be applied to this View.
	 * 
	 * @param currentActivity
	 *            The Activity containing the View.
	 * @param vsetpk
	 *            The options to apply.
	 * @param landscape
	 *            Set 'true' if screen orientation is landscape.
	 */
	public void setViewSettingsPack(ViewGroup cview, ViewGroup content,
			ViewGroup decview, Settings.Setting.ViewSettingsPack vsetpk,
			boolean landscape, SystemBarTintManager tintMan) {
		ViewGroup view;
		switch (vsetpk.from) {
		case Settings.Setting.ViewSettingsPack.FROM_DECVIEW:
			view = decview;
			break;
		case Settings.Setting.ViewSettingsPack.FROM_CVIEW:
			view = cview;
			break;
		case Settings.Setting.ViewSettingsPack.FROM_CONTENT:
			view = content;
			break;
		default:
			return;
		}

		if (vsetpk.levels > 0) {
			boolean seperatechilds = false;
			int all_index = 0;
			if (vsetpk.childindexes.length > 1)
				seperatechilds = true;
			else if (vsetpk.childindexes.length > 0)
				all_index = vsetpk.childindexes[0];
			for (int i = 1; i <= vsetpk.levels; i++) {
				int childindex = 0;
				if (!seperatechilds)
					childindex = all_index;
				else if (vsetpk.childindexes.length >= i)
					childindex = vsetpk.childindexes[i - 1];

				View v = view.getChildAt(childindex);
				if (v instanceof ViewGroup)
					view = (ViewGroup) v;
				else
					break;
			}
		} else if (vsetpk.levels < 0) {
			for (int i = 1; i <= -vsetpk.levels; i++) {
				ViewParent v = view.getParent();
				if (v != null && v instanceof ViewGroup)
					view = (ViewGroup) v;
				else
					break;
			}
		}

		setViewSettings(view, vsetpk.settings, landscape, tintMan);
	}

	/**
	 * Sets all TTSB layout options from the ViewSettings to the specified View.
	 * 
	 * @param view
	 *            The View to apply the settings to.
	 * @param vset
	 *            The options to apply.
	 * @param landscape
	 *            Set 'true' if screen orientation is landscape.
	 */
	public void setViewSettings(ViewGroup view,
			Settings.Setting.ViewSettings vset, boolean landscape,
			SystemBarTintManager tintMan) {
		if (vset.if_land && !landscape || view == null)
			return;
		if (vset.land != null && landscape) {
			setViewSettings(view, vset.land, landscape, tintMan);
			return;
		}

		if (vset.setFSW)
			view.setFitsSystemWindows(vset.setFSW_value);
		if (vset.setCTP)
			view.setClipToPadding(vset.setCTP_value);

		if (vset.padding != null) {
			int left;
			int top;
			int right;
			int bottom;

			final float scale = view.getResources().getDisplayMetrics().density;

			left = (int) (vset.padding.left * scale + 0.5f);
			top = (int) (vset.padding.top * scale + 0.5f);
			right = (int) (vset.padding.right * scale + 0.5f);
			bottom = (int) (vset.padding.bottom * scale + 0.5f);

			if (tintMan != null) {
				SystemBarTintManager.SystemBarConfig config = tintMan
						.getConfig();
				if (vset.padding.plus_status_h
						&& tintMan.isStatusBarTintEnabled())
					top += config.getStatusBarHeight();
				if (vset.padding.plus_actionbar_h
						&& tintMan.isStatusBarTintEnabled())
					top += config.getActionBarHeight();
				if (vset.padding.plus_nav_w && config.hasNavigtionBar()
						&& !config.isNavigationAtBottom()
						&& tintMan.isNavBarTintEnabled())
					right += config.getNavigationBarWidth();
				if (vset.padding.plus_nav_h && config.hasNavigtionBar()
						&& config.isNavigationAtBottom()
						&& tintMan.isNavBarTintEnabled())
					bottom += config.getNavigationBarHeight();
			}

			view.setPadding(view.getPaddingLeft() + left, view.getPaddingTop()
					+ top, view.getPaddingRight() + right,
					view.getPaddingBottom() + bottom);
		}
	}

}
