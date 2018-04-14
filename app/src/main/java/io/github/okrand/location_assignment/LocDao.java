package io.github.okrand.location_assignment;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface LocDao {

    @Query("SELECT * FROM checkins")
    List<Loc> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Loc... Locations);

    @Update
    void updateLoc(Loc... Locations);

    @Delete
    void delete(Loc... Locations);
}
