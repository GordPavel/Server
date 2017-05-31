package mypackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class Employee{

    public static void main( String... args ) throws IOException{
        Integer number = Integer.valueOf( args[ 0 ] );
        if( number == null ){
            System.out.println( "Error " );
            return;
        }
        try( Socket socket = new Socket( InetAddress.getLocalHost(), 8080 ) ;
             DataOutputStream output = new DataOutputStream( socket.getOutputStream() ) ;
             DataInputStream input = new DataInputStream( socket.getInputStream() ) ){
            output.writeInt( number );
            Integer integer;
            Integer answers = 0, end = new Random().nextInt( 15 ) + 5;
            while( ( integer = input.readInt() ) != -1 ){
                if( answers == end )
                    break;
                output.writeBoolean( true );
                answers++;
            }
            System.err.println( "Client " + number + " disconnected" );
        }catch( Exception e ){
            e.printStackTrace();
        }
    }
}