package nl.maartenvisscher.thermodroid;

/**
 * A day of a week.
 */
public enum Day {
    MONDAY(0), TUESDAY(1), WEDNESDAY(2), THURSDAY(3), FRIDAY(4), SATURDAY(5), SUNDAY(6);

    int index;

    Day(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
