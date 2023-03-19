package hcmute.edu.vn.appplaymusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Song> songList;
    private ItemAdapter itemAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchView = findViewById(R.id.search_view);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        songList = new ArrayList<>();

        songList.add(new Song("Bones", "Imagine Dragons",R.drawable.img_music,R.raw.file_music));
        songList.add(new Song("LIKE THIS LIKE THAT", "TÓC TIÊN x TLINH",R.drawable.img_music2,R.raw.file_music));
        songList.add(new Song("CM1X - Nói Dối (Remix)", "Pháo ft. HIEUTHUHAIOFFICIAL",R.drawable.img_music3,R.raw.file_music3));
        songList.add(new Song("Có em (Feat. Low G) [Official MV]", "Madihu",R.drawable.img_music4,R.raw.file_music4));
        songList.add(new Song("Waiting For You", "MONO",R.drawable.img_music5,R.raw.file_music5));
        songList.add(new Song("Thích Quá Rùi Nà", "tlinh feat. Trung Trần",R.drawable.img_music6,R.raw.file_music6));
        songList.add(new Song("Bật Tình Yêu Lên", "Hòa Minzy x Tăng Duy Tân",R.drawable.img_music7,R.raw.file_music7));
        songList.add(new Song("Gặp May (Remix)", "Wren Evans, Masew, Sambi",R.drawable.img_music8,R.raw.file_music8));
        songList.add(new Song("BO XÌ BO (PAUSE PAUSE)", "Hoàng Thuỳ Linh",R.drawable.img_music9,R.raw.file_music9));
        songList.add(new Song("DON'T BREAK MY HEART", "BINZ x TOULIVER",R.drawable.img_music10,R.raw.file_music10));


        itemAdapter = new ItemAdapter(songList);
        recyclerView.setAdapter(itemAdapter);
    }

    private void filterList(String text) {

        List<Song> filteredList =  new ArrayList<>();
        for (Song song : songList){
            if(song.getTitle().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(song);
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
        }else{
            itemAdapter.setFilteredList(filteredList);
        }
    }
}