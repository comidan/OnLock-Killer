package com.dev.smartlock;

import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.melnykov.fab.FloatingActionButton;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by daniele on 15/04/2015.
 */
public class SelectedApps extends Fragment
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
        new LoadSelectedApp().execute();
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
            AllApps.killList=killList;
            new LoadSelectedApp().execute();
        }
    }

    private class LoadSelectedApp extends AsyncTask<Void,Void,Void>
    {
        private ArrayList<PackageMetadata> data=new ArrayList<PackageMetadata>();

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db=database.getReadableDatabase();
            Cursor cursor=db.rawQuery("SELECT package FROM KillList",null);
            killList=new ArrayList<>();
            while(cursor.moveToNext())
                killList.add(cursor.getString(0));
            for(int i=0;i<ChooseAppActivity.apps.size();i++)
                if(killList.indexOf(ChooseAppActivity.apps.get(i).getPackageName())!=-1) {
                    PackageMetadata item = new PackageMetadata();
                    item.setName(ChooseAppActivity.apps.get(i).getName());
                    item.setPackageName(ChooseAppActivity.apps.get(i).getPackageName());
                    item.setIcon(ChooseAppActivity.apps.get(i).getIcon());
                    data.add(item);
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)    {
            loadingBar.setVisibility(View.GONE);
            ListAdapter adapter=new ListAdapter(getActivity(),R.layout.app_item,data,killList);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
            new LoadSelectedApp().execute();
    }
}
