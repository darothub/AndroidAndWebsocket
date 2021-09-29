package com.darothub.androidwithwebsocket.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tech.gusavila92.websocketclient.WebSocketClient;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

public class WebSocket{
    private static WebSocketClient webSocketClient;
    private static StompClient stompClient;
    private static CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static String STOMPCONNECTION = "Stomp connection";
    private static final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @SuppressLint("CheckResult")
    public static void sendMessageWithStomp(Runnables runnables){
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/chat-endpoint/websocket");
        Disposable dispLifeCycle = stompClient.lifecycle().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.i(STOMPCONNECTION, "Opened");
                            break;
                        case ERROR:
                            Log.e(STOMPCONNECTION, "Stomp connection error", lifecycleEvent.getException());
//                            toast("Stomp connection error");
                            break;
                        case CLOSED:
                            Log.d(STOMPCONNECTION, "Stomp connection closed");
//                            resetSubscriptions();
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.e(STOMPCONNECTION, "Stomp failed server heartbeat");
                            break;
                    }
                });

        compositeDisposable.add(dispLifeCycle);
        // Receive greetings
        Disposable dispTopic = stompClient.topic("/topic/reply")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    runnables.runMethod(topicMessage.getPayload());
                    Log.d(STOMPCONNECTION, "Received " + topicMessage.getPayload());
//                    addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
                }, throwable -> {
                    Log.e("/topic/reply", "Error on subscribe topic", throwable);
                });

        compositeDisposable.add(dispTopic);
        stompClient.connect();
//        stompClient.topic("/queue/reply").subscribe(topicMessage -> {
//            Log.d("TAG", topicMessage.getPayload());
//        });
//        stompClient.send("/queue/chat", "My first message ").subscribe();

//        stompClient.disconnect();
    }

    public static void sendEchoViaStomp(String msg) {
      Log.d("Main", "Clicked");
      HashMap hashMap = new HashMap();
      hashMap.put("Darot", "heyman");

        compositeDisposable.add(stompClient.send("/app/chat", String.valueOf(hashMap))
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.d(STOMPCONNECTION, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(STOMPCONNECTION, "Error send STOMP echo", throwable);
//                    toast(throwable.getMessage());
                }));
    }

    public static void createWebSocket(Runnables runnable){
        URI uri;
        try{
            uri = new URI("ws://10.0.2.2:8080/websocket");
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                webSocketClient.send("WOW");
            }

            @Override
            public void onTextReceived(String message) {
                Log.i("WebSocket", "Message received");
                runnable.runMethod(message);
            }

            @Override
            public void onBinaryReceived(byte[] data) {

            }

            @Override
            public void onPingReceived(byte[] data) {

            }

            @Override
            public void onPongReceived(byte[] data) {

            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };
        webSocketClient.setConnectTimeout(10000);
        webSocketClient.setReadTimeout(60000);
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public static void sendMessage(){
        webSocketClient.send("WOWWWWW!");
    }
    protected static CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}


