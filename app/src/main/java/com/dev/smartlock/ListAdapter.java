package com.dev.smartlock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.Switch;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by daniele on 09/04/2015.
 */
public class ListAdapter extends ArrayAdapter implements SectionIndexer
{
    private ArrayList<PackageMetadata> data;
    private ArrayList<String> killList;
    private Context context;
    private HashMap<String,Integer> alphaIndexer;
    private String[] sections;
    private boolean[] selectedSwitches;

    public ListAdapter(Context context,int resource,ArrayList<PackageMetadata> data,ArrayList<String> killList)
    {
        super(context,resource,data);
        this.context=context;
        this.data=data;
        this.killList=killList;
        Collections.sort(data);
        alphaIndexer = new HashMap<String, Integer>();
        for (int i = 0; i < data.size(); i++)
        {
            String s = data.get(i).getName().substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s))
                alphaIndexer.put(s, i);
        }

        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        for (int i = 0; i < sectionList.size(); i++)
            sections[i] = sectionList.get(i);
        selectedSwitches=new boolean[data.size()];
        for(int i=0;i<selectedSwitches.length;i++) {
            selectedSwitches[i]=false;
            for (int j = 0; j < killList.size(); j++)
                if (data.get(i).getPackageName().equals(killList.get(j))) {
                    selectedSwitches[i] = true;
                    break;
                }
        }
    }

    static class ViewHolder {
        public TextView name,packageName;
        public ImageView icon;
        public CheckBox toKill;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView=convertView;
        if(rowView==null)
        {
            LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.app_item, parent, false);
            ViewHolder holder=new ViewHolder();
            holder.name=(TextView)rowView.findViewById(R.id.name);
            holder.packageName=(TextView)rowView.findViewById(R.id.app_package);
            holder.icon=(ImageView)rowView.findViewById(R.id.icon);
            holder.toKill=(CheckBox)rowView.findViewById(R.id.to_be_killed);
            rowView.setTag(holder);
        }
        ViewHolder holder=(ViewHolder)rowView.getTag();
        holder.name.setText(data.get(position).getName());
        holder.packageName.setText(data.get(position).getPackageName());
        holder.icon.setImageDrawable(data.get(position).getIcon());
        holder.toKill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    killList.add(data.get(position).getPackageName());
                    Set<String> s=new LinkedHashSet<String>(killList);
                    killList.clear();
                    killList.addAll(s);
                    selectedSwitches[position]=true;
                }
                else {
                    killList.remove(data.get(position).getPackageName());
                    selectedSwitches[position]=false;
                }
            }
        });
        holder.toKill.setChecked(selectedSwitches[position]);
        return rowView;
    }

    public int getPositionForSection(int section)
    {
        return alphaIndexer.get(sections[section]);
    }

    public int getSectionForPosition(int position)
    {
        return 1;
    }

    public Object[] getSections()
    {
        return sections;
    }
}