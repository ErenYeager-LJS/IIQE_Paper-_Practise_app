package com.iiqe.study;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class StudyDb extends SQLiteOpenHelper {
    StudyDb(Context context) { super(context, "iiqe_study.db", null, 2); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE state (paper TEXT NOT NULL, qid INTEGER NOT NULL, seen INTEGER NOT NULL DEFAULT 0, correct INTEGER NOT NULL DEFAULT 0, wrong INTEGER NOT NULL DEFAULT 0, last_seen INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(paper,qid))");
        db.execSQL("CREATE TABLE settings (paper TEXT PRIMARY KEY, new_count INTEGER NOT NULL, review_count INTEGER NOT NULL, next_index INTEGER NOT NULL)");
        createAttempts(db);
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { if (oldVersion < 2) createAttempts(db); }
    private void createAttempts(SQLiteDatabase db) { db.execSQL("CREATE TABLE IF NOT EXISTS attempts (id INTEGER PRIMARY KEY AUTOINCREMENT, paper TEXT NOT NULL, mode TEXT NOT NULL, completed INTEGER NOT NULL, total INTEGER NOT NULL, correct INTEGER NOT NULL, seconds INTEGER NOT NULL)"); }

    private void ensurePaper(String paper) {
        ContentValues values = new ContentValues(); values.put("paper", paper); values.put("new_count", 10); values.put("review_count", 10); values.put("next_index", 0);
        getWritableDatabase().insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
    int getNewCount(String paper) { return getSetting(paper, "new_count"); }
    int getReviewCount(String paper) { return getSetting(paper, "review_count"); }
    int getNextIndex(String paper) { return getSetting(paper, "next_index"); }
    private int getSetting(String paper, String key) {
        ensurePaper(paper); Cursor c = getReadableDatabase().query("settings", new String[]{key}, "paper=?", new String[]{paper}, null, null, null);
        try { return c.moveToFirst() ? c.getInt(0) : 0; } finally { c.close(); }
    }
    void setCount(String paper, String column, int count) { ensurePaper(paper); ContentValues v=new ContentValues(); v.put(column,count); getWritableDatabase().update("settings",v,"paper=?",new String[]{paper}); }
    void advanceNew(String paper, int index) { ensurePaper(paper); ContentValues v=new ContentValues();v.put("next_index",index);getWritableDatabase().update("settings",v,"paper=?",new String[]{paper}); }
    void record(String paper, int qid, boolean correct) {
        SQLiteDatabase db=getWritableDatabase(); ContentValues seed=new ContentValues(); seed.put("paper",paper);seed.put("qid",qid);db.insertWithOnConflict("state",null,seed,SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE state SET seen=1, correct=correct+?, wrong=wrong+?, last_seen=? WHERE paper=? AND qid=?",new Object[]{correct?1:0,correct?0:1,System.currentTimeMillis(),paper,qid});
    }
    List<Integer> reviewIds(String paper,int limit) { return ids("paper=? AND seen=1 AND last_seen<?",new String[]{paper,String.valueOf(startOfToday())},"wrong DESC, last_seen ASC",limit); }
    List<Integer> errorIds(String paper,int limit) { return ids("paper=? AND wrong>0",new String[]{paper},"wrong DESC,last_seen ASC",limit); }
    private List<Integer> ids(String where,String[] args,String order,int limit) { ArrayList<Integer> out=new ArrayList<>();Cursor c=getReadableDatabase().query("state",new String[]{"qid"},where,args,null,null,order,String.valueOf(limit));try{while(c.moveToNext())out.add(c.getInt(0));}finally{c.close();}return out; }
    int errorCount(String paper) { return scalar("SELECT COUNT(*) FROM state WHERE paper=? AND wrong>0",new String[]{paper}); }
    int todayCount(String paper) { return scalar("SELECT COUNT(*) FROM state WHERE paper=? AND last_seen>=?",new String[]{paper,String.valueOf(startOfToday())}); }
    private int scalar(String sql,String[] args) { Cursor c=getReadableDatabase().rawQuery(sql,args);try{return c.moveToFirst()?c.getInt(0):0;}finally{c.close();} }
    private long startOfToday() { Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.getTimeInMillis(); }

    void saveAttempt(String paper,String mode,int total,int correct,int seconds) { ContentValues v=new ContentValues();v.put("paper",paper);v.put("mode",mode);v.put("completed",System.currentTimeMillis());v.put("total",total);v.put("correct",correct);v.put("seconds",seconds);getWritableDatabase().insert("attempts",null,v); }
    Map<Integer,Integer> attemptDays(int year,int month) {
        HashMap<Integer,Integer> out=new HashMap<>(); Calendar start=Calendar.getInstance();start.set(year,month,1,0,0,0);start.set(Calendar.MILLISECOND,0);Calendar end=(Calendar)start.clone();end.add(Calendar.MONTH,1);
        Cursor c=getReadableDatabase().rawQuery("SELECT completed FROM attempts WHERE completed>=? AND completed<?",new String[]{String.valueOf(start.getTimeInMillis()),String.valueOf(end.getTimeInMillis())});
        try { Calendar day=Calendar.getInstance();while(c.moveToNext()){day.setTimeInMillis(c.getLong(0));int d=day.get(Calendar.DAY_OF_MONTH);out.put(d,(out.containsKey(d)?out.get(d):0)+1);}} finally {c.close();} return out;
    }
    List<String> attemptsOn(String dayKey) {
        ArrayList<String> out=new ArrayList<>();SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd",Locale.US);Cursor c=getReadableDatabase().query("attempts",new String[]{"paper","mode","total","correct","seconds","completed"},null,null,null,null,"completed ASC");
        try { while(c.moveToNext()){if(fmt.format(new Date(c.getLong(5))).equals(dayKey)){out.add(c.getString(0)+" · "+c.getString(1)+" · "+c.getInt(3)+"/"+c.getInt(2)+" · "+(c.getInt(4)/60)+" 分钟");}}} finally {c.close();}return out;
    }
}
