package com.stomas.formulariodepersonajes;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

public class MainActivity extends AppCompatActivity {

    private static String mqttHost = "tcp://personajes.cloud.shiftr.io" ;
    private static String IdUsuario = "AppAndroid";
    private static String Topico= "Ultimo-Registro" ;
    private static String User= "personajes";
    private static String Pass= "qpgeN2fReLEaKk3z" ;

    private MqttClient mqttClient;

    private EditText txtRut, txtNombre, txtEdad, txtOficio;
    private TextView txtAlerta;
    private ListView lista;
    private Spinner spPersonalidad, spGenero, spTipo;
    private FirebaseFirestore db;

    String[] Genero = {"Hombre", "Mujer", "NoBinario", "Otro"};
    String[] Personalidad ={"Serio", "Frivolo", "Alegre", "Depresivo", "Bipolar", "Curioso", "Nervioso", "Tranquilo", "Otro"};
    String[] Tipo ={"Humano", "Vampiro", "Angel", "Demonio", "Alienigena", "Otro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CargarListaFirestore();

        db = FirebaseFirestore.getInstance();
        txtAlerta = findViewById(R.id.txtAlerta);
        txtRut = findViewById(R.id.txtRut);
        txtNombre = findViewById(R.id.txtNombre);
        txtEdad = findViewById(R.id.txtEdad);
        txtOficio = findViewById(R.id.txtOficio);
        lista = findViewById(R.id.lista);
        spPersonalidad = findViewById(R.id.spPersonalidad);
        spGenero = findViewById(R.id.spGenero);
        spTipo = findViewById(R.id.spTipo);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Genero);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGenero.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Personalidad);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPersonalidad.setAdapter(adapter2);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Tipo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(adapter3);

        try {
            mqttClient = new MqttClient(mqttHost, IdUsuario, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(User);
            options.setPassword(Pass.toCharArray());
            mqttClient.connect(options);
            Toast.makeText(this, "Conectado", Toast.LENGTH_SHORT).show();
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d("MQTT", "Conexion Perdida");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String playload = new String(message.getPayload());
                    runOnUiThread(() -> txtAlerta.setText(playload));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d("MQTT", "Enviado");
                }
            });


        } catch (MqttException e) {
           e.printStackTrace();
        }

    }
    public void CargarListaFirestore(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Personajes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    List<String> listaPersonajes = new ArrayList<>();
                    //Recorro el Arraylist de mascotas para mostrarlas
                    for (QueryDocumentSnapshot document : task.getResult()){
                        String linea =
                                "||" + document.getString("rut") +
                                "||" + document.getString("nombre") +
                                "||" + document.getString("edad") +
                                "||" + document.getString("oficio") +
                                "||" + document.getString("personalidad") +
                                "||" + document.getString("tipo") +
                                "||" + document.getString("genero");
                        listaPersonajes.add(linea);
                        //creo un arrayadapter para mostrar los datos en listview
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MainActivity.this, android.R.layout.simple_list_item_1, listaPersonajes);
                        lista.setAdapter(adapter);
                    }
                }
            }
        });

    }

    public void enviarDatosFirestore(View view){

        String rut = txtRut.getText().toString();
        String nombre = txtNombre.getText().toString();
        String edad = txtEdad.getText().toString();
        String oficio = txtOficio.getText().toString();
        String personalidad = spPersonalidad.getSelectedItem().toString();
        String tipo = spTipo.getSelectedItem().toString();
        String genero = spGenero.getSelectedItem().toString();
        String Mensaje ="Rut:"+ rut + " Nombre:"+ nombre;
        //" Edad:"+edad+" Oficio:"+ oficio+ " Personalidad:"+ personalidad+ "TIpo:"+tipo+ "Genero:"+genero

        Map<String, Object> personaje = new HashMap<>();
        personaje.put("rut", rut);
        personaje.put("nombre", nombre);
        personaje.put("edad", edad);
        personaje.put("oficio", oficio);
        personaje.put("personalidad", personalidad);
        personaje.put("tipo", tipo);
        personaje.put("genero", genero);
        db.collection("Personajes").document(rut).set(personaje).addOnSuccessListener(aVoid ->{
                    Toast.makeText(this, "Agregado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();

                });
        try {
            if (mqttClient != null && mqttClient.isConnected()){
                mqttClient.publish(Topico, Mensaje.getBytes(), 0, false);
                txtAlerta.append("\n -" + "Enviado");
                Toast.makeText(MainActivity.this, "Mensaje Enviado", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Mensaje NO Enviado", Toast.LENGTH_SHORT).show();
            }
        } catch (MqttPersistenceException e) {
           e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }
    public void CargarLista(View view){

        CargarListaFirestore();
    }
}