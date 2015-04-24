package com.dev.smartlock;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import eu.chainfire.libsuperuser.Shell;
import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;

public class ChooseAppActivity extends ActionBarActivity implements MaterialTabListener {

    public static ArrayList<String> killList;
    public static ArrayList<PackageMetadata> apps;
    private Toolbar toolbar;
    private MaterialTabHost tabHost;
    private ViewPager pager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_app);
        apps=new ArrayList<>();
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        if(toolbar!=null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        tabHost = (MaterialTabHost) this.findViewById(R.id.tabHost);
        pager = (ViewPager) this.findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                tabHost.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < adapter.getCount(); i++)
            tabHost.addTab(tabHost.newTab().setText(adapter.getPageTitle(i)).setTabListener(this));
        if(!isMyServiceRunning(KillOnLock.class,this))
            startService(new Intent(getApplicationContext(),KillOnLock.class));
    }

    private static boolean isMyServiceRunning(Class<?> serviceClass,Context c) {
        ActivityManager manager = (ActivityManager)c.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_choose_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            AlertDialog.Builder dialog=new AlertDialog.Builder(ChooseAppActivity.this);
            dialog.setTitle("About");
            dialog.setMessage("This app has been developed to help a bunch of my friends to manage closing their apps when they no more needed them. " +
                              "This app automatically look for root binaries and if there are not it will close the app as far as possible.\n" +
                              "Use this app at your risk!");
            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab tab) {

    }

    @Override
    public void onTabUnselected(MaterialTab tab) {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private final String[] titles={"All apps","Sentenced to death"};

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        public Fragment getItem(int num) {
            switch(num)
            {
                case 0: return new AllApps();
                case 1: return new SelectedApps();
                default: return new AllApps();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    public static class KillOnLock extends Service
    {
        private ScreenReceiver receiver;

        @Override
        public void onCreate() {
            super.onCreate();
            final IntentFilter filter=new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            receiver=new ScreenReceiver();
            registerReceiver(receiver,filter);
            initKillList();
            System.out.println("Service and BroadCastReceiver are online, Sir.");
        }

        @Override
        public void onStart(Intent intent, int startId) {
            initKillList();
            if(isScreenOff())
                new KillProcInList().execute(getApplicationContext());
        }

        private void initKillList()
        {
            if(ChooseAppActivity.killList==null)
            {
                ChooseAppActivity.killList=new ArrayList<>();
                AppDatabase appDatabase=new AppDatabase(getApplicationContext());
                SQLiteDatabase db=appDatabase.getReadableDatabase();
                Cursor cursor=db.rawQuery("SELECT package FROM KillList", null);
                while(cursor.moveToNext())
                    ChooseAppActivity.killList.add(cursor.getString(0));
            }
        }

        private boolean isScreenOff() {
            if (Build.VERSION.SDK_INT<20) {
                PowerManager powerManager=(PowerManager)getSystemService(POWER_SERVICE);
                if (!powerManager.isScreenOn())
                    return true;
                else
                    return false;
            }
            else
            {
                DisplayManager dm=(DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
                for (Display display : dm.getDisplays())
                    if (display.getState()==Display.STATE_OFF)
                        return true;
                return false;
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        public static class ScreenReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                if(!isMyServiceRunning(KillOnLock.class,context))
                    context.startService(new Intent(context, KillOnLock.class));
                if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                    new KillProcInList().execute(context);
            }
        }

        private static class KillProcInList extends AsyncTask<Context,Void,Void>
        {
            @Override
            protected Void doInBackground(Context... params) {
                ActivityManager activityManager=(ActivityManager)params[0].getSystemService("activity");
                List<ActivityManager.RunningAppProcessInfo> procInfo=activityManager.getRunningAppProcesses();
                System.out.println("Trying to kill... "+procInfo.size());
                if(killList==null)
                    killList=new ArrayList<>();
                else
                    killList.clear();
                AppDatabase appDatabase=new AppDatabase(params[0]);
                SQLiteDatabase db=appDatabase.getReadableDatabase();
                Cursor cursor=db.rawQuery("SELECT package FROM KillList", null);
                while(cursor.moveToNext())
                    killList.add(cursor.getString(0));
                for (int i=0;i<procInfo.size();i++)
                {
                    ActivityManager.RunningAppProcessInfo process=procInfo.get(i);
                    String name=process.processName;
                    if(killList.indexOf(name)!=-1)
                    {
                        try {
                            if (!PreferenceManager.getDefaultSharedPreferences(params[0]).getBoolean("ROOT", false))
                                activityManager.killBackgroundProcesses(name);
                            else if (Shell.SU.available())
                                Shell.SU.run("am force-stop " + name);
                            else
                                activityManager.killBackgroundProcesses(name);
                            System.out.println("I killed "+name);
                        }
                        catch (Exception exc)
                        {
                            exc.printStackTrace();
                        }
                    }
                }
                return null;
            }
        }
    }
}
