package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.JsonObject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Validar implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        // API para manipulacao de JSONs
        JsonObject j = new JsonObject();
        JsonObject body = new JsonObject();
        JsonObject header = new JsonObject();

        // propriedade mensagem na resposta
        body.addProperty("message", "Api para validação de CPF");

        // um header aleatório na resposta para API Gateway
        header.addProperty("x-custom-header", "my custom header value");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONParser parser = new JSONParser();
            JSONObject event = (JSONObject) parser.parse(reader);
            JSONObject m = (JSONObject) event.get("multiValueQueryStringParameters");
            JSONArray ar = (JSONArray) m.get("cpf");
            String cpf = ar.get(0).toString();

            // body.addProperty("reader", event.toString());
            body.addProperty("cpf", cpf);
            body.addProperty("validation", validarCPF(cpf));
        } catch (Exception e) {
            body.addProperty("validation", "Parametro CPF não encontrado");
        }

        // Http status code 200 OK
        j.addProperty("statusCode", 200);
        j.add("headers", header);
        j.addProperty("body", body.toString());

        // serializa o JSON para um OutputStream
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(j.toString());
        writer.close();
    }

    private String validarCPF(String txCPF){
        String mensagem = "";
        if(txCPF.length() == 11){
            if(validarDigito(txCPF, 0) && validarDigito(txCPF, 1)){
                mensagem = "CPF válido";
            }else{
                mensagem = "CPF inválido";
            }
        }else{
            mensagem = "Quantidade de digitos iniválido";
        }
        return mensagem;
    }

    private boolean validarDigito(String txCPF, int dig){
        int soma = 0;
        for (int i = 0; i < 9 + dig; i++) {
            // System.out.println(((10 + dig) - i) +" * "+ txCPF.charAt(i));
            soma += ((10 + dig) - i) * Character.getNumericValue(txCPF.charAt(i));
        }
        int resto = soma % 11;
        int digitoCerto = (resto < 2) ? 0 : 11 - resto;
        return digitoCerto == Character.getNumericValue(txCPF.charAt(9 + dig));
    }

    // public static void main(String[] args) {
    //     Validar v = new Validar();
    //     System.out.println(v.validarCPF("12345678909"));
    // }

}