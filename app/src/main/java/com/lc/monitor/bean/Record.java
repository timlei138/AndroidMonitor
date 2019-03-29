package com.lc.monitor.bean;

public class Record {

    private int id;

    private String name;

    private int type;

    private int faceCount;

    private long date;

    private String file;


    public Record(){}

    public Record(int id,String name,int type,int faceCount,long date,String file){
        this.id = id;
        this.name = name;
        this.type = type;
        this.faceCount = faceCount;
        this.date = date;
        this.file = file;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public void setFaceCount(int faceCount) {
        this.faceCount = faceCount;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }


    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", faceCount=" + faceCount +
                ", date=" + date +
                ", file='" + file + '\'' +
                '}';
    }
}
