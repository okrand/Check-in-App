package io.github.okrand.location_assignment;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Loc.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LocDao locDao();
}
