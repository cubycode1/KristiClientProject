package com.domain.gems;

/*-----------------------------------

    - Gems -

    created by cubycode ©2017
    All Rights reserved

-----------------------------------*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Identity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    /* Views */
    ImageView avatarImg;
    TextView usernTxt;
    Button spinner;


    /* Variables */
    List<ParseObject> gemsArray;
    MarshMallowPermission mmp = new MarshMallowPermission(this);
    Context ctx = this;
    private ArrayList<String> teamList = new ArrayList<>();
    private String selectedTeam = "";


    // ON START() ----------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        ParseUser currUser = ParseUser.getCurrentUser();

        int userPoints = currUser.getInt(Configs.USER_POINTS);
        int gemsCollected = currUser.getInt(Configs.USER_GEMS_COLLECTED);

        TextView statsTxt = findViewById(R.id.idenStatsTxt);
        // statsTxt.setTypeface(Configs.gayatri);
        statsTxt.setText("GEMS COLLECTED: " + String.valueOf(gemsCollected) + "\nPOINTS: " + String.valueOf(userPoints));


        // Call query
        queryCollectedGems();
    }


    // ON CREATE() ----------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Hide ActionBar
        getSupportActionBar().hide();


        // Init views
        avatarImg = findViewById(R.id.idenAvatarImg);
        usernTxt = findViewById(R.id.idenUsernameTxt);
        spinner = findViewById(R.id.spTeams);

        spinner.setOnClickListener(v -> {
            startActivity(new Intent(this, AllTeamsActivity.class));
        });


        // Init TabBar buttons
        Button tab_one = findViewById(R.id.tab_one);
        Button tab_two = findViewById(R.id.tab_two);
        Button tab_four = findViewById(R.id.tab_four);

        tab_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Identity.this, Home.class));
            }
        });

        tab_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Identity.this, Leaderboards.class));
            }
        });
        tab_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Identity.this, StatisticsActivity.class));
            }
        });


        // Call query
        showUserDetails();


        // MARK: - SHARE BUTTON ------------------------------------
        Button shareButt = findViewById(R.id.idenShareButt);
        //     shareButt.setTypeface(Configs.gayatri);
        shareButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Configs.playSound("click.mp3", false, ctx);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dismissKeyboard();

                if (!mmp.checkPermissionForWriteExternalStorage()) {
                    mmp.requestPermissionForWriteExternalStorage();
                } else {
                    ParseUser currUser = ParseUser.getCurrentUser();

                    Bitmap bitmap = BitmapFactory.decodeResource(Identity.this.getResources(), R.drawable.logo);
                    Uri uri = getImageUri(Identity.this, bitmap);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.putExtra(Intent.EXTRA_TEXT, "I've collected " + String.valueOf(currUser.getInt(Configs.USER_GEMS_COLLECTED)) + " Gems and earned " + String.valueOf(currUser.getInt(Configs.USER_POINTS)) + " Points on #" + getString(R.string.app_name));
                    startActivity(Intent.createChooser(intent, "Share on..."));
                }
            }
        });


        // MARK: - CHANGE AVATAR --------------------------------------------------------
        avatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Configs.playSound("click.mp3", false, ctx);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dismissKeyboard();

                AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
                alert.setTitle("SELECT SOURCE")
                        .setIcon(R.drawable.logo)
                        .setItems(new CharSequence[]{
                                        "Take a picture",
                                        "Pick from Gallery"},
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            // Open Camera
                                            case 0:
                                                if (!mmp.checkPermissionForCamera()) {
                                                    mmp.requestPermissionForCamera();
                                                } else {
                                                    openCamera();
                                                }
                                                break;

                                            // Open Gallery
                                            case 1:
                                                if (!mmp.checkPermissionForReadExternalStorage()) {
                                                    mmp.requestPermissionForReadExternalStorage();
                                                } else {
                                                    openGallery();
                                                }
                                                break;
                                        }
                                    }
                                })
                        .setNegativeButton("Cancel", null);
                alert.create().show();
            }
        });


        // MARK: - SAVE IDENTITY BUTTON ------------------------------------
        Button saveButt = findViewById(R.id.idenSaveButt);
        saveButt.setVisibility(View.VISIBLE);
        saveButt.setOnClickListener(view -> {
            try {
                Configs.playSound("click.mp3", false, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dismissKeyboard();

            if (!usernTxt.getText().toString().matches("")) {

                ParseUser currUser = ParseUser.getCurrentUser();
                Configs.showPD("PLEASE WAIT...", Identity.this);

                currUser.put(Configs.USER_USERNAME, usernTxt.getText().toString());
                currUser.put(Configs.TEAM_CATEGORY, selectedTeam);

                // Save image
                Bitmap bitmap = ((BitmapDrawable) avatarImg.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ParseFile imageFile = new ParseFile("avatar.jpg", byteArray);
                currUser.put(Configs.USER_AVATAR, imageFile);

                // Saving block
                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Configs.hidePD();
                            Configs.simpleAlert("Your Identity has been updated!", ctx);
                        } else {
                            Configs.hidePD();
                            Configs.simpleAlert(e.getMessage(), ctx);
                        }
                    }
                });


                // USERNAME IS REQUIRED!
            } else {
                Configs.simpleAlert("You must type a username!", ctx);
            }
        });


        // MARK: - LOGOUT BUTTON -------------------------------------------------
        Button logoutButt = findViewById(R.id.idenLogoutButt);
        logoutButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Configs.playSound("click.mp3", false, ctx);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dismissKeyboard();

                AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
                alert.setMessage("Are you sure you want to logout?")
                        .setTitle(R.string.app_name)
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Configs.showPD("Logging out...", Identity.this);

                                ParseUser.logOutInBackground(new LogOutCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Configs.hidePD();
                                        // Go Login activity
                                        startActivity(new Intent(Identity.this, Login.class));
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(R.drawable.logo);
                alert.create().show();

            }
        });


    }// end onCreate()


    // MARK: - SHOW USER DETAILS --------------------------------------------------------
    void showUserDetails() {
        ParseUser currentUser = ParseUser.getCurrentUser();

        //  usernTxt.setTypeface(Configs.gayatri);
        usernTxt.setText(currentUser.getString(Configs.USER_USERNAME));

        // Get avatar
        ParseFile fileObject = currentUser.getParseFile(Configs.USER_AVATAR);
        if (fileObject != null) {
            fileObject.getDataInBackground(new GetDataCallback() {
                public void done(byte[] data, ParseException error) {
                    if (error == null) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bmp != null) {
                            avatarImg.setImageBitmap(bmp);
                        }
                    }
                }
            });
        }
    }

