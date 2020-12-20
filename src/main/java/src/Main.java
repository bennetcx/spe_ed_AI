package src;

import java.util.ArrayList;
import java.util.List;

public class Main
{
    private static final List<SafeThread> runningThreads = new ArrayList<>();
    private static ConsoleListenerThread consoleListenerThread;
    public static void main(String[] args)
    {

        // TODO List: (IMPORTANT!!!)
        /*
        * - Move Neat Params to config
        * - Console Commands like "show_pop 150" or "pause_training"
        * - Really should change the fitness calculation.... Fucks up the whole training lol
        * - Refactor/rename src.Stage class
        * */

        consoleListenerThread = new ConsoleListenerThread();
        consoleListenerThread.setName("ConsoleListener");
        consoleListenerThread.start();
    }

    public static void startThread(SafeThread thread){
        runningThreads.add(thread);
        thread.start();
    }

    public static void stopThreads(){
        for(SafeThread thread : runningThreads){
            if(thread != null){
                thread.terminate();
            }
        }
        consoleListenerThread.terminate();
    }

    public static List<SafeThread> getRunningThreads() {
        return runningThreads;
    }
}