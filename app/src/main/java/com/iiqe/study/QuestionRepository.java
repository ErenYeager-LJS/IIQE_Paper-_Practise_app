package com.iiqe.study;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class QuestionRepository {
    static final class Question {
        final int id; final String stem; final String[] choices; final String answer;
        Question(int id, String stem, String[] choices, String answer) { this.id=id; this.stem=stem; this.choices=choices; this.answer=answer; }
    }
    private final Map<String, List<Question>> questions = new HashMap<>();
    private final Map<String, Map<Integer, Question>> byId = new HashMap<>();

    QuestionRepository(Context context) throws Exception {
        InputStream in = context.getAssets().open("questions.json"); ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[8192]; int n;
        while ((n = in.read(buffer)) >= 0) out.write(buffer, 0, n); in.close(); JSONObject root = new JSONObject(out.toString("UTF-8"));
        load("P1", root.getJSONArray("paper1")); load("P3", root.getJSONArray("paper3"));
    }
    private void load(String paper, JSONArray source) throws Exception {
        ArrayList<Question> list = new ArrayList<>(); HashMap<Integer, Question> index = new HashMap<>();
        for (int i=0; i<source.length(); i++) { JSONObject item=source.getJSONObject(i); JSONArray options=item.getJSONArray("options"); String[] values=new String[4]; for(int j=0;j<4;j++) values[j]=options.getString(j); Question q=new Question(item.getInt("id"), item.getString("stem"), values, item.getString("answer")); list.add(q); index.put(q.id,q); }
        questions.put(paper, list); byId.put(paper, index);
    }
    List<Question> all(String paper) { return questions.get(paper); }
    Question get(String paper, int id) { return byId.get(paper).get(id); }
    List<Question> fromIds(String paper, List<Integer> ids) { ArrayList<Question> out=new ArrayList<>(); for(int id:ids){Question q=get(paper,id); if(q!=null)out.add(q);} return out; }
    List<Question> random(String paper, int count) { ArrayList<Question> copy=new ArrayList<>(all(paper)); Collections.shuffle(copy); return new ArrayList<>(copy.subList(0, Math.min(count,copy.size()))); }
}
