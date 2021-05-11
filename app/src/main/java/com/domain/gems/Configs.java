package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.app.AlertDialog;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.io.IOException;
import java.util.Random;


public class Configs extends Application {



    // IMPORTANT: Reaplce the red strings below with your own Application ID and Client Key of your app on https://back4app.com
//    public static String PARSE_APP_ID = "OGFkSBZl68gRInEINoxKngV9LRpkKWPme97WUKKV";
//    public static String PARSE_CLIENT_KEY = "2NcNlYDVRDgrTSpXGDMsEL0p3UAEbNeibILpb3

    public static String PARSE_APP_ID = "czxtgUNbMDjrHAxBlf72s3QLwPnuYce1ubXH9erO";
    public static String PARSE_CLIENT_KEY = "bKv29SZuJekgJUsPzY2hZLRx8ONYog6e81qaEilI";
    //-----------------------------------------------------------------------------



    // Set fonts
    // (the font files are into app/src/main/assets/font folder)
    public static Typeface gayatri;




    // YOU CAN CHANGE THE 5 VALUE AS YOU WISH, IT'S THE AMOUNT OF NEW GEMS THAT GET RANDOMLY GENERATED
    public static Random r = new Random();
    public static int NEW_GEMS_TO_BE_GENERATED = r.nextInt(5);


    // THESE VALUES ARE THE MINIMUM AND MAXIMUM DISTANCES FROM YOUR CURRENT LOCATION WHERE THE APP WILL USE TO RANDOMLY GENERATE GEMS AROUND YOUR AREA
    // YOU CAN EDIT THEM AS YOU WISH, KEEP IN MIND THAT THEY ARE IN 'METERS'
    public static int MINIMUM_DISTANCE_FROM_YOU = 5;
    public static int MAXIMUM_DISTANCE_FROM_YOU = 2000;




    // IMPORTANT: THE STRINGS DECLARES IN THE ARRAY BELOW AND THE NAME OF THE IMAGES IN THE 'drawable' FOLDERS MUST EXACTLY MATCH EACH OTHER
    public static String [] gemNames = {
            "gigernaut",   // 10 points
            "khriptic",    // 20 points
            "morghon",     // 30 points
            "tremor",      // 40 points
            "walwaz",      // 50 points
            "yalowa",      // 100 points
    };




    // YOU CAN EDIT THE POINTS BELOW AS YOU WISH, JUST NOTE THE COMMENTS THAT SHOWS WHICH GEM THE POINTS ARE ABOUT
    public static Integer[] gemPoints = {
            10,  // gigernaut
            20,  // khriptic
            30,  // morghon
            40,  // tremor
            50,  // walwaz
            100, // yalowa
    };



    // YOU CAN CHANGE THIS ZOOM VALUE AS YOU WISH, IT'S FOR THE INITIAL ZOOM OF THE MAP IN THE HOME SCREEN
    public static float mapZoom = (float) 12.0;








    /************ DO NOT EDIT THE CODE BELOW ***********/

    public static String USER_CLASS_NAME = "_User";
    public static String USER_USERNAME = "username";
    public static String USER_AVATAR = "avatar";
    public static String USER_GEMS_COLLECTED = "gemsCollected";
    public static String USER_POINTS = "points";
    public static String USER_CURRENT_LOCATION = "currentLocation";

    public static String GEMS_CLASS_NAME = "Gems";
    public static String GEMS_GEM_NAME = "name";
    public static String GEMS_GEM_LOCATION = "location";
    public static String GEMS_GEM_POINTS = "points";

    public static String COLLECTED_CLASS_NAME = "GemsCollected";
    public static String COLLECTED_USER_POINTER = "userPointer";
    public static String COLLECTED_GEM_NAME = "gemName";
    public static String COLLECTED_GEM_LOCATION = "gemLocation";
    public static String COLLECTED_CREATED_AT = "createdAt";
    public static String TEAM_CATEGORY = "team_category";
    public static String QUIZ_LAT_LNG = "quizlatlng";
    public static String QUIZ_NAME = "quizName";
    public static String STUDENT_TEAM_ID = "student_team_id";
    public static String QUIZ_SCORE = "quizScore";

    public static String QUIZ_CLASS_NAME = "Questions";
    public static String QUIZ_QUESTION = "question";
    public static String QUIZ_OPTIONS = "options";
    public static String QUIZ_CORRECT_ANSWERS = "answers";
    public static String QUIZ_AVATAR = "quiz_avatar";

    public static String COMMENTS_CLASS_NAME = "Comments";
    public static String COMMENTS_FK_USER_ID = "fk_user_id";
    public static String COMMENTS_QUESTION_ID = "question_id";
    public static String COMMENTS_COMMENT = "comment";
    public static String COMMENTS_CREATED_AT = "createdAt";
    public static String COMMENTS_UPDATED_AT = "updatedAt";
    public static String COMMENTS_STATUS = "comment_status";


    public static int newGems = 0;
    public static MediaPlayer mp;


    boolean isParseInitialized = false;


    public void onCreate() {
        super.onCreate();


        // Init Parse
        if (!isParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_ID))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;

            // Init Facebook Utils
            ParseFacebookUtils.initialize(this);
        }


        // Init font
        gayatri = Typeface.createFromAsset(getAssets(),"font/mont_demo_extra_light.otf");

    }// end oncreate()

    // MARK: - CUSTOM PROGRESS DIALOG -----------
    public static AlertDialog pd;
    public static void showPD(String mess, Context ctx) {
        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View dialogView = inflater.inflate(R.layout.pd, null);
        TextView messTxt = dialogView.findViewById(R.id.pdMessTxt);
        messTxt.setText(mess);
        db.setView(dialogView);
        db.setCancelable(true);
        pd = db.create();
        pd.show();
    }
    public static void hidePD(){ pd.dismiss(); }

    // MARK: - SIMPLE ALERT ----------------------------------------------------------------
    public static void simpleAlert(String mess, Context ctx) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setMessage(mess)
            .setTitle(R.string.app_name)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.main_app_logo);
        alert.create().show();
    }




    // MARK: - SCALE BITMAP TO MAX SIZE ------------------------------------------------------------
    public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        return Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
    }




    // MARK: - PLAY A SOUND -----------------------------------------------------------------------
    public static void playSound(String filename, boolean looping, Context ctx) throws IOException {
        AssetFileDescriptor afd = ctx.getAssets().openFd(filename);
        MediaPlayer player = new MediaPlayer();
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        player.setLooping(looping);
        player.prepare();
        player.start();
    }


}//@end



