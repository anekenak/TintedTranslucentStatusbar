package com.woalk.apps.xposed.ttsb;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.woalk.apps.xposed.ttsb.Settings.Parser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SyncActivity extends Activity {
	protected AppSyncListAdapter lA;
	protected TreeMap<String, String> database;
	protected Activity context = this;
	protected ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sync);
		
		lA = new AppSyncListAdapter(context, new ArrayList<ApplicationInfo>(), new ArrayList<Boolean>(), new ArrayList<Boolean>());
		lv = (ListView) findViewById(R.id.listView1);
		lv.setAdapter(lA);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox chkSync = (CheckBox) parent.findViewById(R.id.checkSync);
				chkSync.setChecked(!chkSync.isChecked());
			}
		});
		findViewById(R.id.button_addsync).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				saveSelected();
				context.finish();
			}
		});
		
		getSyncables();
	}
	
	protected void getSyncables() {
		new readDatabaseTask().execute("http://ext.woalk.de/****DATABASE-URL-REMOVED****");
	}
	
	private class readDatabaseTask extends AsyncTask<String, Integer, AppSyncListAdapter> {
		private AlertDialog progress;
		
		protected void onPreExecute() {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(R.string.loadingsync_msg);
			builder.setView(new ProgressBar(context));
			progress = builder.create();
			progress.show();
		}

		@SuppressLint("WorldReadableFiles")
		@SuppressWarnings("deprecation")
		protected AppSyncListAdapter doInBackground(String... params) {
			AppSyncListAdapter lA1 = new AppSyncListAdapter(context, new ArrayList<ApplicationInfo>(), null, null);
			PackageManager pkgMan = context.getPackageManager();
			List<PackageInfo> pkgs = pkgMan.getInstalledPackages(0);
			List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>();
			List<Boolean> is_set = new ArrayList<Boolean>();
			List<Boolean> checked = new ArrayList<Boolean>();
			for (int i = 0; i < pkgs.size(); i++) {
				apps.add(pkgs.get(i).applicationInfo);
			}
			Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pkgMan));
			SharedPreferences sPref = context.getSharedPreferences(Helpers.TTSB_PREFERENCES, Context.MODE_WORLD_READABLE);
			TreeMap<String, ?> tree_sPref = new TreeMap<String, Object>(sPref.getAll());
			
			database = new TreeMap<String, String>();
			InputStream is;
			String result = "";
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(params[0]);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				} 
				is.close();
				result = sb.toString();
				JSONArray jArray = new JSONArray(result);
				for (int i = 0; i < jArray.length(); i++){
					JSONObject json_data = jArray.getJSONObject(i);
					database.put(json_data.getString("package") + "/" + json_data.getString("activity"), json_data.getString("setting"));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			for (int i = 0; i < apps.size(); i++) {
				if (!Settings.Loader.containsPackage(database, apps.get(i).packageName)) {
					apps.remove(i);
					i--;
					continue;
				}
				boolean is_current_set = Settings.Loader.containsPackage(tree_sPref, apps.get(i).packageName);
				is_set.add(is_current_set);
				checked.add(!is_current_set);
			}
			lA1.apps = apps;
			lA1.is_set = is_set;
			lA1.checked = checked;
			return lA1;
		}
		
		protected void onPostExecute(AppSyncListAdapter result) {
			lA.apps.clear();
			lA.is_set.clear();
			lA.checked.clear();
			if (result != null) {
				lA.apps.addAll(result.apps);
				lA.is_set.addAll(result.is_set);
				lA.checked.addAll(result.checked);
			}
			lA.notifyDataSetChanged();
			progress.dismiss();
		}
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	protected void saveSelected() {
		SharedPreferences sPref = context.getSharedPreferences(Helpers.TTSB_PREFERENCES, Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor edit = sPref.edit();
		DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
		String formattedDate = f.format(new Date());
		edit.putString(Helpers.TTSB_PREF_LASTUPDATE, formattedDate);
		edit.apply();
		List<String> packageNames = new ArrayList<String>();
		for (int i = 0; i < lA.apps.size(); i++) {
			if (lA.checked.get(i)) packageNames.add(lA.apps.get(i).packageName);
		}
		if (packageNames.size() == 0) return;
		Settings.Saver.deleteEverythingFromPackages(sPref, packageNames);
		for (int i = 0; i < lA.apps.size(); i++) {
			if (lA.checked.get(i)) {
				SortedMap<String, String> appdb = database.subMap(lA.apps.get(i).packageName, lA.apps.get(i).packageName + Character.MAX_VALUE);
				for (Entry<String, String> entry : appdb.entrySet()) {
					Settings.Parser parser = new Settings.Parser(entry.getValue());
					parser.parseToSettings();
					Settings.Saver.save(sPref, entry.getKey(), parser);
				}
			}
		}
		Toast.makeText(this, R.string.settings_synced_success, Toast.LENGTH_SHORT).show();
	}
}
