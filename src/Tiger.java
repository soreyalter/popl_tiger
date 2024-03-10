import control.CommandLine;
import control.Control;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import lexer.Lexer;
import lexer.Token;
import parser.Parser;

// the Tiger compiler main class.
public class Tiger {
    public static void main(String[] args) throws Exception {
        InputStream fileStream;
        Parser parser;

        // ///////////////////////////////////////////////////////
        // handle command line arguments
        CommandLine cmd = new CommandLine();
        // the file to be compiled:
        String fileName = cmd.scan(args);
        if(fileName == null){
            // no file is given, then exit silently.
            return;
        }

        // /////////////////////////////////////////////////////////
        // otherwise, we continue to process normal compilation phases.
        // first, create a new stream from the input file:
        fileStream = new BufferedInputStream(new FileInputStream(fileName));
        // then create a parser:
        parser = new Parser(fileName, fileStream);
        // parse the file:
        parser.parse();
        // close the stream before exiting.
        fileStream.close();
    }
}


