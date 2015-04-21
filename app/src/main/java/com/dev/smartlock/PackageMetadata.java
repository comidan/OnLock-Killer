package com.dev.smartlock;

import android.graphics.drawable.Drawable;

/**
 * Created by daniele on 09/04/2015.
 */
public class PackageMetadata implements Comparable<PackageMetadata>
{
    private Drawable icon;
    private String name;
    private String packageName;

    String getPackageName()
    {
        return packageName;
    }

    void setPackageName(String packageName)
    {
        this.packageName=packageName;
    }

    String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name=name;
    }

    Drawable getIcon()
    {
        return icon;
    }

    void setIcon(Drawable icon)
    {
        this.icon=icon;
    }

    @Override
    public int compareTo(PackageMetadata another) {
        return name.compareToIgnoreCase(another.getName());
    }
}
