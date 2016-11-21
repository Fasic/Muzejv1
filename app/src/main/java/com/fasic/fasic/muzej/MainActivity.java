package com.fasic.fasic.muzej;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Locale;


public class MainActivity extends Activity {
    Button meni;
    Button izabranaZasatava;
    LinearLayout qr;
    ImageView info;
    String jezik = "sr";
    public static final String PREFS_NAME = "Fasic";
    Point point;
    PopupWindow changeStatusPopUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        jezik = settings.getString("jezik", "sr");

        meni = (Button) findViewById(R.id.srbija);
        izabranaZasatava = (Button) findViewById(R.id.engleska);


        qr =  (LinearLayout) findViewById(R.id.qr);
        info =  (ImageView) findViewById(R.id.info);


        if(jezik.equals("en")){
            setEng();
        }
        else{
            setSrb();
        }

        setFont();

        meni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                meni.getLocationOnScreen(location);
                Log.i("-->", "" + location[0] + " : " + location[1]);

                //Initialize the Point with x, and y positions
                point = new Point();
                point.x = location[0];
                point.y = location[1];
                showStatusPopup(MainActivity.this, point);

            }
        });

        /*izabranaZasatava.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setEng();
            }
        });*/

        qr.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                intent.putExtra("jezik", jezik);
                startActivityForResult(intent, 0);

                /*Intent intent = new Intent(MainActivity.this, Prikaz.class);
                intent.putExtra("id", 1);
                intent.putExtra("jezik", jezik);
                startActivityForResult(intent, 0);*/
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, info.class);
                intent.putExtra("jezik", jezik);
                startActivityForResult(intent, 0);
            }
        });


        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showStatusPopup(final Activity context, Point p) {

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.llSortChangePopup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.layoutpopup, null);
        //layout.setAnimation(AnimationUtils.loadAnimation(context, R.anim.myanim));
        //layout.setAnimation();
        // Creating the PopupWindow
        changeStatusPopUp = new PopupWindow(context);
        changeStatusPopUp.setContentView(layout);
        changeStatusPopUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        changeStatusPopUp.setFocusable(true);
        changeStatusPopUp.setAnimationStyle(R.style.Animation);
        // Some offset to align the popup a bit to the left, and a bit down, relative to button's position.
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px1 = (int) context.getResources().getDimension(R.dimen.zastava_visina);
        int px2 = (int) context.getResources().getDimension(R.dimen.zastava_margina);
        int OFFSET_X = 0 - 5;
        int OFFSET_Y = -px1 * 2 - 6 * px2;

        //Clear the default translucent background
        // changeStatusPopUp.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        changeStatusPopUp.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
    }

    public void dugmeInMenu(View v){
        if(v.getTag().equals("slika1")){
            setSrb();
        }else if(v.getTag().equals("slika2")){
            setEng();
        }else{
            setRus();
        }
        if (changeStatusPopUp.isShowing()) {
            changeStatusPopUp.dismiss();
        }

    }

    protected void onStop(){
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("jezik", jezik);
        editor.commit();
    }

    private void setSrb(){
        izabranaZasatava.setBackground(getResources().getDrawable(R.drawable.srboja));

        String s = ">Izaberite jezik\n Choose language\n Выбрать язык";
        meni.setText(s);
        meni.setTextSize(getResources().getDimension(R.dimen.jezik));

        jezik = "sr";
        setJezik(jezik);
    }

    private void setEng(){
        izabranaZasatava.setBackground(getResources().getDrawable(R.drawable.enboja));

        String s = "Izaberite jezik\n >Choose language\n Выбрать язык";
        meni.setText(s);
        meni.setTextSize(getResources().getDimension(R.dimen.jezik));

        jezik = "en";
        setJezik(jezik);
    }

    private void setRus(){
        izabranaZasatava.setBackground(getResources().getDrawable(R.drawable.ruboja));

        String s = "Izaberite jezik\n Choose language\n >Выбрать язык";
        meni.setText(s);
        meni.setTextSize(getResources().getDimension(R.dimen.jezik));

        jezik = "ru";
        setJezik(jezik);
    }

    private void setFont(){
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView naslovTB = (TextView) findViewById(R.id.textView);
        naslovTB.setTypeface(font);
        naslovTB = (TextView) findViewById(R.id.textView2);
        naslovTB.setTypeface(font);
    }

    protected void setJezik(String jezik){
        Locale locale;
        String drzava;

        if(jezik.equals("sr")){
            drzava = "RS";
        }
        else if(jezik.equals("en")){
            drzava = "US";
        }
        else{
            drzava = "RU";
        }

        locale = new Locale(jezik, drzava);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());



        TextView naslovTB = (TextView) findViewById(R.id.textView);
        naslovTB.setText(getResources().getString(R.string.naslov));
        naslovTB = (TextView) findViewById(R.id.textView2);
        naslovTB.setText(getResources().getString(R.string.qr_kod));
    }

}
