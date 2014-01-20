package com.yaylas.sytech.facerecognizer.facedatabase;

public class Person implements Comparable<Person>{
	private long id = -1;
	private String name = "";
	private String facesFolderPath = "";
	public Person() {
		// TODO Auto-generated constructor stub
	}
	public Person(long id, String name, String facesFolderPath) {
		super();
		this.id = id;
		this.name = name;
		this.facesFolderPath = facesFolderPath;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFacesFolderPath() {
		return facesFolderPath;
	}
	public void setFacesFolderPath(String facesFolderPath) {
		this.facesFolderPath = facesFolderPath;
	}
	@Override
	public int compareTo(Person another) {
		// TODO Auto-generated method stub
		return (int)(id - another.getId());
	}
	
	
}
