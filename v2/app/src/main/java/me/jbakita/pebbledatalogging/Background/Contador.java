package me.jbakita.pebbledatalogging.Background;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Francisco on 10/04/2016.
 */
public class Contador extends Thread {
    public static final int SECOND = 120;
    public static final int STOP= 0;
    boolean running;
    int cont, contSeg, contMin;
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

        cont = 120;
        contSeg=60;
        contMin=2;
        Log.i("haur","Run "+running);

        while (running) {
            try {
                Log.i("haur","while "+running);
                Thread.sleep(200);//numero aproximado en segundos 995
                    //Log.e("haur", "cont>0 "+running);
                    if (contSeg>0 && running==true){
                        contSeg--;
                        Message msg = handler.obtainMessage();
                        msg.what = SECOND;
                        msg.arg1 = contMin;
                        msg.arg2 = contSeg;
                        handler.sendMessage(msg);
                        Log.e("casos", "caso1");
                    }else {
                        contMin--;
                        contSeg=59;
                        Message msg = handler.obtainMessage();
                        msg.what = SECOND;
                        msg.arg1 = contMin;
                        msg.arg2 = contSeg;
                        handler.sendMessage(msg);
                        Log.e("casos", "caso2");}

                        if(contMin==0 && contSeg==0){
                            Message msg = handler.obtainMessage();
                            msg.arg1 = contMin;
                            msg.arg2 = contSeg;
                            msg.what = STOP;
                            running=false;
                            handler.sendMessage(msg);
                            Log.e("casos", "caso3");
                        }




            } catch (InterruptedException e) {
                Log.e("haur", "thread catch");
                e.printStackTrace();
            }

        }

    }
}