//    private void getTeams() {
////        Configs.showPD("Please Wait...", Identity.this);
//        ParseQuery<ParseObject> query = ParseQuery.getQuery("Teams");
//        query.addAscendingOrder(Configs.TEAM_CATEGORY);
//        query.findInBackground((teams, e) -> {
//            if (e == null) {
//                //Object was successfully retrieved
//                for (ParseObject team : teams) {
//                    String name = team.getString(Configs.TEAM_CATEGORY);
//                    teamList.add(name);
//                }
//                ArrayAdapter aa = new ArrayAdapter(this, R.layout.spinner_item, teamList);
//                aa.setDropDownViewResource(android.R.layout.simple_list_item_1);
//                //Setting the ArrayAdapter data on the Spinner
//                spinner.setAdapter(aa);
//                spinner.setOnItemSelectedListener(this);
//                querySelectedTeam();
////                Configs.hidePD();
//            } else {
//                // something went wrong
////                Configs.hidePD();
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    // MARK: - QUERY GEMS COLLECTED ------------------------------------------------------------
    void queryCollectedGems() {
        Configs.showPD("LOADING...", Identity.this);

        ParseUser currUser = ParseUser.getCurrentUser();


        ParseQuery query = new ParseQuery(Configs.COLLECTED_CLASS_NAME);
        query.whereEqualTo(Configs.COLLECTED_USER_POINTER, currUser);
        query.setLimit(100000);
        query.orderByAscending(Configs.COLLECTED_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    gemsArray = objects;
                    Configs.hidePD();


                    // CUSTOM LIST ADAPTER
                    class ListAdapter extends BaseAdapter {
                        private Context context;

                        public ListAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @SuppressLint("SetTextI18n")
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                assert inflater != null;
                                cell = inflater.inflate(R.layout.cell_collected, null);
                            }

                            // Get Parse object
                            ParseObject collObj = gemsArray.get(position);

                            // Get Gem Name
                            TextView nameTxt = cell.findViewById(R.id.ccGemNameTxt);
                            //             nameTxt.setTypeface(Configs.gayatri);
                            nameTxt.setText(collObj.getString(Configs.COLLECTED_GEM_NAME));


                            // Get city/state
                            ParseGeoPoint gp = collObj.getParseGeoPoint(Configs.COLLECTED_GEM_LOCATION);
                            try {
                                Geocoder geocoder = new Geocoder(Identity.this, Locale.getDefault());
                                double lat = gp.getLatitude();
                                double lon = gp.getLongitude();
                                List<Address> addresses = null;
                                addresses = geocoder.getFromLocation(lat, lon, 1);
                                if (Geocoder.isPresent()) {
                                    Address returnAddress = addresses.get(0);
                                    String city = returnAddress.getLocality();
                                    String country = returnAddress.getCountryName();

                                    // Show Address
                                    TextView locTxt = cell.findViewById(R.id.ccGemLocationTxt);
                                    //     locTxt.setTypeface(Configs.gayatri);
                                    locTxt.setText("Collected in " + city + ", " + country);

                                } else {
                                    Toast.makeText(getApplicationContext(), "Geocoder not present!", Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }


                            // Get Gem Image
                            String gemName = collObj.getString(Configs.COLLECTED_GEM_NAME);
                            final int id = getResources().getIdentifier(gemName.toLowerCase(), "drawable", getPackageName());
                            ImageView gemImg = cell.findViewById(R.id.ccGemImg);
                            gemImg.setImageResource(id);


                            return cell;
                        }

                        @Override
                        public int getCount() {
                            return gemsArray.size();
                        }

                        @Override
                        public Object getItem(int position) {
                            return gemsArray.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }
                    }

                    // Init ListView and set its Adapter
                    ListView aList = (ListView) findViewById(R.id.idenGemsCollectedListView);
                    aList.setAdapter(new ListAdapter(Identity.this, gemsArray));

                    // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), ctx);
                }
            }
        });

    }


    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;
    File file;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }


    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }


    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;

            // Image from Camera
            if (requestCode == CAMERA) {

                try {
                    File f = file;
                    ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        angle = 90;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        angle = 180;
                    } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        angle = 270;
                    }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                } catch (IOException | OutOfMemoryError e) {
                    Log.i("log-", e.getMessage());
                }


                // Image from Gallery
            } else if (requestCode == GALLERY) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            // Set image
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(350, bm);
            avatarImg.setImageBitmap(scaledBm);
        }

    }
    //---------------------------------------------------------------------------------------------


    // Method to get URI of a stored image
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "image", null);
        return Uri.parse(path);
    }


    // MARK: - DISMISS KEYBOARD
    public void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernTxt.getWindowToken(), 0);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedTeam = teamList.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}//@end
