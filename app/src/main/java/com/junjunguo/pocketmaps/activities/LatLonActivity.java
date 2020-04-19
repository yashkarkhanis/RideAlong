package com.junjunguo.pocketmaps.activities;

import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.junjunguo.pocketmaps.R;
import com.junjunguo.pocketmaps.model.listeners.OnClickAddressListener;

import java.util.Locale;

public class LatLonActivity  extends AppCompatActivity implements OnClickListener
{
  private static OnClickAddressListener callbackListener;
  EditText txtLat;
  EditText txtLon;
  Button okButton;
  
  /** Set pre-settings.
   *  @param newCallbackListener The Callback listener, called on selected Address. **/
  public static void setPre(OnClickAddressListener newCallbackListener)
  {
    callbackListener = newCallbackListener;
  }
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_latlon);
    okButton = (Button) findViewById(R.id.latLonEnterOk);
    txtLat = (EditText) findViewById(R.id.latLocationEnter);
    txtLon = (EditText) findViewById(R.id.lonLocationEnter);
    okButton.setOnClickListener(this);
  }
  
  @Override protected void onResume()
  {
    super.onResume();
  }
  
  @Override protected void onDestroy()
  {
    super.onDestroy();
  }
  
  @Override
  public void onClick(View v)
  {
    if (v.getId()==R.id.latLonEnterOk)
    {
      log("Selected: Ok for LatLon");
      double lat = 0; 
      double lon = 0; 
      try
      {
        lat = Double.parseDouble(txtLat.getText().toString());
        lon = Double.parseDouble(txtLon.getText().toString());
      }
      catch (NumberFormatException e)
      {
        logUser("Nuber format error! Enter number as 111.123");
        return;
      }
      if (lat>90)
      {
        logUser("Latitude must not be more than 90");
        return;
      }
      if (lat<-90)
      {
        logUser("Latitude must not be less than -90");
        return;
      }
      if (lon>180)
      {
        logUser("Longitude must not be more than 180");
        return;
      }
      if (lon<-180)
      {
        logUser("Longitude must not be less than -180");
        return;
      }
      Address address = new Address(Locale.GERMANY);
      address.setLatitude(lat);
      address.setLongitude(lon);
      address.setAddressLine(0, "" + lat + ", " + lon);
      LatLonActivity.this.finish();
      callbackListener.onClick(address);
    }
  }
  
  private void log(String str)
  {
    Log.i(LatLonActivity.class.getName(), str);
  }
    
  private void logUser(String str)
  {
    Log.i(LatLonActivity.class.getName(), str);
    try
    {
      Toast.makeText(this.getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }
    catch (Exception e) { e.printStackTrace(); }
  }
}

