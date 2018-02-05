package org.opencv.javacv.facerecognition.Database;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityDeletionOrUpdateAdapter;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.database.Cursor;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class PersonDao_Impl implements PersonDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfPerson;

  private final EntityDeletionOrUpdateAdapter __updateAdapterOfPerson;

  public PersonDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPerson = new EntityInsertionAdapter<Person>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `Person`(`pid`,`found_time`) VALUES (?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Person value) {
        if (value.pid == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.pid);
        }
        if (value.found_time == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.found_time);
        }
      }
    };
    this.__deletionAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Person>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `Person` WHERE `pid` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Person value) {
        if (value.pid == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.pid);
        }
      }
    };
    this.__updateAdapterOfPerson = new EntityDeletionOrUpdateAdapter<Person>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `Person` SET `pid` = ?,`found_time` = ? WHERE `pid` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Person value) {
        if (value.pid == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.pid);
        }
        if (value.found_time == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.found_time);
        }
        if (value.pid == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.pid);
        }
      }
    };
  }

  @Override
  public void insertPersons(Person... persons) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfPerson.insert(persons);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deletePersons(Person... person) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfPerson.handleMultiple(person);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updatePersons(Person... persons) {
    __db.beginTransaction();
    try {
      __updateAdapterOfPerson.handleMultiple(persons);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Person> getAllPersons() {
    final String _sql = "SELECT * FROM person";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfPid = _cursor.getColumnIndexOrThrow("pid");
      final int _cursorIndexOfFoundTime = _cursor.getColumnIndexOrThrow("found_time");
      final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Person _item;
        _item = new Person();
        _item.pid = _cursor.getString(_cursorIndexOfPid);
        _item.found_time = _cursor.getString(_cursorIndexOfFoundTime);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<Person> getPerson(String pid) {
    final String _sql = "SELECT * FROM person WHERE pid= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (pid == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, pid);
    }
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfPid = _cursor.getColumnIndexOrThrow("pid");
      final int _cursorIndexOfFoundTime = _cursor.getColumnIndexOrThrow("found_time");
      final List<Person> _result = new ArrayList<Person>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Person _item;
        _item = new Person();
        _item.pid = _cursor.getString(_cursorIndexOfPid);
        _item.found_time = _cursor.getString(_cursorIndexOfFoundTime);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
