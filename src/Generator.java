
import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;


public class Generator {
    private LoadGenerator generator;

    public Generator(){
        Task work = createTask();
        int numberOfTasks = 100;
        int gapBetweenTasks = 1000;
        generator = new LoadGenerator("Client - Server Load Tests ", numberOfTasks,work,gapBetweenTasks);
        generator.generate();
    }

    private Task createTask(){
        return new Cliente();
    }
    public static void main (String args[]){
        Generator gen = new Generator();
    }
}
