package fr.univtln.cniobechoudayer.pimpmytrip.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {

    protected SQLiteDatabase mDb = null;

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * The static string to create the database.
     */
    private static final String DATABASE_CREATE = "create table "
            + DBConstants.MY_TABLE + "(" + DBConstants.KEY_COL_ID
            + " integer primary key autoincrement, " + DBConstants.KEY_COL_NUMERO
            + " INTEGER, " + DBConstants.KEY_COL_TEXT + " TEXT) ";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w("DBOpenHelper", "Mise à jour de la version " + oldVersion
                + " vers la version " + newVersion
                + ", les anciennes données seront détruites ");
        // Drop the old database
        db.execSQL("DROP TABLE IF EXISTS " + DBConstants.MY_TABLE);
        // Create the new one
        onCreate(db);
        // or do a smartest stuff

    }

    public SQLiteDatabase open() {
        // Pas besoin de fermer la dernière base puisque getWritableDatabase s'en charge
        mDb = this.getWritableDatabase();
        return mDb;
    }

    public void close() {
        mDb.close();
    }

    public SQLiteDatabase getDb() {
        return mDb;
    }

    public void insertData(int index,String dataToSave){

        ContentValues values = new ContentValues();
        values.put(DBConstants.KEY_COL_NUMERO, index++);
        values.put(DBConstants.KEY_COL_TEXT, dataToSave);


        mDb.insert(DBConstants.MY_TABLE, null, values);

        /**
         * If we want to return the inserted object
         */
        /*
        long insertId = myDB.insert(DBOpenHelper.Constants.MY_TABLE, null, values);
        Cursor cursor = database.query(DBOpenHelper.Constants.MY_TABLE,
        allColumns, DBOpenHelper.Constants.KEY_COL_ID + " = " + insertId, null,
        null, null, null);
        cursor.moveToFirst();
        Object insertedObject = cursorToComment(cursor);
        cursor.close();

        private Comment cursorToComment(Cursor cursor) {
        Comment comment = new Comment();
        comment.setId(cursor.getLong(0));
        comment.setComment(cursor.getString(1));
        return comment;
        }
         */

    }

    public int getLastIndexInsertedData(){

        this.getReadableDatabase();
        final String MY_QUERY = "SELECT MAX(_id) FROM " + DBConstants.MY_TABLE;
        Cursor cur = mDb.rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        int ID = cur.getInt(0);
        cur.close();
        return ID;

    }

    public String loadData(){

        StringBuilder stringBuilder = new StringBuilder();
        this.getReadableDatabase();
        Cursor cursor = mDb.rawQuery("SELECT * FROM "+DBConstants.MY_TABLE+" DESC", null);
        Log.d("nb results", String.valueOf(cursor.getCount()));

        while (cursor.moveToNext()) {
            stringBuilder.append(cursor.getInt(DBConstants.ID_COLUMN) + " " + cursor.getInt(DBConstants.NUMERO_COLUMN) + " " + cursor.getString(DBConstants.TEXT_COLUMN) + "\n");

        }

        return stringBuilder.toString();
    }

} 
