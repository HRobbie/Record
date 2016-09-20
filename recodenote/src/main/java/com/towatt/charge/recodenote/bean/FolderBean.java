package com.towatt.charge.recodenote.bean;

/**
 * Created by HRobbie on 2016/9/13.
 */
public class FolderBean {
    private int id;

    private String whichFolder;

    private String folderName;
    private long createDate;

    public FolderBean(int id, String whichFolder, String folderName, long createDate) {
        this.id = id;
        this.whichFolder = whichFolder;
        this.folderName = folderName;
        this.createDate = createDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWhichFolder() {
        return whichFolder;
    }

    public void setWhichFolder(String whichFolder) {
        this.whichFolder = whichFolder;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }
}
