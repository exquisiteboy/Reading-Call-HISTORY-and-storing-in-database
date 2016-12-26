package com.example.androidreadcallhistory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity implements MissedCallsFragment.OnFragmentInteractionListener {
    TextView textView = null;
    ViewPager viewPager;
    TabLayout tabLayout;
    ViewPagerAdapter viewPagerAdapter;
    String[] titles = {"Incoming", "Outgoing", "Missed"};
    StringBuffer sbMissed;
    StringBuffer sbOutgoing;
    StringBuffer sbIncoming;
    ArrayList<Model> modelArrayList = null;
    SqliteDatabaseHelper databaseHelper;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        modelArrayList = new ArrayList();
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        databaseHelper = new SqliteDatabaseHelper(this);
        getCallDetails();

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        new InsertDataToDB().execute();

    }

    private static void fileCopyUsingNIOChannelClass(String source, String destination) throws IOException {
        File fileToCopy = new File(source);
        FileInputStream inputStream = new FileInputStream(fileToCopy);
        FileChannel inChannel = inputStream.getChannel();

        File newFile = new File(destination + "CallsDB.db");
        if (newFile.exists())
            newFile.delete();
        FileOutputStream outputStream = new FileOutputStream(newFile);
        FileChannel outChannel = outputStream.getChannel();

        inChannel.transferTo(0, fileToCopy.length(), outChannel);

        inputStream.close();
        outputStream.close();
    }

    private void getCallDetails() {
        sbMissed = new StringBuffer();
        sbOutgoing = new StringBuffer();
        sbIncoming = new StringBuffer();

        String strOrder = CallLog.Calls.DATE + " DESC";
        /* Query the CallLog Content Provider */
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                null, null, strOrder);
        int id = managedCursor.getColumnIndex(CallLog.Calls._ID);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        sbMissed.append("Call Log :");
        Model model = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");

        while (managedCursor.moveToNext()) {

            int logID = managedCursor.getInt(id);
            String phNum = managedCursor.getString(number);
            String callTypeCode = managedCursor.getString(type);
            String strcallDate = managedCursor.getString(date);
            Date callDate = new Date(Long.valueOf(strcallDate));
            String callDuration = managedCursor.getString(duration);
            model = new Model();
            model.setLogId(logID);
            model.setNumber(phNum);
            model.setDate(sdf.format(callDate));
            model.setDuration(callDuration);

            String callType = null;
            int callcode = Integer.parseInt(callTypeCode);
            switch (callcode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    callType = "Outgoing";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    callType = "Incoming";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    callType = "Missed";
                    break;
            }
            if (callType != null) {
                if (callType.equals("Outgoing")) {
                    model.setType("Outgoing");
                    sbOutgoing.append("\nPhone Number:--- " + phNum + " \nCall Type:--- "
                            + callType + " \nCall Date:--- " + callDate
                            + " \nCall duration in sec :--- " + callDuration);
                    sbOutgoing.append("\n----------------------------------");
                } else if (callType.equals("Incoming")) {
                    model.setType("Incoming");
                    sbIncoming.append("\nPhone Number:--- " + phNum + " \nCall Type:--- "
                            + callType + " \nCall Date:--- " + callDate
                            + " \nCall duration in sec :--- " + callDuration);
                    sbIncoming.append("\n----------------------------------");
                } else {
                    model.setType("Missed");
                    sbMissed.append("\nPhone Number:--- " + phNum + " \nCall Type:--- "
                            + callType + " \nCall Date:--- " + callDate
                            + " \nCall duration in sec :--- " + callDuration);
                    sbMissed.append("\n----------------------------------");
                }
                modelArrayList.add(model);
            }
        }
        managedCursor.close();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MissedCallsFragment().newInstance("", sbIncoming.toString());
                case 1:
                    return new MissedCallsFragment().newInstance("", sbOutgoing.toString());
                case 2:
                    return new MissedCallsFragment().newInstance("", sbMissed.toString());
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private class InsertDataToDB extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            pDialog.setMessage("Wait...");
            showDialog();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            databaseHelper.insertCallLog(modelArrayList);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            String myFilePath = null, myFilePath2 = null;
            try {
                myFilePath = "//data//data//" + getPackageName() + "//databases//" + "CallsDB.db";
//            myFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FolderDB/abc.txt";
                myFilePath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/FolderDB/";

                File myFile = new File(myFilePath2);
                if (myFile != null) {
                    if (!myFile.exists())
                        myFile.mkdirs();
                }
            /*  FileOutputStream fOut = new FileOutputStream(myFilePath);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append("lsdjfj");
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getBaseContext(),
                    "Done writing SD 'mysdfile.txt'",
                    Toast.LENGTH_SHORT).show();*/
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            try {
                fileCopyUsingNIOChannelClass(myFilePath, myFilePath2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            hideDialog();

            super.onPostExecute(aVoid);
        }
    }

}
