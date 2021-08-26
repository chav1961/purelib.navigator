package zzz;

public class Animal {

	private String name;
	private int age;

	public Animal() {
		name = "Name " + System.currentTimeMillis();
		age = (int) +System.currentTimeMillis();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}