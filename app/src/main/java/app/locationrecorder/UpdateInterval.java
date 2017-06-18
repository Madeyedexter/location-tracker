package app.locationrecorder;


/**
 * An enum to model various update intervals based on speed range
 */
public enum UpdateInterval {
    TINY(30), SMALL(60), MEDIUM(120), LARGE(300);
    private int interval;

    UpdateInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public UpdateInterval increment() {
        return this.ordinal() < UpdateInterval.values().length - 1
                ? UpdateInterval.values()[this.ordinal() + 1]
                : UpdateInterval.values()[this.ordinal()];
    }

    public UpdateInterval decrement() {
        return this.ordinal() > 0
                ? UpdateInterval.values()[this.ordinal() - 1]
                : UpdateInterval.values()[this.ordinal()];
    }
}