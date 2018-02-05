package org.opencv.javacv.facerecognition.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by rishabh on 02/02/18.
 */

@Entity
public class Person {

    @PrimaryKey @NonNull
    public String pid;

    @ColumnInfo
    public String found_time;

    public Person(){
        this.pid = "";
        this.found_time = "";
    }
}