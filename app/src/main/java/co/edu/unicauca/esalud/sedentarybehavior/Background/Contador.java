package co.edu.unicauca.esalud.sedentarybehavior.Background;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Francisco on 10/04/2016.
 */
public class Contador extends Thread{
    public static final int SECOND = 3600;
    public static final int STOP= 0;
    boolean running;
    int cont;
    Handler handler;


    public Contador(Handler handler)
    {
        running = true;
        this.handler=handler;
    }


    public void stoptemp()
    {

        running=false;
        Log.e("haur", "stopTemp " + running);
    }


    @Override
    public void run() {

        cont = 0;
        Log.i("haur","Run "+running);

        while (running) {
            try {
                Log.i("haur","while "+running);
                Thread.sleep(1000);
                if (cont<3600 && running) {
                    Log.e("haur", "cont>0 "+running);
                    cont++;
                    Message msg = handler.obtainMessage();
                    msg.what = SECOND;
                    msg.arg1 = cont;
                    if(cont==3600){

                        msg.what = STOP;
                    }

                    handler.sendMessage(msg);
                }
                else{
                    Log.e("haur", "");
                }

            } catch (InterruptedException e) {
                Log.e("haur", "thread catch");
                e.printStackTrace();
            }

        }

    }
}
