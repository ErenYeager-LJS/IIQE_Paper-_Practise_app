package com.iiqe.study;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

final class StudyDb extends SQLiteOpenHelper {
    StudyDb(Context context) { super(context, "iiqe_study.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE state (paper TEXT NOT NULL, qid INTEGER NOT NULL, seen INTEGER NOT NULL DEFAULT 0, correct INTEGER NOT NULL DEFAULT 0, wrong INTEGER NOT NULL DEFAULT 0, last_seen INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(paper,qid))");
        db.execSQL("CREATE TABLE settings (paper TEXT PRIMARY KEY, new_count INTEGER NOT NULL, review_count INTEGER NOT NULL, next_index INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE attempts (id INTEGER PRIMARY KEY AUTOINCREMENT, paper TEXT NOT NULL, mode TEXT NOT NULL, completed INTEGER NOT NULL, total INTEGER NOT NULL, correct INTEGER NOT NULL, seconds INTEGER NOT NULL)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

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
    void setCount(String paper, String column, int count) {
        ensurePaper(paper); ContentValues v = new ContentValues(); v.put(column, count); getWritableDatabase().update("settings", v, "paper=?", new String[]{paper});
    }
    void advanceNew(String paper, int nextIndex) { ensurePaper(paper); ContentValues v = new ContentValues(); v.put("next_index", nextIndex); getWritableDatabase().update("settings", v, "paper=?", new String[]{paper}); }
    void record(String paper, int qid, boolean correct) {
        SQLiteDatabase db = getWritableDatabase(); ContentValues seed = new ContentValues(); seed.put("paper", paper); seed.put("qid", qid); db.insertWithOnConflict("state", null, seed, SQLiteDatabase.CONFLICT_IGNORE);
        db.execSQL("UPDATE state SET seen=1, correct=correct+?, wrong=wrong+?, last_seen=? WHERE paper=? AND qid=?", new Object[]{correct ? 1 : 0, correct ? 0 : 1, System.currentTimeMillis(), paper, qid});
    }
    List<Integer> reviewIds(String paper, int limit) { return ids("paper=? AND seen=1", new String[]{paper}, "wrong DESC, last_seen ASC", limit); }
    List<Integer> errorIds(String paper, int limit) { return ids("paper=? AND wrong>0", new String[]{paper}, "wrong DESC, last_seen ASC", limit); }
    int errorCount(String paper) { Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM state WHERE paper=? AND wrong>0", new String[]{paper}); try { return c.moveToFirst() ? c.getInt(0) : 0; } finally { c.close(); } }
    int todayCount(String paper) { long midnight = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L); Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM state WHERE paper=? AND last_seen>=?", new String[]{paper, String.valueOf(midnight)}); try { return c.moveToFirst() ? c.getInt(0) : 0; } finally { c.close(); } }
    private List<Integer> ids(String where, String[] args, String order, int limit) {
        ArrayList<Integer> out = new ArrayList<>(); Cursor c = getReadableDatabase().query("state", new String[]{"qid"}, where, args, null, null, order, String.valueOf(limit));
        try { while (c.moveToNext()) out.add(c.getInt(0)); } finally { c.close(); } return out;
    }
    void saveAttempt(String paper, String mode, int total, int correct, int seconds) { ContentValues v = new ContentValues(); v.put("paper", paper); v.put("mode", mode); v.put("completed", System.currentTimeMillis()); v.put("total", total); v.put("correct", correct); v.put("seconds", seconds); getWritableDatabase().insert("attempts", null, v); }
    List<String> history() {
        ArrayList<String> out = new ArrayList<>(); Cursor c = getReadableDatabase().query("attempts", new String[]{"paper","mode","total","correct","seconds","completed"}, null, null, null, null, "completed DESC", "50");
        try { while (c.moveToNext()) out.add(c.getString(0)+"  |  "+c.getString(1)+"  |  "+c.getInt(3)+"/"+c.getInt(2)+"  |  "+(c.getInt(4)/60)+" 分钟"); } finally { c.close(); } return out;
    }
}
