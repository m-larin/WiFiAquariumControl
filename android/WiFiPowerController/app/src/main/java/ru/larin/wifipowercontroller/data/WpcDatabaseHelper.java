package ru.larin.wifipowercontroller.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.larin.wifipowercontroller.model.Device;

public class WpcDatabaseHelper extends SQLiteOpenHelper {

    public WpcDatabaseHelper(Context context) {
        // конструктор суперкласса
        super(context, "wpc", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // создаем таблицу с полями
        sqLiteDatabase.execSQL("create table devices ("
                + "id long primary key, "
                + "name text, "
                + "ip text, "
                + "img blob"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public boolean exists(Long id){
        String query = "SELECT id FROM devices where id = ?";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, new String[]{id.toString()});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return result;
    }

    public void saveDevice(Device device){
        boolean exists = exists(device.getId());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", device.getId());
        values.put("ip", device.getIp());
        values.put("name", device.getName());
        values.put("img", device.getImg());

        if (exists){
            db.update("devices", values, "id=?", new String[]{String.valueOf(device.getId())});
        }else {
            db.insert("devices", null, values);
        }
        db.close(); // Closing database connection
    }

    public void deleteDevice(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("devices", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Device> findAllDevice(){
        String query = "SELECT id, ip, name, img FROM devices";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        List<Device> result = new ArrayList<Device>();
        if (cursor.moveToFirst()) {
            do {
                Device device = new Device();
                device.setId(cursor.getLong(0));
                device.setIp(cursor.getString(1));
                device.setName(cursor.getString(2));
                device.setImg(cursor.getBlob(3));

                result.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return result;
    }

    public Device findDevice(long id){
        String query = "SELECT id, ip, name, img FROM devices where id = ?";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        Device result = null;
        if (cursor.moveToFirst()) {
            result = new Device();
            result.setId(cursor.getLong(0));
            result.setIp(cursor.getString(1));
            result.setName(cursor.getString(2));
            result.setImg(cursor.getBlob(3));
        }

        cursor.close();
        db.close();

        return result;
    }

}
