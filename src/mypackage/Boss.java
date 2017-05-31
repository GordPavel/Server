package mypackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Boss{


    public static void main( String[] args ) throws Throwable{
        try( ServerSocket serverSocket = new ServerSocket( 8080 ) ){
            System.out.println( "How many clients to init?" );
            Integer clients = new Scanner( System.in ).nextInt();
            initClients( clients );
            while( true ){
                if( clients == 0 ){
                    System.out.println( "Клиенты закончились, сервер откалючается" );
                    break;
                }
                Socket socket = null;
                try{
                    socket = CompletableFuture.supplyAsync( () -> {
                        try{
                            return serverSocket.accept();
                        }catch( IOException e ){
                            return null;
                        }
                    } ).get( 10, TimeUnit.SECONDS );
                    clients--;
                }catch( TimeoutException e ){
                    System.err.println( "Один из клиентов не соединился" );
                }
                new SocketListener( socket ).start();
            }
        }
    }

    static class SocketListener extends Thread{
        Socket socket;

        public SocketListener(){
        }

        SocketListener( Socket socket ){
            this.socket = socket;
        }

        @Override
        public void run(){
            Optional<Integer> number = Optional.empty();
            try( DataOutputStream outputStream = new DataOutputStream( socket.getOutputStream() ) ;
                 DataInputStream inputStream = new DataInputStream( socket.getInputStream() ) ){
                number = CompletableFuture.supplyAsync( ( Supplier<Optional<Integer>> ) () -> {
                    try{
                        return Optional.of( inputStream.readInt() );
                    }catch( IOException e ){
                        return Optional.empty();
                    }
                } ).get( 50, TimeUnit.MILLISECONDS );
                for( int i = 0 ; ; i++ ){
                    outputStream.writeInt( i );
                    Optional<Boolean> answer = CompletableFuture.supplyAsync( ( Supplier<Optional<Boolean>> ) () -> {
                        try{
                            return Optional.of( inputStream.readBoolean() );
                        }catch( IOException e ){
                            return Optional.empty();
                        }
                    } ).get( 1, TimeUnit.SECONDS );
                    if( !answer.isPresent() ){
                        System.err.println( "Client " + number.get() + " disconnected" );
                        break;
                    }
                    System.out.println( "Client " + number.get() + " answered " + answer.get() );
                }
                outputStream.writeInt( -1 );
            }catch( TimeoutException e ){
                System.err.println( "Client " + number.orElse( null ) + " disconnected" );
            }catch( InterruptedException | ExecutionException e ){
                e.printStackTrace();
            }catch( SocketException e ){
                System.err.println( "Client " + number.get() + " disconnected" );
            }catch( Throwable throwable ){
                throwable.printStackTrace();
            }
        }
    }

    private static void initClients( int integer ) throws Throwable{
        for( int i = 0 ; i < integer ; ){
            Runtime.getRuntime().exec(
                    "java -jar /Users/pavelgordeev/lab2/out/artifacts/lab2_jar/lab2.jar " + ( ++i ) );
        }
    }
}

