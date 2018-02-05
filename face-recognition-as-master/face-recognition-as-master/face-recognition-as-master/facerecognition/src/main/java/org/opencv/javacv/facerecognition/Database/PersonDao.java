package org.opencv.javacv.facerecognition.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishabh on 02/02/18.
 */

@Dao
public interface PersonDao {
    @Query("SELECT * FROM person")
    List<Person> getAllPersons();

    @Query("SELECT * FROM person WHERE pid= :pid")
    List<Person> getPerson(String pid);

    @Insert
    void insertPersons(Person... persons);

    @Update
    void updatePersons(Person... persons);

    @Delete
    void deletePersons(Person... person);

}
