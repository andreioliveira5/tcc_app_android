package andrei.com.br.projetobluetooth;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Set;

public class listaDispositivos extends ListActivity {


    private BluetoothAdapter bluetoothAdapter = null;
    static String ENDEREÇO_MAC= null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceMACAddress = device.getAddress(); // MAC address
                ArrayBluetooth.add(deviceName+ "\n" + deviceMACAddress);
            }
        }
        setListAdapter(ArrayBluetooth);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String selecionado = ((TextView) v).getText().toString();
        String Mac =  selecionado.substring(selecionado.length()-17);
        Intent returnMac  = new Intent();
        returnMac.putExtra(ENDEREÇO_MAC, Mac);
        setResult(RESULT_OK, returnMac);
        finish();
    }
}
