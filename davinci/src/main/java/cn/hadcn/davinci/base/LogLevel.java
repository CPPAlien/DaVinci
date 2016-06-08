package cn.hadcn.davinci.base;

/**
 *
 * Created by 90Chris on 2016/6/8.
 */
public enum LogLevel {
    NONE(0),
    DEBUG(15),
    INFO(7),
    WARN(3),
    ERROR(1);

    private int mLevel;
    LogLevel(int level){
        mLevel = level;
    }

    final public int getValue() {
        return mLevel;
    }
}
