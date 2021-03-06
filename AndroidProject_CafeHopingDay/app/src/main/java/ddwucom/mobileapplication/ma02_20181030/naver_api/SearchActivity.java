package ddwucom.mobileapplication.ma02_20181030.naver_api;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import ddwucom.mobileapplication.ma02_20181030.R;

public class SearchActivity extends AppCompatActivity {

    public static final String TAG = "RestaurantActivity";

    EditText etTarget;
    ListView lvList;
    String apiAddress;

    String query;
    MyAdapter adapter;
    ArrayList<NaverSearchDto> resultList;
    NaverSearchXmlParser parser;
    NaverNetworkManager networkManager;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        etTarget = findViewById(R.id.etTarget);
        lvList = findViewById(R.id.lvList);

        resultList = new ArrayList();
//        adapter = new ArrayAdapter<NaverBookDto>(this, android.R.layout.simple_list_item_1, resultList);
        adapter = new MyAdapter(this,R.layout.search_item, resultList);
        lvList.setAdapter(adapter);

        apiAddress = getResources().getString(R.string.api_url);
        parser = new NaverSearchXmlParser();
        networkManager = new NaverNetworkManager(this);
        networkManager.setClientId(getResources().getString(R.string.client_id));
        networkManager.setClientSecret(getResources().getString(R.string.client_secret));

        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
                Log.d(TAG + "Pos", String.valueOf(pos));
                NaverSearchDto restaruantDto = resultList.get(pos);
                Log.d(TAG,restaruantDto.getTitle());
                if(restaruantDto.getLink() != "?????? ??????") {
                    String link = restaruantDto.getLink();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(intent);
                }
            }
        });
    }
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnLookup:
                query = etTarget.getText().toString();
                Log.d(TAG, query);
                new NaverAsyncTask().execute(apiAddress, query);
                break;
        }
    }

    class NaverAsyncTask extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(SearchActivity.this, "Wait", "Downloading...");
        }

        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            String query = strings[1];

            String apiURL = null;
            try {
                apiURL = address + URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String result = networkManager.downloadContents(apiURL);
            Log.d(TAG,result);
            return result;//xml??? ??? ??????
        }
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);
            progressDlg.dismiss();
            //xml????????? ????????? parsedList?????? ??????
            ArrayList<NaverSearchDto> parseredList = parser.parse(result);     // ??????API ????????? ?????? ??????
            if (parseredList.size() == 0) {
                Toast.makeText(SearchActivity.this, "No data!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG + "List", String.valueOf(parseredList.get(0)));
                resultList = parseredList;
                adapter.setList(resultList);    // Adapter ??? ?????? List ??? ?????? ??? notify
                //setList????????? notifySetChanged()??????! ????????? ??? resultList??? ?????????
                progressDlg.dismiss();
            }
        }
    }

}