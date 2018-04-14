package io.github.okrand.location_assignment;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

@Entity(tableName="checkins")
public class Loc {//implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name="name")
    private String name;
    @ColumnInfo(name="latitude")
    private Double latitude = 0.0;
    @ColumnInfo(name="longitude")
    private Double longitude = 0.0;
    @ColumnInfo(name="address")
    private String address;
    @ColumnInfo(name="time")
    private String time;
    @ColumnInfo(name="checklats")
    private String checklats = "";
    @ColumnInfo(name="checklons")
    private String checklons = "";
    @ColumnInfo(name="checktimes")
    private String checktimes = "";

    public int getId(){
        return id;
    }
    public String getName(){return name;}
    public Double getLatitude(){
        return latitude;
    }
    public Double getLongitude(){return longitude;}
    public String getAddress(){ return address; }
    public String getChecklats(){return checklats;}
    public String getChecklons(){return checklons;}
    public String getChecktimes(){return checktimes;}
    public String getTime(){
        return time;
    }

    public void setId(int newID){
        id = newID;
    }
    public void setName(String newName) { name = newName; }
    public void setLatitude(Double newLat){
        latitude = newLat;
    }
    public void setLongitude(Double newLon){
        longitude = newLon;
    }
    public void setAddress(String newAdd) { address = newAdd; }
    public void addChecklat(String newlat){
        if (!checklats.equals(""))
            checklats += "," + newlat;
        else
            checklats = newlat;
    }
    public void addChecklon(String newlon){
        if (!checklons.equals(""))
            checklons += "," + newlon;
        else
            checklons = newlon;
    }
    public void addChecktime(String newTime) {
        if (!checktimes.equals(""))
            checktimes += "," + newTime;
        else
            checktimes = newTime;
    }
    public void setChecklats(String newval) { checklats = newval; }
    public void setChecklons(String newval) { checklons = newval; }
    public void setChecktimes(String newval) { checktimes = newval; }
    public void setTime(String newTime){
        time = newTime;
    }
    public void setTime(Date newTime){
        time = newTime.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Loc) {
            Loc l2 = (Loc) o;
            float[] results = new float[5];
            Location.distanceBetween(getLatitude(),getLongitude(),l2.getLatitude(),l2.getLongitude(),results);
            return results[0] <= 30;
        }
        return false;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(id);
//        dest.writeString(name);
//        dest.writeDouble(latitude);
//        dest.writeDouble(longitude);
//        dest.writeString(address);
//        dest.writeString(time);
//        dest.writeString(checklats);
//        dest.writeString(checklons);
//        dest.writeString(checktimes);
//    }
}
