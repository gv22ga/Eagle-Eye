package org.opencv.javacv.facerecognition.Database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by rishabh on 02/02/18.
 */

@Database(entities = {Person.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{
    public abstract PersonDao getPersonDao();
}
