package com.byron.movieexplorer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.net.URL;

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

public class MovieDetailActivity extends AppCompatActivity {

    public static final String MOVIE_DETAIL_LINK = "movie_detail_link";
    public static final String MOVIE_DETAIL_TITLE = "movie_detail_title";

    @BindView(R.id.movie_title)
    TextView movieTitle;

    @BindView(R.id.movie_thumbnail)
    ImageView movieThumbnail;

    @BindView(R.id.movie_detail)
    TextView movieDetail;

    @BindView(R.id.download_link)
    TextView downloadLink;

    @BindView(R.id.download_btn)
    Button downloadBtn;

    @BindView(R.id.movie_capture)
    ImageView movieCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);


        String link = getIntent().getStringExtra(MOVIE_DETAIL_LINK);

        String title = getIntent().getStringExtra(MOVIE_DETAIL_TITLE);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(title);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getMovieDetail(link);
    }

    private void getMovieDetail(String link) {
        final String fullLink = "http://www.ygdy8.net" + link;
        Timber.d("movie link:" + fullLink);

        Observable<MovieDetailItem> observable = Observable.create(
                new ObservableOnSubscribe<MovieDetailItem>() {
                    @Override
                    public void subscribe(ObservableEmitter<MovieDetailItem> emitter) throws Exception {

                        Document document = Jsoup.connect(fullLink).get();

                        String title = document.selectFirst("div.title_all font").text();
                        Elements elements = document.select("div#Zoom p img");
                        String thumbnail = null;
                        String capture = null;
                        if(elements.size() > 0) {
                            thumbnail = elements.get(0).attr("src");
                            capture = elements.get(elements.size() - 1).attr("src");
                        }


//                        String html = document.selectFirst("div#Zoom").html();
//                        Document subDoc = Jsoup.parseBodyFragment(html);
//
//                        Elements imgs = subDoc.select("img");
//
//                        for (Element img : imgs) {
//                            img.replaceWith(new TextNode( ""));
//                        }
//
//                        Timber.d("dada:" + subDoc.html().replace("<br>", "\n"));
//
//                        String detail = subDoc.html().replace("<br>", "\n");
                        String allDetail = document.selectFirst("div#Zoom").outerHtml();
                        int firstImg = allDetail.indexOf("alt=") + "alt=\"\">".length();
                        int lastImg = allDetail.lastIndexOf("<img");
                        String detail;
                        try{
                            detail = allDetail.substring(firstImg, lastImg).replace("<br>", "\n");
                        } catch (Exception e) {
                            detail = allDetail.replace("<br>", "\n");
                        }

                        Timber.d(detail);
                        String download = document.selectFirst("div#Zoom table a").text();

                        MovieDetailItem item = new MovieDetailItem(title, thumbnail, detail, download, capture);
                        Timber.d("on next");
                        emitter.onNext(item);
                        emitter.onComplete();
                    }
                }
        );

        Observer<MovieDetailItem> observer = new Observer<MovieDetailItem>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(final MovieDetailItem movieDetailItem) {
                Timber.d("Got movie detail:");
                Timber.d(movieDetailItem.getTitle());
                Timber.d(movieDetailItem.getThumbnailLink());
//                Timber.d(movieDetailItem.getTitleDetail());
                Timber.d(movieDetailItem.getDownloadLink());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        movieTitle.setText(movieDetailItem.getTitle());
                        if(movieDetailItem.getThumbnailLink() != null) {
                            Picasso.with(MyApplication.getContext())
                                    .load(movieDetailItem.getThumbnailLink())
                                    .error(R.drawable.blank_thumbnail)
                                    .into(movieThumbnail);
                        } else {
                            Picasso.with(MyApplication.getContext())
                                    .load(R.drawable.blank_thumbnail)
                                    .into(movieThumbnail);
                        }

                        if(movieDetailItem.getCapture() != null) {
                            Picasso.with(MyApplication.getContext())
                                    .load(movieDetailItem.getCapture())
                                    .error(R.drawable.blank_thumbnail)
                                    .into(movieCapture);
                        } else {
                            Picasso.with(MyApplication.getContext())
                                    .load(R.drawable.blank_thumbnail)
                                    .into(movieCapture);
                        }

                        movieDetail.setText(movieDetailItem.getTitleDetail());
                        downloadLink.setText(movieDetailItem.getDownloadLink());
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(observer);
    }

    @OnClick(R.id.download_btn)
    void onDownloadClick() {
        String link = "AA" + downloadLink.getText().toString() + "ZZ";

        String base64Token = Base64.encodeToString(link.getBytes(), Base64.DEFAULT);
        String thunderLink = "thunder://" + base64Token;

        Timber.d("Thunder  download link:" + thunderLink);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(thunderLink));
        intent.addCategory("android.intent.category.DEFAULT");
        try {
            startActivity(intent);
        }catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(this, "请在手机上安装迅雷后使用本软件，或者等待本软件的后续更新", Toast.LENGTH_SHORT).show();
        }
    }
}
