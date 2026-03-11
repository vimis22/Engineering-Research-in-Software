package iwandroid.utils;

public class Timer {
    private Config config;
    private float starttime;
    private float endtime;
    private float lastRecord;

    public Timer(Config config) {
        this.config = config;
        this.starttime = 0;
        this.lastRecord = 0;
        this.endtime = 0;
    }

    public void startTimer() {
        this.starttime = System.currentTimeMillis()/1000.0f;
    }

    public void stopTimer() {
        this.endtime = System.currentTimeMillis()/1000.0f;
    }

    public float lap() {
        float now = System.currentTimeMillis()/1000.0f;
        float diff = now - this.lastRecord;
        this.lastRecord = now;
        return diff;
    }

    public float timeTaken() {
        return this.endtime-this.starttime;
    }
}
