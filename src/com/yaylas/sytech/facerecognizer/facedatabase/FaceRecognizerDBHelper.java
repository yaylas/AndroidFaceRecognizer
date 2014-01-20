package com.yaylas.sytech.facerecognizer.facedatabase;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FaceRecognizerDBHelper extends SQLiteOpenHelper{
	private Context mContext;
	private static int VERSION_INT = 1;
	public static String TABLE_FACES = "facetable";
	public static String COLUMN_ID = "_id";
	public static String COLUMN_NAME = "name";
	public static String COLUMN_FOLDERPATH = "folderpath";
	FaceRecognizerDBHelper(Context context){
		super(context, "facedb", null, VERSION_INT);
		this.mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createFaceTable = "CREATE TABLE IF NOT EXISTS facetable(_id TEXT(10) PRIMARY KEY, name TEXT(30), folderpath TEXT(80));";
		db.execSQL(createFaceTable);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String deleteFaceTable = "DROP TABLE IF EXISTS facetable;";
		db.execSQL(deleteFaceTable);
		onCreate(db);
		
	}

}
