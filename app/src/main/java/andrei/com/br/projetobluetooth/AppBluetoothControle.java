package andrei.com.br.projetobluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class AppBluetoothControle extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static int REQUEST_OK = 0;
    private static final int REQUEST_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;
    ConnectedThread ConnectedThread;

    private static Handler handler;
    StringBuilder dadosBluetooth = new StringBuilder();

    BluetoothAdapter bluetoothAdapter = null;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket bluetoothSocket = null;
    boolean conexao = false;
    private static String Mac = null;
    UUID bluetooth_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private String bat ="0%";
    private String dist="0m";
    private Button botaoconexao;
    private TextView bateria;
    private TextView distancia;
    private Button botaoatualiza;
    private Switch switchonof;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*--- Parametros para retirar a title bar ---*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main_layout_v21);

        bluetoothIsEnableTest();
        inicializaCompontentesView();
        alterarTextView();
        configuraBotaoConexao();
        configuraBotaoAtualizar();
        configuraSwitchOnOF();
        configuraHandler();
    }

    @SuppressLint("HandlerLeak")
    private void configuraHandler() {
        handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_READ){
                    String recebido = (String) msg.obj;
                    dadosBluetooth.append(recebido);
                    testaDados();
                    dadosBluetooth.delete(0, dadosBluetooth.length());
                }
            }
        };
    }

    private void testaDados() {
        int fimInfo = dadosBluetooth.indexOf("}");
        if(fimInfo > 0){
            String dadosOK = dadosBluetooth.substring(0, fimInfo);
            int tamanho = dadosOK.length();
            if( dadosBluetooth.charAt(0) == '{'){
                trataDosDadosLidos(tamanho);
            }
        }
    }

    private void trataDosDadosLidos(int tamanho) {
        String dadosFinais = dadosBluetooth.substring(1, tamanho);
        String removedLineBreak = dadosFinais.replaceAll("\\r\\n", "");
        String[] separados = removedLineBreak.split(";");
        dist = separados[1];
        bat = separados[0];
        alterarTextView();
    }

    private void configuraSwitchOnOF() {
        switchonof.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    ConnectedThread.write("1");
                }else{
                    ConnectedThread.write("0");
                }
            }
        });
    }

    private void configuraBotaoAtualizar() {
        botaoatualiza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    ConnectedThread.write("2");
                }else {
                    Toast.makeText(AppBluetoothControle.this, "Falha ao atualizar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void configuraBotaoConexao() {
        botaoconexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothIsEnableTest();
                testeBotaoConexao();
            }
        });
    }

    private void testeBotaoConexao() {
        if (REQUEST_OK == 0) {
            if (conexao) {
                try {
                    desconetaBluetooth();
                } catch (IOException error) {
                    Toast.makeText(AppBluetoothControle.this, "Erro ao desconectar", Toast.LENGTH_SHORT).show();
                }

            } else {
                inicializaListaDeDispositivos();
            }
        }
    }

    private void inicializaListaDeDispositivos() {
        Intent abreLista = new Intent(AppBluetoothControle.this, listaDispositivos.class);
        startActivityForResult(abreLista, REQUEST_CONEXAO);
    }

    private void desconetaBluetooth() throws IOException {
        bluetoothSocket.close();
        conexao = false;
        botaoconexao.setText("Conectar");
        Toast.makeText(AppBluetoothControle.this, "Bluetooth Desconectado", Toast.LENGTH_SHORT).show();
    }

    private void alterarTextView() {
        bateria.setText(bat);
        distancia.setText(dist + "m");
    }

    private void inicializaCompontentesView() {
        bateria = findViewById(R.id.activity_bluetooth_text_bateria_output);
        distancia = findViewById(R.id.activity_bluetooth_text_distance_output);
        botaoconexao = findViewById(R.id.activity_botao_conectar);
        botaoatualiza = findViewById(R.id.activity_botao_atualizar);
        switchonof = findViewById(R.id.activity_bluetooth_switch_ONOF);
    }

    private void bluetoothIsEnableTest() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(AppBluetoothControle.this, "Dispositivo Incompativel", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void confirmaAtivsaoBluetooth(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            Toast.makeText(AppBluetoothControle.this, "Bluetooth Ativado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AppBluetoothControle.this, "Bluetooth não pode ser Ativado", Toast.LENGTH_SHORT).show();
            REQUEST_OK = 1;
        }
    }

    private void configuraConexaoDispositivos(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Mac = data.getExtras().getString(listaDispositivos.ENDEREÇO_MAC);
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(Mac);
            try {
                conectarDispositivo();
            } catch (IOException erro) {
                conexao = false;
                Toast.makeText(AppBluetoothControle.this, "erro ao conectar: " + Mac, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AppBluetoothControle.this, "Falha ao encontrar MacAdress", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                confirmaAtivsaoBluetooth(resultCode);
                break;
            case REQUEST_CONEXAO:
                configuraConexaoDispositivos(resultCode, data);
        }
    }

    private void conectarDispositivo() throws IOException {
        bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(bluetooth_UUID);
        bluetoothSocket.connect();
        conexao = true;
        ConnectedThread = new ConnectedThread(bluetoothSocket);
        ConnectedThread.start();
        botaoconexao.setText("Desconectar");
        Toast.makeText(AppBluetoothControle.this, "Bluetooth Conectado: " + Mac, Toast.LENGTH_SHORT).show();
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Toast.makeText(AppBluetoothControle.this, "ERRO", Toast.LENGTH_SHORT).show();
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(AppBluetoothControle.this, "ERRO", Toast.LENGTH_SHORT).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.

                    String dados = new String(mmBuffer, 0, numBytes);

                    Message readMsg = handler.obtainMessage(
                            MESSAGE_READ, numBytes, -1,
                            dados);
                     readMsg.sendToTarget();
                } catch (IOException e) {

                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(String dadosEnviar) {
            byte[] mmBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(mmBuffer);
            } catch (IOException e) {
                Toast.makeText(AppBluetoothControle.this, "Não foi possivel enviar", Toast.LENGTH_SHORT).show();
            }
        }
    }
}