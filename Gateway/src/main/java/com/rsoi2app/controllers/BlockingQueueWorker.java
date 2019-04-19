package com.rsoi2app.controllers;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

public class BlockingQueueWorker {
    private LinkedBlockingDeque<String> queueURL;
    private LinkedBlockingDeque<Utils.Services> queueServices;
    private Boolean isWorking;

    private Boolean notCloseWhenEmpty;
    private static BlockingQueueWorker instance = new BlockingQueueWorker();
    private BlockingQueueWorker(){
        queueURL = new LinkedBlockingDeque<>(100);
        queueServices = new LinkedBlockingDeque<>(100);
        isWorking = true;
        notCloseWhenEmpty = false;
        (new Thread(new Worker())).start();
    }
    public static BlockingQueueWorker getInstance(){
        return instance;
    }
    public synchronized void setQuery(String url, Utils.Services service) throws InterruptedException {
        starting();
        queueURL.put(url);
        queueServices.put(service);
    }

    private synchronized Utils.Services getService() {
        return queueServices.poll();
    }

    private synchronized String getQuery() {
        return queueURL.poll();
    }

    private synchronized void displacement() {
        queueURL.removeFirst();
        queueServices.removeFirst();
    }

    private synchronized Boolean checkEmpty(){
        return queueURL.isEmpty();
    }

    public void starting()
    {
        if(!isWorking) {
            (new Thread(new Worker())).start();
            isWorking = true;
        }
    }

    public void stopping()
    {
        isWorking = false;
    }

    public Boolean getNotCloseWhenEmpty() {
        return notCloseWhenEmpty;
    }

    public void setNotCloseWhenEmpty(Boolean notCloseWhenEmpty) {
        this.notCloseWhenEmpty = notCloseWhenEmpty;
    }

    class Worker implements Runnable
    {
        public void run() {
            try {
                while (isWorking) {
                    if(checkEmpty()) {
                        if(notCloseWhenEmpty)
                            Thread.sleep(1000);
                        else {
                            stopping();
                            return;
                        }
                    }
                    Utils.Services service = BlockingQueueWorker.getInstance().getService();
                    String query = BlockingQueueWorker.getInstance().getQuery();
                    if(Utils.requestForService(query,"none",service).contains("Error:Timeout")){
                        try {
                            BlockingQueueWorker.getInstance().setQuery(query,service);
                        } catch (InterruptedException e) {
                            //очередь заполнена, вытесняем более ранние
                            e.printStackTrace();
                            displacement();
                            BlockingQueueWorker.getInstance().setQuery(query,service);
                        }
                        Thread.sleep(1000);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
