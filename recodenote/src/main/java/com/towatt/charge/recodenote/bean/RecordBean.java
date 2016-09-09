package com.towatt.charge.recodenote.bean;

/**
 * user:HRobbie
 * Date:2016/8/22
 * Time:19:07
 * 邮箱：hwwyouxiang@163.com
 * Description:Page Function.
 */
public class RecordBean {
    private int id;
    private String name;
    private long createDate;
    private long duration;
    private String createName;

    private long clockTime;

    private int isAlert;//是否提醒0，不提醒，1提醒

    private boolean isCheck;//是否被选中

    private boolean isShake;

    private String storePosition;
    public RecordBean(int id, String name, long createDate, long duration, String createName, String storePosition) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.duration = duration;
        this.createName = createName;
        this.storePosition = storePosition;
    }
    public RecordBean(int id, String name, long createDate, long duration, String createName, long clockTime, int isAlert, String storePosition) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.duration = duration;
        this.createName = createName;
        this.clockTime = clockTime;
        this.isAlert = isAlert;
        this.storePosition = storePosition;
    }

    public RecordBean(int id, String name, long createDate, long duration, String createName, long clockTime, int isAlert, boolean isCheck, boolean isShake, String storePosition) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.duration = duration;
        this.createName = createName;
        this.clockTime = clockTime;
        this.isAlert = isAlert;
        this.isCheck = isCheck;
        this.isShake = isShake;
        this.storePosition = storePosition;
    }

    public String getStorePosition() {
        return storePosition;
    }

    public void setStorePosition(String storePosition) {
        this.storePosition = storePosition;
    }

    public int getIsAlert() {
        return isAlert;
    }

    public void setIsAlert(int isAlert) {
        this.isAlert = isAlert;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }


    public boolean isShake() {
        return isShake;
    }

    public void setShake(boolean shake) {
        isShake = shake;
    }

    public RecordBean(int id, String name, long createDate, long duration, String createName, long clockTime, int isAlert) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.duration = duration;
        this.createName = createName;
        this.clockTime = clockTime;
        this.isAlert = isAlert;
    }

    public RecordBean() {
    }

    public RecordBean(int id, String name, long createDate, long duration, String createName) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.duration = duration;
        this.createName = createName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCreateName() {
        return createName;
    }

    public void setCreateName(String createName) {
        this.createName = createName;
    }

    public long getClockTime() {
        return clockTime;
    }

    public RecordBean(int id, String name, long createDate, long duration, String createName, long clockTime) {
        this.id = id;
        this.name = name;
        this.createDate = createDate;
        this.duration = duration;
        this.createName = createName;
        this.clockTime = clockTime;
    }

    public void setClockTime(long clockTime) {
        this.clockTime = clockTime;
    }
}
