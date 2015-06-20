package nl.maartenvisscher.thermodroid;

import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("all")
class InterruptTest {

    InterruptTest() {
        Thread thread = new Thread(new ConnectionRunnable());
        thread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        thread.interrupt();
    }

    private class ConnectionRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection connect = (HttpURLConnection) url.openConnection();

                    boolean wasInterruptedBefore = Thread.currentThread().isInterrupted();
                    connect.getInputStream();
                    boolean wasInterruptedAfter = Thread.currentThread().isInterrupted();

                    if (wasInterruptedBefore == true && wasInterruptedAfter == false) {
                        System.out.println("Wut! Interrupted changed from true to false while no InterruptedIOException or InterruptedException was thrown");
                        break;
                    }
                } catch (Exception e) {
                    System.out.println(e.getClass().getName() + ": " + e.getMessage());
                    break;
                }
                for (int i = 0; i < 100000; i += 1) { // Crunching
                    System.out.print("");
                }
            }
            System.out.println("ConnectionThread is stopped");
        }
    }
}