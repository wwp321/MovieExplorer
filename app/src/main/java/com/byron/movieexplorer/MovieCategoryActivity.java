package com.byron.movieexplorer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class MovieCategoryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final int LASTEST_MOVIE = 0;
    final int CHINA_MOVIE = 1;
    final int JAPAN_KOREA_MOVIE = 2;
    final int AMERICA_EURPO_MOVIE = 3;

    static final int MOVIE_ITEM_INSERTED = 1;

    int pageIndex = 1;
    String currentLink;

    List<String> movieCategoryList = new ArrayList<>();
    List<MovieItem> movieItemList = new ArrayList<>();

    @BindView(R.id.movie_list_recyclerview)
    RecyclerView movieListRecyclerview;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MOVIE_ITEM_INSERTED:
                    movieListRecyclerview.getAdapter().notifyItemInserted(movieItemList.size() - 1);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_category);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initRecyclerView();

        initMovieCategory();
        currentLink = movieCategoryList.get(0);
        getSupportActionBar().setTitle(R.string.lastest_movie);

        getMovieList(currentLink, pageIndex);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        movieListRecyclerview.setLayoutManager(layoutManager);

        MovieItemAdapter adapter = new MovieItemAdapter(movieItemList);
        movieListRecyclerview.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.movie_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String link = null;
        int actionBarTitle = 0;
        if (id == R.id.nav_lastest) {
            // Handle the camera action
            link = movieCategoryList.get(LASTEST_MOVIE);
            actionBarTitle = R.string.lastest_movie;
        } else if (id == R.id.nav_rihan) {
            link = movieCategoryList.get(JAPAN_KOREA_MOVIE);
            actionBarTitle = R.string.rihan_movie;
        } else if (id == R.id.nav_guonei) {
            link = movieCategoryList.get(CHINA_MOVIE);
            actionBarTitle = R.string.guonei_movie;
        } else if (id == R.id.nav_oumei) {
            link = movieCategoryList.get(AMERICA_EURPO_MOVIE);
            actionBarTitle = R.string.oumei_movie;
        }

        if(link != null) {
            currentLink = link;

            if(0 != actionBarTitle)
                getSupportActionBar().setTitle(actionBarTitle);
            pageIndex = 1;
            getMovieList(link, pageIndex);
        }else {
            Toast.makeText(this, "请选择正确的电影类别", Toast.LENGTH_SHORT).show();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getMovieList(String link, int page) {
        String linkTmp;
        if(link.contains("china")) {
            if(page == 1) {
                linkTmp = link + "index.html";
            } else {
                linkTmp = link + "list_4_" + page + ".html";
            }
        } else {
            linkTmp = link + page + ".html";
        }
        final String pageLink = linkTmp;

        int size = movieItemList.size();
        movieItemList.clear();
        movieListRecyclerview.getAdapter().notifyItemRangeRemoved(0, size);

        Timber.d("Get movie link:" + pageLink);
        Observable<MovieItem> observable = Observable.create(
                new ObservableOnSubscribe<MovieItem>() {
                    @Override
                    public void subscribe(ObservableEmitter<MovieItem> emitter) throws Exception {
                        Document document = Jsoup.connect(pageLink).get();

                        Elements movies = document.select("div.co_content8 ul table");

                        for (Element movie : movies) {
                            try{
                                Elements Titleelements = movie.select("a");
                                String title = Titleelements.get(Titleelements.size() - 1).text();
                                String link = Titleelements.get(Titleelements.size() - 1).attr("href");
                                String date = movie.selectFirst("font").text();
                                Elements descElements = movie.select("td");
                                String description = descElements.get(descElements.size() - 1).text();

                                MovieItem item = new MovieItem(title, date, description, link);
                                emitter.onNext(item);
                            }catch (Exception e) {

                            }

                        }

                        emitter.onComplete();
                    }
                }
        );

        Observer<MovieItem> observer = new Observer<MovieItem>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(MovieItem movieItem) {
                movieItemList.add(movieItem);
                Message message = new Message();
                message.what = MOVIE_ITEM_INSERTED;
                handler.sendMessage(message);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                Timber.d("complete");
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(observer);
    }

    void initMovieCategory() {
        //zuixin
        movieCategoryList.add("http://www.ygdy8.net/html/gndy/dyzz/list_23_");
        //guonei
        movieCategoryList.add("http://www.ygdy8.net/html/gndy/china/");
        //rihan
        movieCategoryList.add("http://www.ygdy8.net/html/gndy/rihan/list_6_");
        //oumei
        movieCategoryList.add("http://www.ygdy8.net/html/gndy/oumei/list_7_");
    }

    @OnClick(R.id.page_next)
    void onNextPageClick() {
        pageIndex ++;
        getMovieList(currentLink, pageIndex);
    }

    @OnClick(R.id.page_pre)
    void onPrePageClick() {
        if(pageIndex > 1)
            pageIndex --;

        getMovieList(currentLink, pageIndex);
    }
}
