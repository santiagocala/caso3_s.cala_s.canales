


import java.util.Scanner;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;


public class Generator {
    private LoadGenerator generator;

    public Generator(String decision){
        Task work = createTask(decision);
        int numberOfTasks = 400;
        int gapBetweenTasks = 20;
        generator = new LoadGenerator("Client - Server Load Tests ", numberOfTasks,work,gapBetweenTasks);
        generator.generate();
    }

    private Task createTask(String decision){
    	if(decision.equals("1")){
    		return new Cliente();
    	}
    	else if(decision.equals("2")){
    		return new ClienteNoSeguro();
    	}
    	else{
    		System.out.println("es solo 1 o 2, pilas");
    		return null;
    	}
    }
    public static void main (String args[]){
    	System.out.println("1.Cliente Seguro");
    	System.out.println("2.Cliente Inseguro");
    	Scanner sc = new Scanner(System.in);
    	String decision = sc.nextLine();
        Generator gen = new Generator(decision);
    }
}
