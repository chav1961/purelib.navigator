package zzz;

import java.awt.Color;

import javax.swing.JLabel;


public class JLabelAnimal extends JLabel{
	private Animal animal;

	public JLabelAnimal(Animal a){
		this.setAnimal(a);
		this.setOpaque(true);
		this.setBackground(Color.GREEN);
	}

	public void setAnimal(Animal animal) {
		this.animal = animal;
		this.setText(animal.getName() + " - " + animal.getAge());
	}

	public Animal getAnimal() {
		return animal;
	}
}