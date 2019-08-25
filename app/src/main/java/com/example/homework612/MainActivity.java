package com.example.homework612;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    SwipeRefreshLayout swipeRefresh;
    ListView listItems;

    private static final String KEY_NAME = "name";
    private static final String KEY_COUNT = "count";
    private static final String KEY_INDEX_REMOVE = "iRemove";
    public static final String APP_PREFERENCES = "listText";

    private String[] array_from = {KEY_NAME, KEY_COUNT};
    private int[] array_to = {R.id.text_desc, R.id.count};

    private List<Map<String, String>> list_content;
    private SharedPreferences mSettings;
    private SimpleAdapter newAdapter;
    private ArrayList<Integer> indexToRemove = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        listItems = findViewById(R.id.list);
        initPreference(); //заполняем при первом запуске массив значений из Preference
        initList();  //заполняем список элементов

        //обработка клика на элементе списка
        listItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                list_content.remove(i);
                newAdapter.notifyDataSetChanged(); //уведомляем адаптер, что данные изменены
                indexToRemove.add(i);//добавляем в список индекс элемента
            }
        });

        //обработка swipeRefresh
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initList();
                newAdapter.notifyDataSetChanged(); //уведомляем адаптер, что данные изменены
                swipeRefresh.setRefreshing(false); //убираем значек обновления
            }
        });
    }
    //добавляем в bundle данные
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(KEY_INDEX_REMOVE, indexToRemove);
    }
    //получаем данные bundle и обновляем адаптер
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        indexToRemove = savedInstanceState.getIntegerArrayList(KEY_INDEX_REMOVE);
        for (int i = 0; i < indexToRemove.size(); i++) {
            list_content.remove(indexToRemove.get(i).intValue());
        }
        newAdapter.notifyDataSetChanged();
    }

    private void initPreference() {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        Map<String, ?> result = mSettings.getAll();
        //если массив значений переменных пустой, то заполняем
        if (result.size() == 0) {
            SharedPreferences.Editor editor = mSettings.edit();
            String[] result_text = getString(R.string.large_text).split("\n\n");
            for (int i = 0; i < result_text.length; i++) {
                String setting_name = "text_" + i;
                if (!mSettings.contains(setting_name)) {
                    editor.putString(setting_name, result_text[i]);
                    editor.apply();
                }
            }
            editor.commit();
        }
    }

    private void initList() {
        list_content = prepareContent(); //заполняем данные списка значениями из preference
        newAdapter = createAdapter(list_content); //создаем адаптер
        listItems.setAdapter(newAdapter);
    }

    private SimpleAdapter createAdapter(List<Map<String, String>> content) {
        return new SimpleAdapter(this, content, R.layout.list_item, array_from, array_to);
    }

    private List<Map<String, String>> prepareContent() {
        List<Map<String, String>> listObject = new ArrayList();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        Map<String, ?> result = mSettings.getAll();
        for (int i = 0; i < result.size(); i++) {
            HashMap<String, String> resultItem = new HashMap();
            String setting_name = "text_" + i;
            String pref_value = (String) result.get(setting_name);
            int count = pref_value.length();
            resultItem.put(KEY_NAME, pref_value);
            resultItem.put(KEY_COUNT, Integer.toString(count));
            listObject.add(resultItem);
        }
        return listObject;
    }
}
