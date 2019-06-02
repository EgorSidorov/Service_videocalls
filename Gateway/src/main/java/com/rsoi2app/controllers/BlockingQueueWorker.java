package com.rsoi2app.controllers;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;

public class BlockingQueueWorker {
    private LinkedBlockingDeque<String> queueURL;
    private LinkedBlockingDeque<Utils.Services> queueServices;
    private Thread workerThread;

    private Boolean notCloseWhenEmpty;
    private Boolean working;
    private static BlockingQueueWorker instance = new BlockingQueueWorker();
    private BlockingQueueWorker(){
        queueURL = new LinkedBlockingDeque<>(100);
        queueServices = new LinkedBlockingDeque<>(100);
        notCloseWhenEmpty = false;
        working = false;
        starting();
    }
    public static BlockingQueueWorker getInstance(){
        return instance;
    }
    public void setQuery(String url, Utils.Services service) throws InterruptedException {
        queueURL.put(url);
        queueServices.put(service);
        starting();
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
        if(!working) {
            workerThread = new Thread(new Worker());
            workerThread.start();
        }
    }

    public void stopping()
    {
        workerThread.checkAccess();
        workerThread.interrupt();
    }

    public Boolean isWorking()
    {
        return working;
    }

    public Boolean getNotCloseWhenEmpty() {
        return notCloseWhenEmpty;
    }

    public void setNotCloseWhenEmpty(Boolean notCloseWhenEmpty) {
        this.notCloseWhenEmpty = notCloseWhenEmpty;
    }

    class Worker implements Runnable {
        public void run() {
            working = true;

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (checkEmpty()) {
                            if (notCloseWhenEmpty) {
                                    Thread.sleep(1000);
                            } else {
                                break;
                            }
                        }
                        Utils.Services service = BlockingQueueWorker.getInstance().getService();
                        String query = BlockingQueueWorker.getInstance().getQuery();
                        if (Utils.requestForService(query, "none", service).contains("Error:Timeout")) {
                            try {
                                    BlockingQueueWorker.getInstance().setQuery(query, service);
                            } catch (InterruptedException e) {
                                //очередь заполнена, вытесняем более ранние
                                e.printStackTrace();
                                displacement();
                                BlockingQueueWorker.getInstance().setQuery(query, service);
                            }
                                Thread.sleep(1000);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                working = false;
        }
    }
}
