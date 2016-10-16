package com.fasic.fasic.muzej;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;


public class oAktivnost extends Activity implements OnMapReadyCallback {
    protected String jezik;
    LatLng lokacija;
    int zoom = 14;
    String title = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        jezik = intent.getStringExtra("jezik");
        String oCemu = intent.getStringExtra("oCemu");
        setJezik(jezik);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.o_aktivnost);


        ImageView logo = (ImageView) findViewById(R.id.logoID);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ImageView slika = (ImageView) findViewById(R.id.imageView);
        //ViewGroup.LayoutParams params = slika.getLayoutParams();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        slika.requestLayout();
        slika.getLayoutParams().height = (width/16) * 9;



        if(oCemu.equals( "oMuzeju")) setOmuzeju();
        else if(oCemu.equals("oPecini")) setOpecini();
        else if(oCemu.equals("oOrascu")) setOorascu();
        else if(oCemu.equals("oKuci")) setOkuci();
    }

    protected void setJezik(String jezik){
        Locale locale;
        String drzava;

        if(jezik.equals("sr")){
            Log.i("-->", "sr");
            drzava = "RS";
        }
        else{
            Log.i("-->","en");
            drzava = "US";
        }

        locale = new Locale(jezik, drzava);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    protected void setOmuzeju(){
        TextView header = (TextView) findViewById(R.id.headerID);
        header.setText(getResources().getString(R.string.oMuzeju));
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView opis1 = (TextView) findViewById(R.id.opis1OmuzejuID);
        opis1.setText(getResources().getString(R.string.opis1Omuzeju));
        opis1.setTypeface(font);

        TextView opis2 = (TextView) findViewById(R.id.opis2OmuzejuID);
        opis2.setText(getResources().getString(R.string.opis2Omuzeju));
        opis2.setTypeface(font);
        header.setTypeface(font);

        TextView lokacijaTxt = (TextView) findViewById(R.id.lokacijaID);
        lokacijaTxt.setText(getResources().getString(R.string.lokacijaMuzej));
        lokacijaTxt.setTypeface(font);

        ImageView slika = (ImageView) findViewById(R.id.imageView);
        slika.setImageDrawable(getResources().getDrawable(R.drawable.omuzeju));

        lokacija = new LatLng(44.3045032,20.5465969);
        title = (getResources().getString(R.string.narodniMuzej));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    protected void setOpecini(){
        TextView header = (TextView) findViewById(R.id.headerID);
        header.setText(getResources().getString(R.string.oPecini));
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView opis1 = (TextView) findViewById(R.id.opis1OmuzejuID);
        opis1.setText(getResources().getString(R.string.opis1Opecini));
        opis1.setTypeface(font);

        TextView opis2 = (TextView) findViewById(R.id.opis2OmuzejuID);
        opis2.setText(getResources().getString(R.string.opis2Opecini));
        opis2.setTypeface(font);
        header.setTypeface(font);

        ImageView slika = (ImageView) findViewById(R.id.imageView);
        slika.setImageDrawable(getResources().getDrawable(R.drawable.opecini));

        TextView lokacijaTxt = (TextView) findViewById(R.id.lokacijaID);
        lokacijaTxt.setText(getResources().getString(R.string.LokacijaPecine));
        lokacijaTxt.setTypeface(font);

        lokacija = new LatLng(44.3020298,20.5825991);
        title = (getResources().getString(R.string.risovackaPecina));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void setOorascu(){
        TextView header = (TextView) findViewById(R.id.headerID);
        header.setText(getResources().getString(R.string.oOrascu));
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView opis1 = (TextView) findViewById(R.id.opis1OmuzejuID);
        opis1.setText(getResources().getString(R.string.opis1Oorascu));
        opis1.setTypeface(font);

        TextView opis2 = (TextView) findViewById(R.id.opis2OmuzejuID);
        opis2.setText(getResources().getString(R.string.opis2Oorascu));
        opis2.setTypeface(font);
        header.setTypeface(font);

        ImageView slika = (ImageView) findViewById(R.id.imageView);
        slika.setImageDrawable(getResources().getDrawable(R.drawable.oorascu));

        TextView lokacijaTxt = (TextView) findViewById(R.id.lokacijaID);
        lokacijaTxt.setText(getResources().getString(R.string.lokacijaOrasac));
        lokacijaTxt.setTypeface(font);

        lokacija = new LatLng(44.3332227,20.5843301);
        zoom = 12;
        title = (getResources().getString(R.string.orasacJaruga));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void setOkuci(){
        TextView header = (TextView) findViewById(R.id.headerID);
        header.setText(getResources().getString(R.string.oKuci));
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView opis1 = (TextView) findViewById(R.id.opis1OmuzejuID);
        opis1.setText(getResources().getString(R.string.opis1Okuci));
        opis1.setTypeface(font);

        TextView opis2 = (TextView) findViewById(R.id.opis2OmuzejuID);
        opis2.setText(getResources().getString(R.string.opis2Okuci));
        opis2.setTypeface(font);
        header.setTypeface(font);

        ImageView slika = (ImageView) findViewById(R.id.imageView);
        slika.setImageDrawable(getResources().getDrawable(R.drawable.okucitxt));

        TextView lokacijaTxt = (TextView) findViewById(R.id.lokacijaID);
        lokacijaTxt.setText(getResources().getString(R.string.lokacijaKuce));
        lokacijaTxt.setTypeface(font);

        lokacija = new LatLng(44.3040943,20.5820054);
        title = (getResources().getString(R.string.kucaMiloevic));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lokacija, zoom));

        map.addMarker(new MarkerOptions()
                .title(title) //i ovo ide u string u zavisnosti od objekta bla bla
                .position(lokacija));
    }
}
