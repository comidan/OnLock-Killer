package com.dev.smartlock;

import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.melnykov.fab.FloatingActionButton;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by daniele on 15/04/2015.
 */
public class AllApps extends Fragment
{
    private View rootView;
    public static AppDatabase database;
    public static ArrayList<String> killList;

    private ProgressBar loadingBar;
    private FloatingActionButton fab;
    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.selected_apps,container,false);
        database=new AppDatabase(getActivity().getApplicationContext());
        loadingBar=(ProgressBar)rootView.findViewById(R.id.loadingBar);
        fab=(FloatingActionButton)rootView.findViewById(R.id.fab);
        listView=(ListView)rootView.findViewById(R.id.list);
        listView.setFastScrollAlwaysVisible(true);
        fab.setShadow(true);
        fab.attachToListView(listView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FillDatabase().execute();
            }
        });
        new LoadInstalledApp().execute();
        return rootView;
    }

    private class FillDatabase extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            Set<String> s=new LinkedHashSet<String>(killList);
            killList.clear();
            killList.addAll(s);
            SQLiteDatabase db=database.getWritableDatabase();
            db.execSQL("delete from KillList");
            for(int i=0;i<killList.size();i++) {
                ContentValues contentValues=new ContentValues();
                contentValues.put("package",killList.get(i));
                db.insert("KillList",null,contentValues);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getActivity(),"Death list updated",Toast.LENGTH_SHORT).show();
        }
    }

    private class LoadInstalledApp extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor=preferences.edit();
            if(Shell.SU.available())
            {
                editor.putBoolean("ROOT",true);
                editor.apply();
            }
            else
            {
                editor.putBoolean("ROOT",false);
                editor.apply();
                publishProgress();
            }
            PackageManager appInfo=getActivity().getPackageManager();
            final PackageManager pm=getActivity().getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (int i=0;i<packages.size();i++)
            {
                ApplicationInfo process=packages.get(i);
                String name=process.packageName;
                if(!name.equals("com.dev.smartlock"))
                    try
                    {
                        PackageMetadata item=new PackageMetadata();
                        item.setName(appInfo.getApplicationLabel(appInfo.getApplicationInfo(name, PackageManager.GET_META_DATA))
                                .toString());
                        item.setPackageName(name);
                        item.setIcon(appInfo.getApplicationIcon(name));
                        ChooseAppActivity.apps.add(item);
                    }
                    catch(PackageManager.NameNotFoundException exc)
                    {

                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)    {
            loadingBar.setVisibility(View.GONE);
            SQLiteDatabase db=database.getReadableDatabase();
            Cursor cursor=db.rawQuery("SELECT package FROM KillList", null);
            killList=new ArrayList<>();
            while(cursor.moveToNext())
                killList.add(cursor.getString(0));
            ListAdapter adapter=new ListAdapter(getActivity(),R.layout.app_item,ChooseAppActivity.apps,killList);
            listView.setAdapter(adapter);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            Toast.makeText(getActivity(), "Root not detected! I'll kill anyway your selected apps but it won't have the same effects as on " +
                    "a rooted device!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser&&killList!=null&&ChooseAppActivity.apps!=null&&listView!=null)
        {
            ListAdapter adapter=new ListAdapter(getActivity(),R.layout.app_item,ChooseAppActivity.apps,killList);
            listView.setAdapter(adapter);
        }
    }
}
