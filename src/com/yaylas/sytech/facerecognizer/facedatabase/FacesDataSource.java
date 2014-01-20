package com.yaylas.sytech.facerecognizer.facedatabase;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore.Files;

public class FacesDataSource {

	  private SQLiteDatabase database;
	  private FaceRecognizerDBHelper dbHelper;
	  private String[] allColumns = { FaceRecognizerDBHelper.COLUMN_ID,
			  FaceRecognizerDBHelper.COLUMN_NAME,FaceRecognizerDBHelper.COLUMN_FOLDERPATH };

	  public FacesDataSource(Context context) {
		  dbHelper = new FaceRecognizerDBHelper(context);
	  }

	  public void open() throws SQLException {
		  database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
		  dbHelper.close();
	  }

	  public Person createPerson(long person_id, String person_name, String faces_path) {
		  String insertQuery = "INSERT OR REPLACE INTO "+FaceRecognizerDBHelper.TABLE_FACES
	    		+" ("+FaceRecognizerDBHelper.COLUMN_ID+","+FaceRecognizerDBHelper.COLUMN_NAME
	    		+","+FaceRecognizerDBHelper.COLUMN_FOLDERPATH+") VALUES ('"+person_id+"',\""+person_name+"\",\""
	    		+faces_path+"\")";
		  database.execSQL(insertQuery);
		  Cursor cursor = database.query(FaceRecognizerDBHelper.TABLE_FACES,
				  allColumns, FaceRecognizerDBHelper.COLUMN_ID + " = " + person_id, null,
				  null, null, null);
		  cursor.moveToFirst();
		  Person newPerson = cursorToPerson(cursor);
		  cursor.close();
		  return newPerson;
	  }
	  
	  public void updatePerson(Person person){
		  ContentValues values = new ContentValues();
		  values.put(FaceRecognizerDBHelper.COLUMN_NAME, person.getName());
		  values.put(FaceRecognizerDBHelper.COLUMN_FOLDERPATH, person.getFacesFolderPath());
		  database.update(FaceRecognizerDBHelper.TABLE_FACES, values,
				  FaceRecognizerDBHelper.COLUMN_ID+"='"+person.getId()+"'", null);
	  }

	  public void deletePerson(Person person) {
		  long id = person.getId();
		  database.delete(FaceRecognizerDBHelper.TABLE_FACES, FaceRecognizerDBHelper.COLUMN_ID
				  + " = " + id, null);
		  deleteFacesFolder(person.getFacesFolderPath());
	  }

	  public Vector<Person> getAllPersons() {
		  Vector<Person> persons = new Vector<Person>();

		  Cursor cursor = database.query(FaceRecognizerDBHelper.TABLE_FACES,
	        allColumns, null, null, null, null, null);

		  cursor.moveToFirst();
		  Vector<Person> invalidPersonsToDelete = new Vector<Person>();
		  while (!cursor.isAfterLast()) {
			  Person person = cursorToPerson(cursor);
			  if(isPathValid(person.getFacesFolderPath())){
				  persons.add(person);
			  } else {
				  invalidPersonsToDelete.add(person);
			  }
			  cursor.moveToNext();
		  }
		  cursor.close();
		//  sort(persons);
		  for(int i = 0; i<invalidPersonsToDelete.size(); i++) {
			  deletePerson(invalidPersonsToDelete.get(i));
		  }
		  return persons;
	  }
	  
	  private void sort(Vector<Person> personList) {
		    Collections.sort(personList, new Comparator<Person>() {
		        @Override
		        public int compare(Person p1, Person p2) {
		           return p1.compareTo(p2);
		        }           
		    });
		}

	  private Person cursorToPerson(Cursor cursor) {
		  Person person = new Person();
		  person.setId(cursor.getLong(0));
		  person.setName(cursor.getString(1));
		  person.setFacesFolderPath(cursor.getString(2));
		  return person;
	  }
	   
	  // If a person does not have 10 photos for recognition it is not valid data
	  private boolean isPathValid(String path){
		  File f = new File(path);
		  File[] matchingFiles = f.listFiles(new FilenameFilter() {
		      public boolean accept(File dir, String name) {
		          return name.endsWith("jpg");
		      }
		  });
		  if(matchingFiles.length < 10){
			  return false;
		  } else {
			  return true;
		  }
	  }
	  
	  public void deleteFacesFolder(String path){
		  File f = new File(path);
		  if(f.exists()){
			  deleteFolder(f);
		  }
	  }
	  
	  private void deleteFolder(File folder) {
		    File[] files = folder.listFiles();
		    if(files!=null) { //some JVMs return null for empty dirs
		        for(File f: files) {
		            if(f.isDirectory()) {
		                deleteFolder(f);
		            } else {
		                f.delete();
		            }
		        }
		    }
		    folder.delete();
	  }
} 

